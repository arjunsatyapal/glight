///*
// * Copyright (C) Google Inc.
// *
// * Licensed under the Apache License, Version 2.0 (the "License"); you may not
// * use this file except in compliance with the License. You may obtain a copy of
// * the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
// * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
// * License for the specific language governing permissions and limitations under
// * the License.
// */
//package com.google.light.server.servlets.oauth2.google;
//
//import static com.google.common.base.Preconditions.checkArgument;
//import static com.google.common.base.Preconditions.checkNotNull;
//import static com.google.light.server.constants.OAuth2Provider.GOOGLE_LOGIN;
//import static com.google.light.testingutils.TestResourcePaths.GOOGLE_USER_INFO_JSON;
//import static com.google.light.testingutils.TestingUtils.getMockSessionForTesting;
//import static com.google.light.testingutils.TestingUtils.getResourceAsString;
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNotNull;
//import static org.junit.Assert.assertNull;
//import static org.junit.Assert.assertTrue;
//
//import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
//import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
//import com.google.api.client.http.HttpTransport;
//import com.google.inject.Injector;
//import com.google.light.server.constants.LightEnvEnum;
//import com.google.light.server.constants.OAuth2Provider;
//import com.google.light.server.manager.interfaces.OAuth2ConsumerCredentialManager;
//import com.google.light.server.servlets.oauth2.google.pojo.GoogleTokenInfo;
//import com.google.light.server.servlets.oauth2.google.pojo.GoogleUserInfo;
//import com.google.light.server.servlets.path.ServletPathEnum;
//import com.google.light.server.utils.JsonUtils;
//import com.google.light.testingutils.GaeTestingUtils;
//import com.google.light.testingutils.TestResourcePaths;
//import com.google.light.testingutils.TestingUtils;
//import com.google.light.testingutils.scripts.CreateCredentials;
//import java.io.IOException;
//import javax.servlet.http.HttpSession;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//
///**
// * Integration Test for {@link GoogleOAuth2Helper}.
// * 
// * @author Arjun Satyapal
// */
//public class GoogleOAuth2HelperITCase {
//  private static final String SERVER_URL = "http://localhost:8080";
//
//  /*
//   * NOTE : If you set this value to any thing other then UNIT_TEST, then ensure that you have Light
//   * running on the address mentioned above.
//   */
//  private LightEnvEnum defaultEnv = LightEnvEnum.UNIT_TEST;
//  private GaeTestingUtils gaeTestingUtils = null;
//  private Injector injector;
//  private GoogleOAuth2Helper helperInstance;
//  private HttpTransport httpTransport;
//  private OAuth2ConsumerCredentialManager consumerCredentialManager;
//  private GoogleTokenInfo defaultGoogleTokenInfo;
//  private HttpSession mockTestSession;
//
//  @Before
//  public void setUp() throws IOException {
//    envSetup(defaultEnv);
//  }
//
//  /**
//   * Assumption is that {@link CreateCredentials} was ran once, and
//   * {@link TestResourcePaths#UNIT_TEST_OAUTH_2_OWNER_TOKEN_INFO} exist.
//   */
//  public void envSetup(LightEnvEnum env) throws IOException {
//    gaeTestingUtils = TestingUtils.gaeSetup(env);
//
//    defaultGoogleTokenInfo = TestingUtils.getGoogleTokenInfo(env);
//
//    mockTestSession = getMockSessionForTesting(OAuth2Provider.GOOGLE_LOGIN,
//        defaultGoogleTokenInfo.getUserId(), defaultGoogleTokenInfo.getEmail());
//
//    injector = TestingUtils.getInjectorByEnv(env, mockTestSession);
//    helperInstance = checkNotNull(injector.getInstance(GoogleOAuth2Helper.class));
//
//    // TODO(arjuns): Check what happens after expiry.
//
//    // We validate only for UNIT_TEST and DEV_SERVER env.
//    checkArgument(helperInstance.isValidAccessToken(defaultGoogleTokenInfo),
//        "invalid tokenInfo. Recreate it by running : " + CreateCredentials.class.getSimpleName()
//            + ".");
//
//    if (defaultGoogleTokenInfo.hasAccessTokenExpired()) {
//      GoogleTokenInfo refreshedTokenInfo =
//          helperInstance.refreshToken(defaultGoogleTokenInfo.getRefreshToken());
//      TestingUtils.updateGoogleTokenInfo(refreshedTokenInfo);
//      defaultGoogleTokenInfo = refreshedTokenInfo;
//    }
//
//    httpTransport = checkNotNull(injector.getInstance(HttpTransport.class));
//    httpTransport.createRequestFactory();
//
//    consumerCredentialManager =
//        checkNotNull(injector.getInstance(OAuth2ConsumerCredentialManager.class));
//  }
//
//  @After
//  public void envTearDown() {
//    gaeTearDown();
//  }
//
//  private void gaeTearDown() {
//    gaeTestingUtils.tearDown();
//  }
//
//  /**
//   * Test for {@link GoogleOAuth2Helper#getLightUriForLoginCompletion(String)}.
//   */
//  @Test
//  public void test_getLightUriForLoginCompletion() throws Exception {
//    for (LightEnvEnum currEnv : LightEnvEnum.values()) {
//      GaeTestingUtils.cheapEnvSwitch(currEnv);
//      String expectedUrl = SERVER_URL + ServletPathEnum.LOGIN_GOOGLE_CB.get();
//      assertEquals(expectedUrl, helperInstance.getLightUriForLoginCompletion(SERVER_URL));
//    }
//  }
//
//  /**
//   * Test for {@link GoogleOAuth2Helper#getLightUriForLoginCompletion(String)}.
//   */
//  @Test
//  public void test_getGoogleLoginRedirectUri() throws Exception {
//    // First testing for Unit Test Env.
//    String expectedUrl = getExpectedUrl(helperInstance.getLightUriForLoginCompletion(SERVER_URL));
//    assertEquals(expectedUrl, helperInstance.getGoogleLoginRedirectUri(SERVER_URL));
//
//    // Now testing for DEV SERVER env.
//    envTearDown();
//    envSetup(LightEnvEnum.DEV_SERVER);
//    helperInstance =
//        checkNotNull(injector.getInstance(GoogleOAuth2Helper.class));
//    expectedUrl = getExpectedUrl(helperInstance.getLightUriForLoginCompletion(SERVER_URL));
//    assertEquals(expectedUrl, helperInstance.getGoogleLoginRedirectUri(SERVER_URL));
//
//    // For QA and PROD env, we dont have enough parameters to generatete the URL. So we
//    // are assuming that those values will be setup properly in PROD.
//  }
//
//  private String getExpectedUrl(String redirectUrl) {
//    String expectedUrl =
//        "https://accounts.google.com/o/oauth2/auth?"
//            + "client_id="
//            + consumerCredentialManager.getClientId()
//            + "&"
//            + "redirect_uri="
//            + redirectUrl
//            + "&"
//            + "response_type=code&"
//            + "scope=https://www.googleapis.com/auth/userinfo.email%20https://www.googleapis.com/auth/userinfo.profile&"
//            + "access_type=offline";
//
//    return expectedUrl;
//  }
//
//  /**
//   * Test for {@link GoogleOAuth2Helper#getAccessToken(String, String)}.
//   */
//  @Test
//  public void test_getAccessToken() throws Exception {
//    // Not much to test here as it needs a user intervention.
//    // TODO(waltercacau) : Add a selenium test for this
//  }
//
//  /**
//   * Test for {@link GoogleOAuth2Helper#getAuthorizationCodeFlow()}.
//   */
//  @Test
//  public void test_getAuthorizationCodeFlow() {
//    AuthorizationCodeFlow flow = helperInstance.getAuthorizationCodeFlow();
//    assertEquals(GOOGLE_LOGIN.getAuthServerUrl(), flow.getAuthorizationServerEncodedUrl());
//    assertEquals(consumerCredentialManager.getClientId(), flow.getClientId());
//    assertTrue(flow.getClientAuthentication().getClass()
//        .isAssignableFrom(ClientParametersAuthentication.class));
//    assertNull(flow.getCredentialStore());
//    assertNotNull(flow.getJsonFactory());
//    assertNull(flow.getRequestInitializer());
//    assertTrue(TestingUtils.compareScopes(GOOGLE_LOGIN.getScopes(), flow.getScopes()));
//    assertEquals(GOOGLE_LOGIN.getTokenServerUrl(), flow.getTokenServerEncodedUrl());
//    assertNotNull(flow.getTransport());
//  }
//  
//  /**
//   * Test for {@link GoogleOAuth2Helper#getClientAuthentication()}.
//   */
//  @Test
//  public void test_getClientAuthentication() {
//    assertNotNull(helperInstance.getClientAuthentication());
//  }
//  
//  /**
//   * Test for {@link GoogleOAuth2Helper#getTokenInfo(String)}.
//   * 
//   * For this test to work, it is assumed that {@link #test_getAccessToken()} was ran once, and the
//   * generated {@link GoogleTokenInfo} was persisted at ~/credentials/<env>/owner/<owner-email> In
//   * this method, we will not be validating accessToken and refreshToken as they are returned as
//   * part of the TokenResponse which is obtained when User Authorizes consumer for access. That
//   * cannot be tested here.
//   */
//  @Test
//  public void test_getTokenInfo() throws Exception {
//    GoogleTokenInfo freshTokenInfo = helperInstance.getTokenInfo(
//        defaultGoogleTokenInfo.getTokenType(),
//        defaultGoogleTokenInfo.getAccessToken(),
//        defaultGoogleTokenInfo.getRefreshToken());
//
//    assertEquals(defaultGoogleTokenInfo.getAccessToken(), freshTokenInfo.getAccessToken());
//    assertEquals(defaultGoogleTokenInfo.getAccessType(), freshTokenInfo.getAccessType());
//    assertEquals(defaultGoogleTokenInfo.getAudience(), freshTokenInfo.getAudience());
//    assertEquals(defaultGoogleTokenInfo.getEmail(), freshTokenInfo.getEmail());
//    assertEquals(defaultGoogleTokenInfo.getIssuedTo(), freshTokenInfo.getIssuedTo());
//    assertEquals(defaultGoogleTokenInfo.getRefreshToken(), freshTokenInfo.getRefreshToken());
//    assertEquals(defaultGoogleTokenInfo.getScope(), freshTokenInfo.getScope());
//    assertEquals(defaultGoogleTokenInfo.getUserId(), freshTokenInfo.getUserId());
//    assertEquals(defaultGoogleTokenInfo.getVerifiedEmail(), freshTokenInfo.getVerifiedEmail());
//  }
//
//  /**
//   * Test for {@link GoogleOAuth2Helper#getUserInfo(String)}.
//   */
//  @Test
//  public void test_getUserInfo() throws Exception {
//    String jsonString = getResourceAsString(GOOGLE_USER_INFO_JSON.get());
//    GoogleUserInfo existingUserInfo = JsonUtils.getDto(jsonString, GoogleUserInfo.class);
//    GoogleUserInfo recentUserInfo =
//        helperInstance.getUserInfo(defaultGoogleTokenInfo.getAccessToken());
//
//    assertEquals(existingUserInfo, recentUserInfo);
//  }
//  
//  /**
//   * Test for {@link GoogleOAuth2Helper#isValidAccessToken(GoogleTokenInfo)}.
//   */
//  public void test_isValidAccessToken() {
//    // This is already tested as part of the setup. So nothing to test here.
//  }
//
//  /**
//   * Test for {@link GoogleOAuth2Helper#refreshToken(String)}.
//   */
//  @Test
//  public void test_refreshToken() throws Exception {
//    GoogleTokenInfo refreshedTokenInfo =
//        helperInstance.refreshToken(defaultGoogleTokenInfo.getRefreshToken());
//    // Re-running getTokenInfo and userInfo.
//    defaultGoogleTokenInfo = refreshedTokenInfo;
//    test_getTokenInfo();
//    test_getUserInfo();
//  }
//}
