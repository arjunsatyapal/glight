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

import static com.google.light.server.utils.LightPreconditions.checkNotBlank;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.light.server.constants.ContentTypeEnum;
import com.google.light.server.dto.DtoInterface;
import com.google.light.server.servlets.pojo.ServletRequestPojo;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;

/**
 * Utility methods for Servlets and Servlet Environment.
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class ServletUtils {
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
  public static <D extends DtoInterface<D>> ServletRequestPojo<D> getRequestPojo(
      HttpServletRequest request, Class<D> clazz)
      throws IOException, JAXBException {
    String reqContentType = request.getContentType();
    ContentTypeEnum contentType = ContentTypeEnum.getContentTypeByString(reqContentType);

    String reqEntityString = CharStreams.toString(new InputStreamReader(
        request.getInputStream(), Charsets.UTF_8));
    checkNotBlank(reqEntityString);

    return new ServletRequestPojo<D>(request, contentType, reqEntityString, clazz);
  }

  // Utility Method
  private ServletUtils() {
  }
}
