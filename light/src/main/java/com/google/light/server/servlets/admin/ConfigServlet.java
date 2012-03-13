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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.appengine.api.utils.SystemProperty;
import com.google.apphosting.api.ApiProxy;
import com.google.apphosting.api.ApiProxy.Environment;
import com.google.light.server.constants.ContentTypeEnum;
import com.google.light.server.utils.GaeUtils;
import java.util.Map;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * Servlet to handle configuration for Light.
 * 
 * TODO(arjuns) : Add test for this.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class ConfigServlet extends HttpServlet {
  private StringBuilder builder = null;

  @SuppressWarnings("deprecation")
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) {
    try {
      builder = new StringBuilder();
      Environment env = ApiProxy.getCurrentEnvironment();
      appendSectionHeader("Appengine SystemProperty");
      appendKeyValue("applicationid", SystemProperty.applicationId.get());

      String applicationVersion = SystemProperty.applicationVersion.get();
      appendKeyValue("applicationVersion", applicationVersion);

      String[] splits = applicationVersion.split("\\.");
      appendKeyValue("version", splits[0]);

      /*
       * Source :  
       * stackoverflow.com/questions/3948861/appengine-get-current-serving-application-version
       */
      Long timeStamp = (long) (Long.parseLong(splits[1]) / (long) Math.pow(2, 28));
      appendKeyValue("possible deployTime (PST8PDT)",
          new DateTime(timeStamp * 1000, DateTimeZone.forID("PST8PDT")));

      // appendKeyValue("version", applicationVersion.split(".")[0]);
      /*
       * TODO(arjuns): Replace with ServerService when Available. For now suppressing deprecated
       * warning.
       */
      appendKeyValue("instanceReplicaId", SystemProperty.instanceReplicaId.get());
      appendKeyValue("JavaSdkVersion", SystemProperty.version.get());

      appendSectionHeader("Appengine Environment");
      appendKeyValue("applicationEnvironment", SystemProperty.environment.get());

      appendKeyValue("isProduction", GaeUtils.isProductionServer());
      appendKeyValue("isDevServer", GaeUtils.isDevServer());
      appendKeyValue("isQaServer", GaeUtils.isQaServer());
      appendKeyValue("authDomain", env.getAuthDomain());
      appendKeyValue("env.applicationId", env.getAppId());
      appendKeyValue("email", env.getEmail());

      Map<String, Object> map = env.getAttributes();
      for (String currKey : map.keySet()) {
        appendKeyValue(currKey, map.get(currKey));
      }

      response.setContentType(ContentTypeEnum.TEXT_HTML.get());
      response.getWriter().println(builder.toString());
    } catch (Exception e) {
      // TODO(arjuns): Add exception handling.
      throw new RuntimeException(e);
    }
  }

  private void appendSectionHeader(String sectionHeader) {
    builder.append("<br><br><b>").append(sectionHeader).append(": </b>");
  }

  private void appendKeyValue(String key, Object value) {
    checkNotNull(builder);
    builder.append("<br>").append(key).append(" = ").append(value);
  }
}
