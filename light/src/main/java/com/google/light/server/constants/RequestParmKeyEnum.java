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
  AUTH_DOMAIN("auth_domain", false),
  
  // TODO(arjuns) : Rename this to Email.
  GAE_USER_EMAIL("gae_user_email", false),
  @Deprecated /*gae_user_id should not be used any more.*/
  GAE_USER_ID("gae_user_id", false),
  PERSON_ID("person_id", false);
  
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
