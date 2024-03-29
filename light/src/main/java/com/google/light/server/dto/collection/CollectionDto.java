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
package com.google.light.server.dto.collection;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.LightPreconditions.checkNotEmptyCollection;

import com.google.light.server.dto.AbstractDto;
import com.google.light.server.dto.AbstractDtoToPersistence;
import com.google.light.server.dto.pojo.tree.collection.CollectionTreeNodeDto;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.CollectionId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.PersonId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.Version;
import com.google.light.server.dto.thirdparty.google.youtube.ContentLicense;
import com.google.light.server.persistence.entity.collection.CollectionEntity;
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
@XmlRootElement(name = "collection")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonTypeName(value = "collection")
public class CollectionDto extends
    AbstractDtoToPersistence<CollectionDto, CollectionEntity, CollectionId> {
  @XmlElement(name = "collectionId")
  @JsonProperty(value = "collectionId")
  private CollectionId collectionId;
  
  @XmlElement(name = "title")
  @JsonProperty(value = "title")
  private String title;

  @XmlElementWrapper(name = "contentLicenses")
  @XmlAnyElement
  @JsonProperty(value = "contentLicenses")
  private List<ContentLicense> contentLicenses;
  
  @XmlElement(name = "state")
  @JsonProperty(value = "state")
  private CollectionState state;
  
  @XmlElementWrapper(name = "owners")
  @XmlElement(name = "personId")
  @JsonProperty(value = "owners")
  private List<PersonId> owners;
  
  @XmlElement(name = "latestPublishedVersion")
  @JsonProperty(value = "latestPublishedVersion")
  private Version latestPublishedVersion;
  
  @XmlElement(name = "nextVersion")
  @JsonProperty(value = "nextVersion")
  private Version nextVersion;
  
  @XmlElement(name = "root")
  @JsonProperty(value = "root")
  private CollectionTreeNodeDto root;

  /**
   * {@inheritDoc}
   */
  @Override
  public CollectionEntity toPersistenceEntity(CollectionId id) {
    // TODO(arjuns): Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CollectionDto validate() {
    checkNotBlank(title, "title");
    checkNotEmptyCollection(contentLicenses, "contentLicenses");
    checkNotNull(state, "collectionState");
    
    checkNotEmptyCollection(owners, "owners");

    checkNotNull(latestPublishedVersion, "latestPublishedVersion");
    checkNotNull(nextVersion, "nextVersion");
    
    // TODO(arjuns): Fix this check.
//    checkNotNull(root, "root");
    return this;
  }

  public CollectionId getCollectionId() {
    return collectionId;
  }

  public String getTitle() {
    return title;
  }

  public CollectionState getState() {
    return state;
  }

  public List<PersonId> getOwners() {
    return owners;
  }

  public Version getLatestPublishedVersion() {
    return latestPublishedVersion;
  }
  
  public Version getNextVersion() {
    return nextVersion;
  }

  public CollectionTreeNodeDto getRoot() {
    return root;
  }

  public static class Builder extends AbstractDto.BaseBuilder<Builder> {
    private CollectionId collectionId;
    private String title;
    private List<ContentLicense> contentLicenses;
    private CollectionState state;
    private List<PersonId> owners;
    private Version latestPublishedVersion;
    private Version nextVersion;
    private CollectionTreeNodeDto root;

    public Builder collectionId(CollectionId collectionId) {
      this.collectionId = collectionId;
      return this;
    }
    
    public Builder title(String title) {
      this.title = title;
      return this;
    }
    
    public Builder contentLicenses(List<ContentLicense> contentLicenses) {
      this.contentLicenses = contentLicenses;
      return this;
    }

    public Builder state(CollectionState state) {
      this.state = state;
      return this;
    }

    public Builder owners(List<PersonId> owners) {
      this.owners = owners;
      return this;
    }

    public Builder latestPublishedVersion(Version latestPublishedVersion) {
      this.latestPublishedVersion = latestPublishedVersion;
      return this;
    }
    
    public Builder nextVersion(Version nextVersion) {
      this.nextVersion = nextVersion;
      return this;
    }

    public Builder root(CollectionTreeNodeDto root) {
      this.root = root;
      return this;
    }

    @SuppressWarnings("synthetic-access")
    public CollectionDto build() {
      return new CollectionDto(this).validate();
    }
  }

  @SuppressWarnings("synthetic-access")
  private CollectionDto(Builder builder) {
    super(builder);
    this.collectionId = builder.collectionId;
    this.title = builder.title;
    this.contentLicenses = builder.contentLicenses;
    this.state = builder.state;
    this.owners = builder.owners;
    this.latestPublishedVersion = builder.latestPublishedVersion;
    this.nextVersion = builder.nextVersion;
    this.root = builder.root;
  }

  // For Jaxb
  private CollectionDto() {
    super(null);
  }
}
