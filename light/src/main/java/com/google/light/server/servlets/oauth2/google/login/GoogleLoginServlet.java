package com.google.light.server.servlets.oauth2.google.login;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.ServletUtils.getServerUrl;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.light.server.servlets.oauth2.google.GoogleOAuth2Helper;
import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet to handle Google Login.
 * 
 * TODO(arjuns): Add test for this.
 *
 * @author arjuns@google.com (Arjun Satyapal)
 */
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
    String actualRedirectUrl = googleOAuth2Helper.getGoogleLoginRedirectUri(getServerUrl(request));
    logger.info("Redirecting to : " + actualRedirectUrl);
    response.sendRedirect(actualRedirectUrl);
  }
}
