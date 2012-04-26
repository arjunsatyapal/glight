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

import javax.persistence.Embedded;

import com.google.light.server.dto.module.ModuleDto;
import com.google.light.server.dto.pojo.ModuleId;
import com.google.light.server.persistence.entity.AbstractPersistenceEntity;
import com.googlecode.objectify.Key;
import javax.persistence.Id;

/**
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class OriginModuleMappingEntity extends AbstractPersistenceEntity<OriginModuleMappingEntity, Object> {
  @Id
  String id;
  @Embedded
  ModuleId moduleId;

  /**
   * {@inheritDoc}
   */
  @Override
  public Key<OriginModuleMappingEntity> getKey() {
    return generateKey(id);
  }

  /**
   * Method to generate Objectify key for {@link ModuleEntity}.
   */
  public static Key<OriginModuleMappingEntity> generateKey(String id) {
    return new Key<OriginModuleMappingEntity>(OriginModuleMappingEntity.class, id);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ModuleDto toDto() {
    throw new UnsupportedOperationException();
  }

  // For Objectify.
  private OriginModuleMappingEntity() {
  }

  public String getId() {
    return id;
  }

  public ModuleId getModuleId() {
    return moduleId;
  }

  public static class Builder extends AbstractPersistenceEntity.BaseBuilder<Builder>{
    private String id;
    private ModuleId moduleId;

    public Builder id(String id) {
      this.id = id;
      return this;
    }

    public Builder moduleId(ModuleId moduleId) {
      this.moduleId = moduleId;
      return this;
    }

    @SuppressWarnings("synthetic-access")
    public OriginModuleMappingEntity build() {
      return new OriginModuleMappingEntity(this);
    }
  }

  @SuppressWarnings("synthetic-access")
  private OriginModuleMappingEntity(Builder builder) {
    this.id = checkNotBlank(builder.id, "id");
    this.moduleId = checkNotNull(builder.moduleId, "moduleId");
  }
}
