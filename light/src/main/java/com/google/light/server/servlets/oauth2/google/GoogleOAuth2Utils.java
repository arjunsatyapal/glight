/*
 * Copyright 2012 Google Inc.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.light.server.servlets.oauth2.google;

import static com.google.light.server.constants.LightConstants.GOOGLE_USER_INFO_URL;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.light.server.servlets.oauth2.google.pojo.GoogleUserInfo;
import com.google.light.server.utils.JsonUtils;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Utility class for OAuth2 Workflow for Google.
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class GoogleOAuth2Utils {
  /**
   * Method to fetch information about the owner of the AccessToken.
   * 
   * @param accessToken
   * @return
   * @throws IOException
   */
  public static GoogleUserInfo getUserInfo(HttpTransport httpTransport, String accessToken)
      throws IOException {
    // TODO(arjuns): Find a better URL builder.
    GenericUrl userInfoUrl = new GenericUrl(GOOGLE_USER_INFO_URL + "?access_token=" + accessToken);
    HttpRequest userInfoRequest = httpTransport.createRequestFactory().buildGetRequest(userInfoUrl);
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", accessToken);
    userInfoRequest.setHeaders(headers);

    HttpResponse userInfoResponse = userInfoRequest.execute();
    String userInfoString = CharStreams.toString(
        new InputStreamReader(userInfoResponse.getContent(), Charsets.UTF_8));
    return JsonUtils.getPojo(userInfoString, GoogleUserInfo.class);
  }
}
