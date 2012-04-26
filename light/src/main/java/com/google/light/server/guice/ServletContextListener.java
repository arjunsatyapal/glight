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


import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.light.server.constants.LightEnvEnum;
import com.google.light.server.exception.unchecked.ServerConfigurationException;
import com.google.light.server.guice.modules.BaseGuiceModule;
import com.google.light.server.guice.modules.ProdModule;
import com.google.light.server.utils.GaeUtils;
import java.util.logging.Logger;

/**
 * A ServletContext Listener to initialize Guice. More :
 * http://code.google.com/p/google-guice/wiki/ServletModule
 * 
 * @author Arjun Satyapal
 */
public class ServletContextListener extends GuiceServletContextListener {
  private static final Logger logger = Logger.getLogger(ServletContextListener.class.getName());
  
  @Override
  protected Injector getInjector() {
    LightServletModule guiceServletModule = new LightServletModule();
    logger.info("Initializaing : " + LightEnvEnum.getLightEnv());

    BaseGuiceModule guiceModule = null;

    switch (LightEnvEnum.getLightEnv()) {
      case PROD:
      case QA:
      case DEV_SERVER:
        guiceModule = new ProdModule();
        break;
      case UNIT_TEST:
        // For UNIT_TEST environment, ServletContext should not be called.
        //$FALL-THROUGH$
      default:
        throw new ServerConfigurationException("Unknown Environemnt with AppId = "
            + GaeUtils.getAppId());
    }

    return Guice.createInjector(guiceModule, guiceServletModule);
  }
}
