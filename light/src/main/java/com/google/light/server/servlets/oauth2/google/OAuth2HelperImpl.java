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

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.AuthorizationCodeTokenRequest;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.light.server.constants.OAuth2ProviderService;
import com.google.light.server.manager.implementation.oauth2.consumer.OAuth2ConsumerCredentialManagerFactory;
import com.google.light.server.manager.implementation.oauth2.owner.OAuth2OwnerTokenManagerFactory;
import com.google.light.server.manager.interfaces.OAuth2ConsumerCredentialManager;
import com.google.light.server.manager.interfaces.OAuth2OwnerTokenManager;
import com.google.light.server.servlets.oauth2.google.pojo.AbstractOAuth2TokenInfo;
import java.io.IOException;

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
  private OAuth2OwnerTokenManager ownerTokenManager;
  private OAuth2ProviderService providerService;

  @Inject
  public OAuth2HelperImpl(OAuth2ConsumerCredentialManagerFactory consumerCredentialManagerFactory,
      OAuth2OwnerTokenManagerFactory ownerTokenManagerFactory,
      HttpTransport httpTransport, JsonFactory jsonFactory,
      @Assisted OAuth2ProviderService providerService) {
    this.providerService = checkNotNull(providerService, "providerService");

    this.httpTransport = checkNotNull(httpTransport, "httpTransport");
    this.jsonFactory = checkNotNull(jsonFactory, "jsonFactory");
    httpTransport.createRequestFactory();
    this.consumerCredentialManager = checkNotNull(
        consumerCredentialManagerFactory.create(providerService.getProvider()),
        "consumerCredentialManager");

    this.ownerTokenManager = checkNotNull(ownerTokenManagerFactory.create(this.providerService));
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
        consumerCredentialManager.getClientAuthentication(),
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
  public <D extends AbstractOAuth2TokenInfo<D>> D getTokenInfo(String accessToken)
      throws IOException {
    return ownerTokenManager.getInfoByAccessToken(accessToken);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getOAuth2RedirectUri(String cbUrl, String state) {
    AuthorizationCodeRequestUrl codeRequestUrlBuilder =
        getAuthorizationCodeRequestUrlForOfflineAccess(cbUrl);

    if (state != null)
      codeRequestUrlBuilder.setState(state);

    return codeRequestUrlBuilder.build();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getOAuth2RedirectUriWithPrompt(String cbUrl, String state) {
    AuthorizationCodeRequestUrl codeRequestUrlBuilder =
        getAuthorizationCodeRequestUrlForOfflineAccess(cbUrl);

    if (state != null)
      codeRequestUrlBuilder.setState(state);

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

  @Override
  public HttpTransport getHttpTransport() {
    return httpTransport;
  }
}
