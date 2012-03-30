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
package com.google.light.server.utils;

import static com.google.light.server.utils.ServletUtils.getRequestUriWithQueryParams;
import static com.google.light.testingutils.TestingUtils.getRandomString;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.google.light.server.servlets.path.ServletPathEnum;
import javax.servlet.http.HttpServletRequest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test for {@link ServletUtils}
 * 
 * @author Arjun Satyapal
 */
public class ServletUtilsTest {
  private String serverUrl = "http://localhost:8080";

  private String code;
  @Before
  public void setUp() {
    code = getRandomString();
  }
  /**
   * Test for {@link ServletUtils#getRequestUriWithQueryParams(HttpServletRequest)}.
   */
  @Test
  public void test_getRequestUriWithQueryParams() {
    // Positive Test : Testing for redirect returned by Google Login flow for Success.
    HttpServletRequest request1 = Mockito.mock(HttpServletRequest.class);
    
    String codeKeyValue = "code=" + code;
    StringBuffer googLoginCbReqUrlBuffer = new StringBuffer(serverUrl)
        .append(ServletPathEnum.OAUTH2_GOOGLE_LOGIN_CB.get())
        .append("?")
        .append(codeKeyValue);
    when(request1.getRequestURL()).thenReturn(googLoginCbReqUrlBuffer);
    assertEquals(googLoginCbReqUrlBuffer.toString(), getRequestUriWithQueryParams(request1));
    
    // TODO(arjuns) : Add a test when query params are null.
    // TODO(arjuns): Fix this test as mentioned in issue 25001.
  }
  
  /**
   * Test for {@link ServletUtils#getServerUrl(HttpServletRequest)}.
   */
  @Test
  public void test_getServerUrl() {
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    
    String codeKeyValue = "code=" + code;
    String uri = ServletPathEnum.OAUTH2_GOOGLE_LOGIN_CB.get() + "?" + codeKeyValue;
    
    StringBuffer googLoginCbReqUrlBuffer = new StringBuffer(serverUrl)
        .append(uri);
    
    when(request.getRequestURL()).thenReturn(googLoginCbReqUrlBuffer);
    when(request.getRequestURI()).thenReturn(uri);
    
    assertEquals(serverUrl, ServletUtils.getServerUrl(request));
  }
}
