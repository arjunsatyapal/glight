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
package com.google.light.server.guice.modules;

import com.google.light.server.constants.OAuth2Provider;

import com.google.light.server.servlets.test.TestOAuth2ConsumerManagerImpl;

import com.google.light.server.annotations.AnotOAuth2ConsumerGoogleLogin;

import com.google.light.server.manager.interfaces.OAuth2ConsumerManager;

import com.google.light.server.exception.unchecked.ServerConfigurationException;
import com.google.light.server.utils.GaeUtils;


/**
 * Guice Module for DeverServer Environment.
 * 
 * Note : All bindings should be in request scoped.
 * 
 * @author Arjun Satyapal
 */
public class DevServerModule extends BaseGuiceModule {
  public DevServerModule() {
    if (!(GaeUtils.isDevServer() || GaeUtils.isUnitTestServer())) {
      throw new ServerConfigurationException(
          "DevServerModule should be instantiated only for DevServer/UnitTest Env.");
    }
  }

  @Override
  protected void configure() {
    super.configure();
    
    // Add QA Environment specific bindings here.
    bind(OAuth2ConsumerManager.class)
        .annotatedWith(AnotOAuth2ConsumerGoogleLogin.class)
        .toInstance(new TestOAuth2ConsumerManagerImpl(OAuth2Provider.GOOGLE_LOGIN));
  }
}
