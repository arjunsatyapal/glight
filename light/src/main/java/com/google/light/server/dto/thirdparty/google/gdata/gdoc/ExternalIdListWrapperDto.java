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

import static com.google.light.server.utils.LightUtils.getWrapper;
import static com.google.light.server.utils.LightUtils.getWrapperValue;
import static com.google.light.server.utils.LightUtils.isListEmpty;

import com.google.common.collect.Lists;
import com.google.light.server.dto.AbstractDto;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.CollectionId;
import com.google.light.server.dto.pojo.typewrapper.stringwrapper.ExternalId;
import com.google.light.server.exception.unchecked.httpexception.BadRequestException;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
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
@XmlRootElement(name = "googleDocResourceIdList")
@XmlAccessorType(XmlAccessType.FIELD)
public class ExternalIdListWrapperDto extends
    AbstractDto<ExternalIdListWrapperDto> {
  // TODO(arjuns) : See how this looks
  @XmlElementWrapper(name = "list")
  @XmlElement(name = "item")
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
    return getWrapper(collectionId, CollectionId.class);
  }

  public void setCollectionTitle(String collectionTitle) {
    this.collectionTitle = collectionTitle;
  }

  public void setCollectionId(CollectionId collectionId) {
    this.collectionId = getWrapperValue(collectionId);
  }

  public GoogleDocImportBatchType getGoogleDocImportBatchType() {
    if (collectionId != null) {
      // User wants to apppend current list to the existing collection.
      return GoogleDocImportBatchType.EDIT_COLLECTION_JOB;
    } else if (StringUtils.isNotBlank(collectionTitle)) {
      // CollectionId is null, but title is there. This means create new collection.
      return GoogleDocImportBatchType.CREATE_COLLECTION_JOB;
    }

    if (listContainsFolder()) {
      throw new BadRequestException(
          "Request contains a folder, and folders need to be part of a collection. So either "
              + "provide a CollectionId where it can be imported or Provide a collectionTitle "
              + "for new Collection.");
    }
    
    return GoogleDocImportBatchType.MODULE_JOB;
  }

  private boolean listContainsFolder() {
    for (String curr : list) {
      ExternalId externalId = new ExternalId(curr);
      if (externalId.getModuleType().mapsToCollection()) {
        return true;
      }
    }

    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ExternalIdListWrapperDto validate() {

    return this;
  }

  public boolean isEmpty() {
    return isListEmpty(list);
  }

  public List<ExternalId> getList() {
    if (isEmpty()) {
      return null;
    }

    List<ExternalId> requiredList = Lists.newArrayListWithCapacity(list.size());

    for (String curr : list) {
      requiredList.add(new ExternalId(curr).validate());
    }

    return requiredList;
  }

  public void addGoogleDocResource(GoogleDocResourceId googleDocResourceId) {
    if (isEmpty()) {
      list = Lists.newArrayList();
    }

    list.add(googleDocResourceId.getTypedResourceId());
  }

  public ExternalIdListWrapperDto() {
    super(null);
  }
}
