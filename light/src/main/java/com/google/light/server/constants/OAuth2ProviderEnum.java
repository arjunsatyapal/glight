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
package com.google.light.server.constants;

import com.google.light.server.servlets.test.CredentialStandardEnum;

/**
 * Enum listing external resource provider
 * (OAuth2 Providers, search providers, ...) to whom
 * Light needs to authenticate so it can consume
 * its resources.
 * 
 * @author Arjun Satyapal
 * 
 * TODO(waltercacau): Rename this to ConsumerAuthenticationEnum
 */
public enum OAuth2ProviderEnum {
  // OAuth2
  GOOGLE(CredentialStandardEnum.OAUTH2),
  

  // Search Providers
  /**
   * GSS stands for Google Site Search, which is
   * the paid version of CSE (Custom Search Engine)
   */
  GSS(CredentialStandardEnum.CLIENT_LOGIN);
  
  private CredentialStandardEnum credentialStandard;

  private OAuth2ProviderEnum(CredentialStandardEnum credentialStandard) {
    this.credentialStandard = credentialStandard;
  }

  public CredentialStandardEnum getCredentialStandard() {
    return credentialStandard;
  }

}
