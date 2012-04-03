/*
 * Copyright 2012 Google Inc.
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


import java.util.logging.Logger;

import javax.servlet.http.HttpSession;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.light.server.annotations.AnotHttpSession;
import com.google.light.server.manager.implementation.AdminOperationManagerImpl;
import com.google.light.server.manager.implementation.PersonManagerImpl;
import com.google.light.server.manager.implementation.SearchManagerGSSImpl;
import com.google.light.server.manager.implementation.oauth2.consumer.OAuth2ConsumerCredentialManagerFactory;
import com.google.light.server.manager.implementation.oauth2.consumer.OAuth2ConsumerCredentialManagerImpl;
import com.google.light.server.manager.implementation.oauth2.owner.OAuth2OwnerTokenManagerFactory;
import com.google.light.server.manager.implementation.oauth2.owner.OAuth2OwnerTokenManagerImpl;
import com.google.light.server.manager.interfaces.AdminOperationManager;
import com.google.light.server.manager.interfaces.OAuth2ConsumerCredentialManager;
import com.google.light.server.manager.interfaces.OAuth2OwnerTokenManager;
import com.google.light.server.manager.interfaces.PersonManager;
import com.google.light.server.manager.interfaces.SearchManager;
import com.google.light.server.persistence.dao.OAuth2ConsumerCredentialDao;
import com.google.light.server.servlets.oauth2.google.OAuth2Helper;
import com.google.light.server.servlets.oauth2.google.OAuth2HelperFactoryInterface;
import com.google.light.server.servlets.oauth2.google.OAuth2HelperImpl;


/**
 * {@link BaseGuiceModule} will do two things : <br>
 * 1. Do mandatory bindings which are common across all environments. <br>
 * 2. Define bindings that are required, but varies in different environments.
 * <p>
 * NOTE : Even if any one of environment(Product, QA, or DevServer) varies in type of binding, that
 * will cause to define the binding for all the environments in the corresponding Environment
 * Module. * This approach keeps Module bindings simple and easy to maintain.
 * <p>
 * {@link com.google.light.server.guice.module.UnitTestModule} is exception to this rule.
 * UnitTestModule will Override the {@link DevServerModule} as they are almost always similar.
 * 
 * TODO(arjuns): Add test for this.
 * 
 * @author Arjun Satyapal
 */
public abstract class BaseGuiceModule extends AbstractModule {
  private static final Logger logger = Logger.getLogger(BaseGuiceModule.class.getName());

  @Override
  protected void configure() {
    requireBinding(HttpTransport.class);

    // TODO(arjuns): Can this be removed.
    bind(OAuth2ConsumerCredentialDao.class);
    
    
    // Guice Assisted Inject Bindings.
    install(new FactoryModuleBuilder()
      .implement(OAuth2Helper.class, OAuth2HelperImpl.class)
      .build(OAuth2HelperFactoryInterface.class));
    
    install(new FactoryModuleBuilder()
      .implement(OAuth2OwnerTokenManager.class, OAuth2OwnerTokenManagerImpl.class)
      .build(OAuth2OwnerTokenManagerFactory.class));
    
    install(new FactoryModuleBuilder()
      .implement(OAuth2ConsumerCredentialManager.class, OAuth2ConsumerCredentialManagerImpl.class)
      .build(OAuth2ConsumerCredentialManagerFactory.class));
    
    // Binding Manager Implementations.
    bind(AdminOperationManager.class)
        .to(AdminOperationManagerImpl.class);

    bind(PersonManager.class)
        .to(PersonManagerImpl.class);
    bind(SearchManager.class).to(SearchManagerGSSImpl.class);

    bind(HttpSession.class)
        .annotatedWith(AnotHttpSession.class)
        .to(HttpSession.class);

    // TODO(arjuns): Following bindings are global bindings. Need to be fixed.
    /**
     * Implementation is thread-safe, so using it as a singleton.
     * TODO(waltercacau): See if we should use UrlFetchTransport instead.
     * @see http://code.google.com/p/google-http-java-client/wiki/GoogleAppEngine#HTTP_Transport
     */
    bind(HttpTransport.class).to(NetHttpTransport.class).in(Singleton.class);
  }

  @Provides
  public JsonFactory provideJsonFactory() {
    logger.info("Creating new JsonFactory");
    return new JacksonFactory();
  }
}
