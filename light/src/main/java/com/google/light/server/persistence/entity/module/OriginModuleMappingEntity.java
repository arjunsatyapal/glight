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

import static com.google.light.server.utils.LightPreconditions.checkModuleId;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.LightUtils.getWrapperValue;

import com.google.light.server.dto.pojo.typewrapper.longwrapper.ModuleId;

import com.google.light.server.dto.module.ModuleDto;
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
public class OriginModuleMappingEntity extends
    AbstractPersistenceEntity<OriginModuleMappingEntity, Object> {
  @Id
  private String id;
  private Long moduleId;

  public String getId() {
    return id;
  }

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

  public ModuleId getModuleId() {
    return new ModuleId(moduleId);
  }

  @Override
  public OriginModuleMappingEntity validate() {
    super.validate();
    
    checkNotBlank(id, "id");
    checkModuleId(getModuleId());
    
    return this;
  }

  public static class Builder extends AbstractPersistenceEntity.BaseBuilder<Builder> {
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
      return new OriginModuleMappingEntity(this).validate();
    }
  }

  @SuppressWarnings("synthetic-access")
  private OriginModuleMappingEntity(Builder builder) {
    super(builder, true);
    this.id = builder.id;
    this.moduleId = getWrapperValue(builder.moduleId);
  }

  // For Objectify.
  private OriginModuleMappingEntity() {
    super(null, true);
  }
}
