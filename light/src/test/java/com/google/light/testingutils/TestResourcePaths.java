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
package com.google.light.testingutils;

import static com.google.light.server.constants.LightEnvEnum.DEV_SERVER;
import static com.google.light.server.constants.LightEnvEnum.UNIT_TEST;
import static com.google.light.server.constants.OAuth2Provider.GOOGLE_LOGIN;
import static com.google.light.server.servlets.test.CredentialStandardEnum.OAUTH2;
import static com.google.light.server.servlets.test.CredentialUtils.getConsumerCredentialDir;
import static com.google.light.server.servlets.test.CredentialUtils.getOwnerCredentialPasswdFilePath;
import static com.google.light.server.servlets.test.CredentialUtils.getOwnerTokenInfoFilePath;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;

/**
 * Enum to encapsulate paths for Test Resources. Assumption is all the path is relative to
 * classPath.
 * 
 * TODO(arjuns): Add a validation function, which ensures all the resources exist.
 * 
 * @author Arjun Satyapal
 */
public enum TestResourcePaths {
  // Values inside this file are randomly created.
  GOOGLE_TOKEN_INFO_JSON("/login/oauth2/google/google_token_info.json"),
  /*
   * Values inside this file are one's that are pulled from Google. So if some one changes values,
   * then those things will have to be fixed.
   */
  GOOGLE_USER_INFO_JSON("/login/oauth2/google/google_user_info.json"),

  // Resources specific to DEV_SERVER/UNIT_TEST
  DEV_SERVER_OAUTH_2_CONSUMER_GOOGLE_LOGIN(getConsumerCredentialDir(DEV_SERVER, OAUTH2,
      GOOGLE_LOGIN)),
  UNIT_TEST_OAUTH_2_CONSUMER_GOOGLE_LOGIN(getConsumerCredentialDir(UNIT_TEST, OAUTH2,
      GOOGLE_LOGIN)),

  DEV_SERVER_OAUTH_2_OWNER_PASSWORD(getOwnerCredentialPasswdFilePath(DEV_SERVER, OAUTH2,
      "unit-test1@myopenedu.com")),
  UNIT_TEST_OAUTH_2_OWNER_PASSWORD(getOwnerCredentialPasswdFilePath(UNIT_TEST, OAUTH2,
      "unit-test1@myopenedu.com")),

  DEV_SERVER_OAUTH_2_OWNER_TOKEN_INFO(getOwnerTokenInfoFilePath(DEV_SERVER, OAUTH2,
      "unit-test1@myopenedu.com")),
  UNIT_TEST_OAUTH_2_OWNER_TOKEN_INFO(getOwnerTokenInfoFilePath(UNIT_TEST, OAUTH2,
      "unit-test1@myopenedu.com"));

  private String path;

  private TestResourcePaths(String path) {
    this.path = checkNotBlank(path);
  }

  public String get() {
    return path;
  }
}
