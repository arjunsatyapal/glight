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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkNonNegativeLong;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.LightPreconditions.checkNotEmptyCollection;
import static com.google.light.server.utils.LightPreconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkVersion;
import static com.google.light.server.utils.LightUtils.convertListOfValuesToWrapperList;
import static com.google.light.server.utils.LightUtils.convertWrapperListToListOfValues;
import static com.google.light.server.utils.LightUtils.getWrapper;
import static com.google.light.server.utils.LightUtils.getWrapperValue;

import com.google.light.server.annotations.ObjectifyQueryField;
import com.google.light.server.annotations.ObjectifyQueryFieldName;
import com.google.light.server.constants.LightClientType;
import com.google.light.server.dto.collection.CollectionDto;
import com.google.light.server.dto.collection.CollectionState;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.CollectionId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.PersonId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.Version;
import com.google.light.server.dto.thirdparty.google.youtube.ContentLicense;
import com.google.light.server.exception.ExceptionType;
import com.google.light.server.persistence.entity.AbstractPersistenceEntity;
import com.googlecode.objectify.Key;
import java.util.List;
import javax.persistence.Id;
import org.joda.time.Instant;

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
  private Long collectionId;
  private String title;
  private List<ContentLicense> contentLicenses;
  private CollectionState state;
  @ObjectifyQueryFieldName("owners")
  public static final String OFY_COLLECTION_OWNER_QUERY_STRING = "owners IN";

  @ObjectifyQueryField("OFY_COLLECTION_OWNER_QUERY_STRING")
  private List<Long> owners;

  private String etag;
  private Long latestPublishVersion;
  private Long nextVersion;

  @Override
  public CollectionEntity validate() {
    super.validate();
    checkNotBlank(title, "title");
    checkNotEmptyCollection(contentLicenses, "contentLicenses");
    checkNotNull(state, "collectionState");
    checkNotEmptyCollection(owners, "owners");
    

    // TODO(arjuns): Add validation for etag.
    checkNonNegativeLong(latestPublishVersion, "latestVersion");
    checkNonNegativeLong(nextVersion, "nextVersion");
    
    checkNotNull(creationTimeInMillis, "creationTimeInMillis");
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
    return getWrapper(collectionId, CollectionId.class);
  }

  public String getTitle() {
    return title;
  }
  
  public void setTitle(String title) {
    this.title = checkNotBlank(title, "title");
  }

  public List<ContentLicense> getContentLicenses() {
    return contentLicenses;
  }

  public CollectionState getState() {
    return state;
  }

  public void setState(CollectionState state) {
    this.state = checkNotNull(state, ExceptionType.SERVER, "state");
  }

  public List<PersonId> getOwners() {
    return convertListOfValuesToWrapperList(owners, PersonId.class);
  }

  public String getEtag() {
    return etag;
  }

  public Version getLatestPublishVersion() {
    return getWrapper(latestPublishVersion, Version.class);
  }

  public Version getNextVersion() {
    return getWrapper(nextVersion, Version.class);
  }
  
  /**
   * Thisk should be called by callers if they want to reserve a version. And callers are expected
   * to save the entity. 
   */
  public Version reserveVersion() {
    /*
     * PostIncrement is deliberate so that first Version is created with the current nextVersion
     * value and then it is incremented.
     */
    return new Version(nextVersion++);
  }

  /**
   * If the currently published version is newer then latestPublishVersion, then update it.
   * Caller is expected to persist this entity.
   * @param latestVersion
   */
  @SuppressWarnings("unused")
  public void publishVersion(Version latestPublishVersion, Instant lastEditTime, String etag) {
    // Other then builder, all are required to set a positive latestVersion.
    checkVersion(latestPublishVersion);
    
    Long tempVersion = getWrapperValue(latestPublishVersion);
    if (this.latestPublishVersion < tempVersion) {
      this.latestPublishVersion = tempVersion;
      this.etag = etag;
      this.state = CollectionState.PUBLISHED;
    }
    
    checkArgument(this.latestPublishVersion < this.nextVersion, 
        "latestPublishVersion should be always less then nextVersion");
  }
  
  public Version determineBaseVersionForAppend(Version askedVersion, Version reservedVersion, 
      LightClientType clientType) {
    if (askedVersion.isSpecificVersion() || askedVersion.isNoVersion()) {
      return askedVersion;
    }
    
    if (clientType == LightClientType.BROWSER) {
      return reservedVersion.getPreviousVersion();
    }
    
    throw new IllegalArgumentException("Invalid askedVersion : " + askedVersion);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CollectionDto toDto() {
    return new CollectionDto.Builder()
        .collectionId(getCollectionId())
        .title(title)
        .state(state)
        .owners(getOwners())
        .latestPublishedVersion(getLatestPublishVersion())
        .nextVersion(getNextVersion())
        .build();
  }

  public static class Builder extends AbstractPersistenceEntity.BaseBuilder<Builder> {
    private Long id;
    private String title;
    private List<ContentLicense> contentLicenses;
    private CollectionState state;
    private List<PersonId> owners;
    private String etag;
    private Version latestPublishVersion = new Version(0L);
    private Version nextVersion = new Version(1L);

    public Builder id(Long id) {
      this.id = id;
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

    public Builder collectionState(CollectionState collectionState) {
      this.state = collectionState;
      return this;
    }

    public Builder owners(List<PersonId> owners) {
      this.owners = owners;
      return this;
    }

    public Builder etag(String etag) {
      this.etag = etag;
      return this;
    }

    public Builder latestPublishVersion(Version latestPublishVersion) {
      this.latestPublishVersion = latestPublishVersion;
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
    this.collectionId = builder.id;
    this.title = builder.title;
    this.contentLicenses = builder.contentLicenses;
    this.state = builder.state;
    this.owners = convertWrapperListToListOfValues(builder.owners);
    this.etag = builder.etag;
    this.latestPublishVersion = getWrapperValue(builder.latestPublishVersion);
    this.nextVersion = getWrapperValue(builder.nextVersion);
    
  }

  // For objectify.
  private CollectionEntity() {
    super(null, true);
  }
}
