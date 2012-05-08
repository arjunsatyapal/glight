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
package com.google.light.server.dto.thirdparty.google.gdata.gdoc;

import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.collect.Lists;
import com.google.light.server.dto.AbstractDto;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;

/**
 *
 * 
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
@XmlRootElement(name="googleDocResourceIdList")
@XmlAccessorType(XmlAccessType.FIELD)
public class GoogleDocResourceIdListWrapperDto extends AbstractDto<GoogleDocResourceIdListWrapperDto> {
  @XmlElement(name = "list")
  @JsonProperty(value = "list")
  private List<String> list;

  /** 
   * {@inheritDoc}
   */
  @Override
  public GoogleDocResourceIdListWrapperDto validate() {

    return this;
  }
  
  public boolean isEmpty() {
    return list == null || list.size() == 0;
  }
  
  public List<GoogleDocResourceId> get() {
    if (isEmpty()) {
      return null;
    }
    
    List<GoogleDocResourceId> requiredList = Lists.newArrayListWithCapacity(list.size());
    
    for (String curr : list) {
      requiredList.add(new GoogleDocResourceId(curr).validate());
    }
    
    return requiredList;
  }
  
  public void addGoogleDocResource(GoogleDocResourceId googleDocResourceId) {
    if (isEmpty()) {
      list = Lists.newArrayList();
    }
    
    list.add(googleDocResourceId.getTypedResourceId());
  }
  
  public void setGoogleDocResourceList(List<GoogleDocResourceId> googleDocResourceIdList) {
    if (!isEmpty()) {
      list.clear();
    }
    
    for (GoogleDocResourceId curr : googleDocResourceIdList) {
      addGoogleDocResource(curr);
    }
  }

  public GoogleDocResourceIdListWrapperDto() {
    super(null);
  }
}
