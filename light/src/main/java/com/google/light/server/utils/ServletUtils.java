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
package com.google.light.server.utils;

import static com.google.light.server.constants.RequestParamKeyEnum.DEFAULT_EMAIL;
import static com.google.light.server.constants.RequestParamKeyEnum.LOGIN_PROVIDER_ID;
import static com.google.light.server.constants.RequestParamKeyEnum.LOGIN_PROVIDER_USER_ID;
import static com.google.light.server.constants.RequestParamKeyEnum.PERSON_ID;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;

import com.google.light.server.dto.pojo.longwrapper.PersonId;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.io.CharStreams;
import com.google.light.server.constants.HttpHeaderEnum;
import com.google.light.server.constants.LightConstants;
import com.google.light.server.constants.OAuth2ProviderService;
import com.google.light.server.constants.RequestParamKeyEnum;
import com.google.light.server.constants.http.ContentTypeEnum;
import com.google.light.server.dto.AbstractDto;
import com.google.light.server.servlets.path.ServletPathEnum;
import com.google.light.server.servlets.pojo.ServletRequestPojo;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.bind.JAXBException;

/**
 * Utility methods for Servlets and Servlet Environment.
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class ServletUtils {
  private static final Logger logger = Logger.getLogger(ServletUtils.class.getName());
  

  /**
   * Forwards request and response to given path. Handles any
   * exceptions caused by forward target by printing them to logger.
   * 
   * @param request
   * @param response
   * @param path
   */
  public static void forward(final HttpServletRequest request,
          final HttpServletResponse response, final String path) {
      try {
          final RequestDispatcher rd = request.getRequestDispatcher(path);
          rd.forward(request, response);
      }
      catch (Throwable tr) {
          // TODO(waltercacau): Add a proper handler here. (Show a 404 or 50 error page for example)
        logger.severe("Failed due to : " + Throwables.getStackTraceAsString(tr));
      }
  }
  

  /**
   * Returns the URL along-with Query Parameters. Default {@link HttpServletRequest#getRequestURL()}
   * does not return query parameters.
   * 
   * e.g. If the URL is http://example.com/servlet?key=value
   * {@link HttpServletRequest#getRequestURL()} returns http://example.com/servlet.
   * <p>
   * This method will return complete URL.
   * 
   * @param request
   * @return
   */
  public static String getRequestUriWithQueryParams(HttpServletRequest request) {
    StringBuffer buf = request.getRequestURL();
    if (request.getQueryString() != null) {
      buf.append('?').append(request.getQueryString());
    }

    return buf.toString();
  }

  /**
   * This methods returns the Server URL alongwith Scheme, version etc.
   * 
   * e.g. If the URL is http://1.example.com/servlet?key=value, then this method will return
   * http://1.example.com.
   * 
   * @param request
   * @return
   */
  public static String getServerUrl(HttpServletRequest request) {
    String requestUrl = request.getRequestURL().toString();
    String requestUri = request.getRequestURI();

    int uriIndex = requestUrl.indexOf(requestUri);

    return requestUrl.substring(0, uriIndex);
  }

  /**
   * Get RequestPojo from HttpServlet Request.
   * 
   * TODO(arjuns): Add test for this.
   * 
   * D : DTO.
   * 
   * @param request
   * @return
   * @throws IOException
   * @throws JAXBException
   */
  public static <D extends AbstractDto<D>> ServletRequestPojo<D> getRequestPojo(
      HttpServletRequest request, Class<D> clazz)
      throws IOException, JAXBException {
    String reqContentType = request.getContentType();
    ContentTypeEnum contentType = ContentTypeEnum.getContentTypeByString(reqContentType);

    String reqEntityString = CharStreams.toString(new InputStreamReader(
        request.getInputStream(), Charsets.UTF_8));
    checkNotBlank(reqEntityString, "reqEntityString");

    return new ServletRequestPojo<D>(request, contentType, reqEntityString, clazz);
  }

  /**
   * Returns complete url for a Servlet.
   * TODO(arjuns): "Add test for this.
   * 
   * @param serverUrl
   * @param servletPath
   * @return
   */
  public static String getServletUrl(HttpServletRequest request, ServletPathEnum servletPath) {
    return getServerUrl(request) + servletPath.get();
  }
  
  /**
   * TODO(arjuns): Add test for this.
   * Returns value of a RequestParam.
   */
  public static String getRequestParameterValue(
      HttpServletRequest request, RequestParamKeyEnum key) {
    return request.getParameter(key.get());
  }
  
  /**
   * TODO(arjuns): Add test for this.
   * Returns value of a Header.
   */
  public static String getRequestHeaderValue(HttpServletRequest request, HttpHeaderEnum key) {
    return request.getHeader(key.get());
  }
  
  /**
   * Get session from HttpServletRequest. If session does not exist it will create a new session
   * 
   * TODO(arjuns): Add test for this.
   * @param request
   * @return
   */
  public static HttpSession getSession(HttpServletRequest request) {
    HttpSession session = request.getSession(true);
    session.setMaxInactiveInterval(LightConstants.SESSION_MAX_INACTIVITY_PERIOD);
    return session;
  }
  
  /**
   * Invalidating existing session.
   * TODO(arjuns): Add test for this.
   * @param request
   */
  public static void invalidateSession(HttpServletRequest request) {
    request.getSession().invalidate();
  }
  
  /**
   * Prepare Session with reuired Parameters.
   * TODO(arjuns) : move test .
   * @param request
   * @param loginProvider
   * @param personId
   * @param providerUserId
   * @param defaultEmail
   */
  public static void prepareSession(HttpServletRequest request, OAuth2ProviderService loginProvider,
      @Nullable PersonId personId, String providerUserId, String defaultEmail) {
    synchronized (request) {
      // Invalidating existing sessions, and creating new session.
      HttpSession session = getSession(request);
    
      logger.info("Prepairing session with provider[" + loginProvider 
          + ", providerUserId[" + personId
          + "], providerUserEmail[" + defaultEmail + "].");
      session.setAttribute(LOGIN_PROVIDER_ID.get(), loginProvider.name());
      session.setAttribute(PERSON_ID.get(), personId);
      session.setAttribute(DEFAULT_EMAIL.get(), defaultEmail);
      session.setAttribute(LOGIN_PROVIDER_USER_ID.get(), providerUserId);
    }
  }
  
  // Utility Method
  private ServletUtils() {
  }

  /**
   * Set's the response headers necessary to deactivate browsers caching.
   * 
   * @param resp
   */
  public static void avoidCaching(HttpServletResponse resp) {
    resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1
    resp.setHeader("Pragma", "no-cache"); // HTTP 1.0
    resp.setDateHeader("Expires", 0); // Proxies
  }
}
