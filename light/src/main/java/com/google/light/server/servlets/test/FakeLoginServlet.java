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
package com.google.light.server.servlets.test;

import static com.google.light.server.constants.RequestParmKeyEnum.GAE_USER_EMAIL;
import static com.google.light.server.constants.RequestParmKeyEnum.GAE_USER_ID;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;

import javax.servlet.ServletException;

import com.google.inject.Inject;
import com.google.light.server.constants.LightAppIdEnum;
import com.google.light.server.exception.unchecked.FilterInstanceBindingException;
import com.google.light.server.utils.LightUtils;
import java.io.IOException;
import java.util.logging.Logger;
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
    if (LightAppIdEnum.PROD == LightAppIdEnum.getLightAppIdEnum()) {
      String msg = "TestServletFilter should be injected for Test&QA environment only.";
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

    String testGaeUserId = null;
    String testGaeUserEmail = null;
    testGaeUserId = checkNotBlank(request.getParameter(GAE_USER_ID.get()));
    testGaeUserEmail = checkNotBlank(request.getParameter(GAE_USER_EMAIL.get()));
    session.setMaxInactiveInterval(120 /*seconds*/);

    LightUtils.prepareSession(session, testGaeUserId, testGaeUserEmail);

    String msg = "Setting gaeUserId[" + testGaeUserId + "], gaeUserEmail[" + testGaeUserEmail
        + "]"; 
    logger.info(msg);
    response.getWriter().println(msg);
  }
}
