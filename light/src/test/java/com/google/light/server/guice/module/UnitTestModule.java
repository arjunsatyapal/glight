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

import javax.servlet.http.HttpSession;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.servlet.ServletModule;
import com.google.inject.util.Modules;
import com.google.light.server.annotations.AnotHttpSession;
import com.google.light.server.exception.unchecked.ServerConfigurationException;
import com.google.light.server.guice.modules.BaseGuiceModule;
import com.google.light.server.guice.modules.DevServerModule;
import com.google.light.server.manager.implementation.oauth2.consumer.OAuth2ConsumerCredentialManagerFactory;
import com.google.light.server.manager.implementation.oauth2.consumer.TestOAuth2ConsumerCredentialManagerImpl;
import com.google.light.server.manager.interfaces.OAuth2ConsumerCredentialManager;
import com.google.light.server.utils.GaeUtils;

/**
 * UnitTest Guice Module for UnitTests Environment.
 * 
 * @author Arjun Satyapal
 */
public class UnitTestModule extends BaseGuiceModule {
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
    bind(HttpSession.class)
        .annotatedWith(AnotHttpSession.class)
        .toInstance(httpSession);
    
    bind(OAuth2ConsumerCredentialManager.class)
      .to(TestOAuth2ConsumerCredentialManagerImpl.class);
    
    install(new FactoryModuleBuilder()
    .implement(OAuth2ConsumerCredentialManager.class, TestOAuth2ConsumerCredentialManagerImpl.class)
    .build(OAuth2ConsumerCredentialManagerFactory.class));
  }

  public static Module getModule(HttpSession httpSession, ServletModule servletModule) {
    return Modules.override(new DevServerModule(), servletModule)
        .with(new UnitTestModule(httpSession));
  }
  public static Injector getTestInjector(HttpSession httpSession, ServletModule servletModule) {
    return Guice.createInjector(getModule(httpSession, servletModule));
  }
}
