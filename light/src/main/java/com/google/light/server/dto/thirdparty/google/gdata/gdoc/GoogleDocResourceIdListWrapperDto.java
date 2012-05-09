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

import static com.google.light.server.utils.LightUtils.getWrapperValue;

import javax.xml.bind.annotation.XmlElementWrapper;

import com.google.light.server.utils.LightUtils;

import com.google.light.server.dto.pojo.typewrapper.longwrapper.CollectionId;

import com.google.common.collect.Lists;
import com.google.light.server.dto.AbstractDto;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

/**
 *
 * 
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
@JsonTypeName(value = "googleDocResourceIdList")
@XmlRootElement(name="googleDocResourceIdList")
@XmlAccessorType(XmlAccessType.FIELD)
public class GoogleDocResourceIdListWrapperDto extends AbstractDto<GoogleDocResourceIdListWrapperDto> {
  // TODO(arjuns) : See how this looks
  @XmlElementWrapper(name = "list")
  @XmlElement(name = "googleDocResourceId")
  @JsonProperty(value = "list")
  private List<String> list;

  @XmlElement(name = "collectionTitle")
  @JsonProperty(value = "collectionTitle")
  private String collectionTitle;

  @XmlElement(name = "collectionId")
  @JsonProperty(value = "collectionId")
  private Long collectionId; 
  
  public String getCollectionTitle() {
    return collectionTitle;
  }

  public CollectionId getCollectionId() {
    return new CollectionId(collectionId);
  }

  public void setCollectionTitle(String collectionTitle) {
    this.collectionTitle = collectionTitle;
  }

  public void setCollectionId(CollectionId collectionId) {
    this.collectionId = getWrapperValue(collectionId);
  }
  
  
  public boolean isEditCollection() {
    return collectionId != null;
  }
  
  public boolean isCreateCollection() {
    return !isEditCollection() && !StringUtils.isEmpty(collectionTitle);
  }
  /** 
   * {@inheritDoc}
   */
  @Override
  public GoogleDocResourceIdListWrapperDto validate() {

    return this;
  }
  
  public boolean isEmpty() {
    return LightUtils.isListEmpty(list);
  }
  
  public List<GoogleDocResourceId> getList() {
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
