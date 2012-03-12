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
    return getAuthDomainByValue(checkNotBlank(getUser().getAuthDomain()));
  }

  /**
   * Get Email provided by the Federated Identity Provider. This should be called only when user is
   * logged in.
   * 
   * @return
   */
  public static String getGaeUserEmail() {
    return checkNotBlank(getUser().getEmail());
  }
  
  /**
   * Get Federated Identity of the Logged in user. This should be called only when user is logged
   * in.
   * 
   * @return
   */
  public static String getFederatedIdentity() {
    return checkNotBlank(getUser().getFederatedIdentity());
  }
  
  
  /**
   * Get GAE User Service.
   * 
   * @return
   */
  public static UserService getUserService() {
    return checkNotNull(UserServiceFactory.getUserService());
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
    return checkNotBlank(getUser().getUserId());
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
   * Returns true if current user is Admin.
   * Returns false if user is not admin / user is not logged in.
   * 
   * @return
   */
  public static boolean isUserAdmin() {
    return isUserLoggedIn() && getUserService().isUserAdmin();
  }

  // Utility Class.
  private GaeUtils() {
  }
}
