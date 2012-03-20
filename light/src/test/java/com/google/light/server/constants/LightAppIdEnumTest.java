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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.light.server.guice.LightServletModuleTest;
import org.junit.Test;

/**
 * Test for {@link LightEnvEnum}.
 * 
 * @author Arjun Satyapal
 */
public class LightAppIdEnumTest implements EnumTestInterface {

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
   * For {@link LightEnvEnum#DEV_SERVER} and {@link LightEnvEnum#UNIT_TEST}, we dont care as they are
   * for testing purpose only.
   */
  @Test
  public void test_mutualExclusiveCategory() {
    for (String currId : LightEnvEnum.PROD.getAppIds()) {
      assertFalse(LightEnvEnum.QA.getAppIds().contains(currId));
    }
  }

  /**
   * Test for {@link LightEnvEnum#getLightEnvByAppId(String)}.
   * 
   * We are not testing {@link LightEnvEnum#getLightEnvByAppId()} directly as it involves lot of
   * setup and teardown. In addition it depends on
   * {@link LightEnvEnum#getLightAppIdEnumById(String)}. So no point in extra testing.
   * 
   * In order to be safe, its tested as part of
   * {@link LightServletModuleTest#test_validateTestFilterBinding()}.
   */
  @Test
  public void test_getLightEnvEnumByAppId() {
    assertEquals(LightEnvEnum.PROD, getLightEnvByAppId("s~light-prod"));
    assertEquals(LightEnvEnum.QA, getLightEnvByAppId("s~light-qa"));
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
  }
}
