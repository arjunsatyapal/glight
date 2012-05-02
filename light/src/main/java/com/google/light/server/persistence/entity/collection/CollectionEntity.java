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
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.LightPreconditions.checkPersonId;

import com.google.light.server.dto.collection.CollectionDto;
import com.google.light.server.dto.collection.CollectionState;
import com.google.light.server.dto.pojo.longwrapper.CollectionId;
import com.google.light.server.dto.pojo.longwrapper.PersonId;
import com.google.light.server.dto.pojo.longwrapper.Version;
import com.google.light.server.persistence.entity.AbstractPersistenceEntity;
import com.googlecode.objectify.Key;
import javax.persistence.Embedded;
import javax.persistence.Id;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class CollectionEntity extends AbstractPersistenceEntity<CollectionEntity, CollectionDto> {
  @Id
  Long id;
  String title;
  CollectionState collectionState;
  @Embedded
  PersonId ownerPersonId;

  String etag;
  @Embedded
  Version latestVersion;

  @Override
  public CollectionEntity validate() {
    super.validate();
    checkNotBlank(title, "title");
    checkNotNull(collectionState, "collectionState");
    checkPersonId(ownerPersonId);

    // TODO(arjuns): Add validation for etag.
    checkNotNull(latestVersion, "latestVersion");
    return this;
  }

  public static Key<CollectionEntity> generateKey(CollectionId collectionId) {
    checkNotNull(collectionId, "collectionId");
    return new Key<CollectionEntity>(CollectionEntity.class, collectionId.getValue());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Key<CollectionEntity> getKey() {
    return generateKey(getCollectionId());
  }
  

  public CollectionId getCollectionId() {
    if (id == null) {
      return null;
    }
    
    return new CollectionId(id);
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
  
  public void setLatestVersion(Version version) {
    this.latestVersion = version;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CollectionDto toDto() {
    // TODO(arjuns): Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  public static class Builder extends AbstractPersistenceEntity.BaseBuilder<Builder> {
    private Long id;
    private String title;
    private CollectionState collectionState;
    private PersonId ownerPersonId;
    private String etag;
    private Version latestVersion;

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

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

    @SuppressWarnings("synthetic-access")
    public CollectionEntity build() {
      return new CollectionEntity(this).validate();
    }
  }

  @SuppressWarnings("synthetic-access")
  private CollectionEntity(Builder builder) {
    super(builder, true);
    this.id = builder.id;
    this.title = builder.title;
    this.collectionState = builder.collectionState;
    this.ownerPersonId = builder.ownerPersonId;
    this.etag = builder.etag;
    this.latestVersion = builder.latestVersion;
  }

  // For objectify.
  private CollectionEntity() {
    super(null, true);
  }
}
