package com.google.light.server.servlets.oauth2.google.gdoc;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.constants.OAuth2ProviderService.GOOGLE_DOC;
import static com.google.light.server.servlets.path.ServletPathEnum.OAUTH2_GOOGLE_DOC_AUTH_CB;
import static com.google.light.server.utils.GuiceUtils.getInstance;
import static com.google.light.server.utils.LightPreconditions.checkValidSession;

import com.google.light.server.dto.pojo.typewrapper.longwrapper.PersonId;

import com.google.light.server.utils.JsonUtils;

import com.google.light.server.exception.unchecked.MissingOwnerCredentialException;

import com.google.light.server.dto.RedirectDto;



import com.google.api.client.auth.oauth2.AuthorizationCodeResponseUrl;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.light.server.dto.oauth2.owner.OAuth2OwnerTokenDto;
import com.google.light.server.exception.unchecked.GoogleAuthorizationException;
import com.google.light.server.manager.implementation.oauth2.owner.OAuth2OwnerTokenManagerFactory;
import com.google.light.server.manager.interfaces.OAuth2OwnerTokenManager;
import com.google.light.server.manager.interfaces.PersonManager;
import com.google.light.server.persistence.entity.oauth2.owner.OAuth2OwnerTokenEntity;
import com.google.light.server.persistence.entity.person.PersonEntity;
import com.google.light.server.servlets.SessionManager;
import com.google.light.server.servlets.oauth2.google.OAuth2Helper;
import com.google.light.server.servlets.oauth2.google.OAuth2HelperFactoryInterface;
import com.google.light.server.servlets.oauth2.google.pojo.GoogleOAuth2TokenInfo;
import com.google.light.server.utils.ServletUtils;
import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet called by Google during Google OAuth2 Authorization flow for 
 * {@link OAuth2ProviderService#GOOGLE_DOC}
 * 
 * TODO(arjuns): Add test for this.
 * TODO(arjuns) : Lot of code is common between this and Google Login servlet. Refactor !!!
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class GoogleDocAuthCallbackServlet extends HttpServlet {
  private OAuth2Helper helperInstance;
  private OAuth2OwnerTokenManager googDocTokenManager;
  
  private SessionManager sessionManager;
  private PersonManager personManager;
  private String lightCbUrl = null;

  
  @Inject
  public GoogleDocAuthCallbackServlet(Injector injector) {
    checkNotNull(injector, "injector");
    
  }
  
  @Override
  public void service(HttpServletRequest request, HttpServletResponse response) {
    OAuth2HelperFactoryInterface helperFactory = getInstance(OAuth2HelperFactoryInterface.class);
    helperInstance = helperFactory.create(GOOGLE_DOC);

    OAuth2OwnerTokenManagerFactory tokenManagerFactory = getInstance(OAuth2OwnerTokenManagerFactory.class);
    googDocTokenManager = tokenManagerFactory.create(GOOGLE_DOC);
    
    sessionManager = getInstance(SessionManager.class);
    checkValidSession(sessionManager);
    
    personManager = getInstance(PersonManager.class);
    lightCbUrl = ServletUtils.getServletUrl(request, OAUTH2_GOOGLE_DOC_AUTH_CB);

    try {
      super.service(request, response);
    } catch (Exception e) {
      // TODO(arjuns): Add exception handling.
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    AuthorizationCodeResponseUrl authorizationCodeResponseUrl =
        new AuthorizationCodeResponseUrl(ServletUtils.getRequestUriWithQueryParams(request));
    String code = authorizationCodeResponseUrl.getCode();
    RedirectDto state =
        JsonUtils.getDto(authorizationCodeResponseUrl.getState(), RedirectDto.class).validate();

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

      PersonId personId = sessionManager.getPersonId();
      PersonEntity personEntity = personManager.get(personId);
      checkNotNull(personEntity, "personEntity cannot be null as person is in session.");

      TokenResponse tokenResponse = helperInstance.getAccessToken(lightCbUrl, code);
      boolean newRefreshToken = !Strings.isNullOrEmpty(tokenResponse.getRefreshToken());

      if(newRefreshToken) {
        updateOwnerTokenEntity(personId, tokenResponse.getRefreshToken(), tokenResponse);
      } else {
        OAuth2OwnerTokenEntity tokenEntity = null;
        
        try {
          tokenEntity = googDocTokenManager.get();
        } catch (MissingOwnerCredentialException e) {
          // Eat this exception.
        }
        
        if (tokenEntity == null) {
          /*
           * Google did not return refreshToken as User had given Authorization earlier. But light
           * failed to locate a record for that token. So User needs to be forced to give access.
           */
          forceAuthFlowWithPrompt(response, state);
          return;
        }
      }
    }
    
    String redirectPath = state.getRedirectPath();
    if (redirectPath == null) {
      response.getWriter().println("Done");
    } else {
      // TODO(waltercacau): Add a integration test that covers this flow.
      response.sendRedirect(redirectPath);
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
  private void updateOwnerTokenEntity(PersonId personId, String refreshToken, 
      TokenResponse tokenResponse) throws IOException {
    GoogleOAuth2TokenInfo googTokenInfo = helperInstance.getTokenInfo(
        tokenResponse.getAccessToken());

    OAuth2OwnerTokenDto tokenDto = OAuth2OwnerTokenDto.getOAuth2OwnerTokenDto(
        personId, refreshToken, tokenResponse, GOOGLE_DOC, null /*providerUserId*/,
        JsonUtils.toJson(googTokenInfo));

    googDocTokenManager.put(tokenDto.toPersistenceEntity());
  }

  /**
   * TODO(arjuns): Refactor to common place.
   * 
   * @param request
   * @param response
   * @throws IOException
   */
  private void forceAuthFlowWithPrompt(HttpServletResponse response, RedirectDto state) throws IOException {
    /*
     * Will reinitialize session once Light has refreshToken. So we dont care whether
     * request.getSession() returns existing session or new session.
     */
    response.sendRedirect(helperInstance.getOAuth2RedirectUriWithPrompt(lightCbUrl, state.toJson()));
    return;
  }

  /**
   * @param request
   * @param response
   * @param responseUrl
   * @throws IOException
   */
  @SuppressWarnings("unused") 
  private void onError(HttpServletRequest request, HttpServletResponse response,
      AuthorizationCodeResponseUrl responseUrl) throws IOException {
    // TODO(arjuns): Auto-generated method stub

    response.getWriter().println("error.");
  }
}
