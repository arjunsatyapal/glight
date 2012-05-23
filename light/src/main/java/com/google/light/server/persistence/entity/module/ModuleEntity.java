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
package com.google.light.server.persistence.entity.module;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkNonNegativeLong;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.LightPreconditions.checkNotEmptyCollection;
import static com.google.light.server.utils.LightPreconditions.checkPositiveLong;
import static com.google.light.server.utils.LightPreconditions.checkVersion;
import static com.google.light.server.utils.LightUtils.convertListOfValuesToWrapperList;
import static com.google.light.server.utils.LightUtils.convertWrapperListToListOfValues;
import static com.google.light.server.utils.LightUtils.getWrapper;
import static com.google.light.server.utils.LightUtils.getWrapperValue;

import com.google.common.base.Preconditions;
import com.google.light.server.annotations.ObjectifyQueryField;
import com.google.light.server.annotations.ObjectifyQueryFieldName;
import com.google.light.server.dto.module.ModuleDto;
import com.google.light.server.dto.module.ModuleState;
import com.google.light.server.dto.module.ModuleType;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.ModuleId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.PersonId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.Version;
import com.google.light.server.dto.pojo.typewrapper.stringwrapper.ExternalId;
import com.google.light.server.dto.thirdparty.google.youtube.ContentLicense;
import com.google.light.server.persistence.entity.AbstractPersistenceEntity;
import com.google.light.server.utils.LightUtils;
import com.googlecode.objectify.Key;
import java.util.List;
import javax.persistence.Embedded;
import javax.persistence.Id;
import org.joda.time.Instant;

/**
 * Persistence entity for {@link ModuleDto}.
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class ModuleEntity extends AbstractPersistenceEntity<ModuleEntity, ModuleDto> {
  @Id
  private Long moduleId;
  private String title;
  private ModuleState moduleState;
  private ModuleType moduleType;
  private String externalId;

  @ObjectifyQueryFieldName("owners")
  public static final String OFY_MODULE_OWNER_QUERY_STRING = "owners IN";
  @ObjectifyQueryField("OFY_MODULE_OWNER_QUERY_STRING")
  private List<Long> owners;

  private List<ContentLicense> contentLicenses;

  @Deprecated
  private String etag;
  private Long latestPublishVersion;
  private Long nextVersion;
  /** Last Edit Time at original source */
  private Long lastEditTimeInMillis;
  

  // TODO(waltercacau): Create a proper annotation for this order use case.
  @ObjectifyQueryFieldName("latestPublishTimeInMillis")
  public static final String OFY_MODULE_LATEST_PUBLISH_TIME_DESCENDING_ORDER_STRING =
      "-latestPublishTimeInMillis";
  /** Publish Time for Latest Version */
  @ObjectifyQueryField("OFY_MODULE_LATEST_PUBLISH_TIME_DESCENDING_ORDER_STRING")
  private Long latestPublishTimeInMillis;

  @ObjectifyQueryFieldName("searchIndexStatus")
  public static final String OFY_MODULE_SEARCH_INDEX_FTS = "searchIndexStatus.";
  @ObjectifyQueryField("OFY_MODULE_SEARCH_INDEX_FTS")
  @Embedded
  private SearchIndexStatus searchIndexStatus;

  @Override
  public ModuleEntity validate() {
    super.validate();
    if (moduleId != null) {
      getModuleId();
    }

    checkNotBlank(title, "title");

    checkNotNull(moduleState, "moduleState");
    checkNotNull(moduleType, "moduleType");

    if (moduleType.mapsToCollection()) {
      throw new IllegalStateException("Module cannot be created with " + moduleType);
    } else {
      Preconditions.checkNotNull(getExternalId(), "externalId");
    }
    checkNotEmptyCollection(owners, "owners list cannot be empty.");
    checkNotEmptyCollection(contentLicenses, "contentLicenses");
    checkNotNull(creationTimeInMillis, "creationTimeInMillis");
    checkNotNull(lastEditTimeInMillis, "lastEditTimeInMills");

    // In many cases Etag can be null. So not checking it.
    // Latest version can be zero when module is only reserved.
    // TODO(arjuns): Add more checks here for version.
    // TODO(arjuns): Add more checks here for moduleType.

    checkNonNegativeLong(latestPublishVersion, "latestVersion");
    checkNonNegativeLong(nextVersion, "nextVersion");

    checkNotNull(searchIndexStatus, "searchIndexStatus");

    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Key<ModuleEntity> getKey() {
    return generateKey(moduleId);
  }

  // TODO(arjuns): Change Long to ModuleId
  private static Key<ModuleEntity> generateKey(Long id) {
    checkPositiveLong(id, "moduleId");
    return new Key<ModuleEntity>(ModuleEntity.class, id);
  }

  /**
   * Method to generate Objectify key for {@link ModuleEntity}.
   */
  public static Key<ModuleEntity> generateKey(ModuleId moduleId) {
    return generateKey(moduleId.getValue());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ModuleDto toDto() {
    ModuleDto dto = new ModuleDto.Builder()
        .contentLicenses(contentLicenses)
        .externalId(getExternalId())
        .id(new ModuleId(moduleId))
        .latestPublishVersion(getLatestPublishVersion())
        .moduleType(moduleType)
        .moduleState(moduleState)
        .nextVersion(getNextVersion())
        .owners(getOwners())
        .title(title)
        .build();

    return dto.validate();
  }

  public ModuleId getModuleId() {
    return LightUtils.getWrapper(moduleId, ModuleId.class);
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = checkNotBlank(title, "title");
  }

  public ModuleState getModuleState() {
    return moduleState;
  }

  public void setModuleState(ModuleState state) {
    this.moduleState = checkNotNull(state, "state");
  }

  public ModuleType getModuleType() {
    return moduleType;
  }

  public Version getLatestPublishVersion() {
    return getWrapper(latestPublishVersion, Version.class);
  }

  public Version getNextVersion() {
    return getWrapper(nextVersion, Version.class);
  }

  public ExternalId getExternalId() {
    return getWrapper(externalId, ExternalId.class);
  }

  /**
   * Thisk should be called by callers if they want to reserve a version. And callers are expected
   * to save the entity.
   */
  public Version reserveVersion() {
    /*
     * Needs to be postIncrement i.e. first increment with current version and then increment
     * the nextVersion.
     */
    return new Version(nextVersion++);
  }

  /**
   * If the currently published version is newer then latestPublishVersion, then update it.
   * Caller is expected to persist this entity.
   * 
   * @param latestVersion
   */
  public void publishVersion(Version latestPublishVersion, String title,
      Instant lastEditTime, Instant latestPublishTime, String etag, ExternalId externalId) {
    // Other then builder, all are required to set a positive latestVersion.
    checkVersion(latestPublishVersion);

    Long tempVersion = getWrapperValue(latestPublishVersion);
    if (this.latestPublishVersion < tempVersion) {
      this.title = checkNotBlank(title, "title");
      this.latestPublishVersion = tempVersion;
      this.lastEditTimeInMillis = lastEditTime.getMillis();
      this.latestPublishTimeInMillis = latestPublishTime.getMillis();
      this.etag = etag;
      this.moduleState = ModuleState.PUBLISHED;
      this.externalId = getWrapperValue(externalId);

      if (moduleType.isSearchable()) {
        this.searchIndexStatus = SearchIndexStatus.forNewSearchableVersion;
      }
    }

    checkArgument(this.latestPublishVersion < this.nextVersion,
        "publishVersion should be always less then nextVersion.");
  }

  public String getEtag() {
    return etag;
  }

  public void setEtag(String etag) {
    this.etag = checkNotBlank(etag, "etag");
  }

  public Instant getLastEditTime() {
    return new Instant(lastEditTimeInMillis);
  }

  public void setLastEditTime(Instant lastEditTime) {
    Preconditions.checkNotNull(lastEditTime, "lastEditTime");
    this.lastEditTimeInMillis = lastEditTime.getMillis();
  }

  public Instant getLatestPublishTime() {
    if (latestPublishTimeInMillis == null) {
      return null;
    }

    return new Instant(latestPublishTimeInMillis);
  }

  public void setLatestPublishTime(Instant latestPublishTime) {
    checkNotNull(latestPublishTime, "latestPublishTime");
    this.latestPublishTimeInMillis = latestPublishTime.getMillis();
  }

  public static String getOfyModuleOwnerQueryString() {
    return OFY_MODULE_OWNER_QUERY_STRING;
  }

  public List<PersonId> getOwners() {
    return convertListOfValuesToWrapperList(owners, PersonId.class);
  }

  public Long getLastEditTimeInMillis() {
    return lastEditTimeInMillis;
  }

  public SearchIndexStatus getSearchIndexStatus() {
    return searchIndexStatus;
  }

  public void setSearchIndexStatus(SearchIndexStatus searchIndexStatus) {
    this.searchIndexStatus = searchIndexStatus;
  }

  public List<ContentLicense> getContentLicenses() {
    return contentLicenses;
  }

  public Long getLatestPublishTimeInMillis() {
    return latestPublishTimeInMillis;
  }

  public static String getOfyModuleSearchIndexFts() {
    return OFY_MODULE_SEARCH_INDEX_FTS;
  }

  public static class Builder extends AbstractPersistenceEntity.BaseBuilder<Builder> {
    private Long id;
    private String title;
    private ModuleState moduleState;
    private ModuleType moduleType;
    private List<PersonId> owners;
    private String etag;
    private Version latestPublishVersion = new Version(Version.NO_VERSION);
    private Version nextVersion = new Version(Version.DEFAULT_NEXT_VERSION);
    private Instant lastEditTime = new Instant(0L);
    private Instant latestPublishTimeInMillis;
    private List<ContentLicense> contentLicenses;

    private ExternalId externalId;
    private SearchIndexStatus searchIndexStatus;

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder title(String title) {
      this.title = title;
      return this;
    }

    public Builder moduleState(ModuleState moduleState) {
      this.moduleState = moduleState;
      return this;
    }

    public Builder moduleType(ModuleType moduleType) {
      this.moduleType = moduleType;
      return this;
    }

    public Builder owners(List<PersonId> owners) {
      this.owners = owners;
      return this;
    }

    public Builder contentLicenses(List<ContentLicense> contentLicenses) {
      this.contentLicenses = contentLicenses;
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

    public Builder nextVersion(Version nextVersion) {
      this.nextVersion = nextVersion;
      return this;
    }

    public Builder lastEditTime(Instant lastEditTime) {
      this.lastEditTime = lastEditTime;
      return this;
    }

    public Builder externalId(ExternalId externalId) {
      this.externalId = externalId;
      return this;
    }

    public Builder searchIndexStatus(SearchIndexStatus searchIndexStatus) {
      this.searchIndexStatus = searchIndexStatus;
      return this;
    }

    @SuppressWarnings("synthetic-access")
    public ModuleEntity build() {
      return new ModuleEntity(this).validate();
    }
  }

  @SuppressWarnings("synthetic-access")
  private ModuleEntity(Builder builder) {
    super(builder, true);
    this.moduleId = builder.id;
    this.title = builder.title;
    this.moduleState = builder.moduleState;
    this.moduleType = builder.moduleType;
    this.owners = convertWrapperListToListOfValues(builder.owners);
    this.contentLicenses = builder.contentLicenses;
    this.etag = builder.etag;

    if (builder.lastEditTime != null) {
      this.lastEditTimeInMillis = builder.lastEditTime.getMillis();
    }

    if (builder.latestPublishTimeInMillis != null) {
      this.latestPublishTimeInMillis = builder.latestPublishTimeInMillis.getMillis();
    }

    // Latest version can be zero when module is only reserved.
    // TODO(arjuns): Add more checks here for version.

    this.latestPublishVersion = getWrapperValue(builder.latestPublishVersion);
    this.nextVersion = getWrapperValue(builder.nextVersion);
    this.externalId = getWrapperValue(builder.externalId);
    this.searchIndexStatus = builder.searchIndexStatus;
  }

  // For Objectify.
  private ModuleEntity() {
    super(null, true);
  }
}
