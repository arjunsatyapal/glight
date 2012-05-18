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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.bind.Marshaller;

import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;
import com.google.light.server.exception.unchecked.ServerConfigurationException;
import com.google.light.server.guice.jersey.JerseyApplication;
import com.google.light.server.servlets.filters.FilterPathEnum;
import com.google.light.server.servlets.path.ServletPathEnum;
import com.google.light.server.utils.GaeUtils;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

/**
 * Guice Servlet Module to bind Servlets & Filters to corresponding URL Patterns.
 * 
 * @author Arjun Satyapal
 */

public class LightServletModule extends ServletModule {
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

    // Now registering the Servlets.
    for (ServletPathEnum currServlet : ServletPathEnum.values()) {
      if (currServlet.isAllowedInCurrEnv()) {
        initServlet(currServlet);
      }
    }

    // Initializing jersey resources.
    final Map<String, String> params = new HashMap<String, String>();
    params.put("com.sun.jersey.config.feature.DisableWADL", "true");
    params.put(Marshaller.JAXB_FORMATTED_OUTPUT, "true");
    // Without ResourceConfig.FEATURE_REDIRECT set to true, we might
    // break some links that require "/" in the end to match and
    // the client side sent the link without the trailing "/".
    // TODO(waltercacau): Add a test for this
    params.put(ResourceConfig.FEATURE_REDIRECT, "true");
    params.put("javax.ws.rs.Application", JerseyApplication.class.getName());
    serve("/rest/*").with(GuiceContainer.class, params);
  }

  /**
   * Method to initialize Servlet Filters.
   */
  private void initFilters(FilterPathEnum filter, String urlPattern) {
    /*
     * Binding the CharacterEncondingFilter here to be sure no other filter
     * will be called before it.
     * TODO(waltercacau): Remove this filter and set charset in a better place.
     * Eg. the servlet's or while generating the response pojos.
     */
    // filter("*").through(CharacterEncondingFilter.class);

    if (filter.isAllowedInCurrentEnv()) {
      bind(filter.getClazz()).in(Scopes.SINGLETON);
      logger.finest("Binding [" + urlPattern + "] to filter [" + filter.getClazz() + "].");

      filter(urlPattern).through(filter.getClazz());
    }
  }

  /**
   * Function to binding of servlets to their URL Pattern under Singleton scope.
   */
  private void initServlet(ServletPathEnum servletPath) {
    bind(servletPath.getClazz()).in(Scopes.SINGLETON);

    // First registering filters for current Servlet Path.
    for (FilterPathEnum currFilter : servletPath.getListOfFilters()) {
      initFilters(currFilter, servletPath.get());
      if(!servletPath.get().equals(servletPath.getRoot())) {
        initFilters(currFilter, servletPath.getRoot());
      }
    }

    // Now binding Servlet to correct Path.
    logger.finest("Binding [" + servletPath.get() + "] to Servlet [" + servletPath.getClazz()
        + "].");
    serve(servletPath.get()).with(servletPath.getClazz());
    if(!servletPath.get().equals(servletPath.getRoot())) {
      logger.finest("Binding [" + servletPath.getRoot() + "] to Servlet [" + servletPath.getClazz()
          + "].");
      serve(servletPath.getRoot()).with(servletPath.getClazz());
    }
  }
}
