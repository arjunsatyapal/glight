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
package com.google.light.server.servlets.person;

import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.testingutils.FakeLoginHelper.getCookieFromResponse;
import static org.junit.Assert.assertTrue;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.common.collect.ImmutableMap;
import com.google.light.server.constants.RequestParmKeyEnum;
import com.google.light.server.servlets.path.ServletPathEnum;
import com.google.light.server.servlets.test.FakeSessionServlet;
import com.google.light.testingutils.TestingUtils;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test for {@link FakeSessionServlet}.
 * 
 * TODO(arjuns): Convert this test from Integration test to unittest.
 * 
 * @author Arjun Satyapal
 */
public class FakeSessionServletITCase {
  private static String serverUrl;
  private static String email;
  private static String gaeUserId;
  private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

  @BeforeClass
  public static void cookieSetup() {
    String testServerUrl = System.getProperty("test.server_url");

    if (testServerUrl != null) {
      serverUrl = checkNotBlank(testServerUrl, "test.server_url should not be empty.");
    } else {
      serverUrl = "http://localhost:8080";
    }
    // serverUrl = "http://light-qa.appspot.com";

    if (serverUrl.startsWith("http://localhost")) {
    } else {
    }

    // email = "unit-test@myopenedu.com";
    email = TestingUtils.getRandomEmail();
    gaeUserId = TestingUtils.getRandomUserId();
  }

  /**
   * Test for {@link FakeSessionServlet#doPost(HttpServletRequest, HttpServletResponse)}.
   * 
   * Test for creating a session.
   */
  @Test
  public void test_doPost() throws Exception {
    HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory();
    GenericUrl fakeSessionServletUrl =
        new GenericUrl(serverUrl + ServletPathEnum.FAKE_SESSION.get());
    HttpRequest request =
        requestFactory.buildPostRequest(fakeSessionServletUrl, new UrlEncodedContent(null));
    HttpResponse response = request.execute();
    assertTrue(response.isSuccessStatusCode());

    String cookie = getCookieFromResponse(response);
    checkNotBlank(cookie);

    GenericUrl fakeLoginServletUrl = new GenericUrl(serverUrl + ServletPathEnum.FAKE_LOGIN.get());
    Map<String, String> map = new ImmutableMap.Builder<String, String>()
        .put(RequestParmKeyEnum.GAE_USER_EMAIL.get(), email)
        .put(RequestParmKeyEnum.GAE_USER_ID.get(), gaeUserId)
        .put(RequestParmKeyEnum.USER_ADMIN.get(), "false")
        .build();

    request = requestFactory.buildPostRequest(fakeLoginServletUrl, new UrlEncodedContent(map));

    HttpHeaders headers = new HttpHeaders();
    headers.set("Cookie", cookie);
    request.setHeaders(headers);
    response = request.execute();
    assertTrue(response.isSuccessStatusCode());
  }
}
