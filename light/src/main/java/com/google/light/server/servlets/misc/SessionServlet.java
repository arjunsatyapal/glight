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
package com.google.light.server.servlets.misc;

import static com.google.light.server.utils.LightUtils.appendSectionHeader;
import static com.google.light.server.utils.LightUtils.appendSessionData;

import com.google.light.server.constants.ContentTypeEnum;
import com.google.light.server.exception.unchecked.FilterInstanceBindingException;
import com.google.light.server.utils.GaeUtils;
import java.io.IOException;
import java.util.logging.Logger;
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
public class SessionServlet extends HttpServlet {
  private static final Logger logger = Logger.getLogger(SessionServlet.class.getName());
  
  public SessionServlet() {
    if (GaeUtils.isProductionServer()) {
      String msg = "SessionServlet should not be injected for non-production environments.";
      logger.severe(msg);
      throw new FilterInstanceBindingException(msg);
    }
  }
  
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    HttpSession session = request.getSession();
    
    StringBuilder builder = new StringBuilder();
    appendSectionHeader(builder, "Session Details = " + false);
    appendSessionData(builder, session);

    response.setContentType(ContentTypeEnum.TEXT_HTML.get());
    response.getWriter().println(builder.toString());
  }
}