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

import com.google.light.server.constants.LightStringConstants;
import com.google.light.server.dto.AbstractDto;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
@XmlRootElement(name = "pageDto")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonSerialize(include = Inclusion.NON_NULL)
public class PageDto extends AbstractDto<PageDto> {
  @XmlTransient private String lightUri;
  @XmlElement private String previous;
  @XmlElement private String next;
  @SuppressWarnings("rawtypes")
  @XmlElement private List<? extends AbstractDto> list;

  /** 
   * {@inheritDoc}
   */
  @Override
  public PageDto validate() {
    checkNotNull(lightUri, "lightUri");
    
    // Updating previous and next Links.
    if (previous != null) {
      previous = getUpdatedUri(lightUri, previous);
    }
    
    if (next != null) {
      next = getUpdatedUri(lightUri, next);
    }
    
    return this;
  }
  
  private String getUpdatedUri(String lightUri, String pageUri) {
    String uri = lightUri + "?" + LightStringConstants.START_INDEX_STR + "=" + pageUri;
    return uri;
  }
  

  public String getPrevious() {
    return previous;
  }

  public String getNext() {
    return next;
  }

  @SuppressWarnings("rawtypes")
  public List<? extends AbstractDto> getList() {
    return list;
  }

  public static class Builder extends AbstractDto.BaseBuilder<Builder>{
    private String lightUri;
    private String previous;
    private String next;
    @SuppressWarnings("rawtypes")
    private List<? extends AbstractDto> list;

    public Builder lightUri(String lightUri) {
      this.lightUri = lightUri;
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
    this.lightUri = builder.lightUri;
    this.previous = builder.previous;
    this.next = builder.next;
    this.list = builder.list;
  }
  
  // For JAXB
  @JsonCreator
  private PageDto() {
    super(null);
  }
}
