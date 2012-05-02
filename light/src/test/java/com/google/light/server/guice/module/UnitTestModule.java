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
import static com.google.light.testingutils.TestingUtils.getMockPersonIdForFailure;

import com.google.light.server.dto.pojo.longwrapper.PersonId;

import com.google.light.server.dto.pojo.RequestScopedValues;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.servlet.ServletModule;
import com.google.inject.util.Modules;
import com.google.light.server.annotations.AnotActor;
import com.google.light.server.annotations.AnotHttpSession;
import com.google.light.server.annotations.AnotOwner;
import com.google.light.server.exception.unchecked.ServerConfigurationException;
import com.google.light.server.guice.modules.BaseGuiceModule;
import com.google.light.server.guice.modules.ProdModule;
import com.google.light.server.guice.provider.TestRequestScopedValuesProvider;
import com.google.light.server.manager.implementation.oauth2.consumer.OAuth2ConsumerCredentialManagerFactory;
import com.google.light.server.manager.implementation.oauth2.consumer.TestOAuth2ConsumerCredentialManagerImpl;
import com.google.light.server.manager.interfaces.OAuth2ConsumerCredentialManager;
import com.google.light.server.utils.GaeUtils;
import javax.servlet.http.HttpSession;

/**
 * UnitTest Guice Module for UnitTests Environment.
 * 
 * @author Arjun Satyapal
 */
public class UnitTestModule extends BaseGuiceModule {
  private TestRequestScopedValuesProvider testRequestScopedValueProvider;
  private HttpSession httpSession;

  public UnitTestModule(TestRequestScopedValuesProvider testRequestScopedValueProvider,
      HttpSession httpSession) {
    if (!GaeUtils.isUnitTestServer()) {
      throw new ServerConfigurationException(
          "UnitTestModule should be instantiated only for UnitTest Env.");
    }

    this.testRequestScopedValueProvider = checkNotNull(testRequestScopedValueProvider, 
        "testRequestScopedValueProvider");
    this.httpSession = checkNotNull(httpSession);
  }

  @Override
  protected void configure() {
    bind(HttpSession.class)
        .annotatedWith(AnotHttpSession.class)
        .toInstance(httpSession);

    bind(PersonId.class)
        .annotatedWith(AnotOwner.class)
        .toInstance(getMockPersonIdForFailure());

    bind(PersonId.class)
        .annotatedWith(AnotActor.class)
        .toInstance(getMockPersonIdForFailure());

    bind(RequestScopedValues.class)
        .toProvider(testRequestScopedValueProvider);

    bind(OAuth2ConsumerCredentialManager.class)
        .to(TestOAuth2ConsumerCredentialManagerImpl.class);

    install(new FactoryModuleBuilder()
        .implement(OAuth2ConsumerCredentialManager.class,
            TestOAuth2ConsumerCredentialManagerImpl.class)
        .build(OAuth2ConsumerCredentialManagerFactory.class));
  }

  public static Module getModule(TestRequestScopedValuesProvider testRequestScopedValueProvider,
      HttpSession httpSession, ServletModule servletModule) {
    return Modules.override(new ProdModule(), servletModule)
        .with(new UnitTestModule(testRequestScopedValueProvider, httpSession));
  }

  public static Injector getTestInjector(
      TestRequestScopedValuesProvider testRequestScopedValueProvider,
      HttpSession httpSession, ServletModule servletModule) {
    return Guice.createInjector(getModule(testRequestScopedValueProvider, httpSession,
        servletModule));
  }
}
