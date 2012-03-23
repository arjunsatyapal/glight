/*
 * Copyright (C) Google Inc.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.light.server;

import static com.google.light.testingutils.TestingUtils.getMockSessionForTesting;
import static com.google.light.testingutils.TestingUtils.getRandomEmail;
import static com.google.light.testingutils.TestingUtils.getRandomFederatedId;
import static com.google.light.testingutils.TestingUtils.getRandomString;
import static com.google.light.testingutils.TestingUtils.getRandomUserId;

import com.google.inject.Injector;
import com.google.light.server.constants.LightEnvEnum;
import com.google.light.server.constants.OAuth2Provider;
import com.google.light.server.guice.TestInstanceProvider;
import com.google.light.testingutils.GaeTestingUtils;
import com.google.light.testingutils.TestingUtils;
import javax.servlet.http.HttpSession;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * Base class for all the Light Server Tests.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("deprecation")
public abstract class AbstractLightServerTest {
  private static boolean gaeSetupDone = false;

  protected static LightEnvEnum defaultEnv = LightEnvEnum.UNIT_TEST;
  
  protected static OAuth2Provider defaultLoginProvider = OAuth2Provider.GOOGLE_LOGIN;
  protected String testUserId;
  protected String testEmail;
  protected String testFederatedId;
  protected String testFirstName;
  protected String testLastName;

  protected Injector injector;
  protected TestInstanceProvider testInstanceProvider;
  protected static GaeTestingUtils gaeTestingUtils = null;

  // TODO(arjuns): Fix this setup part so that we dont see exceptions in console.
  @BeforeClass
  public static void gaeSetup() {
    // This causes GAE Test Setup once.
    gaeTestingUtils = TestingUtils.gaeSetup(defaultEnv);
    /*
     * Deliberately keeping this false so that each test is forced to initiate with its own Env with
     * each setup.
     */

    gaeSetupDone = true;
  }

  @AfterClass
  public static void gaeTearDown() {
    gaeTestingUtils.tearDown();
    gaeSetupDone = false;
  }

  /**
   * This will simulate the user state for different calls.
   * 
   * @param gaeUserEmail
   * @param isLoggedIn
   * @param isAdmin
   */
  protected void gaeEnvReset(OAuth2Provider provider, String email, String federatedId, String userId,
      boolean isLoggedIn, boolean isAdmin) {
    // TODO(arjuns): Fix this.
//    gaeTestingUtils.setAuthDomain(authDomain);
    gaeTestingUtils.setEmail(email);
    gaeTestingUtils.setAdmin(isAdmin);

    gaeSetupDone = true;
  }

  @Before
  public void setUp() {
    testUserId = getRandomUserId();
    testEmail = getRandomEmail();
    testFederatedId = getRandomFederatedId();
    testFirstName = getRandomString();
    testLastName = getRandomString();

    if (!gaeSetupDone) {
      gaeEnvReset(defaultLoginProvider, testEmail, testFederatedId, testUserId,
          true /* loggedIn */, false /* isAdmin */);
    }
    HttpSession mockSession = getMockSessionForTesting(defaultLoginProvider, testUserId, testEmail);

    injector = TestingUtils.getInjectorByEnv(LightEnvEnum.UNIT_TEST, mockSession);
    testInstanceProvider = injector.getInstance(TestInstanceProvider.class);
  }

  @After
  public void tearDown() {
    testFirstName = null;
    testLastName = null;
    testUserId = null;
    testEmail = null;
    gaeSetupDone = false;
  }
}
