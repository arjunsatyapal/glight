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
package com.google.light.server.servlets.path;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;

import com.google.light.server.exception.unchecked.httpexception.NotFoundException;
import com.google.light.server.servlets.admin.ConfigServlet;
import com.google.light.server.servlets.admin.OAuth2CredentialServlet;
import com.google.light.server.servlets.login.LoginServlet;
import com.google.light.server.servlets.misc.SessionServlet;
import com.google.light.server.servlets.oauth2.google.login.GoogleLoginCallbackServlet;
import com.google.light.server.servlets.oauth2.google.login.GoogleLoginServlet;
import com.google.light.server.servlets.person.PersonServlet;
import com.google.light.server.servlets.test.TestHeaders;
import com.google.light.server.servlets.test.TestLogin;
import com.google.light.server.servlets.test.oauth2.google.login.FakeLoginServlet;
import javax.servlet.http.HttpServlet;

/**
 * Enum to Map Servlets with their Paths and URL Patterns.
 * 
 * TODO(arjuns) : Add tests for this.
 * 
 * @author Arjun Satyapal
 */
public enum ServletPathEnum {
  // TODO(arjuns): Add regex checks here.
  // TODO(arjuns) : Find a way to end URLs without /.
  PERSON(PersonServlet.class, "/api/person",
         true, false, false),
/*TODO(arjuns): Uncooment in next cl.
  LOGIN(LoginServlet.class, "/login",
      false, false, false),
      
  LOGIN_GOOGLE(GoogleLoginServlet.class, "/login/google",
      false, false, false),
      
  LOGIN_GOOGLE_CB(GoogleLoginCallbackServlet.class, "/login/google_login_callback",
      false, false, false),*/

  // Admin Servlets
  CONFIG(ConfigServlet.class, "/admin/config",
      true, true, false),
      
  OAUTH2_CONSUMER_CRENDENTIAL(OAuth2CredentialServlet.class, "/admin/oauth2_consumer_cred",
      true, true, false),

  // Some test servlets.
  FAKE_LOGIN(FakeLoginServlet.class, "/test/fakelogin",
      false, false, true),
      
  SESSION(SessionServlet.class, "/test/session",
      true, false, true),
  TEST_HEADER(TestHeaders.class, "/test/testheader",
      false, false, true),
  TEST_LOGIN(TestLogin.class, "/test/testlogin",
      true, false, true);

  
  private Class<? extends HttpServlet> clazz;
  private String servletPath;
  // Path when this servlet acts as root for others.
  private String servletRoot;

  private boolean requiresLogin;
  private boolean requiresAdminPrivilege;
  private boolean onlyForTest;

  

  private ServletPathEnum(Class<? extends HttpServlet> clazz, String servletPath, 
      boolean requireLogin, boolean requireAdminPrivilege, boolean onlyForTest) {
    this.clazz = clazz;
    this.servletPath = checkNotBlank(servletPath);
    checkArgument(!servletPath.endsWith("/"));
    this.servletRoot = checkNotBlank(servletPath + "/");
    this.requiresLogin = requireLogin;
    
    if (!requireLogin && requireAdminPrivilege) {
      throw new IllegalStateException("For AdminPrivileges, Login is mandatory.");
    }
    this.requiresAdminPrivilege = requireAdminPrivilege;
    
    this.onlyForTest = onlyForTest;
  }

  public Class<? extends HttpServlet> getClazz() {
    return clazz;
  }

  public boolean isRequiredLogin() {
    return requiresLogin;
  }
  
  public boolean isRequiredAdminPrivilege() {
    return requiresAdminPrivilege;
  }
  
  public boolean isOnlyForTest() {
    return onlyForTest;
  }

  public String get() {
    return servletPath;
  }

  public String getRoot() {
    return servletRoot;
  }
  


  // TODO(arjuns): Add test for this.
  public static ServletPathEnum getServletPathEnum(String requestUri) {
    String uri = requestUri.endsWith("/") ? requestUri.substring(0, requestUri.length() - 1)
                  : requestUri;
    
    for (ServletPathEnum currServletPath : ServletPathEnum.values()) {
      if (currServletPath.get().equals(uri)) {
        return currServletPath;
      }
    }

    throw new NotFoundException("ServletPath not found for servletPath[" + requestUri
        + "].");
  }
}
