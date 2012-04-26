/*
 * Copyright 2012 Google Inc.
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
package com.google.light.server.servlets.thirdparty.google.gdata.gdoc;

import static com.google.light.server.servlets.thirdparty.google.gdata.gdoc.GoogleDocUtils.getDocumentFeedWithFolderUrl;
import static com.google.light.server.utils.GuiceUtils.getInstance;
import static com.google.light.server.utils.ServletUtils.getRequestParameterValue;

import com.google.appengine.api.log.InvalidRequestException;
import com.google.light.server.constants.RequestParamKeyEnum;
import com.google.light.server.constants.http.ContentTypeEnum;
import com.google.light.server.dto.pages.PageDto;
import com.google.light.server.servlets.AbstractLightServlet;
import com.google.light.server.thirdparty.clients.google.gdata.gdoc.DocsServiceWrapper;
import com.google.light.server.utils.LightUtils;
import java.io.IOException;
import java.net.URL;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;

/**
 * Servlet to show information about a Google Docs ResourceId.
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class GoogleDocListServlet extends AbstractLightServlet {
  private DocsServiceWrapper docsService;

  /**
   * {@inheritDoc}
   */
  @Override
  public void service(HttpServletRequest request, HttpServletResponse response) {
    docsService = getInstance(DocsServiceWrapper.class);
    super.service(request, response);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response) {
    // TODO(arjuns): Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) {
    try {
      String pagePreviousUri = getRequestParameterValue(request, RequestParamKeyEnum.PAGE_PREVIOUS);
      String pageNextUri = getRequestParameterValue(request, RequestParamKeyEnum.PAGE_NEXT);

      if (!StringUtils.isBlank(pagePreviousUri) && !StringUtils.isBlank(pageNextUri)) {
        throw new InvalidRequestException("Both pagePreviousUr and pageNextUri should not be set.");
      }

      URL url = null;
      if (!StringUtils.isBlank(pagePreviousUri)) {
        // Get values for previous page.
        url = LightUtils.getURL(pagePreviousUri);
      } else if (!StringUtils.isBlank(pageNextUri)) {
        // Get values for next page.
        url = LightUtils.getURL(pageNextUri);
      } else {
        // Strat from page1.
        url = getDocumentFeedWithFolderUrl();
      }
      
    PageDto pageDto = docsService.getDocumentFeedWithFolders(url);
    StringBuilder builder = new StringBuilder(pageDto.toJson());
    
    response.setContentType(ContentTypeEnum.TEXT_PLAIN.get());
    response.getWriter().println(builder.toString());
    } catch (IOException e) {
      // TODO(arjuns): add exception handling.
      throw new RuntimeException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doHead(HttpServletRequest request, HttpServletResponse response) {
    // TODO(arjuns): Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doOptions(HttpServletRequest request, HttpServletResponse response) {
    // TODO(arjuns): Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) {
    // TODO(arjuns): Auto-generated method stub
    throw new UnsupportedOperationException();  }

  /**
   * {@inheritDoc}
   * 
   */
  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) {
    // TODO(arjuns): Auto-generated method stub
    throw new UnsupportedOperationException();
  }
}
