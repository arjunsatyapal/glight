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
import static com.google.light.server.utils.LightUtils.isCollectionEmpty;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.light.server.annotations.OverrideFieldAnnotationName;
import com.google.light.server.constants.PlacementOrder;
import com.google.light.server.dto.AbstractDto;
import com.google.light.server.dto.importresource.ImportBatchType;
import com.google.light.server.dto.importresource.ImportExternalIdDto;
import com.google.light.server.dto.pojo.tree.AbstractTreeNode.TreeNodeType;
import com.google.light.server.dto.pojo.tree.GoogleDocTree;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.CollectionId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.ModuleId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.PersonId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.Version;
import com.google.light.server.dto.pojo.typewrapper.stringwrapper.ExternalId;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
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
@JsonTypeName(value = "googleDocImportBatchJobContext")
@XmlRootElement(name = "googleDocImportBatchJobContext")
@XmlAccessorType(XmlAccessType.FIELD)
public class GoogleDocImportBatchJobContext extends AbstractDto<GoogleDocImportBatchJobContext> {
  @XmlElement(name = "ownerId")
  @JsonProperty(value = "ownerId")
  private PersonId ownerId;

  @XmlElementWrapper(name = "resourceInfoList")
  @XmlAnyElement
  @JsonProperty(value = "resourceInfoList")
  private List<GoogleDocInfoDto> resourceInfoList;

  @XmlElement(name = "root")
  @JsonProperty(value = "root")
  private GoogleDocTree root;

  @XmlElementWrapper(name = "listOfImportDestinations")
  @XmlElement(name = "importDestination")
  @JsonProperty(value = "listOfImportDestinations")
  private List<ImportExternalIdDto> listOfImportDestinations;

  @OverrideFieldAnnotationName(value = "To deserialize properly, need to know type of State.")
  @XmlElement(name = "googleDocImportBatchJobState")
  @JsonProperty(value = "googleDocImportBatchJobState")
  private GoogleDocImportBatchJobState state;

  @XmlElement(name = "collectionTitle")
  @JsonProperty(value = "collectionTitle")
  private String collectionTitle;

  @OverrideFieldAnnotationName(value = "To deserialize properly, need to know type of State.")
  @XmlElement(name = "googleDocImportBatchJobType")
  @JsonProperty(value = "googleDocImportBatchJobType")
  private ImportBatchType type;

  @XmlElement(name = "collectionId")
  @JsonProperty(value = "collectionId")
  private CollectionId collectionId;

  @XmlElement(name = "version")
  @JsonProperty(value = "version")
  private Version version;

  /**
   * {@inheritDoc}
   */
  @Override
  public GoogleDocImportBatchJobContext validate() {
    checkPersonId(getOwnerId());
    checkNotNull(state, "state");
    checkNotNull(type, "type");

    switch (state) {
      case COMPLETE:
        break;

      case COLLECTION_VERSION_PUBLISHED:
        checkNotNull(version, "version");
        //$FALL-THROUGH$

      case COLLECTION_CREATED:
        checkNotNull(collectionId, "collectionId");
        //$FALL-THROUGH$

      case ENQUEUED:
        checkNonEmptyList(resourceInfoList, "resourceInfoList");
        break;

      case BUILDING:
        break;

      default:
        checkNotNull(root, "root");
        checkNonEmptyList(listOfImportDestinations, "listOfImportDestinations");
    }
    return this;
  }

  public PersonId getOwnerId() {
    return ownerId;
  }

  public List<GoogleDocInfoDto> getResourceInfoList() {
    return resourceInfoList;
  }

  public GoogleDocTree getRoot() {
    return root;
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
    Preconditions.checkArgument(root.getNodeType() == TreeNodeType.ROOT_NODE,
        "only root nodes can be set here.");
    this.root = root;
  }

  public List<ImportExternalIdDto> getListOfImportDestinations() {
    return listOfImportDestinations;
  }

  public void setListOfImportDestinations(List<ImportExternalIdDto> importDestinations) {
    this.listOfImportDestinations = importDestinations;
  }

  public void addImportDestination(ImportExternalIdDto importDestination) {
    if (isCollectionEmpty(listOfImportDestinations)) {
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

  public ImportBatchType getType() {
    return type;
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

  public Version getVersion() {
    return version;
  }

  public void setVersion(Version version) {
    this.version = version;
  }

  public boolean isChildJobCreated(ExternalId externalId) {
    if (isCollectionEmpty(listOfImportDestinations)) {
      return false;
    }

    for (ImportExternalIdDto currDest : listOfImportDestinations) {
      if (currDest.getExternalId().equals(externalId)) {
        return true;
      }
    }

    return false;
  }

  public ModuleId getModuleIdForExternalId(ExternalId externalId) {
    if (isCollectionEmpty(listOfImportDestinations)) {
      return null;
    }

    for (ImportExternalIdDto currDest : listOfImportDestinations) {
      if (currDest.getExternalId().equals(externalId)) {
        return currDest.getModuleId();
      }
    }

    return null;
  }

  public void setType(ImportBatchType type) {
    this.type = type;
  }

  public static class Builder extends AbstractDto.BaseBuilder<Builder> {
    private PersonId ownerId;
    private List<GoogleDocInfoDto> resourceInfoList;
    private GoogleDocTree root;
    private List<ImportExternalIdDto> listOfImportDestinations;
    private GoogleDocImportBatchJobState state;
    private String collectionTitle;
    private ImportBatchType type;
    private CollectionId collectionId;
    private Version version;

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

    public Builder listOfImportDestinations(List<ImportExternalIdDto> listOfImportDestinations) {
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

    public Builder type(ImportBatchType type) {
      this.type = type;
      return this;
    }

    public Builder collectionId(CollectionId collectionId) {
      this.collectionId = collectionId;
      return this;
    }

    public Builder version(Version version) {
      this.version = version;
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
    this.type = builder.type;
    this.collectionId = builder.collectionId;
    this.version = builder.version;
  }

  // For JAXB
  private GoogleDocImportBatchJobContext() {
    super(null);
  }

  public static enum GoogleDocImportBatchJobState {
    BUILDING(100),
    ENQUEUED(200),
    COLLECTION_CREATED(300),
    COLLECTION_VERSION_RESERVED(400),
    COLLECTION_VERSION_PUBLISHED(500),
    COMPLETE(1000);

    private int level;

    private GoogleDocImportBatchJobState(int level) {
      this.level = level;
    }

    public int getLevel() {
      return level;
    }

    public PlacementOrder getPlacement(GoogleDocImportBatchJobState state) {
      if (this == state) {
        return PlacementOrder.EQUAL;
      } else if (this.getLevel() < state.getLevel()) {
        return PlacementOrder.BEFORE;
      } else {
        return PlacementOrder.AFTER;
      }
    }
  }
}
