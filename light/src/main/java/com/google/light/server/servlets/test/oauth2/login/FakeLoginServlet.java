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
package com.google.light.server.servlets.test.oauth2.login;

import static com.google.light.server.constants.RequestParamKeyEnum.DEFAULT_EMAIL;
import static com.google.light.server.constants.RequestParamKeyEnum.LOGIN_PROVIDER_ID;
import static com.google.light.server.constants.RequestParamKeyEnum.LOGIN_PROVIDER_USER_ID;
import static com.google.light.server.constants.RequestParamKeyEnum.PERSON_ID;
import static com.google.light.server.utils.LightPreconditions.checkIsNotEnv;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.LightPreconditions.checkPersonId;
import static com.google.light.server.utils.ServletUtils.prepareSession;

import com.google.light.server.dto.pojo.longwrapper.PersonId;

import com.google.inject.Inject;
import com.google.light.server.constants.LightEnvEnum;
import com.google.light.server.constants.OAuth2ProviderService;
import com.google.light.server.constants.http.ContentTypeEnum;
import com.google.light.server.utils.LightUtils;
import com.google.light.server.utils.ServletUtils;
import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet to enable Fake Login for testing.
 * 
 * TODO(arjuns) : Add test for this.
 * 
 * @author Arjun Satyapal
 */
@Deprecated
@SuppressWarnings("serial")
public class FakeLoginServlet extends HttpServlet {
  private static final Logger logger = Logger.getLogger(FakeLoginServlet.class.getName());

  @Inject
  private FakeLoginServlet() {
    checkIsNotEnv(this, LightEnvEnum.PROD);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doGet(request, response);
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    String providerNameStr = checkNotBlank(
        request.getParameter(LOGIN_PROVIDER_ID.get()), "loginProvider");
    OAuth2ProviderService providerService = OAuth2ProviderService.valueOf(providerNameStr);

    String personIdStr = checkNotBlank(request.getParameter(PERSON_ID.get()), "personId");
    PersonId personId = checkPersonId(new PersonId(Long.parseLong(personIdStr)));
    String providerUserId =
        checkNotBlank(request.getParameter(LOGIN_PROVIDER_USER_ID.get()), "loginProviderUserId");
    String defaultEmail = checkNotBlank(request.getParameter(DEFAULT_EMAIL.get()), "email");

    prepareSession(request, providerService, personId, providerUserId, defaultEmail);

    StringBuilder builder = new StringBuilder();
    LightUtils.appendSessionData(builder, ServletUtils.getSession(request));

    logger.info("Session info : " + builder.toString());
    response.setContentType(ContentTypeEnum.TEXT_HTML.get());
    response.getWriter().println(builder.toString());
  }
}
