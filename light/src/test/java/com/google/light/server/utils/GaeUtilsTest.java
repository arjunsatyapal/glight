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

import static com.google.light.testingutils.TestingUtils.gaeSetup;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.light.server.constants.LightEnvEnumTest;

import com.google.appengine.api.utils.SystemProperty;
import com.google.light.server.constants.LightEnvEnum;
import com.google.light.testingutils.GaeTestingUtils;
import org.junit.Test;

/**
 * Test for {@link GaeUtils}.
 * 
 * @author Arjun Satyapal
 */
public class GaeUtilsTest {
  private GaeTestingUtils gaeTestingUtils = null;
  /**
   * Test for {@link GaeUtils#getAppId()}.
   * This is just one test for QA Env. Others are done as part of the {@link LightEnvEnumTest}.
   */
  @Test
  public void test_getAppId() {
    String expectedAppId = "s~light-qa";

    try {
      gaeTestingUtils = gaeSetup(LightEnvEnum.QA);
      assertEquals(expectedAppId, GaeUtils.getAppId());
    } finally {
      gaeTestingUtils.tearDown();
    }
  }

  /**
   * Test for {@link GaeUtils#isDevServer()}
   */
  @Test
  public void test_isDevServer() {
    try {
      gaeTestingUtils = gaeSetup(LightEnvEnum.DEV_SERVER);
      assertTrue(GaeUtils.isDevServer());
      assertFalse(GaeUtils.isProductionServer());
      assertFalse(GaeUtils.isQaServer());
      assertFalse(GaeUtils.isRunningOnGAE());
      assertFalse(GaeUtils.isUnitTestServer());
      assertNotNull(SystemProperty.environment.value());
    } finally {
      gaeTestingUtils.tearDown();
    }
  }

  /**
   * Test for {@link GaeUtils#isProductionServer()}
   */
  @Test
  public void test_isProductionServer() {
    try {
      gaeTestingUtils = gaeSetup(LightEnvEnum.PROD);
      assertFalse(GaeUtils.isDevServer());
      assertTrue(GaeUtils.isProductionServer());
      assertFalse(GaeUtils.isQaServer());
      assertTrue(GaeUtils.isRunningOnGAE());
      assertFalse(GaeUtils.isUnitTestServer());
      assertNotNull(SystemProperty.environment.value());
    } finally {
      gaeTestingUtils.tearDown();
    }
  }

  /**
   * Test for {@link GaeUtils#isQaServer()}
   */
  @Test
  public void test_isQaServer() {
    try {
      gaeTestingUtils = gaeSetup(LightEnvEnum.QA);
      assertFalse(GaeUtils.isDevServer());
      assertFalse(GaeUtils.isProductionServer());
      assertTrue(GaeUtils.isQaServer());
      assertTrue(GaeUtils.isRunningOnGAE());
      assertFalse(GaeUtils.isUnitTestServer());
      assertNotNull(SystemProperty.environment.value());
    } finally {
      gaeTestingUtils.tearDown();
    }
  }

  /**
   * Test for {@link GaeUtils#isRunningOnGAE()}
   * Already tested as part of {@link #test_isProductionServer} and {@link #test_isQaServer()}
   */
  @Test
  public void test_isRunningOnGae() {
    try {
      gaeTestingUtils = gaeSetup(LightEnvEnum.QA);
      assertTrue(GaeUtils.isRunningOnGAE());
    } finally {
      gaeTestingUtils.tearDown();
    }
    
    try {
      gaeTestingUtils = gaeSetup(LightEnvEnum.PROD);
      assertTrue(GaeUtils.isRunningOnGAE());
    } finally {
      gaeTestingUtils.tearDown();
    }
  }
  
  
  /**
   * Test for {@link GaeUtils#isUnitTestServer()}
   */
  @Test
  public void test_isUnitTestServer() {
    try {
      gaeTestingUtils = gaeSetup(LightEnvEnum.UNIT_TEST);
      assertFalse(GaeUtils.isDevServer());
      assertFalse(GaeUtils.isProductionServer());
      assertFalse(GaeUtils.isQaServer());
      assertFalse(GaeUtils.isRunningOnGAE());
      assertTrue(GaeUtils.isUnitTestServer());
      assertNull(SystemProperty.environment.value());
    } finally {
      gaeTestingUtils.tearDown();
    }
  }
}
