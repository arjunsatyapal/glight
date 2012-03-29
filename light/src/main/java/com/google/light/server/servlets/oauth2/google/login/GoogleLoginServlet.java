package com.google.light.server.servlets.oauth2.google.login;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.constants.OAuth2ProviderService.GOOGLE_LOGIN;
import static com.google.light.server.servlets.path.ServletPathEnum.OAUTH2_GOOGLE_LOGIN_CB;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.light.server.servlets.oauth2.google.OAuth2HelperImpl;
import com.google.light.server.servlets.oauth2.google.GoogleOAuth2HelperFactoryInterface;
import com.google.light.server.utils.GuiceUtils;
import com.google.light.server.utils.ServletUtils;
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

  private Injector injector;
  private GoogleOAuth2HelperFactoryInterface factory;

  @Inject
  public GoogleLoginServlet(Injector injector) {
    this.injector = checkNotNull(injector);
    this.factory = GuiceUtils.getInstance(injector, GoogleOAuth2HelperFactoryInterface.class);
  }
  
  @Override
  public void service(HttpServletRequest request, HttpServletResponse response) {
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
    String callbackUrl = ServletUtils.getServletUrl(request, OAUTH2_GOOGLE_LOGIN_CB);
    
    OAuth2HelperImpl instance = factory.create(GOOGLE_LOGIN);
    
    String actualRedirectUrl = instance.getOAuth2RedirectUri(callbackUrl); 
        
    logger.info("Redirecting to : " + actualRedirectUrl);
    response.sendRedirect(actualRedirectUrl);
  }
}
