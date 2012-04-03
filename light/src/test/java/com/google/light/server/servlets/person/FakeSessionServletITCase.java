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

import static com.google.light.server.constants.OAuth2ProviderService.GOOGLE_LOGIN;
import static com.google.light.server.constants.RequestParamKeyEnum.DEFAULT_EMAIL;
import static com.google.light.server.constants.RequestParamKeyEnum.LOGIN_PROVIDER_ID;
import static com.google.light.server.constants.RequestParamKeyEnum.LOGIN_PROVIDER_USER_ID;
import static com.google.light.server.constants.RequestParamKeyEnum.PERSON_ID;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.testingutils.TestingUtils.getRandomPersonId;
import static com.google.light.testingutils.TestingUtils.getRandomProviderUserId;
import static org.junit.Assert.assertTrue;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.common.collect.ImmutableMap;
import com.google.light.server.servlets.path.ServletPathEnum;
import com.google.light.testingutils.FakeLoginHelper;
import com.google.light.testingutils.TestingUtils;
import java.util.Map;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test for {@link SessionInfoServlet}.
 * 
 * TODO(arjuns): Convert this test from Integration test to unit-test.
 * 
 * @author Arjun Satyapal
 */
public class FakeSessionServletITCase {
  private static String serverUrl;
  private static String providerUserId;
  private static String email;
  private static long personId;
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

    providerUserId = getRandomProviderUserId();
    email = TestingUtils.getRandomEmail();
    personId = getRandomPersonId();
  }

  /**
   * Test for {@link SessionInfoServlet#doPost(HttpServletRequest, HttpServletResponse)}.
   * 
   * Test for creating a session.
   * TODO(arjuns): Fix this test and merge with {@link FakeLoginHelper}.
   */
  @Test
  public void test_doPost() throws Exception {
    HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory();

    GenericUrl fakeLoginServletUrl = new GenericUrl(serverUrl + ServletPathEnum.FAKE_LOGIN.get());
    
    Map<String, String> map = new ImmutableMap.Builder<String, String>()
        .put(LOGIN_PROVIDER_ID.get(), GOOGLE_LOGIN.name())
        .put(LOGIN_PROVIDER_USER_ID.get(), providerUserId)
        .put(DEFAULT_EMAIL.get(), email)
        .put(PERSON_ID.get(), Long.toString(personId))
        .build();

    HttpRequest request = requestFactory.buildPostRequest(
        fakeLoginServletUrl, new UrlEncodedContent(map));
    HttpResponse response = request.execute();

    String cookie = FakeLoginHelper.getCookieFromResponse(response);
    checkNotBlank(cookie, "cookie");

    assertTrue(response.isSuccessStatusCode());
  }
}
