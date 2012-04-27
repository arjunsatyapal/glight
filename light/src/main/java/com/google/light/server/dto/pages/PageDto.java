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

import com.google.light.server.dto.AbstractDto;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
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
  @XmlElement(name="handler-uri")
  private String handlerUri;
  
  @XmlElement(name="start-index")
  private String startIndex;
  
  @SuppressWarnings("rawtypes")
  @XmlElement private List<? extends AbstractDto> list;

  /** 
   * {@inheritDoc}
   */
  @Override
  public PageDto validate() {
    checkNotNull(handlerUri, "handlerUri");
    
    return this;
  }
  
  public String getStartIndex() {
    return startIndex;
  }

  @SuppressWarnings("rawtypes")
  public List<? extends AbstractDto> getList() {
    return list;
  }

  public static class Builder extends AbstractDto.BaseBuilder<Builder>{
    private String handlerUri;
    private String startIndex;
    @SuppressWarnings("rawtypes")
    private List<? extends AbstractDto> list;

    public Builder handlerUri(String handlerUri) {
      this.handlerUri = handlerUri;
      return this;
    }
    
    public Builder startIndex(String startIndex) {
      this.startIndex = startIndex;
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
    this.handlerUri = builder.handlerUri;
    this.startIndex = builder.startIndex;
    this.list = builder.list;
  }
  
  // For JAXB
  @JsonCreator
  private PageDto() {
    super(null);
  }
}
