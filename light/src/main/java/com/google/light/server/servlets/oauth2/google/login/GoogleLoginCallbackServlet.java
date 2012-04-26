package com.google.light.server.servlets.oauth2.google.login;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.constants.OAuth2ProviderService.GOOGLE_LOGIN;
import static com.google.light.server.servlets.path.ServletPathEnum.OAUTH2_GOOGLE_LOGIN_CB;
import static com.google.light.server.servlets.path.ServletPathEnum.SESSION;
import static com.google.light.server.utils.GuiceUtils.getInstance;
import static com.google.light.server.utils.LightUtils.enqueueRequestScopedVariables;
import static com.google.light.server.utils.ServletUtils.prepareSession;

import com.google.api.client.auth.oauth2.AuthorizationCodeResponseUrl;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.light.server.dto.LoginStateDto;
import com.google.light.server.dto.oauth2.owner.OAuth2OwnerTokenDto;
import com.google.light.server.dto.person.PersonDto;
import com.google.light.server.dto.pojo.PersonId;
import com.google.light.server.exception.unchecked.GoogleAuthorizationException;
import com.google.light.server.manager.implementation.oauth2.owner.OAuth2OwnerTokenManagerFactory;
import com.google.light.server.manager.interfaces.OAuth2OwnerTokenManager;
import com.google.light.server.manager.interfaces.PersonManager;
import com.google.light.server.persistence.entity.oauth2.owner.OAuth2OwnerTokenEntity;
import com.google.light.server.persistence.entity.person.PersonEntity;
import com.google.light.server.servlets.oauth2.google.GoogleOAuth2Utils;
import com.google.light.server.servlets.oauth2.google.OAuth2Helper;
import com.google.light.server.servlets.oauth2.google.OAuth2HelperFactoryInterface;
import com.google.light.server.servlets.oauth2.google.pojo.GoogleLoginTokenInfo;
import com.google.light.server.servlets.oauth2.google.pojo.GoogleUserInfo;
import com.google.light.server.utils.GaeUtils;
import com.google.light.server.utils.JsonUtils;
import com.google.light.server.utils.LightUtils;
import com.google.light.server.utils.ServletUtils;
import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet called by Google during Google Login flow.
 * 
 * TODO(arjuns): Add test for this.
 * TODO(arjuns): Add code for revoke token.
 * TODO(arjuns): Move createPerson inside personManager
 * 
 * @author arjuns@google.com (Arjun Satyapal)
 */
@SuppressWarnings("serial")
public class GoogleLoginCallbackServlet extends HttpServlet {
  private OAuth2HelperFactoryInterface helperFactory;
  private OAuth2Helper helperInstance;

  private OAuth2OwnerTokenManagerFactory tokenManagerFactory;
  private OAuth2OwnerTokenManager googLoginTokenManager;

  private String lightCbUrl = null;

  @Inject
  public GoogleLoginCallbackServlet() {
    helperFactory = getInstance(OAuth2HelperFactoryInterface.class);
    tokenManagerFactory = getInstance(OAuth2OwnerTokenManagerFactory.class);
  }

  @Override
  public void service(HttpServletRequest request, HttpServletResponse response) {
    helperInstance = helperFactory.create(GOOGLE_LOGIN);

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
    LoginStateDto state =
        JsonUtils.getDto(authorizationCodeResponseUrl.getState(), LoginStateDto.class).validate();

    // TODO(arjun): Move error before the code == null.
    if (code == null) {
      throw new GoogleAuthorizationException("Google did not return Authorization Code.");
    } else if (authorizationCodeResponseUrl.getError() != null) {
      onError(request, response, authorizationCodeResponseUrl);
    } else {
      // TODO(arjuns): Move following to onSuccess.
      // TODO(arjuns) : Break this method into more parts.
      // TODO(arjuns) : Write whole workflow as this is complex to understand.

      String email = null;
      String providerUserId = null;
      boolean createNewPerson = false;
      String refreshToken = null;

      TokenResponse tokenResponse = helperInstance.getAccessToken(lightCbUrl, code);

      GoogleLoginTokenInfo googTokenInfo = helperInstance.getTokenInfo(
          tokenResponse.getAccessToken());
      email = googTokenInfo.getEmail();
      providerUserId = googTokenInfo.getUserId();
      refreshToken = tokenResponse.getRefreshToken();

      /*
       * Update session with required values except PersonId so that SessionManager is available
       * for Guice injection.
       */
      prepareSessionAfterLogin(request, null, providerUserId, email);
      googLoginTokenManager = tokenManagerFactory.create(GOOGLE_LOGIN);

      // TODO(arjuns): Replace this with OwnerManager as dao should not be used here.
      PersonEntity personEntity = null;
      personEntity = getPersonEntityIfExists(email, providerUserId);

      /*
       * If Google does not return refreshToken, that means Person had visited light earlier and had
       * given Access. So lets search for the Person on Light. On the other hand, if refreshToken is
       * set, then that means Person was prompted to give Access. That means the refreshToken has
       * changed and needs to be updated on DataStore. So for both the cases we need to find the
       * associated Person.
       */
      boolean updatePersistedTokenDetails = false;

      /*
       * If person is still not found, then it is safe to assume that a new person needs to be
       * created.
       */
      PersonId personId = null;
      if (personEntity == null) {
        personEntity = createNewPersonEntity(tokenResponse);
        updatePersistedTokenDetails = true;
      }
      
      checkNotNull(personEntity, "After this point, personEntity should not be null.");
      personId = personEntity.getPersonId();

      enqueueRequestScopedVariables(personId, personId);
      LightUtils.enqueueRequestScopedVariables(personId, personId);

      
      // Update session with personId which is not available.
      prepareSessionAfterLogin(request, personId, providerUserId, email);

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
        forceLoginWithPrompt(request, response, state);
        return;
      }

      /*
       * Now see that TokenDetails are present. It is possible that Person was successfully created,
       * but we failed to store the TokenDetails. This check will ensure that values are present.
       */

      // TODO(arjuns): Fix this hardcoded id generation.
      updatePersistedTokenDetails = doesTokenDetailEntityNeedsUpdate(updatePersistedTokenDetails);

      if (updatePersistedTokenDetails) {
        if (Strings.isNullOrEmpty(refreshToken)) {
          forceLoginWithPrompt(request, response, state);
          return;
        }
        updateOwnerTokenEntity(personId, providerUserId, refreshToken, tokenResponse);
      }

      /*
       * Now update the session with relevant parameters, so that Person's next visit will not
       * require him to login again.
       */
      prepareSessionAfterLogin(request, personId, providerUserId, email);

      String redirectPath = state.getRedirectPath();
      if (redirectPath == null) {
        if (!GaeUtils.isProductionServer()) {
          response.sendRedirect(SESSION.get());
        } else {
          // TODO(waltercacau): Eliminate this and redirect to Light's main page
          response.getWriter().println("success");
        }
      } else {
        // TODO(waltercacau): Add a integration test that covers this flow.
        response.sendRedirect(redirectPath);
      }
    }
  }

  /**
   * @param personId
   * @param updatePersistedTokenDetails
   * @return
   */
  private boolean doesTokenDetailEntityNeedsUpdate(boolean updatePersistedTokenDetails) {
    if (updatePersistedTokenDetails)
      return true;
    OAuth2OwnerTokenEntity fetchedTokenEntity = googLoginTokenManager.get();
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
  private void prepareSessionAfterLogin(HttpServletRequest request, PersonId personId,
      String providerUserId, String email) {
    try {
      prepareSession(request, GOOGLE_LOGIN, personId, providerUserId, email);
    } catch (Exception e) {
      try {
        // TODO(arjuns): Revisit this convoluted logic again.
        ServletUtils.invalidateSession(request);
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
  private void updateOwnerTokenEntity(PersonId personId, String providerUserId,
      String refreshToken,
      TokenResponse tokenResponse) throws IOException {
    GoogleLoginTokenInfo googTokenInfo = helperInstance.getTokenInfo(

        tokenResponse.getAccessToken());
    OAuth2OwnerTokenDto tokenDto =
        OAuth2OwnerTokenDto.getOAuth2OwnerTokenDto(
            personId, refreshToken, tokenResponse, GOOGLE_LOGIN, providerUserId,
            googTokenInfo.toJson());

    googLoginTokenManager.put(tokenDto.toPersistenceEntity());
  }

  /**
   * TODO(arjuns): Move this to {@link PersonManager#createPerson(PersonEntity)}.
   * 
   * @param tokenResponse
   * @return
   * @throws IOException
   */
  private PersonEntity createNewPersonEntity(TokenResponse tokenResponse) throws IOException {
    PersonEntity personEntity;

    /*
     * First fetch UserInfo from Google.
     */
    GoogleUserInfo userInfo = GoogleOAuth2Utils.getUserInfo(helperInstance.getHttpTransport(),
        tokenResponse.getAccessToken());

    PersonDto dto = new PersonDto.Builder()
        .firstName(userInfo.getGivenName())
        .lastName(userInfo.getFamilyName())
        .email(userInfo.getEmail())
        .acceptedTos(false)
        .build();

    PersonManager personManager = getInstance(PersonManager.class);
    personEntity = personManager.create(dto.toPersistenceEntity(null));
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
  private PersonEntity getPersonEntityIfExists(String email, String providerUserId) {
    PersonId personId = null;
    PersonManager personManager = getInstance(PersonManager.class);

    PersonEntity personEntity = null;
    /*
     * Search by loginProviderUserId is prioritized above email, because its possible that a email
     * provided by LoginProvider may change. But assumption here is that providerUserId will never
     * change.
     */
    OAuth2OwnerTokenEntity ownerTokenEntity = googLoginTokenManager.getByProviderUserId(
        providerUserId);

    if (ownerTokenEntity != null) {
      personId = ownerTokenEntity.getPersonId();
      personEntity = personManager.get(personId);
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
      personEntity = personManager.findByEmail(email);
    }

    return personEntity;
  }

  /**
   * @param request
   * @param response
   * @param state
   * @throws IOException
   */
  private void forceLoginWithPrompt(HttpServletRequest request,
      HttpServletResponse response, LoginStateDto state) throws IOException {
    /*
     * Will reinitialize session once Light has refreshToken. So we dont care whether
     * request.getSession() returns existing session or new session.
     */
    ServletUtils.invalidateSession(request);
    response
        .sendRedirect(helperInstance.getOAuth2RedirectUriWithPrompt(lightCbUrl, state.toJson()));
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
