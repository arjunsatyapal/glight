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

import com.google.light.server.dto.pojo.typewrapper.longwrapper.ModuleId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.Version;



import com.google.light.server.dto.module.GSBlobInfo;
import com.google.light.server.dto.module.ModuleVersionDto;
import com.google.light.server.persistence.entity.AbstractPersistenceEntity;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Parent;
import javax.persistence.Embedded;
import javax.persistence.Id;

/**
 * Persistence entity for {@link ModuleDto}.
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class ModuleVersionResourceEntity extends
    AbstractPersistenceEntity<ModuleVersionResourceEntity, Object> {
  @Id
  private String id;
  @Parent
  private Key<ModuleVersionEntity> moduleVersionKey;
  @Embedded
  private GSBlobInfo resourceInfo;

  /**
   * {@inheritDoc}
   */
  @Override
  public Key<ModuleVersionResourceEntity> getKey() {
    return generateKey(moduleVersionKey, id);
  }

  /**
   * Method to generate Objectify key for {@link ModuleEntity}.
   */
  public static Key<ModuleVersionResourceEntity> generateKey(
      Key<ModuleVersionEntity> moduleVersionKey, String id) {
    checkNotNull(moduleVersionKey, "moduleVersionKey cannot be null");
    checkNotBlank(id, "id");
    return new Key<ModuleVersionResourceEntity>(
        moduleVersionKey, ModuleVersionResourceEntity.class, id);
  }

  public static Key<ModuleVersionResourceEntity> generateKey(ModuleId moduleId,
      Version version, String id) {
    Key<ModuleEntity> moduleKey = ModuleEntity.generateKey(moduleId);
    Key<ModuleVersionEntity> moduleVersionKey =
        ModuleVersionEntity.generateKey(moduleKey, version);
    return generateKey(moduleVersionKey, id);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ModuleVersionDto toDto() {
    throw new UnsupportedOperationException();
  }

  public String getId() {
    return id;
  }

  public Key<ModuleVersionEntity> getModuleVersionKey() {
    return moduleVersionKey;
  }

  public GSBlobInfo getGSBlobInfo() {
    return resourceInfo;
  }



  public static class Builder extends AbstractPersistenceEntity.BaseBuilder<Builder> {
    private String id;
    private Key<ModuleVersionEntity> moduleVersionKey;
    private GSBlobInfo resourceInfo;

    public Builder id(String id) {
      this.id = id;
      return this;
    }

    public Builder moduleVersionKey(Key<ModuleVersionEntity> moduleVersionKey) {
      this.moduleVersionKey = moduleVersionKey;
      return this;
    }

    public Builder resourceInfo(GSBlobInfo resourceInfo) {
      this.resourceInfo = resourceInfo;
      return this;
    }

    @SuppressWarnings("synthetic-access")
    public ModuleVersionResourceEntity build() {
      return new ModuleVersionResourceEntity(this).validate();
    }
  }

  @SuppressWarnings("synthetic-access")
  private ModuleVersionResourceEntity(Builder builder) {
    super(builder, true);
    this.id = builder.id != null ? checkNotBlank(builder.id, "id") : id;
    this.moduleVersionKey = checkNotNull(builder.moduleVersionKey, "moduleVersionKey");
    this.resourceInfo = checkNotNull(builder.resourceInfo, "resourceInfo");
  }
  
  // For Objectify.
  private ModuleVersionResourceEntity() {
    super(null, true);
  }
}
