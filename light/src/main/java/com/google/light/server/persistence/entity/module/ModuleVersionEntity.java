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
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.LightPreconditions.checkPositiveLong;
import static com.google.light.server.utils.LightUtils.getWrapper;
import static com.google.light.server.utils.LightUtils.getWrapperValue;

import com.google.light.server.annotations.ObjectifyQueryField;
import com.google.light.server.annotations.ObjectifyQueryFieldName;
import com.google.light.server.dto.module.ModuleVersionDto;
import com.google.light.server.dto.module.ModuleVersionState;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.ModuleId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.Version;
import com.google.light.server.dto.pojo.typewrapper.stringwrapper.ExternalId;
import com.google.light.server.persistence.entity.AbstractPersistenceEntity;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Parent;
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
public class ModuleVersionEntity extends
    AbstractPersistenceEntity<ModuleVersionEntity, ModuleVersionDto> {
  @Id
  private Long version;
  @Parent
  private Key<ModuleEntity> moduleKey;
  private ModuleVersionState state;
  private String title;
  private String content;
  private String etag;
  private String externalId;
//  private Text

  // TODO(arjuns): Add a test to ensure this string matches with field name.
  @ObjectifyQueryFieldName("lastEditTimeInMillis")
  public static final String OFY_GREATER_THEN_LAST_EDIT_TIME_IN_MILLIS_QUERY_STRING =
      "lastEditTimeInMillis >";
  @ObjectifyQueryField("OFY_GREATER_THEN_LAST_EDIT_TIME_IN_MILLIS_QUERY_STRING")
  private Long lastEditTimeInMillis;

  /**
   * {@inheritDoc}
   */
  @Override
  public Key<ModuleVersionEntity> getKey() {
    return generateKey(moduleKey, new Version(version));
  }

  public static Key<ModuleVersionEntity> generateKey(Key<ModuleEntity> moduleKey, Version version) {
    checkNotNull(moduleKey, "moduleKey cannot be null");
    checkNotNull(version, "version");
    return new Key<ModuleVersionEntity>(moduleKey, ModuleVersionEntity.class, version.getValue());

  }

  public static Key<ModuleVersionEntity> generateKey(ModuleId moduleId, Version version) {
    return generateKey(ModuleEntity.generateKey(moduleId), version);
  }

  public ModuleVersionState getState() {
    return state;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ModuleVersionDto toDto() {
    ModuleVersionDto dto = new ModuleVersionDto.Builder()
        .moduleId(getModuleId())
        .version(getVersion())
        .content(content)
        .externalId(getExternalId())
        .build();

    return dto;
  }

  public Version getVersion() {
    if (version == null) {
      return null;
    }

    return new Version(version);
  }

  public String getTitle() {
    return title;
  }
  
  public void setTitle(String title) {
    this.title = checkNotBlank(title, "title");
  }
  
  public String getContent() {
    return content;
  }
  
  public void setContent(String content) {
    this.content = checkNotBlank(content, "content");
  }

  public Key<ModuleEntity> getModuleKey() {
    return moduleKey;
  }
  
  public ModuleId getModuleId() {
    return getWrapper(moduleKey.getId(), ModuleId.class);
  }

  public String getEtag() {
    return etag;
  }
  
  public void setEtag(String etag) {
    this.etag = etag;
  }
  
  public ExternalId getExternalId() {
    return getWrapper(externalId, ExternalId.class);
  }

  public Instant getLastEditTime() {
    return new Instant(lastEditTimeInMillis);
  }

  @Override
  public ModuleVersionEntity validate() {
    checkPositiveLong(version, "version");

    checkNotNull(state, "state");
    switch (state) {
      case RESERVED:
        break;

      default:
        checkNotBlank(content, "content");
    }

    checkNotNull(moduleKey, "moduleKey");

    checkPositiveLong(lastEditTimeInMillis, "lastEditTime");

    return this;
  }

  public static class Builder extends AbstractPersistenceEntity.BaseBuilder<Builder> {
    private Version version;
    private String title;
    private String content;
    private Key<ModuleEntity> moduleKey;
    private ModuleVersionState state;
    private String etag;
    private Instant lastEditTime;
    private ExternalId externalId;

    public Builder moduleKey(Key<ModuleEntity> moduleKey) {
      this.moduleKey = moduleKey;
      return this;
    }

    public Builder version(Version version) {
      this.version = version;
      return this;
    }

    public Builder title(String title) {
      this.title = title;
      return this;
    }
    
    public Builder content(String content) {
      this.content = content;
      return this;
    }

    public Builder state(ModuleVersionState state) {
      this.state = state;
      return this;
    }

    public Builder etag(String etag) {
      this.etag = etag;
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

    @SuppressWarnings("synthetic-access")
    public ModuleVersionEntity build() {
      return new ModuleVersionEntity(this).validate();
    }
  }

  @SuppressWarnings("synthetic-access")
  private ModuleVersionEntity(Builder builder) {
    super(builder, true);
    this.version = getWrapperValue(builder.version);
    
    this.title = builder.title;
    this.content = builder.content;
    
    this.moduleKey = builder.moduleKey;
    this.state = builder.state;
    this.etag = builder.etag;
    this.externalId = getWrapperValue(builder.externalId);
    
    if (builder.lastEditTime != null) {
      this.lastEditTimeInMillis = builder.lastEditTime.getMillis();
    }
  }

  // For Objectify.
  private ModuleVersionEntity() {
    super(null, true);
  }
}
