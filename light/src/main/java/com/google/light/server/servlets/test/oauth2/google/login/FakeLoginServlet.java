/*
 * Copyright (C) Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.light.server.servlets.test.oauth2.google.login;

import static com.google.light.server.constants.RequestParmKeyEnum.LOGIN_PROVIDER_ID;
import static com.google.light.server.constants.RequestParmKeyEnum.LOGIN_PROVIDER_USER_EMAIL;
import static com.google.light.server.constants.RequestParmKeyEnum.LOGIN_PROVIDER_USER_ID;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;

import com.google.light.server.utils.GaeUtils;

import com.google.inject.Inject;
import com.google.light.server.constants.ContentTypeEnum;
import com.google.light.server.constants.OAuth2Provider;
import com.google.light.server.exception.unchecked.FilterInstanceBindingException;
import com.google.light.server.utils.LightUtils;
import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet to enable Fake Login for testing.
 * 
 * TODO(arjuns) : Add test for this.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class FakeLoginServlet extends HttpServlet {
  private static final Logger logger = Logger.getLogger(FakeLoginServlet.class.getName());

  @Inject
  private FakeLoginServlet() {
    if (GaeUtils.isProductionServer()) {
      String msg = "TestServletFilter should not be injected for Production Env.";
      logger.severe(msg);
      throw new FilterInstanceBindingException(msg);
    }
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doGet(request, response);
  }
  
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    HttpSession session = request.getSession();

    String providerNameStr = checkNotBlank(request.getParameter(LOGIN_PROVIDER_ID.get()));
    OAuth2Provider provider = OAuth2Provider.valueOf(providerNameStr);
    
    String providerUserId = checkNotBlank(request.getParameter(LOGIN_PROVIDER_USER_ID.get()));
    String providerUserEmail = checkNotBlank(request.getParameter(LOGIN_PROVIDER_USER_EMAIL.get()));
    session.setMaxInactiveInterval(120 /*seconds*/);

    LightUtils.prepareSession(session, provider, providerUserId, providerUserEmail);
    
    StringBuilder builder = new StringBuilder();
    LightUtils.appendSessionData(builder, session);

    logger.info("Session info : " + builder.toString());
    response.setContentType(ContentTypeEnum.TEXT_HTML.get());
    response.getWriter().println(builder.toString());
  }
}
