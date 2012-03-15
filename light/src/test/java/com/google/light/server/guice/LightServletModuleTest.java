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
package com.google.light.server.guice;

import static com.google.light.server.utils.GaeUtils.isDevServer;
import static com.google.light.server.utils.GaeUtils.isProductionServer;
import static com.google.light.server.utils.GaeUtils.isQaServer;
import static com.google.light.testingutils.TestingUtils.getRandomEmail;
import static com.google.light.testingutils.TestingUtils.getRandomFederatedId;
import static com.google.light.testingutils.TestingUtils.getRandomUserId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import com.google.light.server.constants.LightAppIdEnum;

import com.google.light.server.servlets.filters.ProdServletFilter;

import com.google.inject.ProvisionException;

import com.google.light.server.exception.unchecked.FilterInstanceBindingException;

import com.google.light.server.utils.GaeUtils;

import com.google.light.server.servlets.filters.TestServletFilter;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import com.google.light.server.constants.OpenIdAuthDomain;
import com.google.light.testingutils.GaeTestingUtils;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test for {@link LightServletModule}.
 * 
 * @author Arjun Satyapal
 */
public class LightServletModuleTest {
  protected static GaeTestingUtils gaeTestingUtils = null;

  @BeforeClass
  public static void gaeSetUp() {
    gaeTestingUtils =
        new GaeTestingUtils(OpenIdAuthDomain.GOOGLE.get(), getRandomEmail(),
            getRandomFederatedId(),
            true /* isFederatedUser */, getRandomUserId(), true /* isUserLoggedIn */,
            false /* isGaeAdmin */);
    gaeTestingUtils.setUp();
  }

  @Before
  public void setUp() {
    initMocks(this);
  }

  @AfterClass
  public static void gaeTearDown() {
    gaeTestingUtils.tearDown();
  }

  /**
   * Test to ensure that Guice can initialize servlets and filters.
   */
  @Test
  public void test_ensureBindingPossible() throws Exception {
    Injector injector = Guice.createInjector(new LightServletModule());
    GuiceFilter filter = injector.getInstance(GuiceFilter.class);
    FilterConfig filterConfig = mock(FilterConfig.class);
    ServletContext context = mock(ServletContext.class);
    when(filterConfig.getServletContext()).thenReturn(context);
    filter.init(filterConfig);
    filter.destroy();
  }

  /**
   * Test bindings for Filters in different scenarios.
   */
  @Test
  public void test_validateTestFilterBinding() {
    // Default env = test.
    gaeTestingUtils.tearDown();
    gaeTestingUtils.setAppId("test");
    gaeTestingUtils.setUp();
    
    Injector testInjector = Guice.createInjector(new LightServletModule());
    assertTrue(isDevServer());
    assertFalse(isQaServer());
    assertFalse(isProductionServer());
    assertEquals(LightAppIdEnum.TEST, LightAppIdEnum.getLightAppIdEnum());
    assertNotNull(testInjector.getInstance(TestServletFilter.class));
    // TODO(arjuns): fix this.
//    assertEquals("test.", NamespaceManager.get());
    
    
    try {
      testInjector.getInstance(ProdServletFilter.class);
      fail("should have failed.");
    } catch (ProvisionException e) {
      assertTrue(e.getCause() instanceof FilterInstanceBindingException);
    } finally {
      testInjector.getInstance(GuiceFilter.class).destroy();
    }
    
    // Now simulating : qa.
    gaeTestingUtils.tearDown();
    gaeTestingUtils.setAppId("s~light-qa");
    gaeTestingUtils.setUp();
    assertFalse(isDevServer());
    assertTrue(isQaServer());
    assertFalse(isProductionServer());
    assertEquals(LightAppIdEnum.QA, LightAppIdEnum.getLightAppIdEnum());
    
    Injector qaInjector = Guice.createInjector(new LightServletModule());
    assertNotNull(qaInjector.getInstance(TestServletFilter.class));
    // TODO(arjuns): fix this.
//    assertEquals("test.", NamespaceManager.get());
    
    try {
      qaInjector.getInstance(ProdServletFilter.class);
      fail("should have failed.");
    } catch (ProvisionException e) {
      assertTrue(e.getCause() instanceof FilterInstanceBindingException);
    } finally {
      gaeTestingUtils.tearDown();
      gaeTestingUtils.setAppId("test");
      gaeTestingUtils.setUp();
      assertTrue(GaeUtils.isDevServer());
      qaInjector.getInstance(GuiceFilter.class).destroy();
    }
    
    // Now simulating Prod.
    gaeTestingUtils.tearDown();
    gaeTestingUtils.setAppId("s~light-prod");
    gaeTestingUtils.setUp();
    Injector prodInjector = Guice.createInjector(new LightServletModule());
    assertFalse(isDevServer());
    assertFalse(isQaServer());
    assertTrue(isProductionServer());
    assertEquals(LightAppIdEnum.PROD, LightAppIdEnum.getLightAppIdEnum());
    assertNotNull(prodInjector.getInstance(ProdServletFilter.class));
    // TODO(arjuns): fix this.
//    assertEquals("", NamespaceManager.get());
    
    try {
      prodInjector.getInstance(TestServletFilter.class);
      fail("should have failed.");
    } catch (ProvisionException e) {
      assertTrue(e.getCause() instanceof FilterInstanceBindingException);
    } finally {
      
      // Resetting things back to test.
      gaeTestingUtils.tearDown();
      gaeTestingUtils.setAppId("test");
      gaeTestingUtils.setUp();
      assertTrue(GaeUtils.isDevServer());
      prodInjector.getInstance(GuiceFilter.class).destroy();
    }
  }
}
