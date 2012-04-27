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
package com.google.light.server.manager.implementation;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Inject;
import com.google.light.server.dto.module.ModuleType;
import com.google.light.server.dto.pojo.ChangeLogEntryPojo;
import com.google.light.server.manager.interfaces.ImportManager;
import com.google.light.server.persistence.dao.ImportJobDao;
import com.google.light.server.persistence.dao.ImportStageDetailDao;
import com.google.light.server.persistence.entity.queue.importflow.ImportJobEntity;
import com.google.light.server.persistence.entity.queue.importflow.ImportStageDetailEntity;
import com.googlecode.objectify.Objectify;
import java.util.List;

/**
 * Implementation for {@link ImportManager}
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class ImportManagerImpl implements ImportManager {
  private ImportJobDao importJobDao;
  private ImportStageDetailDao importStageDetailDao;

  @Inject
  public ImportManagerImpl(ImportJobDao importJobDao, ImportStageDetailDao importStageDetailDao) {
    this.importJobDao = checkNotNull(importJobDao, "importJobDao");
    this.importStageDetailDao = checkNotNull(importStageDetailDao, "importStageDetailDao"); 
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ImportJobEntity put(Objectify ofy, ImportJobEntity entity, ChangeLogEntryPojo changeLog) {
    checkNotNull(entity, "entity");

    /*
     * Before persisting, check whether there is any existing ImportEntity with same Key already
     * created. If yes, then just return that Entity without creating any new task. In case
     * one wants to force to create a new Task, in that case, first Delete the existing
     * ImportEntity, and then re-enqueue for Import.
     */
    // ImportEntity existingEntity = get(entity.getId());
    // if (existingEntity != null) {
    // return existingEntity;
    // }
    //

    entity.addToChangeLog(changeLog);
    return importJobDao.put(ofy, entity);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ImportJobEntity get(ModuleType moduleType, String resourceId) {
    return importJobDao.get(moduleType, resourceId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ImportJobEntity get(String importId) {
    return importJobDao.get(importId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void delete(ModuleType moduleType, String resourceId) {
    importJobDao.delete(moduleType, resourceId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<ImportStageDetailEntity> getImportStageDetails(String importId) {
    return importStageDetailDao.getAllStages(ImportJobEntity.generateKey(importId));
  }
}
