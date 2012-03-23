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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkEmail;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;

import com.google.appengine.api.utils.SystemProperty;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import com.google.light.server.constants.LightEnvEnum;
import com.google.light.server.constants.OAuth2Provider;
import com.google.light.server.utils.GaeUtils;
import javax.servlet.http.HttpSession;

/**
 * Helper class for tweaking GAE Environment Parameters. Some getters are similar to
 * {@link GaeUtils} but in testing env, some parameters are relaxed so that we can do testing
 * easily. In production there is stricter checking.
 * 
 * @author Arjun Satyapal
 */
public class GaeTestingUtils {
  private LightEnvEnum env;
  private OAuth2Provider provider;
  private String email;
  private String userId;
  
  /** See similar variables in {@link com.google.appengine.api.users.UserServiceImpl} */
  static final String USER_ID_KEY =
      "com.google.appengine.api.users.UserService.user_id_key";

  static final String FEDERATED_IDENTITY_KEY =
      "com.google.appengine.api.users.UserService.federated_identity";

  static final String FEDERATED_AUTHORITY_KEY =
      "com.google.appengine.api.users.UserService.federated_authority";

  static final String IS_FEDERATED_USER_KEY =
      "com.google.appengine.api.users.UserService.is_federated_user";

  private LocalServiceTestHelper gaeTestHelper;

  public GaeTestingUtils(LightEnvEnum env, OAuth2Provider provider, String email,
      String userId, boolean isAdmin) {
    this.env = checkNotNull(env);
    this.provider = checkNotNull(provider);
    this.email = checkEmail(email);
    this.userId = checkNotBlank(userId);
    
    this.env = checkNotNull(env);
    basicSystemPropertySetup(env);
    
    gaeTestHelper = new LocalServiceTestHelper(
            new LocalDatastoreServiceTestConfig(),
            new LocalTaskQueueTestConfig(),
            new LocalUserServiceTestConfig())
        
    // TODO(arjuns): Fix this.
        .setEnvAuthDomain("google")
        .setEnvEmail(email)
        .setEnvIsAdmin(isAdmin)
        .setEnvAppId(env.getAppIds().get(0));
    
  }

  /**
   * @param env
   */
  private static void basicSystemPropertySetup(LightEnvEnum env) {
    switch (env) {
      case DEV_SERVER:
        SystemProperty.environment.set(SystemProperty.Environment.Value.Development);
        break;
        
      case PROD:
        //$FALL-THROUGH$
      case QA:
        SystemProperty.environment.set(SystemProperty.Environment.Value.Production);
        break;

      case UNIT_TEST:
      default:
        SystemProperty.environment.set("");
    }
    SystemProperty.applicationId.set(env.getAppIds().get(0));
  }

  /**
   * Setup the Testing Environment for GAE.
   */
  public void setUp() {
    gaeTestHelper.setUp();
  }

  /**
   * Teardown GAE testing environment.
   */
  public void tearDown() {
    HttpSession mockSession = TestingUtils.getMockSessionForTesting(provider, userId, email);
    Injector injector = TestingUtils.getInjectorByEnv(env, mockSession);
    injector.getInstance(GuiceFilter.class).destroy();
    gaeTestHelper.tearDown();
  }

  /**
   * Set GAE UserEmail.
   * 
   * @param envEmail
   */
  public void setEmail(String envEmail) {
    gaeTestHelper.setEnvEmail(envEmail);
  }

  public void setAppId(String appId) {
    gaeTestHelper.setEnvAppId(appId);
  }

  /**
   * Set current GAE user as GAE Admin.
   * 
   * @param isAdmin
   */
  public void setAdmin(boolean isAdmin) {
    gaeTestHelper.setEnvIsAdmin(isAdmin);
  }

  /**
   * Return Env with which setup was done.
   * 
   * @return
   */
  public LightEnvEnum getEnv() {
    return env;
  }
  
  /**
   * Use at your own risk. If not cleaned up after this, then env can be in inconsistent state. 
   */
  public static void cheapEnvSwitch(LightEnvEnum env) {
    basicSystemPropertySetup(env);
  }
}
