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
package com.google.light.server.servlets.admin;

import static com.google.light.server.utils.LightUtils.appendKeyValue;
import static com.google.light.server.utils.LightUtils.appendSectionHeader;
import static com.google.light.server.utils.LightUtils.appendSessionData;
import static com.google.light.server.utils.LightUtils.getPST8PDTime;

import com.google.appengine.api.utils.SystemProperty;
import com.google.apphosting.api.ApiProxy;
import com.google.apphosting.api.ApiProxy.Environment;
import com.google.inject.Inject;
import com.google.light.server.constants.ContentTypeEnum;
import com.google.light.server.servlets.AbstractLightServlet;
import com.google.light.server.utils.GaeUtils;
import com.google.light.server.utils.LightPreconditions;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet to handle configuration for Light.
 * 
 * TODO(arjuns) : Add test for this.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class ConfigServlet extends AbstractLightServlet {
  

  @Inject
  public ConfigServlet() {
  }
  
  @SuppressWarnings("deprecation")
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) {
    // TODO(arjuns) : Move this check to filter or some where else as this causes Guice Injection
    // failures if put if put in constructor. Probably can be moved to filter.
    
    LightPreconditions.checkPersonIsGaeAdmin();
    
    try {
      StringBuilder builder = new StringBuilder();
      Environment env = ApiProxy.getCurrentEnvironment();
      appendSectionHeader(builder, "Appengine SystemProperty");
      appendKeyValue(builder, "applicationid", SystemProperty.applicationId.get());

      String applicationVersion = SystemProperty.applicationVersion.get();
      appendKeyValue(builder, "applicationVersion", applicationVersion);

      String[] splits = applicationVersion.split("\\.");
      appendKeyValue(builder, "version", splits[0]);

      /*
       * Source :
       * stackoverflow.com/questions/3948861/appengine-get-current-serving-application-version
       */
      Long timeStamp = (long) (Long.parseLong(splits[1]) / (long) Math.pow(2, 28));
      appendKeyValue(builder, "possible deployTime (PST8PDT)", getPST8PDTime(timeStamp * 1000));

      // appendKeyValue("version", applicationVersion.split(".")[0]);
      /*
       * TODO(arjuns): Replace with ServerService when Available. For now suppressing deprecated
       * warning.
       */
      appendKeyValue(builder, "instanceReplicaId", SystemProperty.instanceReplicaId.get());
      appendKeyValue(builder, "JavaSdkVersion", SystemProperty.version.get());

      appendSectionHeader(builder, "Appengine Environment");
      appendKeyValue(builder, "applicationEnvironment", SystemProperty.environment.get());

      appendKeyValue(builder, "isDevServer", GaeUtils.isDevServer());
      appendKeyValue(builder, "isProduction", GaeUtils.isProductionServer());
      appendKeyValue(builder, "isQaServer", GaeUtils.isQaServer());
      appendKeyValue(builder, "isUnitTestServer", GaeUtils.isUnitTestServer());
      appendKeyValue(builder, "authDomain", env.getAuthDomain());
      appendKeyValue(builder, "env.applicationId", env.getAppId());
      appendKeyValue(builder, "email", env.getEmail());

      Map<String, Object> map = env.getAttributes();
      for (String currKey : map.keySet()) {
        appendKeyValue(builder, currKey, map.get(currKey));
      }

      // Populating Session Data.
      HttpSession session = request.getSession();
      appendSectionHeader(builder, "Session Details = " + false);
      appendSessionData(builder, session);

      response.setContentType(ContentTypeEnum.TEXT_HTML.get());
      response.getWriter().println(builder.toString());

    } catch (Exception e) {
      // TODO(arjuns): Add exception handling.
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
  public long getLastModified(HttpServletRequest request) {
    return -1;
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
