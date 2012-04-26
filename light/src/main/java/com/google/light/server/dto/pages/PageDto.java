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
package com.google.light.server.dto.pages;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Preconditions;
import com.google.light.server.constants.RequestParamKeyEnum;
import com.google.light.server.dto.AbstractDto;
import com.google.light.server.servlets.path.ServletPathEnum;
import java.util.List;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class PageDto extends AbstractDto<PageDto> {
  private ServletPathEnum servletPath;
  private String previous;
  private String next;
  private String type;
  @SuppressWarnings("rawtypes")
  private List<? extends AbstractDto> list;

  /** 
   * {@inheritDoc}
   */
  @Override
  public PageDto validate() {
    checkNotNull(servletPath, "servletPath");
    
    // Updating previous and next Links.
    if (previous != null) {
      previous = getUpdatedUri(servletPath, previous, RequestParamKeyEnum.PAGE_PREVIOUS);
    }
    
    if (next != null) {
      next = getUpdatedUri(servletPath, next, RequestParamKeyEnum.PAGE_NEXT);
    }
    
    return this;
  }
  
  private String getUpdatedUri(ServletPathEnum servletPath, String pageUri, 
      RequestParamKeyEnum requestParam) {
    Preconditions.checkArgument(requestParam == RequestParamKeyEnum.PAGE_NEXT
        || requestParam == RequestParamKeyEnum.PAGE_PREVIOUS);
    String uri = servletPath.get() + "?" + requestParam.get() + "=" + pageUri;
    return uri;
  }
  

  public String getPrevious() {
    return previous;
  }

  public String getNext() {
    return next;
  }

  public String getType() {
    return type;
  }

  @SuppressWarnings("rawtypes")
  public List<? extends AbstractDto> getList() {
    return list;
  }

  public static class Builder extends AbstractDto.BaseBuilder<Builder>{
    private ServletPathEnum servletPath;
    private String previous;
    private String next;
    private String type;
    @SuppressWarnings("rawtypes")
    private List<? extends AbstractDto> list;

    public Builder serlvetPath(ServletPathEnum servletPath) {
      this.servletPath = checkNotNull(servletPath, "servletPath");
      return this;
    }
    
    
    public Builder previous(String previous) {
      this.previous = previous;
      return this;
    }

    public Builder next(String next) {
      this.next = next;
      return this;
    }

    public Builder type(String type) {
      this.type = type;
      return this;
    }

    @SuppressWarnings("rawtypes")
    public Builder list(List<? extends AbstractDto> list) {
      this.list = list;
      return this;
    }

    @SuppressWarnings("synthetic-access")
    public PageDto build() {
      return new PageDto(this).validate();
    }
  }

  @SuppressWarnings("synthetic-access")
  private PageDto(Builder builder) {
    super(builder);
    this.servletPath = builder.servletPath;
    this.previous = builder.previous;
    this.next = builder.next;
    this.type = builder.type;
    this.list = builder.list;
  }
}
