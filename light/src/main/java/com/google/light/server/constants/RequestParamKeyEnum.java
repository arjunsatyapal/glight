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
 * Enum to encapsulate Strings that are used for RequestParameters, Stored in Persistence etc.
 * 
 * TODO(arjuns): Move params to another class which are not used for Production.
 * TODO(arjuns): Add boolean category to show what is used for RequestParams and what is used
 * for Persistence.
 * TODO(arjuns): Rename this class to RequestKeys.
 *
 * 
 * @author Arjun Satyapal
 */
public enum RequestParamKeyEnum {
  CLIENT_ID("client_id", true),
  CLIENT_SECRET("client_secret", true),

  DEFAULT_EMAIL("default_email", true),

  JOB_ID("job_id", true),
  
  // TODO(arjuns): Add category to differentiate on what goes in Session and what comes in URL.
  LOGIN_PROVIDER_ID("login_provider_id", true),
  LOGIN_PROVIDER_USER_ID("login_provider_user_id", true),

  MODULE_TYPE("module_type", true),
  MODULE_ID("module_id", true),
  MODULE_VERSION("module_version", true),


  OAUTH2_PROVIDER_NAME("oauth2_provider_name", true),

  
  PASSWORD("password", false),
  PERSON_ID("person_id", true),
  PIPELINE_ID("pipeline_id", true),
  
  PROMISE_HANDLE("promise_handle", true),
  PROMISE_VALUE("promise_value", true),
  EMAIL("email", false),
  FULLNAME("fullname", false),

  REDIRECT("redirect", true);

  private String key;
  private boolean allowedInProd;

  private RequestParamKeyEnum(String key, boolean allowedInProd) {
    this.key = checkNotBlank(key, "key");
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
