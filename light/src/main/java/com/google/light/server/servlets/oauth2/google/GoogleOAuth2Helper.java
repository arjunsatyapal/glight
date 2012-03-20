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
package com.google.light.server.servlets.oauth2.google;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.servlets.path.ServletPathEnum.LOGIN_GOOGLE_CB;
import static com.google.light.server.utils.ServletUtils.getServerUrl;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.light.server.annotations.AnotOAuth2ConsumerGoogleLogin;
import com.google.light.server.guice.providers.InstanceProvider;
import com.google.light.server.manager.interfaces.OAuth2ConsumerManager;
import com.google.light.server.servlets.oauth2.google.pojo.GoogleTokenInfo;
import com.google.light.server.servlets.oauth2.google.pojo.GoogleUserInfo;
import com.google.light.server.utils.JsonUtils;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.servlet.http.HttpServletRequest;

/**
 * Helper class for Google OAuth.
 * 
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */
public class GoogleOAuth2Helper {
  /** OAuth 2 Login Scope. */
  public static final String[] LOGIN_SCOPE = {
    "https://www.googleapis.com/auth/userinfo.email",
    "https://www.googleapis.com/auth/userinfo.profile",
  };

  private InstanceProvider instanceProvider;
  private HttpTransport httpTransport;
  private JsonFactory jsonFactory;
  private OAuth2ConsumerManager oauth2ConsumerManager;

  private final HttpRequestFactory requestFactory;
  @Inject
  public GoogleOAuth2Helper(Provider<InstanceProvider> instanceProviderProvider,
      @AnotOAuth2ConsumerGoogleLogin OAuth2ConsumerManager oauth2ConsumerManager) {
    this.instanceProvider = instanceProviderProvider.get();
    this.httpTransport = instanceProvider.getHttpTransport();
    this.jsonFactory = instanceProvider.getJsonFactory();
    this.requestFactory = httpTransport.createRequestFactory();
    this.oauth2ConsumerManager = checkNotNull(oauth2ConsumerManager);
  }


  public static final String TOKEN_SERVER_URL = "https://accounts.google.com/o/oauth2/token";
  public static final String TOKEN_INFO_URL = "https://www.googleapis.com/oauth2/v1/tokeninfo";
  public static final String AUTHORIZATION_SERVER_URL = "https://accounts.google.com/o/oauth2/auth";
  public static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v1/userinfo";
  
  public AuthorizationCodeFlow getAuthorizationCodeFlow() {
    return new AuthorizationCodeFlow.Builder(
            BearerToken.authorizationHeaderAccessMethod(),
            httpTransport,
            jsonFactory, 
            new GenericUrl(GoogleOAuth2Helper.TOKEN_SERVER_URL),
            new ClientParametersAuthentication(
                oauth2ConsumerManager.getClientId(), oauth2ConsumerManager.getClientSecret()),
            oauth2ConsumerManager.getClientId(),
            AUTHORIZATION_SERVER_URL)
      .setScopes(LOGIN_SCOPE)
      .build();
  }
  
  public TokenResponse getAccessToken(HttpServletRequest request, String code) throws IOException {
    AuthorizationCodeFlow flow = getAuthorizationCodeFlow();
    String redirectUri = getServerUrl(request) + LOGIN_GOOGLE_CB.get();
    return flow.newTokenRequest(code).setRedirectUri(redirectUri).execute();
  }

  public GoogleTokenInfo getTokenInfo(String accessToken) throws IOException {
    GenericUrl tokenUrl = new GenericUrl(TOKEN_INFO_URL + "?access_token=" + accessToken); 
    
    HttpRequest tokenInfoRequest = requestFactory.buildGetRequest(tokenUrl);
    HttpResponse tokenInfoResponse = tokenInfoRequest.execute();

    String tokenInfoStr = CharStreams.toString(new InputStreamReader(
        tokenInfoResponse.getContent(), Charsets.UTF_8));
    return JsonUtils.getDto(tokenInfoStr, GoogleTokenInfo.class);
  }
  
  public GoogleUserInfo getUserInfo(String accessToken) throws IOException {
    // TODO(arjuns): Find a better URL builder.
    GenericUrl userInfoUrl = new GenericUrl(USER_INFO_URL + "?access_token=" + accessToken);
    HttpRequest userInfoRequest = requestFactory.buildGetRequest(userInfoUrl);
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", accessToken);
    userInfoRequest.setHeaders(headers);

    HttpResponse userInfoResponse = userInfoRequest.execute();
    String userInfoString = CharStreams.toString(
        new InputStreamReader(userInfoResponse.getContent(), Charsets.UTF_8));
    return JsonUtils.getDto(userInfoString, GoogleUserInfo.class);
  }
}
