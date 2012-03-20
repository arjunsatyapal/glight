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


import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.light.server.annotations.AnotHttpSession;
import com.google.light.server.manager.implementation.AdminOperationManagerImpl;
import com.google.light.server.manager.interfaces.AdminOperationManager;
import java.util.logging.Logger;
import javax.servlet.http.HttpSession;

/**
 * {@link BaseGuiceModule} will do two things :
 * <br> 1. Do mandatory bindings which are common across all environments.
 * <br> 2. Define bindings that are required, but varies in different environments.
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
      
    
    bind(HttpSession.class)
        .annotatedWith(AnotHttpSession.class)
        .to(HttpSession.class);
    
    bind(AdminOperationManager.class)
      .to(AdminOperationManagerImpl.class);
  }
  
  // TODO(arjuns): Following bindings are global bindings. Need to be fixed.
  @Provides
  public HttpTransport provideHttpTransport() {
    logger.info("Creating new HttpTransport");
    return new NetHttpTransport();
  }
  
  @Provides
  public JsonFactory provideJsonFactory() {
    logger.info("Creating new JsonFactory");
    return new JacksonFactory();
  }
}
