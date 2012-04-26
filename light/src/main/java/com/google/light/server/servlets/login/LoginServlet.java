/*
 * Copyright (C) Google Inc.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.light.server.servlets.login;

import com.google.light.server.constants.OAuth2ProviderService;
import com.google.light.server.constants.http.ContentTypeEnum;
import com.google.light.server.servlets.AbstractLightServlet;
import com.google.light.server.servlets.path.ServletPathEnum;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that asks user for choosing their Login Provider.
 * 
 * TODO(arjuns) : Add test for this.
 * TODO(waltercacau) : probably this can be replaced with Static HTML.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class LoginServlet extends AbstractLightServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) {
    try {
      response.setContentType(ContentTypeEnum.TEXT_HTML.get());
      StringBuilder builder = new StringBuilder("Hello <b>Anonymous</b> : Login using <br>");
      // TODO(arjuns): Create a Enum for HTML tags, and move this to LightUtils.
      builder.append("<a id=" + OAuth2ProviderService.GOOGLE_LOGIN.name() + " href=\"")
          .append(ServletPathEnum.OAUTH2_GOOGLE_LOGIN.get()).append("\">Google")
          .append("</a></div>");
      response.getWriter().println(builder.toString());
    } catch (Exception e) {
      // TODO(arjuns): add exception handling.
      throw new RuntimeException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response) {
    // TODO(arjuns): Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doHead(HttpServletRequest request, HttpServletResponse response) {
    // TODO(arjuns): Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doOptions(HttpServletRequest request, HttpServletResponse response) {
    // TODO(arjuns): Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) {
    // TODO(arjuns): Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) {
    // TODO(arjuns): Auto-generated method stub
    throw new UnsupportedOperationException();
  }
}
