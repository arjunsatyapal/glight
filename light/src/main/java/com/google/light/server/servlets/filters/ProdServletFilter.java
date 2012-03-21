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

import static com.google.light.server.utils.LightPreconditions.checkIsEnv;

import com.google.inject.Inject;
import com.google.light.server.constants.LightEnvEnum;
import java.io.IOException;
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
  @Inject
  private ProdServletFilter() {
    checkIsEnv(this, LightEnvEnum.PROD);
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
