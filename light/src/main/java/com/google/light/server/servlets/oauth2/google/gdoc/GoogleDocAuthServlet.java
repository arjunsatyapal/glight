package com.google.light.server.servlets.oauth2.google.gdoc;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.constants.OAuth2ProviderService.GOOGLE_DOC;
import static com.google.light.server.servlets.path.ServletPathEnum.OAUTH2_GOOGLE_DOC_AUTH_CB;
import com.google.inject.Inject;
import com.google.light.server.dto.RedirectDto;
import com.google.light.server.servlets.oauth2.google.OAuth2Helper;
import com.google.light.server.servlets.oauth2.google.OAuth2HelperFactoryInterface;
import com.google.light.server.utils.QueryUtils;
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
 * TODO(arjuns): Add check for person is logged in.
 * 
 * @author arjuns@google.com (Arjun Satyapal)
 */
@SuppressWarnings("serial")
public class GoogleDocAuthServlet extends HttpServlet {
  private static final Logger logger = Logger.getLogger(GoogleDocAuthServlet.class.getName());

  private OAuth2HelperFactoryInterface factory;

  @Inject
  public GoogleDocAuthServlet(OAuth2HelperFactoryInterface factory) {
    this.factory = checkNotNull(factory, "factory");
  }
  
  @Override
  public void service(HttpServletRequest req, HttpServletResponse resp) {
    try {
      super.service(req, resp);
    } catch (Exception e) {
      // TODO(arjuns): Auto-generated catch block
      throw new RuntimeException(e);
    }
  }
  

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    String callbackUrl = ServletUtils.getServletUrl(request, OAUTH2_GOOGLE_DOC_AUTH_CB);
    String state = QueryUtils.getValidDto(request, RedirectDto.class).toJson();
    
    OAuth2Helper instance = factory.create(GOOGLE_DOC);

    String actualRedirectUrl = instance.getOAuth2RedirectUri(callbackUrl, state);
    logger.info("Redirecting to : " + actualRedirectUrl);
    response.sendRedirect(actualRedirectUrl);
  }
}
