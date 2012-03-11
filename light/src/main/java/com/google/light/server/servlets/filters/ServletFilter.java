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


import javax.servlet.http.HttpServletResponse;

import javax.servlet.http.HttpServletRequest;

import com.google.common.base.Throwables;
import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Servlet Filter for All Light Requests.
 * See {@link FilterPathEnum} to see what all URLs are covered by this Filter.
 * 
 * @author Arjun Satyapal
 */

public class ServletFilter implements Filter {
  private static final Logger logger = Logger.getLogger(Filter.class.getName());
  private FilterConfig filterConfig;

  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain)
      throws IOException, ServletException {
    try {
      HttpServletRequest request = (HttpServletRequest) req;
      HttpServletResponse response = (HttpServletResponse) res;
      
      // TODO(arjuns) : Add changeLog.
      filterChain.doFilter(request, response);
    } catch (Exception e) {
      // TODO(arjuns) : check log(level, message, throwable) logs whole stack.
      logger.severe("Failed due to : " + Throwables.getStackTraceAsString(e));
    } finally {
    }
  }

  public FilterConfig getFilterConfig() {
    return filterConfig;
  }

  @Override
  public void init(FilterConfig filterConfig) {
    this.filterConfig = filterConfig;
  }

  @Override
  public void destroy() {
  }
}
