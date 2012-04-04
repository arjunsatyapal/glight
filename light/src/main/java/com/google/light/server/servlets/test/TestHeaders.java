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
package com.google.light.server.servlets.test;

import static com.google.light.server.utils.LightPreconditions.checkIsNotEnv;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.light.server.constants.LightEnvEnum;
import com.google.light.server.servlets.AbstractLightServlet;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A simple servlet to show all the headers sent by the client.
 * 
 * TODO(arjuns) : Remove this class eventually.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class TestHeaders extends AbstractLightServlet {
  
  @Inject
  public TestHeaders() {
    checkIsNotEnv(this, LightEnvEnum.PROD);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response) {
    doGet(request, response);
  }

  /**
   * {@inheritDoc}
   * 
   * @throws IOException
   */
  @SuppressWarnings("unchecked")
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) {
    try {
      StringBuilder builder = new StringBuilder();
      builder.append("<br>Attributes = ");
      @SuppressWarnings("rawtypes")
      Enumeration attributes = request.getAttributeNames();
      while (attributes.hasMoreElements()) {
        Object element = attributes.nextElement();
        builder.append(element + ", ");
      }

      builder.append("<br>authType = " + request.getAuthType());
      builder.append("<br>characterEncoding = " + request.getCharacterEncoding());
      builder.append("<br>contentLength = " + request.getContentLength());
      builder.append("<br>contentType = " + request.getContentType());
      builder.append("<br>contextPath = " + request.getContextPath());

      Cookie[] cookies = request.getCookies();
      builder.append("<br>cookies = ");
      if (cookies != null) {
        for (Cookie cookie : cookies) {
          Gson gson = new GsonBuilder().setPrettyPrinting().create();
          builder.append("<br>    <pre>" + gson.toJson(cookie) + "</pre>");
        }
      } else {
        builder.append("null");
      }

      builder.append("<br>localAddr = " + request.getLocalAddr());

      builder.append("<br>locales : ");

      @SuppressWarnings("rawtypes")
      Enumeration locales = request.getLocales();
      while (locales.hasMoreElements()) {
        builder.append(locales.nextElement() + ", ");
      }

      builder.append("<br>localName = " + request.getLocalName());
      builder.append("<br>localPort = " + request.getLocalPort());
      builder.append("<br>method = " + request.getMethod());

      Map<String, String[]> paramMap = request.getParameterMap();
      builder.append("<br>ParameterMap = ");

      if (paramMap != null) {
        for (String currKey : paramMap.keySet()) {
          String[] currValueArray = paramMap.get(currKey);
          for(String currValue : currValueArray) {
            builder.append("<br>    [" + currKey + "] = [" + currValue + "]");
          }
        }
      } else {
        builder.append("null");
      }

      builder.append("<br>pathInfo = " + request.getPathInfo());
      builder.append("<br>pathTranslated = " + request.getPathTranslated());
      builder.append("<br>protocol = " + request.getProtocol());
      builder.append("<br>queryString = " + request.getQueryString());
      builder.append("<br>remoteAddr = " + request.getRemoteAddr());
      builder.append("<br>remoteHost = " + request.getRemoteHost());
      builder.append("<br>remotePort = " + request.getRemotePort());
      builder.append("<br>remoteUser = " + request.getRemoteUser());
      builder.append("<br>requestedSessionId = " + request.getRequestedSessionId());
      builder.append("<br>requestUri = " + request.getRequestURI());
      builder.append("<br>scheme = " + request.getScheme());
      builder.append("<br>serverName = " + request.getServerName());
      builder.append("<br>serverPort = " + request.getServerPort());
      builder.append("<br>servletPath = " + request.getServletPath());
      builder.append("<br>userPrincipal = " + request.getUserPrincipal());
      builder.append("<br>hashcode = " + request.hashCode());
      builder.append("<br>isRequestedSessionIdFromCookie = "
          + request.isRequestedSessionIdFromCookie());
      builder.append("<br> isRequestedSessionIdFromURL = " + request.isRequestedSessionIdFromURL());
      builder
          .append("<br>request.isRequestedSessionIdValid = " + request.isRequestedSessionIdValid());
      builder.append("<br>isSecure = " + request.isSecure());
      builder.append("<br>request.toString = " + request.toString());

      ServletInputStream is = request.getInputStream();
      String isInUTF8 = CharStreams.toString(new InputStreamReader(is, Charsets.UTF_8));
      builder.append("<br> inputStreamInUTF8 = " + isInUTF8);

      response.setContentType("text/html");
      response.getWriter().println(builder.toString());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doHead(HttpServletRequest request, HttpServletResponse response) {
    doGet(request, response);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doOptions(HttpServletRequest request, HttpServletResponse response) {
    doGet(request, response);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) {
    doGet(request, response);

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) {
    doGet(request, response);
  }
}
