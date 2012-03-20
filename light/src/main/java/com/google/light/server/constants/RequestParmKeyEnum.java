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
 * Enum to hold Keys for Http Headers.
 * 
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */
public enum RequestParmKeyEnum {
  // TODO(arjuns): Add cateogry to differentiate on what goes in Sessino and what comes in URL.
  LOGIN_PROVIDER_ID("login_provider_id", true),
  LOGIN_PROVIDER_USER_ID("login_provider_user_id", true),
  LOGIN_PROVIDER_USER_EMAIL("login_provider_user_email", true),
  
  PERSON_ID("person_id", true),
  
  OAUTH2_PROVIDER_NAME("oauth2_provider_name", true),
  CLIENT_ID("client_id", true),
  CLIENT_SECRET("client_secret", true);
  
  private String key;
  private boolean allowedInProd;
  
  private RequestParmKeyEnum(String key, boolean allowedInProd) {
    this.key = checkNotBlank(key);
    this.allowedInProd = allowedInProd;
  }
  
  public String get() {
    return key;
  }
  
  // TODO(arjuns) : Add header validation.
  public boolean isAllowedInProd() {
    return allowedInProd;
  }
}
