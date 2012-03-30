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
import static com.google.light.server.constants.LightConstants.GOOLGE_TOKEN_INFO_URL;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.AuthorizationCodeTokenRequest;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.RefreshTokenRequest;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.light.server.constants.OAuth2ProviderService;
import com.google.light.server.dto.DtoInterface;
import com.google.light.server.manager.implementation.oauth2.consumer.OAuth2ConsumerCredentialManagerFactory;
import com.google.light.server.manager.interfaces.OAuth2ConsumerCredentialManager;
import com.google.light.server.utils.JsonUtils;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Implementation for {@link OAuth2Helper}.
 * 
 * TODO(arjuns): Add test for this class.
 * TODO(arjuns) : Inject Provider.
 * TODO(arjuns): Move this out of google package.
 * 
 * @author Arjun Satyapal
 */
public class OAuth2HelperImpl implements OAuth2Helper {
  private HttpTransport httpTransport;
  private JsonFactory jsonFactory;
  private OAuth2ConsumerCredentialManager consumerCredentialManager;
  private OAuth2ProviderService providerService;
  private final HttpRequestFactory requestFactory;

  @Inject
  public OAuth2HelperImpl(OAuth2ConsumerCredentialManagerFactory consumerCredentialManagerFactory,
      HttpTransport httpTransport, JsonFactory jsonFactory,
      @Assisted OAuth2ProviderService providerService) {
    this.providerService = checkNotNull(providerService, "providerService");

    this.httpTransport = checkNotNull(httpTransport, "httpTransport");
    this.jsonFactory = checkNotNull(jsonFactory, "jsonFactory");
    this.requestFactory = httpTransport.createRequestFactory();
    this.consumerCredentialManager = checkNotNull(
            consumerCredentialManagerFactory.create(providerService.getProvider()),
            "consumerCredentialManager");

  }

  private ClientParametersAuthentication getClientAuthentication() {
    return new ClientParametersAuthentication(
        consumerCredentialManager.getClientId(),
        consumerCredentialManager.getClientSecret());
  }

  /**
   * Method to get {@link AuthorizationCodeFlow} for {@link OAuth2ProviderService#GOOGLE_LOGIN}.
   * 
   * @return
   */
  @Override
  public AuthorizationCodeFlow getAuthorizationCodeFlow() {
    return new AuthorizationCodeFlow.Builder(
        BearerToken.authorizationHeaderAccessMethod(),
        httpTransport,
        jsonFactory,
        new GenericUrl(providerService.getTokenServerUrl()),
        getClientAuthentication(),
        consumerCredentialManager.getClientId(),
        providerService.getAuthServerUrl())
        .setScopes(providerService.getScopes())
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
  @Override
  public TokenResponse getAccessToken(String cbUrl, String code)
      throws IOException {
    AuthorizationCodeFlow flow = getAuthorizationCodeFlow();
    AuthorizationCodeTokenRequest tokenRequest = flow.newTokenRequest(code);
    tokenRequest.setRedirectUri(cbUrl);
    return tokenRequest.execute();
  }

  /**
   * Method to get TokenInfo for a Google Access Token.
   * 
   * @param accessToken
   * @return
   * @throws IOException
   */
  @Override
  public <D extends DtoInterface<D>> D getTokenInfo(String accessToken, Class<D> clazz)
      throws IOException {
    GenericUrl tokenUrl = new GenericUrl(GOOLGE_TOKEN_INFO_URL + "?access_token=" + accessToken);

    HttpRequest tokenInfoRequest = requestFactory.buildGetRequest(tokenUrl);
    HttpResponse tokenInfoResponse = tokenInfoRequest.execute();

    String tokenInfoStr = CharStreams.toString(new InputStreamReader(
        tokenInfoResponse.getContent(), Charsets.UTF_8));

    DtoInterface<D> tokenInfo = JsonUtils.getDto(tokenInfoStr, clazz);

    return tokenInfo.validate();
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public String getOAuth2RedirectUri(String cbUrl) {
    return getAuthorizationCodeRequestUrlForOfflineAccess(cbUrl).build();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getOAuth2RedirectUriWithPrompt(String cbUrl) {
    AuthorizationCodeRequestUrl codeRequestUrlBuilder =
        getAuthorizationCodeRequestUrlForOfflineAccess(cbUrl);

    /*
     * approval_prompt is forced, so that refreshToken is returned irrespective of user visiting
     * first or second time.
     */
    codeRequestUrlBuilder.set("approval_prompt", "force");

    return codeRequestUrlBuilder.build();
  }

  private AuthorizationCodeRequestUrl getAuthorizationCodeRequestUrlForOfflineAccess(String cbUrl) {
    AuthorizationCodeFlow flow = getAuthorizationCodeFlow();

    AuthorizationCodeRequestUrl authorizationUrlBuilder = flow.newAuthorizationUrl();
    authorizationUrlBuilder.setRedirectUri(cbUrl);
    /*
     * access_type needs to be set to offline, as we want Google to return refreshToken. If this is
     * not set, then Google will return only an accessToken with expiry period of 1 hour. And
     * presence of user is required if another accessToken is required.
     */
    authorizationUrlBuilder.set("access_type", "offline");
    return authorizationUrlBuilder;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <D extends DtoInterface<D>> D refreshAccessToken(String refreshToken, Class<D> clazz)
      throws IOException {
    RefreshTokenRequest refreshRequest = new RefreshTokenRequest(
        httpTransport,
        jsonFactory,
        new GenericUrl(providerService.getTokenServerUrl()),
        refreshToken)
        .setClientAuthentication(getClientAuthentication());

    TokenResponse tokenResponse = refreshRequest.execute();
    // TODO(arjuns): See if refresh request returns a accessToken.
    return getTokenInfo(tokenResponse.getAccessToken(), clazz);
  }

  // TODO(arjuns) : Add code for refreshToken for user unsubscription.

  /**
   * {@inheritDoc}
   */
  @Override
  public <D extends DtoInterface<D>> boolean isValidAccessToken(String accessToken, Class<D> clazz) {
    try {
      // TODO(arjuns) : see if additional checks are required her.
      getTokenInfo(accessToken, clazz);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public HttpTransport getHttpTransport() {
    return httpTransport;
  }
}
