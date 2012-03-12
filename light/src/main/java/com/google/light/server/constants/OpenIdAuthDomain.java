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

import static com.google.light.server.utils.LightPreconditions.checkNotBlank;

/**
 * This enum is a wrapper for AuthDomains for Different OpenId Providers.
 * 
 * @author Arjun Satyapal
 */
public enum OpenIdAuthDomain {
  GOOGLE("https://www.google.com/accounts/o8/ud");
  
  private String domain;
  
  private OpenIdAuthDomain(String domain) {
    this.domain = checkNotBlank(domain);
  }
  
  public String get() {
    return domain;
  }
  
  public static OpenIdAuthDomain getAuthDomainByValue(String domain) {
    for (OpenIdAuthDomain curr : OpenIdAuthDomain.values()) {
      if (curr.get().equals(domain))
        return curr;
    }
    
    // TODO(arjuns) : Add test for this.
    throw new IllegalArgumentException("Illegal domain : " + domain);
  }
}
