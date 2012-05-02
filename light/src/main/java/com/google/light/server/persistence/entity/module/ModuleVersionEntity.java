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

import com.google.light.server.dto.module.ModuleVersionDto;
import com.google.light.server.dto.pojo.longwrapper.ModuleId;
import com.google.light.server.dto.pojo.longwrapper.Version;
import com.google.light.server.persistence.entity.AbstractPersistenceEntity;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Parent;
import javax.persistence.Id;

/**
 * Persistence entity for {@link ModuleDto}.
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class ModuleVersionEntity extends AbstractPersistenceEntity<ModuleVersionEntity, ModuleVersionDto> {
  @Id
  Long version;
  @Parent
  Key<ModuleEntity> moduleKey;
  String content;

  /**
   * {@inheritDoc}
   */
  @Override
  public Key<ModuleVersionEntity> getKey() {
    return generateKey(moduleKey, version);
  }

  private static Key<ModuleVersionEntity> generateKey(Key<ModuleEntity> moduleKey, Long version) {
    checkNotNull(moduleKey, "moduleKey cannot be null");
    checkNotNull(version, "version");
    return new Key<ModuleVersionEntity>(moduleKey, ModuleVersionEntity.class, version);
    
  }
  /**
   * Method to generate Objectify key for {@link ModuleEntity}.
   */
  public static Key<ModuleVersionEntity> generateKey(Key<ModuleEntity> moduleKey, Version version) {
    return generateKey(moduleKey, version.get());
  }
  
  public static Key<ModuleVersionEntity> generateKey(ModuleId moduleId, Version version) {
    return generateKey(ModuleEntity.generateKey(moduleId), version);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ModuleVersionDto toDto() {
    ModuleVersionDto dto = new ModuleVersionDto.Builder()
      .version(version)
      .content(content)
      .build();

    return dto;
  }

  public Version getVersion() {
    if (version == null) {
      return null;
    }
    
    return new Version(version);
  }

  public String getContent() {
    return content;
  }
  
  public Key<ModuleEntity> getModuleKey() {
    return moduleKey;
  }
  
  public ModuleId getModuleId() {
    checkNotNull(getModuleKey(), "moduleKey");
    Long id = getModuleKey().getId();
    return new ModuleId(id);
  }

  public static class Builder extends AbstractPersistenceEntity.BaseBuilder<Builder> {
    private Version version;
    private String content;
    private Key<ModuleEntity> moduleKey;

    public Builder moduleKey(Key<ModuleEntity> moduleKey) {
      this.moduleKey = moduleKey;
      return this;
    }
    
    public Builder version(Version version) {
      this.version = version;
      return this;
    }

    public Builder content(String content) {
      this.content= content;
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
    checkNotNull(builder.version, "version");
    this.version = builder.version.get();
    this.content= checkNotBlank(builder.content, "content");
    this.moduleKey = checkNotNull(builder.moduleKey, "moduleKey");
  }

  // For Objectify.
  private ModuleVersionEntity() {
    super(null, true);
  }
}
