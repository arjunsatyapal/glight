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
import static com.google.light.server.utils.LightPreconditions.checkPersonId;

import com.google.light.server.dto.pojo.tree.CollectionTreeNode;

import com.google.light.server.dto.pojo.longwrapper.Version;

import com.google.light.server.dto.pojo.longwrapper.PersonId;

import com.google.light.server.dto.AbstractDto;
import com.google.light.server.dto.AbstractDtoToPersistence;
import com.google.light.server.persistence.entity.collection.CollectionEntity;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class CollectionDto extends
    AbstractDtoToPersistence<CollectionDto, CollectionEntity, String> {

  private String title;
  private CollectionState collectionState;
  private PersonId ownerPersonId;
  private String etag;
  private Version latestVersion;
  private CollectionTreeNode root;

  /**
   * {@inheritDoc}
   */
  @Override
  public CollectionEntity toPersistenceEntity(String id) {
    // TODO(arjuns): Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CollectionDto validate() {
    checkNotBlank(title, "title");
    checkNotNull(collectionState, "collectionState");
    checkPersonId(ownerPersonId);

    // TODO(arjuns): Add validation for etag.
    checkNotNull(latestVersion, "latestVersion");
    checkNotNull(root, "root");
    return this;
  }

  public String getTitle() {
    return title;
  }

  public CollectionState getCollectionState() {
    return collectionState;
  }

  public PersonId getOwnerPersonId() {
    return ownerPersonId;
  }

  public String getEtag() {
    return etag;
  }

  public Version getLatestVersion() {
    return latestVersion;
  }

  public CollectionTreeNode getRoot() {
    return root;
  }

  public static class Builder extends AbstractDto.BaseBuilder<Builder> {
    private String title;
    private CollectionState collectionState;
    private PersonId ownerPersonId;
    private String etag;
    private Version latestVersion;
    private CollectionTreeNode root;

    public Builder title(String title) {
      this.title = title;
      return this;
    }

    public Builder collectionState(CollectionState collectionState) {
      this.collectionState = collectionState;
      return this;
    }

    public Builder ownerPersonId(PersonId ownerPersonId) {
      this.ownerPersonId = ownerPersonId;
      return this;
    }

    public Builder etag(String etag) {
      this.etag = etag;
      return this;
    }

    public Builder latestVersion(Version latestVersion) {
      this.latestVersion = latestVersion;
      return this;
    }

    public Builder root(CollectionTreeNode root) {
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
    this.title = builder.title;
    this.collectionState = builder.collectionState;
    this.ownerPersonId = builder.ownerPersonId;
    this.etag = builder.etag;
    this.latestVersion = builder.latestVersion;
    this.root = builder.root;
  }
}
