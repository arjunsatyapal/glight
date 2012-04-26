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

import static com.google.light.server.constants.LightEnvEnum.getLightEnvByAppId;
import static com.google.light.server.utils.LightPreconditions.checkNonEmptyList;
import static com.google.light.testingutils.TestingUtils.getRandomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.light.server.exception.unchecked.ServerConfigurationException;
import com.google.light.testingutils.GaeTestingUtils;
import com.google.light.testingutils.TestingUtils;
import java.util.List;
import org.junit.Test;

/**
 * Test for {@link LightEnvEnum}.
 * 
 * @author Arjun Satyapal
 */
public class LightEnvEnumTest implements EnumTestInterface {
  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_count() {
    assertEquals(4, LightEnvEnum.values().length);
  }

  /**
   * Ensure that Ids mentioned inside {@link LightEnvEnum#PROD} and {@link LightEnvEnum#QA} are
   * Mutually Exclusive.
   * 
   * For {@link LightEnvEnum#DEV_SERVER} and {@link LightEnvEnum#UNIT_TEST}, we don't care as 
   * they are for testing purposes only.
   */
  @Test
  public void test_mutualExclusiveCategory() {
    for (String currId : LightEnvEnum.PROD.getAppIds()) {
      assertFalse(LightEnvEnum.QA.getAppIds().contains(currId));
    }
  }

  /**
   * Test for {@link LightEnvEnum#getLightEnv()}.
   */
  public void test_getLightEnv() {
    for (LightEnvEnum currEnv : LightEnvEnum.values()) {
      GaeTestingUtils gaeTestingUtils = TestingUtils.gaeSetup(currEnv);
      assertEquals(currEnv, LightEnvEnum.getLightEnv());
      gaeTestingUtils.tearDown();
    }
  }
  
  /**
   * Test for {@link LightEnvEnum#getLightEnvByAppId(String)}.
   */
  @Test
  public void test_getLightEnvEnumByAppId() {
    assertEquals(LightEnvEnum.PROD, getLightEnvByAppId("light-prod"));
    assertEquals(LightEnvEnum.QA, getLightEnvByAppId("light-qa"));
    assertEquals(LightEnvEnum.UNIT_TEST, getLightEnvByAppId("test"));

    // Ensure that for none of the mentioned AppIds, DEV_SERVER is returned.
    for (LightEnvEnum currEnum : LightEnvEnum.values()) {
      for (String currAppId : currEnum.getAppIds()) {
        LightEnvEnum expectedEnum = currEnum;
        
        // Since DEV_SERVER inherits AppId from QA, so getValue by AppId should return QA.
        if (expectedEnum == LightEnvEnum.DEV_SERVER) {
          expectedEnum = LightEnvEnum.QA;
        }
        
        assertEquals(expectedEnum, getLightEnvByAppId(currAppId));
        // DEV_SERVER is never returned.
        assertTrue(LightEnvEnum.DEV_SERVER != getLightEnvByAppId(currAppId));
      }
    }
    
    try {
      getLightEnvByAppId(getRandomString());
      fail("should have failed.");
    } catch (ServerConfigurationException e) {
      // Expected
    }
  }
  
  /**
   * Test for {@link LightEnvEnum#getAppIds()}.
   */
  @Test
  public void test_getAppIds() {
    // We are just testing for Prod, but others can be similar.
    List<String> appIdList = LightEnvEnum.PROD.getAppIds();
    checkNonEmptyList(appIdList, "appIdList");
    assertEquals(1, appIdList.size());
    assertTrue(appIdList.contains("light-prod"));
  }
}
