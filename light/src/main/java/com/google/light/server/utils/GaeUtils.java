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
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;

import com.google.light.server.constants.LightStringConstants;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.utils.SystemProperty;
import com.google.apphosting.api.ApiProxy;
import com.google.apphosting.api.ApiProxy.Environment;
import com.google.light.server.constants.LightEnvEnum;

/**
 * Utility class to wrap different GAE specific functions.
 * 
 * @author Arjun Satyapal
 */
public class GaeUtils {

  /**
   * Get GAE User Service.
   * 
   * @return
   */
  public static UserService getUserService() {
    return checkNotNull(UserServiceFactory.getUserService(), "userService is null.");
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

  /**
   * Returns Appengine AppId for the current environment.
   * 
   * @return
   */
  public static String getAppId() {
    Environment env = ApiProxy.getCurrentEnvironment();
    if (env == null) {
      return LightStringConstants.TEST;
    }
    return checkNotBlank(env.getAppId(), "appId");
  }
  
  public static String getBaseUrlForDefaultVersion() {
    if(isDevServer()) {
      return "http://localhost:8080";
    } else {
      return "http://"+getAppIdFromSystemProperty()+".appspot.com";
    }
  }

  public static String getAppIdFromSystemProperty() {
    return SystemProperty.applicationId.get();
  }
  
  public static String getHostForDefaultVersion() {
    if(isDevServer()) {
      return "localhost";
    } else {
      return getAppIdFromSystemProperty()+".appspot.com";
    }
  }
  
  

  /**
   * Ensures that Application is running on AppEngine.
   * 
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
    return isRunningOnGAE() && LightEnvEnum.PROD == LightEnvEnum.getLightEnv();
  }

  /**
   * Returns true if Light Instance is running on AppEngine and is configured as a QA App.
   * This depends on AppId.
   */
  public static boolean isQaServer() {
    return isRunningOnGAE()
        && LightEnvEnum.QA == LightEnvEnum.getLightEnv();
  }

  /**
   * Returns true if Light Instance is running locally for UnitTest.
   * This depends on AppId.
   */
  public static boolean isUnitTestServer() {
    return SystemProperty.environment.value() == null
        && LightEnvEnum.UNIT_TEST == LightEnvEnum.getLightEnv();
  }

  // Utility Class.
  private GaeUtils() {
  }

}
