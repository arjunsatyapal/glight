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
package com.google.light.server.utils;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.constants.OpenIdAuthDomain.getAuthDomainByValue;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;

import com.google.light.server.servlets.SessionManager;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.utils.SystemProperty;
import com.google.apphosting.api.ApiProxy;
import com.google.apphosting.api.ApiProxy.Environment;
import com.google.light.server.constants.LightAppIdEnum;
import com.google.light.server.constants.OpenIdAuthDomain;

/**
 * Utility class to wrap different GAE specific functions.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("deprecation")
public class GaeUtils {
  /**
   * Get FederatedAuthDomain for Current Logged in user. This should be called only when user is
   * Logged in.
   * 
   * TODO(arjuns) : Remove this method or add test for it.
   * @deprecated
   * @return
   */
  @Deprecated
  public static OpenIdAuthDomain getAuthDomain() {
    return getAuthDomainByValue(checkNotBlank(getUser().getAuthDomain(), "authDomain is blank."));
  }

  /**
   * Get Email provided by the Federated Identity Provider. This should be called only when user is
   * logged in.
   * TODO(arjuns) : Remove this method or add test for it.
   * @deprecated Going forward, {@link SessionManager#getGaeEmail()} should be used.
   * @return
   */
  @Deprecated
  public static String getGaeUserEmail() {
    return checkNotBlank(getUser().getEmail(), "userEmail is blank.");
  }

  /**
   * Get Federated Identity of the Logged in user. This should be called only when user is logged
   * in.
   * TODO(arjuns) : Remove this method or add test for it.
   * @deprecated
   * @return
   */
  @Deprecated
  public static String getFederatedIdentity() {
    // TODO(arjuns) : Fix this with Federated Login.
    return null;
    // return checkNotBlank(getUser().getFederatedIdentity(), "federatedIdentity is blank.");
  }

  /**
   * Get GAE User Service.
   * TODO(arjuns) : add test for it.
   * @return
   */
  public static UserService getUserService() {
    return checkNotNull(UserServiceFactory.getUserService(), "userService is null.");
  }

  /**
   * Get current logged in GAE User.
   * TODO(arjuns) : add test for this.
   * @return Return current Logged In user. Null if user is not logged in.
   */
  public static User getUser() {
    return checkNotNull(getUserService().getCurrentUser(), "Seems user is not logged in.");
  }

  /**
   * Get current logged in GAE User's id. This should be called when you are sure that user is
   * logged in.
   * TODO(arjuns) : Remove this method or add test for it.
   * @deprecated Going forward {@link SessionManager#getPersonId()} should be used. 
   * @return
   */
  @Deprecated
  public static String getGaeUserId() {
    return checkNotBlank(getUser().getUserId(), "userId is blank.");
  }

  /**
   * Returns true if user is logged in.
   * 
   * @return
   */
  public static boolean isUserLoggedIn() {
    return getUserService().isUserLoggedIn();
  }

  /**
   * Returns true if current user is Admin. Returns false if user is not admin / user is not logged
   * in.
   * 
   * @return
   */
  public static boolean isUserAdmin() {
    return isUserLoggedIn() && getUserService().isUserAdmin();
  }

  // Utility Class.
  private GaeUtils() {
  }

  /**
   * Returns Appengine AppId for the current environment.
   * 
   * @return
   */
  public static String getAppId() {
    Environment env = ApiProxy.getCurrentEnvironment();
    return checkNotBlank(env.getAppId());
  }

  /**
   * Ensures that Application is running on AppEngine.
   * 
   * TODO(arjuns): Add test for this.
   * @return
   */
  public static boolean isRunningOnGAE() {
    return SystemProperty.Environment.Value.Production == SystemProperty.environment.value();
  }
  /**
   * Returns true if Application is running on local DevelopmentServer.
   * This depends on the {@link SystemProperty#environment}.
   */
  public static boolean isDevServer() {
    return SystemProperty.Environment.Value.Development == SystemProperty.environment.value();
  }
  
  /**
   * Returns true if Light Instance is running on AppEngine and is configured as a Production App.
   * This depends on AppId.
   */
  public static boolean isProductionServer() {
    return isRunningOnGAE() && LightAppIdEnum.PROD == LightAppIdEnum.getLightAppIdEnum();
  }

  /**
   * Returns true if Light Instance is running on AppEngine and is configured as a QA App.
   * This depends on AppId.
   */
  public static boolean isQaServer() {
    return isRunningOnGAE()
        && LightAppIdEnum.QA == LightAppIdEnum.getLightAppIdEnum();
  }

  /**
   * Returns true if Light Instance is running locally for UnitTest.
   * This depends on AppId.
   */
  public static boolean isUnitTestServer() {
    return SystemProperty.environment.value() == null
        && LightAppIdEnum.TEST == LightAppIdEnum.getLightAppIdEnum();
  }
}
