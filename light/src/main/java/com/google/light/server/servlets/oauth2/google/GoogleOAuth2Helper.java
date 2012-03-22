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
import static com.google.light.server.constants.OAuth2Provider.GOOGLE_LOGIN;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;

import com.google.common.annotations.VisibleForTesting;

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
import com.google.light.server.annotations.AnotGoogleLoginCallbackUri;
import com.google.light.server.constants.OAuth2Provider;
import com.google.light.server.guice.providers.InstanceProvider;
import com.google.light.server.manager.interfaces.OAuth2ConsumerCredentialManager;
import com.google.light.server.servlets.oauth2.google.pojo.GoogleTokenInfo;
import com.google.light.server.servlets.oauth2.google.pojo.GoogleUserInfo;
import com.google.light.server.utils.GaeUtils;
import com.google.light.server.utils.JsonUtils;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Helper class for Google OAuth.
 * 
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */
public class GoogleOAuth2Helper {
  private InstanceProvider instanceProvider;
  private HttpTransport httpTransport;
  private JsonFactory jsonFactory;
  private OAuth2ConsumerCredentialManager consumerCredentialManager;
  private String googleLoginCbUri;

  private final HttpRequestFactory requestFactory;
  @Inject
  public GoogleOAuth2Helper(Provider<InstanceProvider> instanceProviderProvider,
      OAuth2ConsumerCredentialManager consumerCredentialManager,
      @AnotGoogleLoginCallbackUri String googleLoginCbUri) {
    this.instanceProvider = instanceProviderProvider.get();
    this.httpTransport = instanceProvider.getHttpTransport();
    this.jsonFactory = instanceProvider.getJsonFactory();
    this.requestFactory = httpTransport.createRequestFactory();
    this.consumerCredentialManager = checkNotNull(consumerCredentialManager);
    this.googleLoginCbUri = checkNotBlank(googleLoginCbUri);
  }

  /**
   * URL from where Information about a Google Access Token can be fetched.
   */
  @VisibleForTesting
  static final String TOKEN_INFO_URL = "https://www.googleapis.com/oauth2/v1/tokeninfo";
  
  /**
   * URL from where a Google Customer's Profile and Email can be fetched.
   */
  @VisibleForTesting
  static final String USER_INFO_URL = "https://www.googleapis.com/oauth2/v1/userinfo";
  
  /**
   * Method to get {@link AuthorizationCodeFlow} for {@link OAuth2Provider#GOOGLE_LOGIN}.
   * 
   * @return
   */
  public AuthorizationCodeFlow getAuthorizationCodeFlow() {
    return new AuthorizationCodeFlow.Builder(
            BearerToken.authorizationHeaderAccessMethod(),
            httpTransport,
            jsonFactory, 
            new GenericUrl(GOOGLE_LOGIN.getTokenServerUrl()),
            new ClientParametersAuthentication(
                consumerCredentialManager.getClientId(), consumerCredentialManager.getClientSecret()),
            consumerCredentialManager.getClientId(),
            GOOGLE_LOGIN.getAuthServerUrl())
      .setScopes(GOOGLE_LOGIN.getScopes())
      .build();
  }
  
  /**
   * Method to get AccessToken once a Person has given Authorization to Light for Accessing his/her
   * Email and UserInfo from Google.
   * 
   * @param request
   * @param code
   * @return
   * @throws IOException
   */
  public TokenResponse getAccessToken(String serverUrl, String code) throws IOException {
    AuthorizationCodeFlow flow = getAuthorizationCodeFlow();
    String redirectUri = getLightUriForLoginCompletion(serverUrl);
    
    return flow.newTokenRequest(code).setRedirectUri(redirectUri).execute();
  }

  /**
   * Method to get TokenInfo for a Google Access Token.
   * 
   * @param accessToken
   * @return
   * @throws IOException
   */
  public GoogleTokenInfo getTokenInfo(String accessToken) throws IOException {
    GenericUrl tokenUrl = new GenericUrl(TOKEN_INFO_URL + "?access_token=" + accessToken); 
    
    HttpRequest tokenInfoRequest = requestFactory.buildGetRequest(tokenUrl);
    HttpResponse tokenInfoResponse = tokenInfoRequest.execute();

    String tokenInfoStr = CharStreams.toString(new InputStreamReader(
        tokenInfoResponse.getContent(), Charsets.UTF_8));
    return JsonUtils.getDto(tokenInfoStr, GoogleTokenInfo.class);
  }
  
  /**
   * Method to fetch information about the owner of the AccessToken.
   * 
   * @param accessToken
   * @return
   * @throws IOException
   */
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
  
  /**
   * Get a Light URI where Google will redirect the User's browser once Login flow is complete.
   * 
   * @param serverUrl
   * @return
   */
  @VisibleForTesting
  String getLightUriForLoginCompletion(String serverUrl) {
    if (GaeUtils.isUnitTestServer()) {
      return googleLoginCbUri;
    } else {
      return  serverUrl + googleLoginCbUri;
    }
  }
  
  /**
   * Get a Google URI where Light should redirect User's browser to initiate the Login Procedure.
   * 
   * For more details, see {@link https://developers.google.com/accounts/docs/OAuth2Login}.
   * For browser based Login Flow, a sample URL might look like :
   *    https://accounts.google.com/o/oauth2/auth?
   *    scope=https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.email+https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.profile&
   *    state=%2Fprofile&
   *    redirect_uri=https%3A%2F%2Foauth2-login-demo.appspot.com%2Foauthcallback&
   *    response_type=token&
   *    client_id=812741506391.apps.googleusercontent.com
   * 
   * @return
   */
  public String getGoogleLoginRedirectUri(String serverUrl) {
    String lightUri = getLightUriForLoginCompletion(serverUrl);
    
    AuthorizationCodeFlow flow = getAuthorizationCodeFlow();
    
    return flow.newAuthorizationUrl().setRedirectUri(lightUri).build();
  }
}
