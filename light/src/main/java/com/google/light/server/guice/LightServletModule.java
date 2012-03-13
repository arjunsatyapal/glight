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


import com.google.inject.Scopes;
import com.google.inject.servlet.ServletModule;
import com.google.light.server.servlets.filters.FilterPathEnum;
import com.google.light.server.servlets.path.ServletPathEnum;

/**
 * Guice Servlet Module to bind Servlets & Filters to corresponding URL Patterns.
 * 
 * @author Arjun Satyapal
 */

class LightServletModule extends ServletModule {
  @Override
  protected void configureServlets() {
    
    
    // First registering the Filters.
    // TODO(arjuns): Do special handling for TestFilter.
    for (FilterPathEnum currFilter : FilterPathEnum.values()) {
      initFilters(currFilter);
    }

    // Now registering the Servlets.
    // TODO(arjuns) : Do special handling for TestServlet.
    for (ServletPathEnum currServlet : ServletPathEnum.values()) {
      initServlet(currServlet);
    }
  }

  /**
   * Method to initialize Servlet Filters.
   */
  private void initFilters(FilterPathEnum filter) {
    bind(filter.getClazz()).in(Scopes.SINGLETON);
    filter(filter.getUrlPattern()).through(filter.getClazz());
  }

  /**
   * Function to binding of servlets to their URL Pattern under Singleton scope.
   */
  private void initServlet(ServletPathEnum servletPath) {
    bind(servletPath.getClazz()).in(Scopes.SINGLETON);
    serve(servletPath.get()).with(servletPath.getClazz());
    serve(servletPath.getRoot()).with(servletPath.getClazz());
  }
}
