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

import static com.google.light.testingutils.TestingUtils.getRandomEmail;
import static com.google.light.testingutils.TestingUtils.getRandomFederatedId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.appengine.api.utils.SystemProperty;
import com.google.light.server.constants.LightEnvEnum;
import com.google.light.server.constants.OAuth2Provider;
import com.google.light.testingutils.GaeTestingUtils;
import com.google.light.testingutils.TestingUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link GaeUtils}.
 * 
 * @author Arjun Satyapal
 */
public class GaeUtilsTest {
  private String gaeUserEmail;
  @Before
  public void setUp() {
    gaeUserEmail = getRandomEmail();
  }

  /**
   * Test for {@link GaeUtils#getAppId()}.
   */
  @Test
  public void test_getAppId() {
    // First appId for QA env.
    String expectedAppId = "s~light-qa";
    GaeTestingUtils gaeTestingUtils = getGaeTestingUtils(LightEnvEnum.QA);

    try {
      gaeTestingUtils.setUp();
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
    // First appId for QA env.
    GaeTestingUtils gaeTestingUtils = getGaeTestingUtils(LightEnvEnum.DEV_SERVER);

    try {
      gaeTestingUtils.setUp();
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
    // First appId for QA env.
    GaeTestingUtils gaeTestingUtils = getGaeTestingUtils(LightEnvEnum.PROD);

    try {
      gaeTestingUtils.setUp();
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
    // First appId for QA env.
    GaeTestingUtils gaeTestingUtils = getGaeTestingUtils(LightEnvEnum.QA);

    try {
      gaeTestingUtils.setUp();
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
    // nothing to test.
  }
  
  /**
   * Test for {@link GaeUtils#isUnitTestServer()}
   */
  @Test
  public void test_isUnitTestServer() {
    // First appId for QA env.
    GaeTestingUtils gaeTestingUtils = getGaeTestingUtils(LightEnvEnum.UNIT_TEST);

    try {
      gaeTestingUtils.setUp();
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

  private GaeTestingUtils getGaeTestingUtils(LightEnvEnum env) {
    return new GaeTestingUtils(env, OAuth2Provider.GOOGLE_LOGIN, gaeUserEmail, getRandomFederatedId(),
        false /* isFederated */, TestingUtils.getRandomUserId()/* userId */, true/* loggedIn */,
        false/* isAdmin */);
  }
}
