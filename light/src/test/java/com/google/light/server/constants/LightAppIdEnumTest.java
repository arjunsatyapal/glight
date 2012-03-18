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

import static com.google.light.server.constants.LightAppIdEnum.getLightAppIdEnumById;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.light.server.guice.LightServletModuleTest;

import org.junit.Test;

/**
 * Test for {@link LightAppIdEnum}.
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
    assertEquals(4, LightAppIdEnum.values().length);
  }

  /**
   * Ensure that Ids mentioned inside {@link LightAppIdEnum#PROD} and {@link LightAppIdEnum#QA} are
   * Mutually Exclusive.
   * 
   * For {@link LightAppIdEnum#DEV_SERVER} and {@link LightAppIdEnum#TEST}, we dont care as they are
   * for testing purpose only.
   */
  @Test
  public void test_mutualExclusiveCategory() {
    for (String currId : LightAppIdEnum.PROD.getAppIds()) {
      assertFalse(LightAppIdEnum.QA.getAppIds().contains(currId));
    }
  }

  /**
   * Test for {@link LightAppIdEnum#getLightAppIdEnumById(String)}.
   * 
   * We are not testing {@link LightAppIdEnum#getLightAppIdEnum()} directly as it involves lot of
   * setup and teardown. In addition it depends on
   * {@link LightAppIdEnum#getLightAppIdEnumById(String)}. So no point in extra testing.
   * 
   * In order to be safe, its tested as part of
   * {@link LightServletModuleTest#test_validateTestFilterBinding()}.
   */
  @Test
  public void test_getLightAppIdEnum() {
    assertEquals(LightAppIdEnum.PROD, LightAppIdEnum.getLightAppIdEnumById("s~light-prod"));
    assertEquals(LightAppIdEnum.QA, LightAppIdEnum.getLightAppIdEnumById("s~light-qa"));
    assertEquals(LightAppIdEnum.TEST, LightAppIdEnum.getLightAppIdEnumById("test"));

    // Ensure that for none of the mentioend AppIds, Dev Server is returned.
    for (LightAppIdEnum currEnum : LightAppIdEnum.values()) {
      for (String currAppId : currEnum.getAppIds()) {
        LightAppIdEnum expectedEnum = currEnum;
        
        // Since DEV_SERVER inherits AppId from QA, so getValue by AppId should return QA.
        if (expectedEnum == LightAppIdEnum.DEV_SERVER) {
          expectedEnum = LightAppIdEnum.QA;
        }
        
        assertEquals(expectedEnum, getLightAppIdEnumById(currAppId));
        // DEV_SERVER is never returned.
        assertTrue(LightAppIdEnum.DEV_SERVER != getLightAppIdEnumById(currAppId));
      }
    }
  }
}
