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
package com.google.light.server.guice.module;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.servlet.ServletModule;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.google.light.server.annotations.AnotGoogleLoginCallbackUri;
import com.google.light.server.annotations.AnotHttpSession;
import com.google.light.server.exception.unchecked.ServerConfigurationException;
import com.google.light.server.guice.modules.DevServerModule;
import com.google.light.server.utils.GaeUtils;
import com.google.light.testingutils.TestingConstants;
import javax.servlet.http.HttpSession;

/**
 * UnitTest Guice Module for UnitTests Environment.
 * 
 * @author Arjun Satyapal
 */
public class UnitTestModule extends AbstractModule {
  private HttpSession httpSession;

  public UnitTestModule(HttpSession httpSession) {
    if (!GaeUtils.isUnitTestServer()) {
      throw new ServerConfigurationException(
          "UnitTestModule should be instantiated only for UnitTest Env.");
    }

    this.httpSession = checkNotNull(httpSession);
  }

  @Override
  protected void configure() {
    // Guice Bindings for Strings.
    bind(String.class)
        .annotatedWith(AnotGoogleLoginCallbackUri.class)
        .toInstance(TestingConstants.GOOGLE_OAUTH_CB_CMD_LINE_URI);

    bind(HttpSession.class)
        .annotatedWith(AnotHttpSession.class)
        .toInstance(httpSession);
  }

  public static Injector getTestInjector(HttpSession httpSession, ServletModule servletModule) {
    return Guice.createInjector(Modules.override(new DevServerModule(), servletModule)
        .with(new UnitTestModule(httpSession)));
  }
}
