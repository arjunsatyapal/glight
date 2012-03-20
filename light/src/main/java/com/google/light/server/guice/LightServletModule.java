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

import com.google.light.server.exception.unchecked.ServerConfigurationException;

import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;
import com.google.light.server.servlets.filters.FilterPathEnum;
import com.google.light.server.servlets.path.ServletPathEnum;
import com.google.light.server.utils.GaeUtils;
import java.util.logging.Logger;

/**
 * Guice Servlet Module to bind Servlets & Filters to corresponding URL Patterns.
 * 
 * @author Arjun Satyapal
 */

class LightServletModule extends ServletModule {
  private static final Logger logger = Logger.getLogger(LightServletModule.class.getName());

  @Override
  protected void configureServlets() {
    // One of the env should be true.
    boolean knownEnv = GaeUtils.isDevServer()
        || GaeUtils.isProductionServer()
        || GaeUtils.isQaServer()
        || GaeUtils.isUnitTestServer();
    
    if (!knownEnv) {
      throw new ServerConfigurationException("Unknown AppId : " + GaeUtils.getAppId());
    }
    
    // First registering the Filters.
    boolean isProduction = GaeUtils.isProductionServer(); 
    if (isProduction) {
      initFilters(FilterPathEnum.API);
    } else {
      initFilters(FilterPathEnum.TEST);
    }

    // Now registering the Servlets.
    // TODO(arjuns) : Do special handling for TestServlet.
    for (ServletPathEnum currServlet : ServletPathEnum.values()) {
      // Will skip Servlets which are only for test.
      if (isProduction && currServlet.isOnlyForTest()) {
        continue;
      }
      initServlet(currServlet);
    }
  }

  /**
   * Method to initialize Servlet Filters.
   */
  private void initFilters(FilterPathEnum filter) {
    bind(filter.getClazz()).in(Scopes.SINGLETON);
    for (String currPattern : filter.getUrlPatterns()) {
      logger.info("Binding [" + currPattern + "] to filter [" + filter.getClazz() + "].");
      filter(currPattern).through(filter.getClazz());
    }
  }

  /**
   * Function to binding of servlets to their URL Pattern under Singleton scope.
   */
  private void initServlet(ServletPathEnum servletPath) {
    bind(servletPath.getClazz()).in(Scopes.SINGLETON);
    logger.info("Binding [" + servletPath.get() + "] to Servlet [" + servletPath.getClazz() + "].");
    serve(servletPath.get()).with(servletPath.getClazz());
    logger.info("Binding [" + servletPath.getRoot() + "] to Servlet [" + servletPath.getClazz()
        + "].");
    serve(servletPath.getRoot()).with(servletPath.getClazz());
  }
}
