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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.HttpUnsuccessfulResponseHandler;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;
import com.google.light.server.utils.LightPreconditions;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;

/**
 * A helper class to allow UnitTests to login to either: 1. AppEngine Dev Server. 2. AppEngine
 * Server running in real Environment.
 * 
 * @author Arjun Satyapal
 */
public class OpenIdCookieProvider {
  private final String email;
  private final String passwd;
  private final String authServletUrl;
  private final boolean isLocalHost;

  private String AUTH_SERVLET = "/_ah/login";

  private String cookie = null;

  private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

  public OpenIdCookieProvider(boolean isLocalHost, String email, String serverUrl)
      throws IOException {
    this.email = checkNotBlank(email);
    checkNotBlank(serverUrl);
    checkArgument(!serverUrl.endsWith("/"), "ServerUrl should not end with /.");

    this.isLocalHost = isLocalHost;
    if (isLocalHost) {
      passwd = null;
    } else {
      String homeDir = System.getProperty("user.home");

      File file = new File(homeDir + "/credentials/openid/" + email);
      checkArgument(file.exists(), file.getName() + " does not exist.");
      checkArgument(file.isFile(), file.getName() + " is not a file.");
      this.passwd = Files.toString(file, Charsets.UTF_8);
      checkNotBlank(this.passwd, "For email[" + email + "], password was blank.");
    }

    this.authServletUrl = serverUrl + AUTH_SERVLET;
  }

  public String getCookie() throws IOException {
    if (cookie != null) {
      return cookie;
    }

    return getCookieFromServer();
  }

  private void setCookie(String cookie) {
    this.cookie = LightPreconditions.checkNotBlank(cookie);
  }

  public String getEmail() {
    return email;
  }

  /**
   * @return
   * @throws IOException
   */
  private String getCookieFromServer() throws IOException {
    HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory();

    HttpRequest request = null;
    Map<String, String> map = getMap();

    if (!isLocalHost) {
      UrlEncodedContent authContent = new UrlEncodedContent(map);
      GenericUrl authTokenUrl = new GenericUrl("https://www.google.com/accounts/ClientLogin");
      request = requestFactory.buildPostRequest(authTokenUrl, authContent);
      HttpResponse response = request.execute();
      String responseString =
          CharStreams.toString(new InputStreamReader(response.getContent(), Charsets.UTF_8));
      String authToken = responseString.split("\n")[2].split("=")[1];
      GenericUrl loginUrl = new GenericUrl(authServletUrl + "?auth=" + authToken);

      request = requestFactory.buildPostRequest(loginUrl, new UrlEncodedContent(null));
    } else {
      UrlEncodedContent authContent = new UrlEncodedContent(map);
      GenericUrl loginUrl = new GenericUrl(authServletUrl);
      request = requestFactory.buildPostRequest(loginUrl, authContent);
    }

    request.setUnsuccessfulResponseHandler(getHttpUnsuccessfulResponseHandler());
    checkNotNull(request);

    try {
      request.execute();
    } catch (HttpResponseException e) {

    }

    return checkNotBlank(cookie);
  }

  private Map<String, String> getMap() {
    Map<String, String> map = Maps.newConcurrentMap();
    if (isLocalHost) {
      map.put("email", email);
      map.put("action", "Log+In");
    } else {
      map.put("Email", email);
      map.put("Passwd", passwd);
      map.put("accountType", "HOSTED_OR_GOOGLE");
      map.put("source", "light");
      map.put("service", "ah");
    }

    return map;
  }

  private HttpUnsuccessfulResponseHandler getHttpUnsuccessfulResponseHandler() {
    return new HttpUnsuccessfulResponseHandler() {

      @SuppressWarnings("synthetic-access")
      @Override
      public boolean handleResponse(HttpRequest request, HttpResponse response,
          boolean retrySupported)
          throws IOException {
        HttpHeaders headers = response.getHeaders();
        @SuppressWarnings("unchecked")
        ArrayList<String> cookieValues = (ArrayList<String>) headers.get("Set-Cookie");
        LightPreconditions.checkNonEmptyList(cookieValues);

        setCookie(cookieValues.get(0).split(";")[0]);
        return false;
      }
    };
  }
}
