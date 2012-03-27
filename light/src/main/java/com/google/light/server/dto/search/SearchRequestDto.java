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
package com.google.light.server.dto.search;

import static com.google.common.base.Preconditions.*;
import static com.google.light.server.utils.LightPreconditions.*;

import com.google.light.server.dto.DtoInterface;
import com.google.light.server.utils.QueryUtils;

/**
 * Dto to hold a search request.
 * 
 * @author Walter Cacau
 */
@SuppressWarnings("serial")
public class SearchRequestDto implements DtoInterface<SearchRequestDto> {
  /**
   * Default search page.
   * 
   * Friendly reminder: This Dto is constructed through a builder or through QueryUtils. So it is
   * important to make sure that through both ways if page is not defined it must be set to this
   * value.
   */
  private static final int DEFAULT_PAGE = 1;

  String query;
  int page = DEFAULT_PAGE;

  @Override
  public SearchRequestDto validate() {
    checkState(page > 0);
    checkNotBlank(query);
    return this;
  }

  @Override
  public String toJson() {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toXml() {
    throw new UnsupportedOperationException();
  }

  public String getQuery() {
    return query;
  }

  public void setQuery(String query) {
    this.query = query;
  }

  public int getPage() {
    return page;
  }

  public void setPage(int page) {
    this.page = page;
  }

  public static class Builder {
    private int page = DEFAULT_PAGE;
    private String query;

    public Builder query(String query) {
      this.query = query;
      return this;
    }

    public Builder page(int page) {
      this.page = page;
      return this;
    }

    public SearchRequestDto build() {
      return new SearchRequestDto(this).validate();
    }
  }

  private SearchRequestDto(Builder builder) {
    this.query = builder.query;
    this.page = builder.page;
  }

  /**
   * Constructor necessary for {@link QueryUtils} to work.
   */
  private SearchRequestDto() {
  }
}
