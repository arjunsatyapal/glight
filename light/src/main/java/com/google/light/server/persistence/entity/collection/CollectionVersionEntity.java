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
package com.google.light.server.persistence.entity.collection;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkPositiveLong;

import com.google.appengine.api.datastore.Text;
import com.google.light.server.dto.collection.CollectionVersionDto;
import com.google.light.server.dto.pojo.longwrapper.CollectionId;
import com.google.light.server.dto.pojo.longwrapper.Version;
import com.google.light.server.dto.pojo.tree.CollectionTreeNode;
import com.google.light.server.persistence.entity.AbstractPersistenceEntity;
import com.google.light.server.utils.JsonUtils;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Parent;
import javax.persistence.Id;

/**
 * Persistence entity for {@link CollectionDto}.
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class CollectionVersionEntity extends AbstractPersistenceEntity<CollectionVersionEntity, CollectionVersionDto> {
  @Id
  Long version;
  @Parent
  Key<CollectionEntity> collectionKey;
  Text collectionTreeJson;
  /**
   * {@inheritDoc}
   */
  @Override
  public Key<CollectionVersionEntity> getKey() {
    return generateKey(collectionKey, version);
  }

  private static Key<CollectionVersionEntity> generateKey(Key<CollectionEntity> collectionKey, Long version) {
    checkNotNull(collectionKey, "collectionKey cannot be null");
    checkNotNull(version, "version");
    return new Key<CollectionVersionEntity>(collectionKey, CollectionVersionEntity.class, version);
    
  }
  /**
   * Method to generate Objectify key for {@link CollectionEntity}.
   */
  public static Key<CollectionVersionEntity> generateKey(Key<CollectionEntity> collectionKey, Version version) {
    return generateKey(collectionKey, version.get());
  }
  
  public static Key<CollectionVersionEntity> generateKey(CollectionId collectionId, Version version) {
    return generateKey(CollectionEntity.generateKey(collectionId), version);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CollectionVersionDto toDto() {
    CollectionVersionDto dto = new CollectionVersionDto.Builder()
      .version(version)
      .collectionTree(getCollectionTree())
      .build();

    return dto;
  }

  public Version getVersion() {
    if (version == null) {
      return null;
    }
    
    return new Version(version);
  }

  public CollectionTreeNode getCollectionTree() {
    checkNotNull(collectionTreeJson, "collectionTreeJson");
    return JsonUtils.getDto(collectionTreeJson.getValue(), CollectionTreeNode.class);
  }
  
  public Key<CollectionEntity> getCollectionKey() {
    return collectionKey;
  }
  
  @Override
  public CollectionVersionEntity validate() {
    checkPositiveLong(version, "version");
    checkNotNull(collectionKey, "collectionKey");
    checkNotNull(collectionTreeJson, "collectionTreeJson");
    return this;
  }

  public static class Builder extends AbstractPersistenceEntity.BaseBuilder<Builder> {
    private Version version;
    private CollectionTreeNode collectionTree;
    private Key<CollectionEntity> collectionKey;

    public Builder collectionKey(Key<CollectionEntity> collectionKey) {
      this.collectionKey = collectionKey;
      return this;
    }
    
    public Builder version(Version version) {
      this.version = version;
      return this;
    }

    public Builder collectionTree(CollectionTreeNode collectionTree) {
      this.collectionTree = collectionTree;
      return this;
    }
    
    @SuppressWarnings("synthetic-access")
    public CollectionVersionEntity build() {
      return new CollectionVersionEntity(this).validate();
    }
  }

  @SuppressWarnings("synthetic-access")
  private CollectionVersionEntity(Builder builder) {
    super(builder, true);
    
    checkNotNull(builder.version, "version");
    this.version = builder.version.get();
    
    checkNotNull(builder.collectionTree, "collectionTree");
    this.collectionTreeJson = new Text(builder.collectionTree.toJson());
    
    this.collectionKey = checkNotNull(builder.collectionKey, "collectionKey");
  }

  // For Objectify.
  private CollectionVersionEntity() {
    super(null, true);
  }
}
