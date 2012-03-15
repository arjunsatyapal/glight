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

import com.google.inject.AbstractModule;
import com.google.light.server.annotations.AnotLightScope;
import com.google.light.server.annotations.AnotSession;
import com.google.light.server.guice.providers.InstanceProvider;
import com.google.light.server.guice.scope.LightScope;
import com.google.light.server.manager.implementation.PersonManagerImpl;
import com.google.light.server.manager.interfaces.PersonManager;
import com.google.light.server.servlets.SessionManager;
import javax.servlet.http.HttpSession;

/**
 * Guice Production Module for Light.
 * 
 * @author Arjun Satyapal
 */
public class LightModule extends AbstractModule {

  @Override
  protected void configure() {
    // tell Guice about the scope
    LightScope lightScope = new LightScope();
    bindScope(AnotLightScope.class, lightScope);
    bind(LightScope.class).toInstance(lightScope);

    // Binding lightScope variables.
    bind(HttpSession.class).annotatedWith(AnotSession.class)
        .toProvider(LightScope.<HttpSession> seededKeyProvider()).in(lightScope);
    
    bind(InstanceProvider.class).in(lightScope);
    bind(SessionManager.class).in(lightScope);
    bind(PersonManager.class).to(PersonManagerImpl.class).in(lightScope);
  }
}
