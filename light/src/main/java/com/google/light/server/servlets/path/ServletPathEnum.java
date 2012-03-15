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

import static com.google.light.server.utils.LightPreconditions.checkNotBlank;

import com.google.light.server.servlets.test.FakeSessionServlet;

import com.google.light.server.servlets.test.FakeLoginServlet;


import com.google.light.server.servlets.admin.ConfigServlet;

import com.google.light.server.exception.unchecked.httpexception.NotFoundException;
import com.google.light.server.servlets.person.PersonServlet;
import com.google.light.server.servlets.test.TestHeaders;
import com.google.light.server.servlets.test.TestLogin;
import javax.servlet.http.HttpServlet;

/**
 * Enum to Map Servlets with their Paths and URL Patterns.
 * 
 *TODO(arjuns) : Add tests for this.
 * 
 * @author Arjun Satyapal
 */
public enum ServletPathEnum {
  // TODO(arjuns): Add regex checks here.
  // TODO(arjuns) : Find a way to end URLs without /.
  PERSON(PersonServlet.class, false, "/api/person", "/api/person/"),
  CONFIG(ConfigServlet.class, false, "/admin/config", "/admin/config/"),
  
  // Some test servlets.
  FAKE_LOGIN(FakeLoginServlet.class, true, "/test/fakelogin", "/test/fakelogin/"),
  FAKE_SESSION(FakeSessionServlet.class, true, "/test/fakesession", "/test/fakesession/"),
  TEST_HEADER(TestHeaders.class, true, "/test/testheader", "/test/testheader/"),
  TEST_LOGIN(TestLogin.class, true, "/test/testlogin", "/test/testlogin/");
  

  private Class<? extends HttpServlet> clazz;
  private boolean onlyForTest;
  
  private String servletPath;
  // Path when this servlet acts as root for others.
  private String servletRoot;
  

  private ServletPathEnum(Class<? extends HttpServlet> clazz, boolean onlyForTest,
      String servletPath, String servletRoot) {
    this.clazz = clazz;
    this.servletPath = checkNotBlank(servletPath);
    this.servletRoot = checkNotBlank(servletRoot);
    this.onlyForTest = onlyForTest;
  }

  public Class<? extends HttpServlet> getClazz() {
    return clazz;
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

  public static ServletPathEnum getServletPathEnum(String servletPath) {
    for (ServletPathEnum currServletPath : ServletPathEnum.values()) {
      if (currServletPath.get().equals(servletPath)) {
        return currServletPath;
      }
    }

    throw new NotFoundException("ServletPath not found for servletPath[" + servletPath
        + "].");
  }
}
