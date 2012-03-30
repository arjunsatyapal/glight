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

import static com.google.light.server.constants.OAuth2ProviderService.GOOGLE_LOGIN;
import static com.google.light.testingutils.TestingUtils.getMockSessionForTesting;
import static com.google.light.testingutils.TestingUtils.getRandomEmail;
import static com.google.light.testingutils.TestingUtils.getRandomPersonId;
import static com.google.light.testingutils.TestingUtils.getRandomProviderUserId;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.inject.Injector;
import com.google.inject.servlet.GuiceFilter;
import com.google.light.server.constants.LightEnvEnum;
import com.google.light.testingutils.GaeTestingUtils;
import com.google.light.testingutils.TestingUtils;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import org.junit.Test;

/**
 * Test for {@link LightServletModule}.
 * 
 * @author Arjun Satyapal
 */
public class LightServletModuleTest {
  /**
   * Test to ensure that Guice can initialize servlets and filters.
   */
  @Test
  public void test_ensureBindingPossible() throws Exception {
    for (LightEnvEnum currEnv : LightEnvEnum.values()) {
      HttpSession session = null;
      session = getMockSessionForTesting(currEnv, GOOGLE_LOGIN, getRandomProviderUserId(), 
          getRandomPersonId(), getRandomEmail());
      GaeTestingUtils gaeTestingUtils = TestingUtils.gaeSetup(currEnv);
      try {
        Injector injector = TestingUtils.getInjectorByEnv(currEnv, session);

        GuiceFilter filter = injector.getInstance(GuiceFilter.class);
        FilterConfig filterConfig = mock(FilterConfig.class);
        ServletContext context = mock(ServletContext.class);

        when(filterConfig.getServletContext()).thenReturn(context);
        filter.init(filterConfig);
        filter.destroy();
      } catch (Exception e) {
        throw new Exception("Failed for : " + currEnv, e);
      } finally {
        gaeTestingUtils.tearDown();
      }
    }
  }
  // TODO(arjuns): Add tests for ServletBindings.
}
