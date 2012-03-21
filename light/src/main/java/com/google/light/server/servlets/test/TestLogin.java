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

import static com.google.light.server.utils.LightPreconditions.checkEmail;
import static com.google.light.server.utils.LightPreconditions.checkIsNotEnv;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.inject.Inject;
import com.google.light.server.constants.LightEnvEnum;
import com.google.light.server.servlets.AbstractLightServlet;
import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A simple class to help testing for Login.
 * 
 * TODO(arjuns) : Remove this once integration tests are stable.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class TestLogin extends AbstractLightServlet {
  private static final Logger logger = Logger.getLogger(TestLogin.class.getName());
  
  @Inject
  public TestLogin() {
    checkIsNotEnv(this, LightEnvEnum.PROD);
  }
  
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) {
    try {
      StringBuilder builder = new StringBuilder("Hello ");

      UserService userService = UserServiceFactory.getUserService();
      User user = userService.getCurrentUser();
      logger.info("Logged in : " + userService.isUserLoggedIn());
      if (user == null) {
        logger.info("user == null.");
        builder.append("<anonymous>");
      } else {
        logger.info("Email = " + user.getEmail());
        checkEmail(user.getEmail());
        builder.append(user.getEmail());
      }

      response.getWriter().println(builder.toString());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /** 
   * {@inheritDoc}
   */
  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response) {
    doGet(request, response);
  }

  /** 
   * {@inheritDoc}
   */
  @Override
  public long getLastModified(HttpServletRequest request) {
    return -1;
  }

  /** 
   * {@inheritDoc}
   */
  @Override
  public void doHead(HttpServletRequest request, HttpServletResponse response) {
    doGet(request, response);
  }

  /** 
   * {@inheritDoc}
   */
  @Override
  public void doOptions(HttpServletRequest request, HttpServletResponse response) {
    doGet(request, response);
  }

  /** 
   * {@inheritDoc}
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) {
    doGet(request, response);
  }

  /** 
   * {@inheritDoc}
   */
  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) {
    doGet(request, response);
  }
}
