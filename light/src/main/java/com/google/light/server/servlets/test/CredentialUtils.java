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
import com.google.light.server.constants.OAuth2Provider;

/**
 * A utility class that will be used for testing purpose.
 * Unfortunately in order to work with both Dev Server and TestEnv it has to be placed
 * inside Test directory.
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
   * Returns Credentials directory. e.g. /home/foo/credentials
   * 
   * @return
   */
  public static String getCredentialDir() {
    return getHomeDir() + "/light-credentials";
  }

  /**
   * Returns path for directory containing Credentials for specific Env. e.g.
   * /home/foo/credentials/DEV_SERVER
   * 
   * @param env
   * @return
   */
  public static String getEnvCredneitalDir(LightEnvEnum env) {
    return getCredentialDir() + "/" + env;
  }

  /**
   * Returns path for a Standard for a given env. /home/foo/credentials/DEV_SERVER/OAUTH2
   * 
   * @param env
   * @param standard
   * @return
   */
  public static String getStdCredentialDir(LightEnvEnum env, CredentialStandardEnum standard) {
    return getEnvCredneitalDir(env) + "/" + standard;
  }

  /**
   * Returns path to the directory for Resource Owner for a Specific env and Standard. e.g
   * /home/foo/credentials/DEV_SERVER/OAUTH2/owner/unit-test1@gmail.com
   * 
   * @return
   */
  public static String getOwnerCredentialDir(LightEnvEnum env, CredentialStandardEnum standard,
      String ownerEmail) {
    return getStdCredentialDir(env, standard) + "/" + "owner/" + ownerEmail;
  }
  
  /**
   * Returns path to the password file for Resource Owner.
   * 
   * @return
   */
  public static String getOwnerCredentialPasswdFilePath(LightEnvEnum env, CredentialStandardEnum standard,
      String ownerEmail) {
    return getOwnerCredentialDir(env, standard, ownerEmail) + "/passwd"; 
  }
  
  /**
   * Returns path to TokenInfo file for Resource Owner.
   * 
   * @return
   */
  public static String getOwnerTokenInfoFilePath(LightEnvEnum env, CredentialStandardEnum standard,
      String ownerEmail) {
    return getOwnerCredentialDir(env, standard, ownerEmail) + "/tokeninfo"; 
  }

  /**
   * Returns path to the directory for Resource Consumer for a Specific env. e.g
   * /home/foo/credentials/DEV_SERVER/OAUTH2/consumer/google
   * 
   * @return
   */
  public static String getConsumerCredentialDir(LightEnvEnum env, CredentialStandardEnum standard, 
      OAuth2Provider provider) {
    return getStdCredentialDir(env, standard) + "/" + "consumer/" + provider.getProviderName();
  }

  // Utility class.
  private CredentialUtils() {
  }
}
