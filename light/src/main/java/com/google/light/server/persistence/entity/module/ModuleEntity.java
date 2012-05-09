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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkNonEmptyList;
import static com.google.light.server.utils.LightPreconditions.checkNonNegativeLong;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.LightPreconditions.checkPositiveLong;
import static com.google.light.server.utils.LightPreconditions.checkVersion;
import static com.google.light.server.utils.LightUtils.getWrapperValue;

import com.google.common.base.Preconditions;
import com.google.light.server.annotations.ObjectifyQueryField;
import com.google.light.server.annotations.ObjectifyQueryFieldName;
import com.google.light.server.dto.module.ModuleDto;
import com.google.light.server.dto.module.ModuleState;
import com.google.light.server.dto.module.ModuleType;
import com.google.light.server.dto.pojo.longwrapper.ModuleId;
import com.google.light.server.dto.pojo.longwrapper.PersonId;
import com.google.light.server.dto.pojo.longwrapper.Version;
import com.google.light.server.dto.pojo.longwrapper.Version.State;
import com.google.light.server.persistence.entity.AbstractPersistenceEntity;
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
  private Long id;
  private String title;
  private ModuleState state;
  private ModuleType type;

  @ObjectifyQueryFieldName("owners")
  public static final String OFY_OWNER_QUERY_STRING = "owners.value IN";
  @Embedded
  @ObjectifyQueryField("OFY_OWNER_QUERY_STRING")
  private List<PersonId> owners;

  private String etag;
  private Long latestPublishVersion;
  private Long nextVersion;
  private Long lastEditTimeInMillis;

  /**
   * {@inheritDoc}
   */
  @Override
  public Key<ModuleEntity> getKey() {
    return generateKey(id);
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
        .id(new ModuleId(id))
        .title(title)
        .state(state)
        .owners(owners)
        .latestPublishVersion(getLatestPublishVersion())
        .build();

    return dto;
  }

  public ModuleId getModuleId() {
    if (id == null) {
      return null;
    }

    return new ModuleId(id);
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = checkNotBlank(title, "title");
  }

  public ModuleState getState() {
    return state;
  }
  
  public void setState(ModuleState state) {
    this.state = checkNotNull(state, "state");
    
  }

  public ModuleType getType() {
    return type;
  }

  public Version getLatestPublishVersion() {
    if (latestPublishVersion == 0) {
      return null;
    }
    
    return new Version(latestPublishVersion);
  }

  public Version getNextVersion() {
    return new Version(nextVersion);
  }
  
  /**
   * Thisk should be called by callers if they want to reserve a version. And callers are expected
   * to save the entity. 
   */
  public Version reserveVersion() {
    nextVersion++;
    return new Version(nextVersion);
  }

  /**
   * If the currently published version is newer then latestPublishVersion, then update it.
   * Caller is expected to persist this entity.
   * @param latestVersion
   */
  public void publishVersion(Version latestPublishVersion, Instant lastEditTime, String etag) {
    // Other then builder, all are required to set a positive latestVersion.
    checkVersion(latestPublishVersion);
    
    Long tempVersion = getWrapperValue(latestPublishVersion);
    if (this.latestPublishVersion < tempVersion) {
      this.latestPublishVersion = tempVersion;
      this.lastEditTimeInMillis = lastEditTime.getMillis();
      this.etag = etag;
      this.state = ModuleState.PUBLISHED;
    }
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

  @Override
  public ModuleEntity validate() {
    super.validate();
    if (id != null) {
      getModuleId();
    }

    checkNotBlank(title, "title");
    checkNotNull(state, "moduleState");
    checkNonEmptyList(owners, "owners list cannot be empty.");
    checkNotNull(lastEditTimeInMillis, "lastEditTimeInMills");
    
    // In many cases Etag can be null. So not checking it.
    // Latest version can be zero when module is only reserved.
    // TODO(arjuns): Add more checks here for version.
    // TODO(arjuns): Add more checks here for moduleType.

    checkNonNegativeLong(latestPublishVersion, "latestVersion");
    checkNonNegativeLong(nextVersion, "nextVersion");
    return this;
  }

  public static class Builder extends AbstractPersistenceEntity.BaseBuilder<Builder> {
    private Long id;
    private String title;
    private ModuleState state;
    private ModuleType type;
    private List<PersonId> owners;
    private String etag;
    private Version latestPublishVersion = new Version(0L, State.NO_VERSION);
    private Version nextVersion = new Version(0L, State.NO_VERSION);
    private Instant lastEditTime = new Instant(0L);

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder title(String title) {
      this.title = title;
      return this;
    }

    public Builder state(ModuleState state) {
      this.state = state;
      return this;
    }

    public Builder type(ModuleType type) {
      this.type = type;
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
    
    public Builder nextVersion(Version nextVersion) {
      this.nextVersion = nextVersion;
      return this;
    }
    
    public Builder lastEditTime(Instant lastEditTime) {
      this.lastEditTime = lastEditTime;
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
    this.id = builder.id;
    this.title = builder.title;
    this.state = builder.state;
    this.type = builder.type;
    this.owners = builder.owners;
    this.etag = builder.etag;
    
    if (builder.lastEditTime != null) {
      this.lastEditTimeInMillis = builder.lastEditTime.getMillis();
    }
    // Latest version can be zero when module is only reserved.
    // TODO(arjuns): Add more checks here for version.

    this.latestPublishVersion = getWrapperValue(builder.latestPublishVersion);
    this.nextVersion = getWrapperValue(builder.nextVersion);
  }

  // For Objectify.
  private ModuleEntity() {
    super(null, true);
  }
}
