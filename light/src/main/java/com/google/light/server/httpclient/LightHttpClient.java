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
package com.google.light.server.httpclient;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.light.server.utils.LightPreconditions.checkNotNull;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.inject.Inject;
import com.google.light.server.constants.HttpHeaderEnum;
import com.google.light.server.dto.pojo.typewrapper.stringwrapper.ExternalId;
import com.google.light.server.exception.ExceptionType;
import com.google.light.server.utils.LightUtils;
import java.io.IOException;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class LightHttpClient {
  private HttpTransport httpTransport;
  @SuppressWarnings("unused")
  private JsonFactory jsonFactory;

  @Inject
  public LightHttpClient(HttpTransport httpTransport, JsonFactory jsonFactory) {
    this.httpTransport = checkNotNull(
        httpTransport, ExceptionType.SERVER_GUICE_INJECTION, "httpTransport");

    this.jsonFactory = checkNotNull(
        jsonFactory, ExceptionType.SERVER_GUICE_INJECTION, "jsonFactory");
  }

  // public HttpResponse post(URI uri, HttpContent content) throws IOException {
  // HttpRequest postRequest = HTTP_TRANSPORT.createRequestFactory().buildPostRequest(
  // new GenericUrl(uri.toString()), content);
  //
  // postRequest.setHeaders(httpHeaders);
  // HttpResponse response = postRequest.execute();
  // return response;
  // }

  public HttpResponse get(URI uri) throws IOException {
    HttpRequest request = httpTransport.createRequestFactory()
        .buildGetRequest(new GenericUrl(uri.toString()));
    HttpResponse response = request.execute();
    checkArgument(response.isSuccessStatusCode(), "Failed to download");
    return response;
  }

  public static String
      getHeaderValueFromRequest(HttpRequest request, HttpHeaderEnum requiredHeader) {
    return doGetHeaderValue(request.getHeaders(), requiredHeader);
  }

  public static String getHeaderValueFromResponse(HttpResponse response,
      HttpHeaderEnum requiredHeader) {
    return doGetHeaderValue(response.getHeaders(), requiredHeader);
  }

  /**
   * @param headers
   * @param requiredHeader
   * @return
   */
  private static String doGetHeaderValue(HttpHeaders headers, HttpHeaderEnum requiredHeader) {
    for (String curr : headers.keySet()) {
      if (curr.toLowerCase().equals(requiredHeader.get().toLowerCase())) {
        return (String) headers.get(curr);
      }
    }

    return null;
  }

  public String getTitle(HttpResponse response, ExternalId externalId) {
    String generatedTitle = LightUtils.generateNameForExternalId(externalId);
    try {
      String html = LightUtils.getInputStreamAsString(response.getContent());

      html = html.replaceAll("\\s+", " ");
      Pattern pattern = Pattern.compile("<title>(.*?)</title>");
      Matcher matcher = pattern.matcher(html);

      if (matcher.find() == true) {
        return matcher.group(1);
      }

      return generatedTitle;
    } catch (Exception e) {
      return generatedTitle;
    }
  }
}
