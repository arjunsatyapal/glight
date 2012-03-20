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

import static com.google.light.testingutils.TestingUtils.getRandomEmail;
import static com.google.light.testingutils.TestingUtils.getRandomFederatedId;
import static com.google.light.testingutils.TestingUtils.getRandomUserId;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import com.google.light.server.constants.OAuth2Provider;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.ServletModule;
import com.google.light.server.constants.LightAppIdEnum;
import com.google.light.server.guice.modules.ProdModule;
import com.google.light.testingutils.GaeTestingUtils;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test for {@link ServletModule}.
 * 
 * @author Arjun Satyapal
 */
public class LightServletModuleTest {
  protected static GaeTestingUtils gaeTestingUtils = null;

  @BeforeClass
  public static void gaeSetUp() {
    // Deliberately creating PROD Env.
    gaeTestingUtils =
        new GaeTestingUtils(LightAppIdEnum.PROD, OAuth2Provider.GOOGLE_LOGIN, getRandomEmail(),
            getRandomFederatedId(), true /* isFederatedUser */, getRandomUserId(),
            true /* isUserLoggedIn */, false /* isGaeAdmin */);
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
    Injector injector = Guice.createInjector(
        new ProdModule() /*for bindings on which servlets are dependent*/, 
        new LightServletModule());
    GuiceFilter filter = injector.getInstance(GuiceFilter.class);
    FilterConfig filterConfig = mock(FilterConfig.class);
    ServletContext context = mock(ServletContext.class);
    when(filterConfig.getServletContext()).thenReturn(context);
    filter.init(filterConfig);
    filter.destroy();
  }
  
  // TODO(arjuns): Add tests for ServletBindings.
}
