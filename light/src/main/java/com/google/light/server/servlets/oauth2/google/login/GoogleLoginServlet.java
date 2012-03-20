package com.google.light.server.servlets.oauth2.google.login;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.ServletUtils.getServerUrl;

import com.google.inject.Inject;
import com.google.inject.Provider;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.light.server.servlets.oauth2.google.GoogleOAuth2Helper;
import com.google.light.server.servlets.path.ServletPathEnum;
import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class GoogleLoginServlet extends HttpServlet {
  private static final Logger logger = Logger.getLogger(GoogleLoginServlet.class.getName());

  private GoogleOAuth2Helper googleOAuth2Helper;

  @Inject
  public GoogleLoginServlet(Provider<GoogleOAuth2Helper> googleOAuth2HelperProvider) {
    this.googleOAuth2Helper = checkNotNull(googleOAuth2HelperProvider.get());
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    String redirectUri = getServerUrl(request) + ServletPathEnum.LOGIN_GOOGLE_CB.get();

    AuthorizationCodeFlow flow = googleOAuth2Helper.getAuthorizationCodeFlow();
    String actualRedirectUrl = flow.newAuthorizationUrl().setRedirectUri(redirectUri).build();

    logger.info("Redirecting to : " + actualRedirectUrl);
    response.sendRedirect(actualRedirectUrl);
  }
}
