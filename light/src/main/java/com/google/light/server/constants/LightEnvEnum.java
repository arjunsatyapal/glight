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
package com.google.light.server.constants;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.light.server.utils.LightPreconditions.checkNotEmptyCollection;

import java.util.List;

import com.google.appengine.api.utils.SystemProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.light.server.exception.unchecked.ServerConfigurationException;
import com.google.light.server.utils.GaeUtils;

/**
 * Enum to identify the Environment.
 * 
 * @author Arjun Satyapal
 */
public enum LightEnvEnum {
  PROD(newArrayList("light-prod")),
  QA(newArrayList("light-qa", "light-qa1", "light-demo")),
 
  // TODO(arjuns) : Create separate appengine-web.xml files for Prod, QA.
  // DevServer picks the value from appengine-web.xml. So it will be always same as QA. But
  // the difference is that SystemProperty.environment.value() differs for QA and DEV_SERVER.
  DEV_SERVER(newArrayList("light-qa")),
  UNIT_TEST(newArrayList(LightStringConstants.TEST));
  
  private List<String> appIds;
  
  private LightEnvEnum(List<String> appIds) {
    this.appIds = checkNotEmptyCollection(appIds, "appIds");
  }
  
  public List<String> getAppIds() {
    return appIds;
  }
  
  /**
   * Returns the correct enum on the basis of ApplicationIds.
   * If value is not a recognized one, then returns TEST. 
   * 
   * TODO(arjuns): Eventually, instead of hardcoding the applicationIds, move them to 
   * a properties file.
   * 
   * @return
   */
  public static LightEnvEnum getLightEnv() {
    String appId = GaeUtils.getAppIdFromSystemProperty();
    LightEnvEnum returnEnum = getLightEnvByAppId(appId);
    
    if (returnEnum == QA) {
      /*
       * Since search by AppId always prioritizes QA over DEV_SERVER, so this step of checking
       * the environment value is here to correct that.
       */
      if (GaeUtils.isDevServer()) {
        return DEV_SERVER;
      }
    }
    
    return returnEnum;
  }
  
  /**
   * This method will search for {@link LightEnvEnum} by AppId. Since DEV_SERVER and QA share
   * AppIds, so this method will always prioritize QA over DEV_SERVER.
   * 
   * @param envId
   * @return
   */
  @VisibleForTesting
  protected static LightEnvEnum getLightEnvByAppId(String envId) {
    for (LightEnvEnum currEnum : LightEnvEnum.values()) {
      // DevServer is not calculated on the basis of envId. So it should never be returned.
      if (currEnum == LightEnvEnum.DEV_SERVER) {
        continue;
      }
      
      for (String currId : currEnum.appIds) {
        if (currId.equals(envId)) {
          return currEnum;
        }
      }
    }
    
    throw new ServerConfigurationException("Invalid EnvId : " + envId);
  }
}
