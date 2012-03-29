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
package com.google.light.testingutils;

import static com.google.light.server.constants.RequestParmKeyEnum.DEFAULT_EMAIL;
import static com.google.light.server.constants.RequestParmKeyEnum.LOGIN_PROVIDER_ID;
import static com.google.light.server.constants.RequestParmKeyEnum.PERSON_ID;
import static com.google.light.server.utils.LightPreconditions.checkEmail;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.LightPreconditions.checkPersonId;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.light.server.constants.OAuth2ProviderService;
import com.google.light.server.servlets.path.ServletPathEnum;
import com.google.light.server.utils.LightPreconditions;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/**
 * Helper class for Fake Login which uses {@link FakeLoginServlet}.
 * 
 * @author Arjun Satyapal
 */
public class FakeLoginHelper {

  private String serverUrl;
  private OAuth2ProviderService providerService;
  private long personId;
  private String providerUserEmail;
  private String cookie;

  public FakeLoginHelper(String serverUrl, OAuth2ProviderService providerService, long personId,
      String providerUserEmail, boolean isUserAdmin) throws IOException {
    this.serverUrl = checkNotBlank(serverUrl);
    this.providerService = Preconditions.checkNotNull(providerService);
    this.personId = checkPersonId(personId);
    this.providerUserEmail = checkEmail(providerUserEmail);
    init();
  }

  private void init() throws IOException {
    if (cookie != null) {
      return;
    }

    HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory();
    GenericUrl url = new GenericUrl(serverUrl + ServletPathEnum.FAKE_LOGIN.get());

    Map<String, String> map = new ImmutableMap.Builder<String, String>()
        .put(LOGIN_PROVIDER_ID.get(), providerService.name())
        .put(PERSON_ID.get(), Long.toString(personId))
        .put(DEFAULT_EMAIL.get(), providerUserEmail)
        .build();
    
    HttpRequest request = requestFactory.buildPostRequest(url, new UrlEncodedContent(map));
    HttpResponse response = request.execute();
    cookie = getCookieFromResponse(response);
  }

  public static String getCookieFromResponse(HttpResponse response) {
    Preconditions.checkNotNull(response);
    HttpHeaders headers = response.getHeaders();
    @SuppressWarnings("unchecked")
    ArrayList<String> cookieValues = (ArrayList<String>) headers.get("Set-Cookie");
    LightPreconditions.checkNonEmptyList(cookieValues);
    return cookieValues.get(0).split(";")[0];
  }

  public String getCookie() {
    return cookie;
  }
}
