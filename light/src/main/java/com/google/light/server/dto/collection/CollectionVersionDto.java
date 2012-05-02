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
import static com.google.light.server.utils.LightPreconditions.checkPositiveLong;

import com.google.light.server.dto.AbstractDtoToPersistence;
import com.google.light.server.dto.pojo.longwrapper.Version;
import com.google.light.server.dto.pojo.tree.CollectionTreeNode;
import com.google.light.server.persistence.entity.collection.CollectionVersionEntity;

/**
 * DTO for Light Collection Version.
 * 
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class CollectionVersionDto extends AbstractDtoToPersistence<CollectionVersionDto, CollectionVersionEntity, Long> {
  private Long version;
  private CollectionTreeNode collectionTree;

  /** 
   * {@inheritDoc}
   */
  @Override
  public CollectionVersionDto validate() {
    checkPositiveLong(version, "version");
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

  public CollectionTreeNode getCollectionTree() {
    return collectionTree;
  }

  public static class Builder extends AbstractDtoToPersistence.BaseBuilder<Builder> {
    private Long version;
    private CollectionTreeNode collectionTree;

    public Builder version(Long version) {
      this.version = version;
      return this;
    }

    public Builder collectionTree(CollectionTreeNode collectionTree) {
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
    this.version = builder.version;
    this.collectionTree = builder.collectionTree;
  }
}
