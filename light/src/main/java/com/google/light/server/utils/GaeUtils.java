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

import com.google.appengine.api.utils.SystemProperty;

import com.google.apphosting.api.ApiProxy.Environment;

import com.google.apphosting.api.ApiProxy;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.light.server.constants.OpenIdAuthDomain;

/**
 * Utility class to wrap different GAE specific functions.
 * 
 * @author Arjun Satyapal
 */
public class GaeUtils {
  /**
   * Get FederatedAuthDomain for Current Logged in user. This should be called only when user is
   * Logged in.
   * 
   * @return
   */
  public static OpenIdAuthDomain getGaeAuthDomain() {
    return getAuthDomainByValue(checkNotBlank(getUser().getAuthDomain(), "authDomain is blank."));
  }

  /**
   * Get Email provided by the Federated Identity Provider. This should be called only when user is
   * logged in.
   * 
   * @return
   */
  public static String getGaeUserEmail() {
    return checkNotBlank(getUser().getEmail(), "userEmail is blank.");
  }

  /**
   * Get Federated Identity of the Logged in user. This should be called only when user is logged
   * in.
   * 
   * @return
   */
  public static String getFederatedIdentity() {
    // TODO(arjuns) : Fix this with Federated Login.
    return null;
    // return checkNotBlank(getUser().getFederatedIdentity(), "federatedIdentity is blank.");
  }

  /**
   * Get GAE User Service.
   * 
   * @return
   */
  public static UserService getUserService() {
    return checkNotNull(UserServiceFactory.getUserService(), "userService is null.");
  }

  /**
   * Get current logged in GAE User.
   * 
   * @return Return current Logged In user. Null if user is not logged in.
   */
  public static User getUser() {
    return checkNotNull(getUserService().getCurrentUser(), "Seems user is not logged in.");
  }

  /**
   * Get current logged in GAE User's id. This should be called when you are sure that user is
   * logged in.
   * 
   * @return
   */
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
   * Returns true if AppEngine is production instance for Light. This method depends on 
   * ApplicationId for Application.
   * 
   * TODO(arjuns): add test for this.
   * @return
   */
  public static boolean isProductionServer() {
    Environment env = ApiProxy.getCurrentEnvironment();
    if (env.getAppId().equals("light-prod")) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Returns true if AppEngine is running on Devserver.
   * 
   * // TODO(arjuns): Add test for this.
   * @return
   */
  public static boolean isDevServer() {
    if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Development) {
      return true;
    }
    return false;
  }
  
  /**
   * Returns true for an Application which is running on AppEngine production environment,
   * but is not a Production Application. 
   * 
   * TODO(arjuns): Add test for this.
   * @return True when its neither ProductionServer nor DevServer.
   */
  public static boolean isQaServer() {
    Environment env = ApiProxy.getCurrentEnvironment();
    return !isProductionServer() && env.getAppId().equals("light-qa");
  }
  
}
