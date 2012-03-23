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

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.light.server.guice.providers.InstanceProvider;
import com.google.light.server.manager.interfaces.OAuth2ConsumerCredentialManager;
import com.google.light.server.persistence.dao.PersonDao;

/**
 * Classes which are not added to {@link InstanceProvider} can be added here for test.
 * 
 * @author Arjun Satyapal
 */

@SuppressWarnings("deprecation")
public class TestInstanceProvider {
  private Injector injector;
  
  @Inject 
  public TestInstanceProvider(Injector injector) {
    this.injector = checkNotNull(injector);
  }
  
  public PersonDao getPersonDao() {
    return checkNotNull(injector.getInstance(PersonDao.class));
  }
  
  public OAuth2ConsumerCredentialManager oauth2ConsumerGoogleLogin() {
    return checkNotNull(injector.getInstance(OAuth2ConsumerCredentialManager.class));
  }
}
