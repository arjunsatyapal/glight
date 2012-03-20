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

import com.google.apphosting.api.ApiProxy;
import com.google.apphosting.api.ApiProxy.Environment;
import com.google.common.annotations.VisibleForTesting;
import com.google.light.server.utils.LightPreconditions;
import java.util.List;

/**
 * Enum to identify the Environment.
 * 
 * TODO(arjuns): Rename this class to LightEnvironmentEnum.
 * 
 * @author Arjun Satyapal
 */
public enum LightAppIdEnum {
  PROD(newArrayList("s~light-prod")),
  QA(newArrayList("s~light-qa")),
 
  // TODO(arjuns) : Create separate appengine-web.xml files for Prod and Qa.
  // DevServer picks the value from appengine-web.xml. So it will be always same as QA. But
  // the difference is that SystemProperty.environment.value() differs for QA and DEV_SERVER.
  DEV_SERVER(newArrayList("s~light-qa")),
  // TODO(arjuns): Rename this to UnitTest.
  TEST(newArrayList("test"));
  
  private List<String> appIds;
  
  private LightAppIdEnum(List<String> appIds) {
    this.appIds = LightPreconditions.checkNonEmptyList(appIds);
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
  public static LightAppIdEnum getLightAppIdEnum() {
    Environment env = ApiProxy.getCurrentEnvironment();
    
    return getLightAppIdEnumById(env.getAppId());
  }
  
  @VisibleForTesting
  static LightAppIdEnum getLightAppIdEnumById(String envId) {
    for (LightAppIdEnum currEnum : LightAppIdEnum.values()) {
      // DevServer is not calculated on the basis of envId. So it should never be returned.
      if (currEnum == LightAppIdEnum.DEV_SERVER) {
        continue;
      }
      
      for (String currId : currEnum.appIds) {
        if (currId.equals(envId)) {
          return currEnum;
        }
      }
    }
    
    return TEST;
  }

}