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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.google.light.server.annotations.AnotHttpSession;
import javax.servlet.http.HttpSession;

/**
 * Guice Module for UnitTests for Light.
 * 
 * @author Arjun Satyapal
 */
public class TestLightModule extends AbstractModule {
  private HttpSession httpSession;
  
  public TestLightModule(HttpSession httpSession) {
    this.httpSession = checkNotNull(httpSession);
  }
  
  @Override
  protected void configure() {
    bind(HttpSession.class)
        .annotatedWith(AnotHttpSession.class)
        .toInstance(httpSession);

  }

  public static Injector getTestInjector(HttpSession httpSession) {
    return Guice.createInjector(Modules.override(
        new LightModule()).with(new TestLightModule(httpSession)));
  }
}
