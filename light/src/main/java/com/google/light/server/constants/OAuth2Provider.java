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
package com.google.light.server.constants;

import static com.google.light.server.utils.LightPreconditions.checkNonEmptyList;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.LightPreconditions.checkValidUri;

import com.google.common.collect.Lists;
import java.net.URISyntaxException;
import java.util.ArrayList;

/**
 * Enum to encapsulate details for different OAuth2 providers.
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public enum OAuth2Provider {
  GOOGLE_LOGIN("google",
               "https://accounts.google.com/o/oauth2/auth",
               "https://accounts.google.com/o/oauth2/token",
               Lists.newArrayList(
                   "https://www.googleapis.com/auth/userinfo.email",
                   "https://www.googleapis.com/auth/userinfo.profile"
                   ));

  private String providerName;
  private String authServerUrl;
  private String tokenServerUrl;
  private ArrayList<String> scopes;

  private OAuth2Provider(String providerName, String authServerUrl, String tokenServerUrl, 
      ArrayList<String> scopes) {
    try {
      this.providerName = checkNotBlank(providerName);
      this.authServerUrl = checkValidUri(authServerUrl);
      this.tokenServerUrl = checkValidUri(tokenServerUrl);
      this.scopes = (ArrayList<String>) checkNonEmptyList(scopes);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException(e);
    }
  }

  public String getProviderName() {
    return providerName;
  }
  
  public String getAuthServerUrl() {
    return authServerUrl;
  }

  public String getTokenServerUrl() {
    return tokenServerUrl;
  }

  public ArrayList<String> getScopes() {
    return scopes;
  }
}
