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
import static com.google.light.server.utils.LightUtils.getWrapper;
import static com.google.light.server.utils.LightUtils.getWrapperValue;

import com.google.light.server.dto.module.ModuleDto;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.ModuleId;
import com.google.light.server.dto.pojo.typewrapper.stringwrapper.ExternalId;
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
public class ExternalIdMappingEntity extends
    AbstractPersistenceEntity<ExternalIdMappingEntity, Object> {
  @Id
  private String externalId;
  private Long moduleId;

  public ExternalId getExternalId() {
    return getWrapper(externalId, ExternalId.class);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Key<ExternalIdMappingEntity> getKey() {
    return generateKey(getExternalId());
  }

  /**
   * Method to generate Objectify key for {@link ModuleEntity}.
   */
  public static Key<ExternalIdMappingEntity> generateKey(ExternalId externalId) {
    return new Key<ExternalIdMappingEntity>(ExternalIdMappingEntity.class, externalId.getValue());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ModuleDto toDto() {
    throw new UnsupportedOperationException();
  }

  public ModuleId getModuleId() {
    return getWrapper(moduleId, ModuleId.class);
  }

  @Override
  public ExternalIdMappingEntity validate() {
    super.validate();
    
    checkNotBlank(externalId, "id");
    checkModuleId(getModuleId());
    
    return this;
  }

  public static class Builder extends AbstractPersistenceEntity.BaseBuilder<Builder> {
    private ExternalId externalId;
    private ModuleId moduleId;

    public Builder externalId(ExternalId externalId) {
      this.externalId = externalId;
      return this;
    }

    public Builder moduleId(ModuleId moduleId) {
      this.moduleId = moduleId;
      return this;
    }

    @SuppressWarnings("synthetic-access")
    public ExternalIdMappingEntity build() {
      return new ExternalIdMappingEntity(this).validate();
    }
  }

  @SuppressWarnings("synthetic-access")
  private ExternalIdMappingEntity(Builder builder) {
    super(builder, true);
    this.externalId = getWrapperValue(builder.externalId);
    this.moduleId = getWrapperValue(builder.moduleId);
  }

  // For Objectify.
  private ExternalIdMappingEntity() {
    super(null, true);
  }
}
