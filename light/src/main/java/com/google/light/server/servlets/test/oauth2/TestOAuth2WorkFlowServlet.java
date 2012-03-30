/*
 * Copyright 2012 Google Inc.
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
package com.google.light.server.servlets.test.oauth2;

import com.google.light.server.constants.ContentTypeEnum;
import com.google.light.server.constants.HtmlPathEnum;
import com.google.light.server.servlets.path.ServletPathEnum;
import com.google.light.server.utils.LightUtils;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet to trigger any OAuth2 workflow.
 * 
 * TODO(arjuns): Move utility methods to LightUtils.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class TestOAuth2WorkFlowServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) {
    try {
      StringBuilder builder = new StringBuilder();
      LightUtils.appendSectionHeader(builder, "Consumer Credentials :");
      appendHtmlPath(builder, HtmlPathEnum.PUT_OAUTH2_CONSUMER_CREDENTIAL);

      LightUtils.appendSectionHeader(builder, "Owner OAuth2 Workflow :");
      appendServletPath(builder, ServletPathEnum.OAUTH2_GOOGLE_LOGIN);
      appendServletPath(builder, ServletPathEnum.OAUTH2_GOOGLE_DOC_AUTH);

      response.setContentType(ContentTypeEnum.TEXT_HTML.get());
      response.getWriter().println(builder.toString());
    } catch (Exception e) {
      // TODO(arjuns): Add exception handling.
      throw new RuntimeException(e);
    }
  }

  private void appendHtmlPath(StringBuilder builder, HtmlPathEnum htmlPath) {
    appendHtml(builder, htmlPath.get(), htmlPath.name());
  }

  private void appendServletPath(StringBuilder builder, ServletPathEnum servletPath) {
    appendHtml(builder, servletPath.get(), servletPath.name());
  }

  private void appendHtml(StringBuilder builder, String href, String name) {
    String idStr = "id=" + name;
    builder.append("<a " + idStr + " href=\"").append(href).append("\">").append(name).append("</a><br>");
  }
}
