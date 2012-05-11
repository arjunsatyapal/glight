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

import com.google.light.server.dto.pojo.typewrapper.longwrapper.CollectionId;

import com.google.light.server.dto.AbstractDtoToPersistence;
import com.google.light.server.dto.pojo.tree.CollectionTreeNodeDto;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.Version;
import com.google.light.server.persistence.entity.collection.CollectionVersionEntity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * DTO for Light Collection Version.
 * 
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
@JsonTypeName(value = "collectionVersion")
@XmlRootElement(name = "collectionVersion")
@XmlAccessorType(XmlAccessType.FIELD)
public class CollectionVersionDto extends AbstractDtoToPersistence<CollectionVersionDto, CollectionVersionEntity, Long> {
  @XmlElement(name = "collectionId")
  @JsonProperty(value = "collectionId")
  private CollectionId collectionId;
  
  @XmlElement(name = "version")
  @JsonProperty(value = "version")
  private Version version;
  
  @XmlElement(name = "collectionTree")
  @JsonProperty(value = "collectionTree")
  private CollectionTreeNodeDto collectionTree;

  /** 
   * {@inheritDoc}
   */
  @Override
  public CollectionVersionDto validate() {
    checkNotNull(collectionId, "collectionId");
    checkNotNull(version, "version");
    checkNotNull(collectionTree, "collectionTree");
    return this;
  }

  /** 
   * {@inheritDoc}
   */
  @Override
  public CollectionVersionEntity toPersistenceEntity(Long version) {
    CollectionVersionEntity entity = new CollectionVersionEntity.Builder()
        .version(new Version(version))
        .collectionTree(collectionTree)
        .build();

    return entity;
  }

  public CollectionTreeNodeDto getCollectionTree() {
    return collectionTree;
  }

  public Version getVersion() {
    return version;
  }

  public static class Builder extends AbstractDtoToPersistence.BaseBuilder<Builder> {
    private CollectionId collectionId;
    private Version version;
    private CollectionTreeNodeDto collectionTree;

    public Builder collectionId(CollectionId collectionId) {
      this.collectionId = collectionId;
      return this;
    }
    
    public Builder version(Version version) {
      this.version = version;
      return this;
    }

    public Builder collectionTree(CollectionTreeNodeDto collectionTree) {
      this.collectionTree = collectionTree;
      return this;
    }

    @SuppressWarnings("synthetic-access")
    public CollectionVersionDto build() {
      return new CollectionVersionDto(this).validate();
    }
  }

  @SuppressWarnings("synthetic-access")
  private CollectionVersionDto(Builder builder) {
    super(builder);
    this.collectionId = builder.collectionId;
    this.version = builder.version;
    this.collectionTree = builder.collectionTree;
  }

  // For JAXB.
  private CollectionVersionDto() {
    super(null);
  }
}
