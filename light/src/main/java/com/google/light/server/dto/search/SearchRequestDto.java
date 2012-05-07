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

import static com.google.common.base.Preconditions.checkState;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlElement;

import javax.xml.bind.annotation.XmlRootElement;

import com.google.light.server.constants.LightConstants;
import com.google.light.server.dto.AbstractDto;

/**
 * Dto to hold a search request.
 * 
 * @author Walter Cacau
 */
@SuppressWarnings("serial")
@XmlRootElement(name="search_request")
@XmlAccessorType(XmlAccessType.FIELD)
public class SearchRequestDto extends AbstractDto<SearchRequestDto> {
  /**
   * User's search query
   */
  @XmlElement
  private String query;

  /**
   * User's languageCode
   */
  @XmlElement(name = "client_language_code")
  @JsonProperty(value = "client_language_code")
  private String clientLanguageCode;

  /**
   * Search page number.
   * 
   * This attribute controls the search results range the user is requesting
   * to see. Value {@link LightConstants#FIRST_SEARCH_PAGE_NUMBER} means the results 1 to 10,
   * {@link LightConstants#FIRST_SEARCH_PAGE_NUMBER}+1 means results 11 to 20, ...
   * 
   * Friendly reminder: This Dto is constructed through a builder or through QueryUtils. So it is
   * important to make sure that through both ways if page is not defined it must be set to this
   * value.
   * 
   * TODO(waltercacau) : move search under third_party.
   */
  // TODO(waltercacau): Check with arjun about renaming this variable (issue 39002)
  @XmlElement
  private int page = LightConstants.FIRST_SEARCH_PAGE_NUMBER;

  @Override
  public SearchRequestDto validate() {
    checkState(page > 0, "Page number need to be greater then 0");
    checkNotBlank(query, "query");
    checkNotBlank(clientLanguageCode, "clientLanguageCode");
    return this;
  }

  public String getQuery() {
    return query;
  }

  // TODO(arjuns) : See if this is required.
  public void setQuery(String query) {
    this.query = query;
  }

  public int getPage() {
    return page;
  }

  public void setPage(int page) {
    this.page = page;
  }

  public String getClientLanguageCode() {
    return clientLanguageCode;
  }

  public void setClientLanguageCode(String clientLanguageCode) {
    this.clientLanguageCode = clientLanguageCode;
  }

  public static class Builder extends AbstractDto.BaseBuilder<Builder> {
    private String query;
    private String clientLanguageCode;
    private int page = LightConstants.FIRST_SEARCH_PAGE_NUMBER;

    public Builder query(String query) {
      this.query = query;
      return this;
    }

    public Builder clientLanguageCode(String clientLanguageCode) {
      this.clientLanguageCode = clientLanguageCode;
      return this;
    }

    public Builder page(int page) {
      this.page = page;
      return this;
    }

    @SuppressWarnings("synthetic-access")
    public SearchRequestDto build() {
      return new SearchRequestDto(this).validate();
    }
  }

  @SuppressWarnings("synthetic-access")
  private SearchRequestDto(Builder builder) {
    super(builder);
    this.query = builder.query;
    this.clientLanguageCode = builder.clientLanguageCode;
    this.page = builder.page;
  }

  /**
   * Constructor necessary for {@link QueryUtils} to work.
   */
  private SearchRequestDto() {
    super(null);
  }
}
