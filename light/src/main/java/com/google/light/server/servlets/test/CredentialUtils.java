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
package com.google.light.server.servlets.test;

import static com.google.light.server.utils.LightPreconditions.checkIsNotEnv;

import com.google.light.server.constants.LightEnvEnum;
import com.google.light.server.constants.OAuth2ProviderEnum;
import com.google.light.server.constants.OAuth2ProviderService;

/**
 * A utility class that will be used for testing purpose.
 * Unfortunately in order to work with both Dev Server and TestEnv it has to be placed
 * inside servlets/test directory.
 * <br>
 * This file is used by {@link TestCredentialBackupServlet} which runs on DEV_SERVER and 
 * therefore needs to be present inside src/java branch. 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class CredentialUtils {
  static {
    // A hack to ensure that this is not called in production.
    checkIsNotEnv(new CredentialUtils(), LightEnvEnum.PROD);
  }

  /**
   * Return Home Directory for current user. e.g. /home/foo
   * 
   * @return
   */
  public static String getHomeDir() {
    return System.getProperty("user.home");
  }

  /**
   * Returns Credentials directory. e.g. /home/foo/light-credentials
   * 
   * @return
   */
  public static String getCredentialDir() {
    return "light-credentials";
  }

  /**
   * Returns relaitve path for a CredentialStandard. light-credentials/OAUTH2
   * 
   * @param env
   * @param standard
   * @return
   */
  public static String getStdCredentialDir(CredentialStandardEnum standard) {
    return getCredentialDir() + "/" + standard;
  }

  /**
   * Returns relative path to the directory for Resource Owner for a CredentialStandard. e.g
   * light-credentials/OAUTH2/owner/unit-test1@gmail.com
   * 
   * @return
   */
  public static String getOwnerCredentialDir(CredentialStandardEnum standard) {
    return getStdCredentialDir(standard) + "/" + "owner";
  }

  /**
   * Returns relative path for the password file for Resource Owner.
   * e.g. light-credentials/OAUTH2/owner/unit-test1@gmail.com/passwd 
   * @return
   */
  public static String getOwnerCredentialPasswdFileAbsPath(CredentialStandardEnum standard, String email) {
    return getHomeDir() + "/" + getOwnerCredentialDir(standard) + "/" + email;
  }
  
  /**
   * Returns relative path for TokenInfo file for Resource Owner.
   * e.g. light-credentials/OAUTH2/owner/unit-test1@gmail.com/GOOGLE_LOGIN
   * @return
   */
  public static String getOwnerTokenInfoFilePath(CredentialStandardEnum standard,
      OAuth2ProviderService providerService) {
    return getOwnerCredentialDir(standard) + "/" + providerService.name();
  }
  
  /**
   * Returns abstolute path for the directory for Resource Consumer for a Specific env. e.g
   * light-credentials/OAUTH2/consumer/
   * 
   * @return
   */
  public static String getConsumerCredentialDir(CredentialStandardEnum standard) {
    return getStdCredentialDir(standard) + "/" + "consumer";
  }
  
  /**
   * Returns relative path. e.g. credentials/OAUTH2/consumer/GOOGLE
   * @param standard
   * @param provider
   * @return
   */
  public static String getConsumerCredentialFilePath(
      CredentialStandardEnum standard, OAuth2ProviderEnum provider) {
    return getConsumerCredentialDir(standard) + "/" + provider.name();
  }
  
  /**
   * Returns absolute  path for the file containing consumer credentials.
   * e.g. /home/foo/light-credentials/OAUTH2/consumer/GOOGLE
   * 
   * @param standard
   * @param provider
   * @return
   */
  public static String getConsumerCredentialFileAbsPath(
      CredentialStandardEnum standard, OAuth2ProviderEnum provider) {
    return getHomeDir() + "/" + getConsumerCredentialFilePath(standard, provider);
  }
  
  /**
   * Returns absolute file path for the zip file containing the credentials downloaded from 
   * DEV_SERVER. e.g. /home/foo/light-credentials/credential.zip
   * 
   * @return
   */
  public static String getCredentialZipFilePath(String email) {
    return getHomeDir() + "/" + getCredentialDir() + "/backup/" + email + ".zip";
  }

  // Utility class.
  private CredentialUtils() {
  }
}
