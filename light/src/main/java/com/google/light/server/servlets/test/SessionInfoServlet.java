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

import static com.google.light.server.utils.LightPreconditions.checkIsNotEnv;
import static com.google.light.server.utils.LightUtils.appendSessionData;

import com.google.light.server.constants.LightEnvEnum;
import com.google.light.server.constants.http.ContentTypeEnum;
import com.google.light.server.utils.ServletUtils;
import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet to show Session Information to User.
 * 
 * TODO(arjuns) : Add test for this.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class SessionInfoServlet extends HttpServlet {
  public SessionInfoServlet() {
    checkIsNotEnv(this, LightEnvEnum.PROD);
  }

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    HttpSession session = ServletUtils.getSession(request);

    StringBuilder builder = new StringBuilder();
    appendSessionData(builder, session);

    response.setContentType(ContentTypeEnum.TEXT_HTML.get());
    response.getWriter().println(builder.toString());
  }
}
