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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkNonEmptyList;
import static com.google.light.server.utils.LightPreconditions.checkPersonId;
import static com.google.light.server.utils.LightUtils.isListEmpty;

import org.apache.commons.lang.StringUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.light.server.dto.AbstractDto;
import com.google.light.server.dto.module.ImportDestinationDto;
import com.google.light.server.dto.pojo.longwrapper.CollectionId;
import com.google.light.server.dto.pojo.longwrapper.ModuleId;
import com.google.light.server.dto.pojo.longwrapper.PersonId;
import com.google.light.server.dto.pojo.tree.AbstractTreeNode.TreeNodeType;
import com.google.light.server.dto.pojo.tree.GoogleDocTree;
import com.google.light.server.utils.LightUtils;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
@XmlRootElement(name = "googleDocImportBatchJobContext")
@XmlAccessorType(XmlAccessType.FIELD)
public class GoogleDocImportBatchJobContext extends AbstractDto<GoogleDocImportBatchJobContext> {
  @XmlElement(name = "ownerId")
  @JsonProperty(value = "ownerId")
  private PersonId ownerId;
  
  @XmlElement(name = "resourceInfoList")
  @JsonProperty(value = "resourceInfoList")
  private List<GoogleDocInfoDto> resourceInfoList;
  
  @XmlElement(name = "root")
  @JsonProperty(value = "root")
  private GoogleDocTree root;
  
  @XmlElement(name = "listOfImportDestinations")
  @JsonProperty(value = "listOfImportDestinations")
  private List<ImportDestinationDto> listOfImportDestinations;
  
  @XmlElement(name = "state")
  @JsonProperty(value = "state")
  private GoogleDocImportBatchJobState state;
  
  @XmlElement(name = "collectionTitle")
  @JsonProperty(value = "collectionTitle")
  private String collectionTitle;

  @XmlElement(name = "collectionId")
  @JsonProperty(value = "collectionId")
  private CollectionId collectionId; 

  /**
   * {@inheritDoc}
   */
  @Override
  public GoogleDocImportBatchJobContext validate() {
    checkPersonId(ownerId);
    checkNotNull(state, "state");

    switch (state) {
      case COMPLETE:
        break;

      case ENQUEUED:
        checkNonEmptyList(resourceInfoList, "resourceInfoList");
        break;

      case BUILDING :
        break;
        
      default:
        checkNotNull(root, "root");
        checkNonEmptyList(listOfImportDestinations, "listOfImportDestinations");
    }
    return this;
  }

  public boolean isEmpty() {
    return resourceInfoList == null || resourceInfoList.size() == 0;
  }

  public List<GoogleDocInfoDto> get() {
    return resourceInfoList;
  }

  public GoogleDocTree getTreeRoot() {
    return root;
  }

  public void setTreeRoot(GoogleDocTree root) {
    Preconditions.checkArgument(root.getType() == TreeNodeType.ROOT_NODE,
        "only root nodes can be set here.");
    this.root = root;
  }

  public List<ImportDestinationDto> getListOfImportDestinations() {
    return listOfImportDestinations;
  }

  public void setListOfImportDestinations(List<ImportDestinationDto> importDestinations) {
    this.listOfImportDestinations = importDestinations;
  }

  public void addImportDestination(ImportDestinationDto importDestination) {
    if (LightUtils.isListEmpty(listOfImportDestinations)) {
      listOfImportDestinations = Lists.newArrayList();
    }

    listOfImportDestinations.add(importDestination);
  }

  public void addGoogleDocResource(GoogleDocInfoDto resourceInfo) {
    if (isEmpty()) {
      resourceInfoList = Lists.newArrayList();
    }

    resourceInfoList.add(resourceInfo);
  }

  public GoogleDocImportBatchJobState getState() {
    return state;
  }

  public void setState(GoogleDocImportBatchJobState state) {
    this.state = Preconditions.checkNotNull(state, "state");
  }

  public String getCollectionTitle() {
    return collectionTitle;
  }

  public CollectionId getCollectionId() {
    return collectionId;
  }

  public void setCollectionTitle(String collectionTitle) {
    this.collectionTitle = collectionTitle;
  }

  public void setCollectionId(CollectionId collectionId) {
    this.collectionId = collectionId;
  }
  
  
  public boolean isEditCollection() {
    return collectionId != null;
  }
  
  public boolean isCreateCollection() {
    return !isEditCollection() && !StringUtils.isEmpty(collectionTitle);
  }

  public boolean isChildJobCreated(String externalId, ModuleId moduleId) {
    if (isListEmpty(listOfImportDestinations)) {
      return false;
    }

    for (ImportDestinationDto currDest : listOfImportDestinations) {
      if (currDest.getExternalId().equals(externalId)
          && currDest.getModuleId().equals(moduleId)) {
        return true;
      }
    }

    return false;
  }
  
  public ModuleId getModuleIdForExternalId(String externalId) {
    if (LightUtils.isListEmpty(listOfImportDestinations)) {
      return null;
    }
    
    for (ImportDestinationDto currDest : listOfImportDestinations) {
      if (currDest.getExternalId().equals(externalId)) {
        return currDest.getModuleId();
      }
    }
    
    return null;
  }

  public static class Builder extends AbstractDto.BaseBuilder<Builder> {
    private PersonId ownerId;
    private List<GoogleDocInfoDto> resourceInfoList;
    private GoogleDocTree root;
    private List<ImportDestinationDto> listOfImportDestinations;
    private GoogleDocImportBatchJobState state;
    private String collectionTitle;
    private CollectionId collectionId; 

    public Builder ownerId(PersonId ownerId) {
      this.ownerId = ownerId;
      return this;
    }

    public Builder resourceInfoList(List<GoogleDocInfoDto> resourceInfoList) {
      this.resourceInfoList = resourceInfoList;
      return this;
    }

    public Builder root(GoogleDocTree root) {
      this.root = root;
      return this;
    }

    public Builder listOfImportDestinations(List<ImportDestinationDto> listOfImportDestinations) {
      this.listOfImportDestinations = listOfImportDestinations;
      return this;
    }

    public Builder state(GoogleDocImportBatchJobState state) {
      this.state = state;
      return this;
    }
    
    public Builder collectionTitle(String collectionTitle) {
      this.collectionTitle = collectionTitle;
      return this;
    }
    
    public Builder collectionId(CollectionId collectionId) {
      this.collectionId = collectionId;
      return this;
    }

    @SuppressWarnings("synthetic-access")
    public GoogleDocImportBatchJobContext build() {
      return new GoogleDocImportBatchJobContext(this).validate();
    }
  }

  @SuppressWarnings("synthetic-access")
  private GoogleDocImportBatchJobContext(Builder builder) {
    super(builder);
    this.ownerId = builder.ownerId;
    this.resourceInfoList = builder.resourceInfoList;
    this.root = builder.root;
    this.listOfImportDestinations = builder.listOfImportDestinations;
    this.state = builder.state;
    this.collectionTitle = builder.collectionTitle;
    this.collectionId = builder.collectionId;
  }

  // For JAXB
  private GoogleDocImportBatchJobContext() {
    super(null);
  }

  public static enum GoogleDocImportBatchJobState {
    BUILDING,
    ENQUEUED,
    COMPLETE;
  }
}
