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
package com.google.light.server.servlets.oauth2.google;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.constants.LightEnvEnum.DEV_SERVER;
import static com.google.light.server.servlets.path.ServletPathEnum.LOGIN_GOOGLE_CB;
import static com.google.light.server.servlets.test.CredentialStandardEnum.OAUTH2;
import static com.google.light.server.servlets.test.CredentialUtils.getOwnerCredentialDir;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.testingutils.TestResourcePaths.GOOGLE_USER_INFO_JSON;
import static com.google.light.testingutils.TestingConstants.GOOGLE_OAUTH_CB_CMD_LINE_URI;
import static com.google.light.testingutils.TestingConstants.RESOURCE_OWNER_EMAIL;
import static com.google.light.testingutils.TestingUtils.getMockSessionForTesting;
import static com.google.light.testingutils.TestingUtils.getResourceAsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.inject.Injector;
import com.google.light.server.constants.LightEnvEnum;
import com.google.light.server.constants.OAuth2Provider;
import com.google.light.server.manager.interfaces.OAuth2ConsumerCredentialManager;
import com.google.light.server.servlets.oauth2.google.pojo.GoogleTokenInfo;
import com.google.light.server.servlets.oauth2.google.pojo.GoogleUserInfo;
import com.google.light.server.utils.JsonUtils;
import com.google.light.testingutils.GaeTestingUtils;
import com.google.light.testingutils.TestingConstants;
import com.google.light.testingutils.TestingUtils;
import java.io.File;
import java.io.IOException;
import javax.servlet.http.HttpSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration Test for {@link GoogleOAuth2Helper}.
 * 
 * @author Arjun Satyapal
 */
public class GoogleOAuth2HelperITCase {
  private static final String SERVER_URL = "http://localhost:8080";

  private GaeTestingUtils gaeTestingUtils = null;
  private Injector injector;
  private GoogleOAuth2Helper helperInstance;
  private HttpTransport httpTransport;
  private OAuth2ConsumerCredentialManager consumerCredentialManager;
  private GoogleTokenInfo defaultGoogleTokenInfo;

  @Before
  public void setUp() throws IOException {
    envSetup(LightEnvEnum.UNIT_TEST);
  }

  public void envSetup(LightEnvEnum env) throws IOException {
    gaeTestingUtils = TestingUtils.gaeSetup(env);
    defaultGoogleTokenInfo = getTokenInfoFromFileSystem();
    HttpSession mockSession =
        getMockSessionForTesting(OAuth2Provider.GOOGLE_LOGIN, defaultGoogleTokenInfo.getUserId(),
            defaultGoogleTokenInfo.getEmail());

    injector = TestingUtils.getInjectorByEnv(env, mockSession);
    helperInstance = checkNotNull(injector.getInstance(GoogleOAuth2Helper.class));
    httpTransport = checkNotNull(injector.getInstance(HttpTransport.class));
    httpTransport.createRequestFactory();

    consumerCredentialManager =
        checkNotNull(injector.getInstance(OAuth2ConsumerCredentialManager.class));
  }

  @After
  public void envTearDown() {
    gaeTearDown();
  }

  private void gaeTearDown() {
    gaeTestingUtils.tearDown();
  }

  /**
   * Test for {@link GoogleOAuth2Helper#getLightUriForLoginCompletion(String)}.
   */
  @Test
  public void test_getLightUriForLoginCompletion() throws Exception {
    // First testing for Unit Test Env.
    assertEquals(GOOGLE_OAUTH_CB_CMD_LINE_URI,
        helperInstance.getLightUriForLoginCompletion(SERVER_URL));

    // Now testing for other Envs.
    String expectedUrl = SERVER_URL + LOGIN_GOOGLE_CB.get();

    for (LightEnvEnum currEnv : LightEnvEnum.values()) {

      if (currEnv == LightEnvEnum.UNIT_TEST) {
        // already tested.
        continue;
      }
      envTearDown(); // Each env will be setup before it is used.
      envSetup(currEnv);
      injector = TestingUtils.getInjectorByEnv(currEnv, null);
      GoogleOAuth2Helper helperInstance =
          checkNotNull(injector.getInstance(GoogleOAuth2Helper.class));
      assertEquals(expectedUrl, helperInstance.getLightUriForLoginCompletion(SERVER_URL));
    }
  }

  /**
   * Test for {@link GoogleOAuth2Helper#getLightUriForLoginCompletion(String)}.
   */
  @Test
  public void test_getGoogleLoginRedirectUri() throws Exception {
    // First testing for Unit Test Env.
    String expectedUrl = getExpectedUrl(helperInstance.getLightUriForLoginCompletion(SERVER_URL));
    assertEquals(expectedUrl, helperInstance.getGoogleLoginRedirectUri(SERVER_URL));

    // Now testing for DEV SERVER env.
    envTearDown();
    envSetup(DEV_SERVER);
    helperInstance =
        checkNotNull(injector.getInstance(GoogleOAuth2Helper.class));
    expectedUrl = getExpectedUrl(helperInstance.getLightUriForLoginCompletion(SERVER_URL));
    assertEquals(expectedUrl, helperInstance.getGoogleLoginRedirectUri(SERVER_URL));

    // For QA and PROD env, we dont have enough parameters to generatete the URL. So we
    // are assuming that those values are setup in PROD.
  }

  private String getExpectedUrl(String redirectUrl) {
    String expectedUrl =
        "https://accounts.google.com/o/oauth2/auth?"
            + "client_id="
            + consumerCredentialManager.getClientId()
            + "&"
            + "redirect_uri="
            + redirectUrl
            + "&"
            + "response_type=code&"
            + "scope=https://www.googleapis.com/auth/userinfo.email%20https://www.googleapis.com/auth/userinfo.profile";

    return expectedUrl;
  }

  /**
   * Test for {@link GoogleOAuth2Helper#getAccessToken(String, String)}.
   */
  @Test
  public void test_getAccessToken() throws Exception {
    /*
     * Unfortunately for AccessToken, user intervention is required, so cannot be tested during
     * tests. So set boolean to ture if you want to run it.
     */

    boolean runTest = false;
    if (!runTest) {
      return;
    }

    /*
     * Unfortunately OAuth2 client messes up when TestingConstants.GOOGLE_OAUTH_CB_CMD_LINE_URI is
     * passed as redirectUri to Authorization Flow. So for our local testing we are relying that it
     * will work in production. TODO(waltercacau) : This may be eventually covered using a Selenium
     * Test. TODO(arjuns) : Create a Utility to initialize all the credentials.
     */
    String message = "Open a browser, redirect to following URL, and then enter the "
        + "code returned by Google : \n"
        + getExpectedUrl(TestingConstants.GOOGLE_OAUTH_CB_CMD_LINE_URI)
        + "\n\nEnter the code returned by Google : ";
    String code = TestingUtils.readLineFromConsole(message);

    TokenResponse tokenResponse = helperInstance.getAccessToken(SERVER_URL, code);

    checkNotBlank(tokenResponse.getAccessToken());
    checkNotBlank(tokenResponse.getRefreshToken());
    assertEquals(new Long(3600), tokenResponse.getExpiresInSeconds());
    assertEquals("Bearer", tokenResponse.getTokenType());

    GoogleTokenInfo tokenInfo = helperInstance.getTokenInfo(tokenResponse.getAccessToken());
    tokenInfo.setTokenType(tokenResponse.getTokenType());

    // All the values are validated above, so saving them directly.
    tokenInfo.setTokenType(tokenResponse.getTokenType());
    tokenInfo.setAccessToken(tokenResponse.getAccessToken());
    tokenInfo.setRefreshToken(tokenResponse.getRefreshToken());
    // Before persisting, ensure we are persiting write values.
    tokenInfo.validate();

    String ownerCredFilePath = getOwnerCredentialDir(DEV_SERVER, OAUTH2, RESOURCE_OWNER_EMAIL);
    File ownerCredFile = new File(ownerCredFilePath);
    Files.createParentDirs(ownerCredFile);
    Files.write(JsonUtils.toJson(tokenInfo), ownerCredFile, Charsets.UTF_8);
    System.out.println("For [" + RESOURCE_OWNER_EMAIL + "], file created at : \n"
        + ownerCredFilePath);
    test_getTokenInfo();
    test_getUserInfo();
  }

  /**
   * Test for {@link GoogleOAuth2Helper#getTokenInfo(String)}.
   * 
   * For this test to work, it is assumed that {@link #test_getAccessToken()} was ran once, and the
   * generated {@link GoogleTokenInfo} was persisted at ~/credentials/<env>/owner/<owner-email> In
   * this method, we will not be validating accessToken and refreshToken as they are returned as
   * part of the TokenResponse which is obtained when User Authorizes consumer for access. That
   * cannot be tested here.
   */
  @Test
  public void test_getTokenInfo() throws Exception {
    String ownerCredFilePath = getOwnerCredentialDir(DEV_SERVER, OAUTH2, RESOURCE_OWNER_EMAIL);
    String jsonString = Files.toString(new File(ownerCredFilePath), Charsets.UTF_8);
    GoogleTokenInfo existingTokenInfo = JsonUtils.getDto(jsonString, GoogleTokenInfo.class);
    existingTokenInfo.validate();
    System.out.println(existingTokenInfo.isAccessTokenExpired());

    // Now we have a valid tokenInfo file read from Command Line. Lets verify values by fetching
    // from Gooogle.
    String accessToken = existingTokenInfo.getAccessToken();
    GoogleTokenInfo freshTokenInfo = helperInstance.getTokenInfo(accessToken);

    // TokenInfo does not return AccessToken.
    assertNull(freshTokenInfo.getAccessToken());
    assertEquals(existingTokenInfo.getAccessType(), freshTokenInfo.getAccessType());
    assertEquals(existingTokenInfo.getAudience(), freshTokenInfo.getAudience());
    assertEquals(existingTokenInfo.getEmail(), freshTokenInfo.getEmail());
    assertEquals(existingTokenInfo.getIssuedTo(), freshTokenInfo.getIssuedTo());
    // TokenInfo does not return RefreshToken.
    assertNull(freshTokenInfo.getRefreshToken());
    assertEquals(existingTokenInfo.getScope(), freshTokenInfo.getScope());
    assertEquals(existingTokenInfo.getUserId(), freshTokenInfo.getUserId());
    assertEquals(existingTokenInfo.getVerifiedEmail(), freshTokenInfo.getVerifiedEmail());
  }

  /**
   * Test for {@link GoogleOAuth2Helper#getUserInfo(String)}.
   */
  @Test
  public void test_getUserInfo() throws Exception {
    String jsonString = getResourceAsString(GOOGLE_USER_INFO_JSON.get());
    GoogleUserInfo existingUserInfo = JsonUtils.getDto(jsonString, GoogleUserInfo.class);
    GoogleUserInfo recentUserInfo = helperInstance.getUserInfo(
        getTokenInfoFromFileSystem().getAccessToken());

    assertEquals(existingUserInfo, recentUserInfo);
  }

  private GoogleTokenInfo getTokenInfoFromFileSystem() throws IOException {
    String ownerCredFilePath = getOwnerCredentialDir(DEV_SERVER, OAUTH2, RESOURCE_OWNER_EMAIL);
    String jsonString = Files.toString(new File(ownerCredFilePath), Charsets.UTF_8);
    GoogleTokenInfo tokenInfoFromFile = JsonUtils.getDto(jsonString, GoogleTokenInfo.class);
    tokenInfoFromFile.validate();
    return tokenInfoFromFile;
  }
}
