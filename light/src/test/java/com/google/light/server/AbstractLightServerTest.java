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

import static com.google.light.server.constants.OAuth2ProviderService.GOOGLE_LOGIN;
import static com.google.light.testingutils.TestingUtils.getMockSessionForTesting;
import static com.google.light.testingutils.TestingUtils.getRandomEmail;
import static com.google.light.testingutils.TestingUtils.getRandomPersonId;
import static com.google.light.testingutils.TestingUtils.getRandomString;

import com.google.inject.Injector;
import com.google.light.server.constants.LightEnvEnum;
import com.google.light.server.constants.OAuth2ProviderService;
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
public abstract class AbstractLightServerTest {
  protected static LightEnvEnum defaultEnv = LightEnvEnum.UNIT_TEST;
  
  protected static OAuth2ProviderService defaultProviderService = GOOGLE_LOGIN;
  protected long testPersonId;
  protected String testEmail;
  protected String testFirstName;
  protected String testLastName;

  protected Injector injector;
  protected static GaeTestingUtils gaeTestingUtils = null;

  // TODO(arjuns): Fix this setup part so that we dont see exceptions in console.
  @BeforeClass
  public static void gaeSetup() {
    // This causes GAE Test Setup once.
    gaeTestingUtils = TestingUtils.gaeSetup(defaultEnv);
  }

  @AfterClass
  public static void gaeTearDown() {
    gaeTestingUtils.tearDown();
//    gaeSetupDone = false;
  }

  /**
   * This will simulate the user state for different calls.
   * 
   * @param gaeUserEmail
   * @param isLoggedIn
   * @param isAdmin
   */
  @Deprecated
  protected void gaeEnvReset(String email, boolean isLoggedIn, boolean isAdmin) {
    // TODO(arjuns): Fix this.
//    gaeTestingUtils.setAuthDomain(authDomain);
    gaeTestingUtils.setEmail(email);
    gaeTestingUtils.setAdmin(isAdmin);
  }

  @Before
  public void setUp() {
    testPersonId = getRandomPersonId();
    testEmail = getRandomEmail();
    testFirstName = getRandomString();
    testLastName = getRandomString();

    HttpSession mockSession = getMockSessionForTesting(defaultEnv, defaultProviderService, 
        testPersonId, testEmail);

    injector = TestingUtils.getInjectorByEnv(LightEnvEnum.UNIT_TEST, mockSession);
  }

  @After
  public void tearDown() {
  }
}
