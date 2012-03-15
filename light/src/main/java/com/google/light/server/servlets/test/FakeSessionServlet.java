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
import static com.google.light.server.utils.LightUtils.appendKeyValue;
import static com.google.light.server.utils.LightUtils.appendSectionHeader;
import static com.google.light.server.utils.LightUtils.getPST8PDTime;

import com.google.light.server.utils.LightUtils;

import com.google.inject.Inject;
import com.google.light.server.constants.ContentTypeEnum;
import com.google.light.server.constants.LightAppIdEnum;
import com.google.light.server.exception.unchecked.FilterInstanceBindingException;
import com.google.light.server.exception.unchecked.httpexception.PersonLoginRequiredException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Logger;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet to enable Fake Session for testing.
 * 
 * TODO(arjuns) : Add test for this.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class FakeSessionServlet extends HttpServlet {
  private static final Logger logger = Logger.getLogger(FakeSessionServlet.class.getName());

  @Inject
  private FakeSessionServlet() {
    if (LightAppIdEnum.PROD == LightAppIdEnum.getLightAppIdEnum()) {
      String msg = "FakeSessionServlet should be injected for Test&QA environment only.";
      logger.severe(msg);
      throw new FilterInstanceBindingException(msg);
    }
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    HttpSession session = request.getSession();
    if (!session.isNew()) {
      logger.info("existing session.");
      return;
    }
    logger.info("Created new session.");
    session.setMaxInactiveInterval(120 /* seconds */);
  }

  @SuppressWarnings("deprecation")
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    StringBuilder builder = new StringBuilder();
    appendSectionHeader(builder, "Session Details = " + false);
    HttpSession session = request.getSession();

    String testGaeUserId = null;
    String testGaeUserEmail = null;

    if (session.isNew()) {
      session.invalidate();
      throw new PersonLoginRequiredException("First create a session.");
    }

    testGaeUserId = checkNotBlank((String) session.getAttribute(GAE_USER_ID.get()));
    testGaeUserEmail = checkNotBlank((String) session.getAttribute(GAE_USER_EMAIL.get()));

    appendKeyValue(builder, "testGaeUserId", testGaeUserId);
    appendKeyValue(builder, "testGaeUserEmail", testGaeUserEmail);

    appendKeyValue(builder, "creationTimeInMillis", session.getCreationTime());
    appendKeyValue(builder, "creationTime", getPST8PDTime(session.getCreationTime()));
    appendKeyValue(builder, "sessionId", session.getId());
    appendKeyValue(builder, "lastAccessedTimeInMillis", session.getLastAccessedTime());
    appendKeyValue(builder, "lastAccessedTimeInMillis",
        getPST8PDTime(session.getLastAccessedTime()));
    appendKeyValue(builder, "maxInactiveIntervalInSec", session.getMaxInactiveInterval());
    appendKeyValue(builder, "servletContext", session.getServletContext());
    appendKeyValue(builder, "sessionContext", session.getSessionContext());

    @SuppressWarnings("rawtypes")
    Enumeration names = session.getAttributeNames();
    builder.append("<br>names = ");
    while (names.hasMoreElements()) {
      builder.append(" ").append(names.nextElement());
    }

    response.setContentType(ContentTypeEnum.TEXT_HTML.get());
    response.getWriter().println(builder.toString());
  }
}
