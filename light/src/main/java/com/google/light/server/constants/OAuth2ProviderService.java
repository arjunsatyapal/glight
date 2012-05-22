/*
 * Copyright (C) Google Inc.
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
package com.google.light.server.constants;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkNotEmptyCollection;
import static com.google.light.server.utils.LightPreconditions.checkValidUri;

import com.google.common.collect.Lists;
import com.google.light.server.servlets.oauth2.google.pojo.AbstractOAuth2TokenInfo;
import com.google.light.server.servlets.oauth2.google.pojo.GoogleLoginTokenInfo;
import com.google.light.server.servlets.oauth2.google.pojo.GoogleOAuth2TokenInfo;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * Enum to encapsulate details for different OAuth2 providers.
 * 
 * @author Arjun Satyapal
 */
public enum OAuth2ProviderService {
  // TODO(arjuns) : Once we have more values here, then update tests to get a random value from
  // this.
  GOOGLE_LOGIN(OAuth2ProviderEnum.GOOGLE,
               true /* usedForLogin */,
               "https://accounts.google.com/o/oauth2/auth",
               "https://accounts.google.com/o/oauth2/token",
               "https://www.googleapis.com/oauth2/v1/tokeninfo",
               GoogleLoginTokenInfo.class,
               Lists.newArrayList(
                   "https://www.googleapis.com/auth/userinfo.email",
                   "https://www.googleapis.com/auth/userinfo.profile"
                   )),
  GOOGLE_DOC(OAuth2ProviderEnum.GOOGLE,
             false /* usedForLogin */,
             "https://accounts.google.com/o/oauth2/auth",
             "https://accounts.google.com/o/oauth2/token",
             "https://www.googleapis.com/oauth2/v1/tokeninfo",
             GoogleOAuth2TokenInfo.class,
             Lists.newArrayList(
                 "https://docs.google.com/feeds/",
                 "https://docs.googleusercontent.com/",
                 "https://spreadsheets.google.com/feeds/"
                 ));

  /**
   * OAuth2 Provider for this ProviderService. More then one ProviderSerivce can share the same
   * provider.
   */
  private OAuth2ProviderEnum provider;
  private boolean usedForLogin;
  private String authServerUrl;
  private String tokenServerUrl;
  private String tokenInfoUrl;
  @SuppressWarnings("rawtypes")
  private Class tokenInfoClazz;
  private ArrayList<String> scopes;
  

  private <D extends AbstractOAuth2TokenInfo<D>> OAuth2ProviderService(
      OAuth2ProviderEnum provider,
      boolean usedForLogin,
      String authServerUrl,
      String tokenServerUrl,
      String tokenInfoUrl,
      Class<D> tokenInfoClazz,
      ArrayList<String> scopes) {
    try {
      this.provider = checkNotNull(provider, "provider");
      this.usedForLogin = usedForLogin;
      this.authServerUrl = checkValidUri(authServerUrl);
      this.tokenServerUrl = checkValidUri(tokenServerUrl);
      this.tokenInfoUrl = checkValidUri(tokenInfoUrl);
      this.tokenInfoClazz = checkNotNull(tokenInfoClazz);
      this.scopes = (ArrayList<String>) checkNotEmptyCollection(scopes, "scopes");
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException(e);
    }
  }

  /**
   * Get OAuth2 Provider for this.
   * 
   * @return
   */
  public OAuth2ProviderEnum getProvider() {
    return provider;
  }

  public String getAuthServerUrl() {
    return authServerUrl;
  }

  public String getTokenServerUrl() {
    return tokenServerUrl;
  }
  
  public String getTokenInfoUrl() {
    return tokenInfoUrl;
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public  Class<? extends AbstractOAuth2TokenInfo> getTokenInfoClazz() {
    return tokenInfoClazz;
  }

  public ArrayList<String> getScopes() {
    return scopes;
  }
  
  public boolean isUsedForLogin() {
    return usedForLogin;
  }
}
