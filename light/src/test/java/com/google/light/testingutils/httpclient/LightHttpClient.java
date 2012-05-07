/*
 * Copyright 2012 Google Inc.
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
package com.google.light.testingutils.httpclient;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.light.server.constants.HttpHeaderEnum;
import java.io.IOException;
import java.net.URI;
import org.openqa.selenium.Cookie;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class LightHttpClient {
  private Cookie cookie;
  private HttpHeaders httpHeaders;
  
  public LightHttpClient(Cookie cookie) {
    this.cookie = checkNotNull(cookie, "cookie cannot be null");
    this.httpHeaders = new HttpHeaders();
    httpHeaders.set(HttpHeaderEnum.COOKIE.get(), cookie.toString());
  }
  
  private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
  public HttpResponse post(URI uri, HttpContent content) throws IOException {
    HttpRequest postRequest = HTTP_TRANSPORT.createRequestFactory().buildPostRequest(
        new GenericUrl(uri.toString()), content);
    
    postRequest.setHeaders(httpHeaders);
    HttpResponse response = postRequest.execute();
    return response;
  }
}
