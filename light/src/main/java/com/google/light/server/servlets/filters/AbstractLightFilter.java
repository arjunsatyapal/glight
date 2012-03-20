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

import static com.google.light.server.utils.GuiceUtils.seedEntityInRequestScope;
import static com.google.light.server.utils.LightPreconditions.checkPersonIsGaeAdmin;

import com.google.common.base.Throwables;
import com.google.light.server.annotations.AnotHttpSession;
import com.google.light.server.exception.unchecked.httpexception.PersonLoginRequiredException;
import com.google.light.server.servlets.path.ServletPathEnum;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Logger;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet Filter for All Light Requests. See {@link FilterPathEnum} to see what all URLs are
 * covered by this Filter. TODO(arjuns) : Add test .
 * 
 * @author Arjun Satyapal
 */

public abstract class AbstractLightFilter implements Filter {
  private static final Logger logger = Logger.getLogger(Filter.class.getName());
  private FilterConfig filterConfig;

  public void validateAndDoFilter(HttpSession session, ServletRequest req, ServletResponse res,
      FilterChain filterChain, ServletPathEnum servletPath) {
    try {
      HttpServletRequest request = (HttpServletRequest) req;
      HttpServletResponse response = (HttpServletResponse) res;

      /*
       * For Admin Privileges, we depend on GAE. So even if on ServletPathEnum, we mandate that for
       * requireAdminPrivilege, requireLogin should be true (as that is conceptually) correct. And
       * infact will work fine on real AppEngine. But in testing world, we are using FakeLogins. So
       * essentially at the time of login, session is established, which will be used for detecting
       * login. But for detecting whether User is Admin, we depend on AppEngine. In order to make
       * testing easier, we first check if Admin Privileges is required. If that is true, then we
       * ask GAE if user is logged in. And if UserIs logged in, then without checking the session,
       * we allow the user to visit those URLs. This makes testing easier, as it reduces one
       * additional step of Logging in and creating Session which is of no use for Servlets that
       * require Admin Privileges.
       */
      if (servletPath.isRequiredAdminPrivilege()) {
        checkPersonIsGaeAdmin();
      } else if (servletPath.isRequiredLogin()) {
        if (session == null) {
          throw new PersonLoginRequiredException("");
        } else if (session.isNew()) {
          session.invalidate();
          throw new PersonLoginRequiredException(
              "Session should not be created inside AbstractLightFiter.");
        } else {
          seedEntityInRequestScope(request, HttpSession.class, AnotHttpSession.class, session);
        }
      }

      // TODO(arjuns) : Add changeLog.
      filterChain.doFilter(request, response);
    } catch (Exception e) {
      // TODO(arjuns) : check log(level, message, throwable) logs whole stack.
      logger.severe("Failed due to : " + Throwables.getStackTraceAsString(e));
    }
  }

  /**
   * Tells whether parent filter should be ignored and instead directly go to the filter chain.
   * Since TestFilter covers all the URLs, this method will be used to exclude few Servlets from
   * TestServletFilter.
   */
  boolean skipSessionRequirements(Set<ServletPathEnum> ignoreSet, String requestUri) {
    for (ServletPathEnum currEnum : ignoreSet) {
      if (requestUri.startsWith(currEnum.get())) {
        return true;
      }
    }

    return false;
  }

  /**
   * This method checks if Login is required or not. If login is required, then request is forwarded
   * to {@link #validateAndDoFilter(ServletRequest, ServletResponse, FilterChain)} else it will be
   * handed over to {@link super#doFilter(ServletRequest, ServletResponse, FilterChain)}.
   * 
   * @param request
   * @param response
   * @param filterChain
   * @throws IOException
   * @throws ServletException
   */
  void handleFilterChain(ServletRequest request, ServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) request;

    ServletPathEnum servletPath = ServletPathEnum.getServletPathEnum(req.getRequestURI());

    if (servletPath.isRequiredLogin()) {
      validateAndDoFilter(req.getSession(false), req, response, filterChain, servletPath);
    } else {
      filterChain.doFilter(request, response);
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
