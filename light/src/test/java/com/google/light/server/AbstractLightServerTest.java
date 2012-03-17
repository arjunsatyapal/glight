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

import static com.google.light.server.constants.RequestParmKeyEnum.AUTH_DOMAIN;
import static com.google.light.server.constants.RequestParmKeyEnum.GAE_USER_EMAIL;
import static com.google.light.server.constants.RequestParmKeyEnum.GAE_USER_ID;
import static com.google.light.testingutils.TestingUtils.getRandomEmail;
import static com.google.light.testingutils.TestingUtils.getRandomFederatedId;
import static com.google.light.testingutils.TestingUtils.getRandomString;
import static com.google.light.testingutils.TestingUtils.getRandomUserId;
import static org.mockito.Mockito.when;

import com.google.inject.Injector;
import com.google.light.server.constants.OpenIdAuthDomain;
import com.google.light.server.guice.TestInstanceProvider;
import com.google.light.server.guice.TestLightModule;
import com.google.light.server.guice.providers.InstanceProvider;
import com.google.light.testingutils.GaeTestingUtils;
import javax.servlet.http.HttpSession;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.mockito.Mockito;

/**
 * Base class for all the Light Server Tests.
 * 
 * @author Arjun Satyapal
 */
public abstract class AbstractLightServerTest {
  private static boolean gaeSetupDone = false;

  protected static OpenIdAuthDomain defaultAuthDomain = OpenIdAuthDomain.GOOGLE;
  protected String testUserId;
  protected String testEmail;
  protected String testFederatedId;
  protected String testFirstName;
  protected String testLastName;

  protected Injector injector;
  protected InstanceProvider instanceProvider;
  protected TestInstanceProvider testInstanceProvider;
  protected static GaeTestingUtils gaeTestingUtils = null;

  @BeforeClass
  public static void gaeSetup() {
    // This causes GAE Test Setup once.
    gaeTestingUtils =
        new GaeTestingUtils(defaultAuthDomain.get(), getRandomEmail(), getRandomFederatedId(), 
            true /*isFederatedUser*/, getRandomUserId(), true /* isUserLoggedIn */, 
            false /* isGaeAdmin */);
    gaeTestingUtils.setUp();
    
    /*
     * Deliberately keeping this false so that each test is forced to initiate with its own Env
     * with each setup.
     */
    
    gaeSetupDone = false;
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
  protected void gaeEnvReset(String authDomain, String email, String federatedId, String userId,
      boolean isLoggedIn, boolean isAdmin) {
    gaeTestingUtils.setAuthDomain(authDomain);
    gaeTestingUtils.setEmail(email);
    gaeTestingUtils.setFederatedIdentity(federatedId);
    gaeTestingUtils.setUserId(userId);

    gaeTestingUtils.setLoggedIn(isLoggedIn);
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
      gaeEnvReset(defaultAuthDomain.get(), testEmail, testFederatedId, testUserId,
          true /* loggedIn */, false /* isAdmin */);
    }
    HttpSession mockSession = Mockito.mock(HttpSession.class);
    when(mockSession.getAttribute(AUTH_DOMAIN.get())).thenReturn(defaultAuthDomain);
    when(mockSession.getAttribute(GAE_USER_EMAIL.get())).thenReturn(testEmail);
    when(mockSession.getAttribute(GAE_USER_ID.get())).thenReturn(testUserId);
    
    injector = TestLightModule.getTestInjector(mockSession);
    instanceProvider = injector.getInstance(InstanceProvider.class);
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
