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

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.light.server.exception.unchecked.FilterInstanceBindingException;
import com.google.light.server.servlets.path.ServletPathEnum;
import com.google.light.server.utils.GaeUtils;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Logger;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Servlet Filter for QA and Test environment.
 * 
 * @author Arjun Satyapal
 */

public class TestServletFilter extends AbstractLightFilter {
  private static final Logger logger = Logger.getLogger(Filter.class.getName());
  @Inject
  private TestServletFilter() {
    if (GaeUtils.isProductionServer()) {
      String msg = "TestServletFilter should be injected for Test&QA environment only.";
      logger.severe(msg);
      throw new FilterInstanceBindingException(msg);
    }
  }
  /** 
   * {@inheritDoc}
   */
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {
    handleFilterChain(request, response, filterChain);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  Set<ServletPathEnum> getSkipServletSet() {
    Set<ServletPathEnum> set = Sets.newHashSet(
        ServletPathEnum.CONFIG, // Admin servlets rely on Gae Environment. So are ignored.
        ServletPathEnum.LOGIN, // Creates session. So have to be ignored.
        ServletPathEnum.FAKE_LOGIN // Creates session. So have to be ignored.
        );
    
    return set;
  }
}
