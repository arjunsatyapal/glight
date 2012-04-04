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
package com.google.light.server.servlets.search;

import static com.google.light.server.utils.LightUtils.wrapIntoRuntimeExceptionAndThrow;
import static com.google.common.base.Preconditions.checkNotNull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.light.server.constants.ContentTypeEnum;
import com.google.light.server.dto.search.SearchRequestDto;
import com.google.light.server.dto.search.SearchResultDto;
import com.google.light.server.exception.unchecked.httpexception.MethodNotAllowedException;
import com.google.light.server.manager.interfaces.SearchManager;
import com.google.light.server.servlets.AbstractLightServlet;
import com.google.light.server.servlets.pojo.ServletResponsePojo;
import com.google.light.server.utils.QueryUtils;

/**
 * Servlet to handle search requests
 * 
 * @author Walter Cacau
 */
@SuppressWarnings("serial")
public class SearchServlet extends AbstractLightServlet {

  private Provider<SearchManager> searchManagerProvider;

  @Inject
  public SearchServlet(Provider<SearchManager> searchManagerProvider) {
    this.searchManagerProvider = checkNotNull(searchManagerProvider);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response) {
    throw new MethodNotAllowedException("For Search, delete is not allowed.");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) {
    SearchRequestDto searchRequest = QueryUtils.getValidDto(request, SearchRequestDto.class);

    try {
      SearchResultDto searchResultDto = searchManagerProvider.get().search(searchRequest);
      ServletResponsePojo<SearchResultDto> responsePojo =
          new ServletResponsePojo<SearchResultDto>(
              response, ContentTypeEnum.getContentTypeByString(request.getContentType()),
              searchResultDto);

      responsePojo.logAndReturn();
    } catch (Exception e) {
      wrapIntoRuntimeExceptionAndThrow(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doHead(HttpServletRequest request, HttpServletResponse response) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doOptions(HttpServletRequest request, HttpServletResponse response) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) {
    throw new UnsupportedOperationException();
  }
}
