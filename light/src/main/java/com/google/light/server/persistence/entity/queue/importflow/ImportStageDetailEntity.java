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
package com.google.light.server.persistence.entity.queue.importflow;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkPositiveLong;

import com.google.light.server.persistence.entity.AbstractPersistenceEntity;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Parent;
import javax.persistence.Id;

/**
 * Entity to store details of each individual stage.
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class ImportStageDetailEntity extends
    AbstractPersistenceEntity<ImportStageDetailEntity, Object> {
  @Id 
  private Long id;
  
  @Parent
  Key<ImportJobEntity> importEntityKey;
  
  private ImportStageEnum importStage;

  /**
   * Method to generate Key for {@link ImportStageDetailEntity}.
   * 
   * @param importEntity
   * @param id
   * @return
   */
  public static Key<ImportStageDetailEntity> generateKey(Key<ImportJobEntity> importEntityKey, Long id) {
    checkNotNull(importEntityKey, "importEntity");
    checkPositiveLong(id, "id");
    
    return new Key<ImportStageDetailEntity>(importEntityKey, ImportStageDetailEntity.class, id);
  }
  /**
   * {@inheritDoc}
   */
  @Override
  public Key<ImportStageDetailEntity> getKey() {
    return generateKey(importEntityKey, id);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object toDto() {
    // TODO(arjuns): Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  public Long getId() {
    return id;
  }
  
  public Key<ImportJobEntity> getImportEntityKey() {
    return importEntityKey;
  }

  public ImportStageEnum getImportStage() {
    return importStage;
  }

  public static class Builder extends AbstractPersistenceEntity.BaseBuilder<Builder> {
    private Long id;
    private Key<ImportJobEntity> importEntity;
    private ImportStageEnum importStage;

    public Builder id(Long id) {
      this.id = id;
      return this;
    }
    
    public Builder importEntity(Key<ImportJobEntity> importEntity) {
      this.importEntity = importEntity;
      return this;
    }

    public Builder importStage(ImportStageEnum importStage) {
      this.importStage = importStage;
      return this;
    }

    @SuppressWarnings("synthetic-access")
    public ImportStageDetailEntity build() {
      return new ImportStageDetailEntity(this);
    }
  }

  @SuppressWarnings("synthetic-access")
  private ImportStageDetailEntity(Builder builder) {
    super(builder, false);
    this.id = id != null ? checkPositiveLong(builder.id, "id") : null;
    this.importEntityKey = checkNotNull(builder.importEntity, "importEntity");
    this.importStage = checkNotNull(builder.importStage, "importStage");
  }
  
  // For Jaxb
  private ImportStageDetailEntity() {
    super(null, false);
  }
}
