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
package com.google.light.server.servlets.filters;

import com.google.inject.Inject;
import com.google.light.server.exception.unchecked.FilterInstanceBindingException;
import com.google.light.server.utils.GaeUtils;
import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Servlet Filter for All Light Requests. See {@link FilterPathEnum} to see what all URLs are
 * covered by this Filter.
 * 
 * @author Arjun Satyapal
 */
public class ProdServletFilter extends AbstractLightFilter {
  private static final Logger logger = Logger.getLogger(Filter.class.getName());

  @Inject
  private ProdServletFilter() {
    if (!GaeUtils.isProductionServer()) {
      String msg = "ProdServletFilter should not be injected for non-production environments.";
      logger.severe(msg);
      throw new FilterInstanceBindingException(msg);
    }
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {
    // TODO(arjuns): If user is logged in and tries to visit a cached URL without going through
    // login flow, it may be an issue. Will fix that once we have some more infrastructure around
    // login.
    handleFilterChain(request, response, filterChain);
  }
}
