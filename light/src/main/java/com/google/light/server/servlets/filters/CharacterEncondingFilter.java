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

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.google.inject.Singleton;

/**
 * Responsible to set the character encoding for all
 * servlet responses.
 * 
 * Currently it sets to UTF-8 by default.
 * 
 * TODO(waltercacau): Remove this filter and set charset in a better place.
 * Eg. the servlet's or while generating the response pojos.
 * 
 * @author Walter Cacau
 */
@Singleton
public class CharacterEncondingFilter implements Filter {

  @Override
  public void destroy() {
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
      throws IOException, ServletException {
    resp.setCharacterEncoding("UTF-8");
    chain.doFilter(req, resp);
  }

  @Override
  public void init(FilterConfig config) throws ServletException {
  }

}
