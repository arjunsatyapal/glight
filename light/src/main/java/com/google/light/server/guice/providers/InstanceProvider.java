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
package com.google.light.server.guice.providers;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.light.server.manager.implementation.PersonManagerImpl;

import com.google.inject.Provider;

import com.google.inject.util.Providers;

import com.google.light.server.servlets.SessionManager;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.light.server.manager.interfaces.PersonManager;

/**
 * Service Provider for various Guice Injected Classes.
 * 
 * NOTE : DAOs should be injected directly in the relevant managers.
 * 
 * @author Arjun Satyapal
 */
public class InstanceProvider {
  private Injector injector;
  private Provider<PersonManagerImpl> personManagerProvider;

  @Inject
  private InstanceProvider(Injector injector, Provider<PersonManagerImpl> personManagerProvider) {
    this.injector = checkNotNull(injector);
    this.personManagerProvider = checkNotNull(personManagerProvider);
  }

  public PersonManager getPersonManager() {
    return checkNotNull(personManagerProvider.get());
  }
  
  public SessionManager getSessionManager() {
    return checkNotNull(injector.getInstance(SessionManager.class));
  }
}
