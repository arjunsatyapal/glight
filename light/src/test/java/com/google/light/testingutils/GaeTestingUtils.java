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

import com.google.appengine.api.utils.SystemProperty;

import com.google.light.server.constants.LightAppIdEnum;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalTaskQueueTestConfig;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.apphosting.api.ApiProxy;
import com.google.apphosting.api.ApiProxy.Environment;
import com.google.light.server.utils.GaeUtils;
import java.util.HashMap;

/**
 * Helper class for tweaking GAE Environment Parameters. Some getters are similar to
 * {@link GaeUtils} but in testing env, some parameters are relaxed so that we can do testing
 * easily. In production there is stricter checking.
 * 
 * @author Arjun Satyapal
 */
public class GaeTestingUtils {
  /** See similar variables in {@link com.google.appengine.api.users.UserServiceImpl} */
  static final String USER_ID_KEY =
      "com.google.appengine.api.users.UserService.user_id_key";

  static final String FEDERATED_IDENTITY_KEY =
      "com.google.appengine.api.users.UserService.federated_identity";

  static final String FEDERATED_AUTHORITY_KEY =
      "com.google.appengine.api.users.UserService.federated_authority";

  static final String IS_FEDERATED_USER_KEY =
      "com.google.appengine.api.users.UserService.is_federated_user";

  private HashMap<String, Object> gaeEnvAttr;

  private LocalServiceTestHelper gaeTestHelper;

  public GaeTestingUtils(LightAppIdEnum env, String federatedAuthority, String email,
      String federatedId, Boolean isFederated, String userId, boolean loggedIn,
      boolean isAdmin) {
    // TODO(arjuns) : Figure out what is the difference between federatedAuthority and authDomain.
    // For the time being treating as same.
    initFederatedUser(userId, federatedId, federatedAuthority, isFederated);

    switch (env) {
      case DEV_SERVER:
        SystemProperty.environment.set(SystemProperty.Environment.Value.Development);
        break;
      case PROD:
        
      case QA:
        SystemProperty.environment.set(SystemProperty.Environment.Value.Production);
        break;

      case TEST:
      default:
        SystemProperty.environment.set("");
    }

    gaeTestHelper = new LocalServiceTestHelper(
        new LocalDatastoreServiceTestConfig(),
        new LocalTaskQueueTestConfig(),
        new LocalUserServiceTestConfig())
        .setEnvAuthDomain(federatedAuthority)
        .setEnvIsLoggedIn(loggedIn)
        .setEnvAttributes(gaeEnvAttr)
        .setEnvEmail(email)
        .setEnvIsAdmin(isAdmin)
        .setEnvAppId(env.getAppIds().get(0));

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
    gaeTestHelper.tearDown();
  }

  /**
   * Set GAE UserId.
   * 
   * @param userId
   */
  public void setUserId(String userId) {
    Environment env = ApiProxy.getCurrentEnvironment();
    env.getAttributes().put(USER_ID_KEY, userId);
  }

  /**
   * Set GAE UserId.
   * 
   * @param userId
   */
  public void setFederatedIdentity(String federatedId) {
    Environment env = ApiProxy.getCurrentEnvironment();
    env.getAttributes().put(FEDERATED_IDENTITY_KEY, federatedId);
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
   * Set GAE AuthDomain.
   * 
   * @param authDomain
   */
  public void setAuthDomain(String authDomain) {
    Environment env = ApiProxy.getCurrentEnvironment();
    env.getAttributes().put(FEDERATED_AUTHORITY_KEY, authDomain);
    gaeTestHelper.setEnvAuthDomain(authDomain);
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
   * Set LoggedIn mode for GAE User.
   */
  public void setLoggedIn(boolean loggedIn) {
    gaeTestHelper.setEnvIsLoggedIn(loggedIn);
  }

  private void initFederatedUser(String userId, String federatedId, String federatedAuthority,
      Boolean isFederated) {
    gaeEnvAttr = new HashMap<String, Object>();
    gaeEnvAttr.put(USER_ID_KEY, userId);
    gaeEnvAttr.put(FEDERATED_IDENTITY_KEY, federatedId);
    gaeEnvAttr.put(FEDERATED_AUTHORITY_KEY, federatedAuthority);
    gaeEnvAttr.put(IS_FEDERATED_USER_KEY, isFederated);
  }
}
