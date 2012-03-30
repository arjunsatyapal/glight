package com.google.light.server.servlets.oauth2.google.login;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.constants.OAuth2ProviderService.GOOGLE_LOGIN;
import static com.google.light.server.servlets.oauth2.google.pojo.AbstractGoogleOAuth2TokenInfo.calculateExpireInMillis;
import static com.google.light.server.servlets.path.ServletPathEnum.OAUTH2_GOOGLE_LOGIN_CB;
import static com.google.light.server.servlets.path.ServletPathEnum.SESSION;
import static com.google.light.server.utils.GuiceUtils.getInstance;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.LightUtils.prepareSession;

import com.google.light.server.manager.implementation.oauth2.owner.OAuth2OwnerTokenManagerFactory;

import com.google.api.client.auth.oauth2.AuthorizationCodeResponseUrl;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.light.server.dto.oauth2.owner.OAuth2OwnerTokenDto;
import com.google.light.server.dto.person.PersonDto;
import com.google.light.server.exception.unchecked.GoogleAuthorizationException;
import com.google.light.server.manager.interfaces.OAuth2OwnerTokenManager;
import com.google.light.server.manager.interfaces.PersonManager;
import com.google.light.server.persistence.entity.oauth2.owner.OAuth2OwnerTokenEntity;
import com.google.light.server.persistence.entity.person.PersonEntity;
import com.google.light.server.servlets.SessionManager;
import com.google.light.server.servlets.oauth2.google.OAuth2HelperImpl;
import com.google.light.server.servlets.oauth2.google.GoogleOAuth2HelperFactoryInterface;
import com.google.light.server.servlets.oauth2.google.GoogleOAuth2Utils;
import com.google.light.server.servlets.oauth2.google.pojo.GoogleLoginTokenInfo;
import com.google.light.server.servlets.oauth2.google.pojo.GoogleUserInfo;
import com.google.light.server.utils.GaeUtils;
import com.google.light.server.utils.GuiceUtils;
import com.google.light.server.utils.ServletUtils;
import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet called by Google during Google Login flow.
 * 
 * TODO(arjuns): Add test for this. TODO(arjuns): Add code for revoke token.
 * 
 * @author arjuns@google.com (Arjun Satyapal)
 */
@SuppressWarnings("serial")
public class GoogleLoginCallbackServlet extends HttpServlet {
  private Injector injector;
  
  private OAuth2HelperImpl helperInstance;
  private OAuth2OwnerTokenManager googLoginTokenManager;
  
  private String lightCbUrl = null;

  @Inject
  public GoogleLoginCallbackServlet(Injector injector) {
    this.injector = checkNotNull(injector, "injector");
  }

  @Override
  public void service(HttpServletRequest request, HttpServletResponse response) {
    GoogleOAuth2HelperFactoryInterface helperFactory = getInstance(
        injector, GoogleOAuth2HelperFactoryInterface.class);
    helperInstance = helperFactory.create(GOOGLE_LOGIN);
    
    OAuth2OwnerTokenManagerFactory tokenManagerFactory = getInstance(
        injector, OAuth2OwnerTokenManagerFactory.class);
    googLoginTokenManager = tokenManagerFactory.create(GOOGLE_LOGIN);

    lightCbUrl = ServletUtils.getServletUrl(request, OAUTH2_GOOGLE_LOGIN_CB);
    try {
      super.service(request, response);
    } catch (Exception e) {
      // TODO(arjuns): Auto-generated catch block
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    AuthorizationCodeResponseUrl authorizationCodeResponseUrl =
        new AuthorizationCodeResponseUrl(ServletUtils.getRequestUriWithQueryParams(request));
    String code = authorizationCodeResponseUrl.getCode();

    // TODO(arjun): Move error before the code == null.
    if (code == null) {
      throw new GoogleAuthorizationException("Google did not return Authorization Code.");
    } else if (authorizationCodeResponseUrl.getError() != null) {
      onError(request, response, authorizationCodeResponseUrl);
    } else {
      // TODO(arjuns): Move following to onSuccess.
      // TODO(arjuns) : First Check for session.
      // TODO(arjuns) : Break this method into more parts.
      // TODO(arjuns) : Write whole workflow as this is complex to understand.

      Long personId = null;
      String email = null;
      String providerUserId = null;
      boolean createNewPerson = false;
      String refreshToken = null;

      HttpSession session = request.getSession();
      // For non-login servlets this is injected.
      SessionManager sessionManager = new SessionManager(session);
      if (!sessionManager.isValidSession()) {
        request.getSession().invalidate();
        session = request.getSession(true);
        checkArgument(session.isNew(), "session should be new.");
      }

      TokenResponse tokenResponse = helperInstance.getAccessToken(lightCbUrl, code);

      GoogleLoginTokenInfo googTokenInfo = null;
      if (session.isNew()) {
        /*
         * Then seed providerUserId and providerUserEmail from TokenInfo.
         */
        googTokenInfo = helperInstance.getTokenInfo(tokenResponse.getAccessToken(),
            GoogleLoginTokenInfo.class);
        email = googTokenInfo.getEmail();
        providerUserId = googTokenInfo.getUserId();
        refreshToken = tokenResponse.getRefreshToken();
      } else {
        /*
         * Assumption is that session has been validated earlier. And since session is valid so
         * fetching values from session instead of TokenInfo.
         */
        email = sessionManager.getEmail();
        providerUserId = sessionManager.getLoginProviderUserId();
        personId = sessionManager.getPersonId();
      }

      // TODO(arjuns): Replace this with OwnerManager as dao should not be used here.
      PersonEntity personEntity = null;
      personEntity = getPersonEntityIfExists(personId, email, providerUserId);

      /*
       * If person is still not found, then it is safe to assume that a new person needs to be
       * created.
       */
      if (personEntity == null) {
        createNewPerson = true;
      }

      /*
       * If Google does not return refreshToken, that means Person had visited light earlier and had
       * given Access. So lets search for the Person on Light. On the other hand, if refreshToken is
       * set, then that means Person was prompted to give Access. That means the refreshToken has
       * changed and needs to be updated on DataStore. So for both the cases we need to find the
       * associated Person.
       */
      boolean updatePersistedTokenDetails = false;
      boolean newRefreshToken = !Strings.isNullOrEmpty(tokenResponse.getRefreshToken());

      if (newRefreshToken) {
        /*
         * At present Light is not in a position to determine whether new Person needs to be created
         * or not. Reason being, user might have revoked the access, and then tried to visit Light
         * again. So first, fetch User's profile from Google, so that Light has access to Person's
         * Google UserId.
         */
        refreshToken = tokenResponse.getRefreshToken();
        updatePersistedTokenDetails = true;
      }

      /*
       * If refreshToken was not returned, and Light cannot find a associated Person, then Person
       * must be redirected back to Login screen with a forced Prompt, so that Google will redirect
       * back with a RefreshToken. Otherwise Person will be in a bad state due to absence of
       * RefreshToken.
       */
      boolean forceLoginWithPrompt = !newRefreshToken && createNewPerson;
      if (forceLoginWithPrompt) {
        forceLoginWithPrompt(request, response);
        return;
      }

      if (createNewPerson) {
        request.getSession().invalidate();
        personEntity = createNewPersonEnttiy(tokenResponse);
        updatePersistedTokenDetails = true;
      }

      checkNotNull(personEntity, "After this point, personEntity should not be null.");
      personId = personEntity.getId();

      /*
       * Now see that TokenDetails are present. It is possible that Person was successfully created,
       * but we failed to store the TokenDetails. This check will ensure that values are present.
       */

      // TODO(arjuns): Fix this hardcoded id generation.
      updatePersistedTokenDetails = doesTokenDetailEntityNeedsUpdate(
          personId, updatePersistedTokenDetails);

      if (updatePersistedTokenDetails) {
        if (Strings.isNullOrEmpty(refreshToken)) {
          forceLoginWithPrompt(request, response);
          return;
        }
        updateOwnerTokenEntity(personId, providerUserId, tokenResponse);
      }

      /*
       * Now update the session with relevant parameters, so that Person's next visit will not
       * require him to login again.
       */
      prepareSessionAfterLogin(request, personId, providerUserId, personEntity);

      // TODO(arjuns): Add handler for redirectUri.
      if (!GaeUtils.isProductionServer()) {
        response.sendRedirect(SESSION.get());
      } else {
        response.getWriter().println("success");
      }
    }
  }

  /**
   * @param personId
   * @param updatePersistedTokenDetails
   * @return
   */
  private boolean doesTokenDetailEntityNeedsUpdate(Long personId,
      boolean updatePersistedTokenDetails) {
    OAuth2OwnerTokenEntity fetchedTokenEntity = googLoginTokenManager.getToken(personId);
    if (fetchedTokenEntity == null) {
      updatePersistedTokenDetails = true;
    }
    return updatePersistedTokenDetails;
  }

  /**
   * @param request
   * @param personId
   * @param providerUserId
   * @param personEntity
   */
  private void prepareSessionAfterLogin(HttpServletRequest request, Long personId,
      String providerUserId, PersonEntity personEntity) {
    try {
      prepareSession(request.getSession(), GOOGLE_LOGIN, personId,
          providerUserId, personEntity.getEmail());
    } catch (Exception e) {
      try {
        request.getSession().invalidate();
      } catch (Exception e1) {
        // ignore this exception.
      }
      // TODO(arjuns) : Fix this exception handling.
      throw new RuntimeException(e);
    }
  }

  /**
   * @param personId
   * @param providerUserId
   * @param tokenResponse
   * @param googTokenInfo
   * @param expiresInMillis
   * @throws IOException
   */
  private void updateOwnerTokenEntity(Long personId, String providerUserId,
      TokenResponse tokenResponse) throws IOException {
    long expiresInMillis = calculateExpireInMillis(tokenResponse.getExpiresInSeconds());

    GoogleLoginTokenInfo googTokenInfo = helperInstance.getTokenInfo(
        tokenResponse.getAccessToken(), GoogleLoginTokenInfo.class);

    googTokenInfo.getExpiresIn();

    OAuth2OwnerTokenDto tokenDto = new OAuth2OwnerTokenDto.Builder()
        .personId(personId)
        .provider(GOOGLE_LOGIN)
        .providerUserId(providerUserId)
        .accessToken(tokenResponse.getAccessToken())
        .refreshToken(tokenResponse.getRefreshToken())
        .expiresInMillis(expiresInMillis)
        .tokenType(tokenResponse.getTokenType())
        .tokenInfo(googTokenInfo.toJson())
        .build();

    googLoginTokenManager.putToken(tokenDto.toPersistenceEntity());
  }

  /**
   * @param tokenResponse
   * @return
   * @throws IOException
   */
  private PersonEntity createNewPersonEnttiy(TokenResponse tokenResponse) throws IOException {
    PersonEntity personEntity;
    checkNotBlank(tokenResponse.getRefreshToken(),
        "refreshToken cannot be blank, when trying to create a new Person.");

    /*
     * First fetch UserInfo from Google.
     */
    
    GoogleUserInfo userInfo = GoogleOAuth2Utils.getUserInfo(helperInstance.getHttpTransport(), 
        tokenResponse.getAccessToken());

    PersonDto dto = new PersonDto.Builder()
        .firstName(userInfo.getGivenName())
        .lastName(userInfo.getFamilyName())
        .email(userInfo.getEmail())
        .build();

    PersonManager personManager = GuiceUtils.getInstance(injector, PersonManager.class);
    personEntity = personManager.createPerson(dto.toPersistenceEntity(null));
    return personEntity;
  }

  /**
   * @param personId
   * @param email
   * @param providerUserId
   * @param ownerTokenDao
   * @param personManager
   * @param personEntity
   * @return
   */
  private PersonEntity getPersonEntityIfExists(Long personId, String email, String providerUserId) {
    PersonManager personManager = GuiceUtils.getInstance(injector, PersonManager.class);

    PersonEntity personEntity = null;
    if (personId == null) {
      /*
       * Search by loginProviderUserId is prioritized above email, because its possible that a email
       * provided by LoginProvider may change. But assumption here is that providerUserId will never
       * change.
       */
      OAuth2OwnerTokenEntity ownerTokenEntity =
          googLoginTokenManager.getTokenByProviderUserId(providerUserId);

      if (ownerTokenEntity != null) {
        personId = ownerTokenEntity.getPersonId();
      } else {
        /*
         * Search by Email. The underLying assumption is that no two accounts will share same-email
         * ever. It is possible that two different Persons can try to game the system by sharing the
         * Email. That can cause some issues here.
         */

        /*
         * TODO(arjuns): Document the consequences when two different Persons try to share
         * defaultEmail.
         */
        personEntity = personManager.getPersonByEmail(email);
      }
    }

    /*
     * Since there is a chance to get PersonId from OwnerTokenEntity, so we are not putting this in
     * a separate block
     */
    if (personId != null) {
      /*
       * Search by personId.
       */
      personEntity = personManager.getPerson(personId);
    }
    return personEntity;
  }

  /**
   * @param request
   * @param response
   * @throws IOException
   */
  private void forceLoginWithPrompt(HttpServletRequest request,
      HttpServletResponse response) throws IOException {
    /*
     * Will reinitialize session once Light has refreshToken. So we dont care whether
     * request.getSession() returns existing session or new session.
     */

    request.getSession().invalidate();
    response.sendRedirect(helperInstance.getOAuth2RedirectUriWithPrompt(lightCbUrl));
    return;
  }

  /**
   * @param request
   * @param response
   * @param responseUrl
   * @throws IOException
   */
  private void onError(HttpServletRequest request, HttpServletResponse response,
      AuthorizationCodeResponseUrl responseUrl) throws IOException {
    // TODO(arjuns): Auto-generated method stub

    response.getWriter().println("error.");
  }
}
