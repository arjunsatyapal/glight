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
package com.google.light.testingutils.scripts;

import static com.google.light.testingutils.TestResourcePaths.DEV_SERVER_OAUTH_2_CONSUMER_GOOGLE_LOGIN;
import static com.google.light.testingutils.TestResourcePaths.DEV_SERVER_OAUTH_2_OWNER_TOKEN_INFO;
import static com.google.light.testingutils.TestResourcePaths.UNIT_TEST_OAUTH_2_CONSUMER_GOOGLE_LOGIN;
import static com.google.light.testingutils.TestingUtils.createFile;
import static com.google.light.testingutils.TestingUtils.getInjectorByEnv;
import static com.google.light.testingutils.TestingUtils.getMockSessionForTesting;
import static com.google.light.testingutils.TestingUtils.readLineFromConsole;

import com.google.appengine.api.utils.SystemProperty;
import com.google.common.base.Preconditions;
import com.google.inject.Injector;
import com.google.light.server.constants.LightEnvEnum;
import com.google.light.server.constants.OAuth2Provider;
import com.google.light.server.constants.RequestParmKeyEnum;
import com.google.light.server.servlets.oauth2.google.GoogleOAuth2Helper;
import com.google.light.testingutils.TestResourcePaths;
import java.io.IOException;

/**
 * Utility class to create all the credentials required for Light Testing.
 * 
 * Arjun Satyapal
 */
public class CreateCredentials {
  private static Injector injector;
  
  private static final String GOOGLE_CLIENT_ID = "160638920188.apps.googleusercontent.com";
  

  public static void main(String args[]) throws IOException {
    // Initializing Dummy GAE.
    SystemProperty.applicationId.set("test");
    SystemProperty.environment.set("");
    injector = getInjectorByEnv(LightEnvEnum.UNIT_TEST, getMockSessionForTesting(
        OAuth2Provider.GOOGLE_LOGIN, "115639870677665060321", "unit-test1@myopenedu.com"));

    CreateCredentials instance = new CreateCredentials();
    instance.doCreateCredentials();

    System.out.println("Success.");
  }

  private void doCreateCredentials() throws IOException {
    createGoogleLoginConsumerCredneitals();
    createTokenInfoForOwner(DEV_SERVER_OAUTH_2_OWNER_TOKEN_INFO);
  }

  /**
   * Create
   * 
   * @param testOauth2OwnerTokenInfo
   * @throws IOException
   */
  private void createTokenInfoForOwner(TestResourcePaths testOauth2OwnerTokenInfo)
      throws IOException {
    String serverUrl = "http://localhost:8080";
    GoogleOAuth2Helper helperInstance = injector.getInstance(GoogleOAuth2Helper.class);
    Preconditions.checkNotNull(helperInstance);
    String redirectUri = helperInstance.getGoogleLoginRedirectUri(serverUrl);
    
    // TODO(arjuns): Fix this once we start storing the token info.
    String content = readLineFromConsole("Ensure GAE DevServer is running at " + serverUrl
            + ". Then open a browser, and redirect to url mentioned below. After finishing "
            + "the process, put the single line json returned in the response.\n"
            + redirectUri + "\n\n Enter json format for tokenInfo : \n");
    
    createFile(content, TestResourcePaths.DEV_SERVER_OAUTH_2_OWNER_TOKEN_INFO);
    createFile(content, TestResourcePaths.UNIT_TEST_OAUTH_2_OWNER_TOKEN_INFO);
  }

  /**
   * Create Google Login Consumer Credentials for given Env.
   * 
   * @param env
   * @throws IOException
   */
  private void
      createGoogleLoginConsumerCredneitals()
          throws IOException {
    System.out.println("Creating Google Login Consumer Credential : ");
    String clientSecret = readLineFromConsole("Enter clientSecret for " + GOOGLE_CLIENT_ID + " : ");

    String content =
        new StringBuilder()
            .append("####")
            .append("\n")
            .append("#  Env : TEST")
            .append("\n")
            .append("####")
            .append("\n")
            .append(RequestParmKeyEnum.CLIENT_ID.get()).append("=").append(GOOGLE_CLIENT_ID)
            .append("\n")
            .append(RequestParmKeyEnum.CLIENT_SECRET.get()).append("=").append(clientSecret)
            .append("\n")
            .toString();
    System.out.println(content);
    createFile(content, DEV_SERVER_OAUTH_2_CONSUMER_GOOGLE_LOGIN);
    createFile(content, UNIT_TEST_OAUTH_2_CONSUMER_GOOGLE_LOGIN);
  }
}
