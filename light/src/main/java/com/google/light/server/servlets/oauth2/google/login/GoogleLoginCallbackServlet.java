package com.google.light.server.servlets.oauth2.google.login;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.constants.OAuth2Provider.GOOGLE_LOGIN;
import static com.google.light.server.servlets.path.ServletPathEnum.SESSION;
import static com.google.light.server.utils.ServletUtils.getRequestUriWithQueryParams;

import com.google.light.server.utils.GaeUtils;

import com.google.api.client.auth.oauth2.AuthorizationCodeResponseUrl;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.light.server.exception.unchecked.GoogleLoginException;
import com.google.light.server.servlets.oauth2.google.GoogleOAuth2Helper;
import com.google.light.server.servlets.oauth2.google.pojo.GoogleTokenInfo;
import com.google.light.server.servlets.oauth2.google.pojo.GoogleUserInfo;
import com.google.light.server.utils.LightUtils;
import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class GoogleLoginCallbackServlet extends HttpServlet {
  private static final Logger logger = Logger.getLogger(GoogleLoginCallbackServlet.class.getName());

  private GoogleOAuth2Helper googleOAuth2Helper;

  @Inject
  public GoogleLoginCallbackServlet(Provider<GoogleOAuth2Helper> googleOAuth2HelperProvider) {
    this.googleOAuth2Helper = checkNotNull(googleOAuth2HelperProvider.get());
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    AuthorizationCodeResponseUrl authorizationCodeResponseUrl =
        new AuthorizationCodeResponseUrl(getRequestUriWithQueryParams(request));
    String code = authorizationCodeResponseUrl.getCode();

    // TODO(arjun): Move error before the code == null.
    if (code == null) {
      throw new GoogleLoginException("Google did not return Authorization Code.");
    } else if (authorizationCodeResponseUrl.getError() != null) {
      onError(request, response, authorizationCodeResponseUrl);
    } else {
      TokenResponse tokenResponse = googleOAuth2Helper.getAccessToken(request, code);
      logger.info("accessToken = " + tokenResponse.getAccessToken());
      GoogleTokenInfo tokenInfo = googleOAuth2Helper.getTokenInfo(tokenResponse.getAccessToken());
      logger.info("tokenExpiry time = " + tokenInfo.getExpiresInSeconds());
      GoogleUserInfo userInfo = googleOAuth2Helper.getUserInfo(tokenResponse.getAccessToken());

      onSuccess(request, response, tokenInfo, userInfo);
    }
  }

  /**
   * TODO(arjuns): Update Javadoc and add test.
   * 
   * @param request
   * @param response
   * @param userInfo
   * @param tokenInfo
   * @throws IOException
   */
  private void onSuccess(HttpServletRequest request, HttpServletResponse response, 
      GoogleTokenInfo tokenInfo, GoogleUserInfo userInfo) throws IOException {
    LightUtils.prepareSession(request.getSession(), GOOGLE_LOGIN, 
        userInfo.getId(), userInfo.getEmail());
    response.getWriter().println("success");
    
    // TODO(arjuns): Add handler for redirectUri.
    if (!GaeUtils.isProductionServer()) {
      response.sendRedirect(SESSION.get());
    } else {
      response.getWriter().println("success");
    }
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
