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

import static com.google.light.server.utils.LightPreconditions.checkIsEnv;

import com.google.light.server.constants.LightEnvEnum;
import com.google.light.server.manager.implementation.oauth2.consumer.GoogleOAuth2ConsumerManagerImpl;
import com.google.light.server.manager.interfaces.OAuth2ConsumerCredentialManager;

/**
 * Production Guice module for Production Environment.
 * 
 * Note : All bindings should be in request scoped.
 * 
 * @author Arjun Satyapal
 */
public class ProdModule extends BaseGuiceModule {
  public ProdModule() {
    checkIsEnv(this, LightEnvEnum.PROD);
  }

  @Override
  protected void configure() {
    super.configure();
    
    // Add Production Environment specific bindings here.
    bind(OAuth2ConsumerCredentialManager.class)
      .to(GoogleOAuth2ConsumerManagerImpl.class);
  }
}
