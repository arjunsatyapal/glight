/*
 * Copyright (C) Google Inc.
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
package com.google.light.server.persistence.dao;

import com.google.inject.Inject;
import com.google.light.server.dto.module.ModuleType;
import com.google.light.server.persistence.entity.queue.importflow.ImportJobEntity;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;

/**
 * DAO for {@link ImportEntity}.
 * 
 * @author Arjun Satyapal
 */
public class ImportJobDao extends AbstractBasicDao<Object, ImportJobEntity> {
  static {
    ObjectifyService.register(ImportJobEntity.class);
  }

  @Inject
  public ImportJobDao() {
    super(ImportJobEntity.class);
  }

  /**
   * Fetch {@link ImportEntity} from DataStore.
   * 
   * @param id
   * @return
   */
  public ImportJobEntity get(ModuleType moduleType, String resourceId) {
    Key<ImportJobEntity> key = ImportJobEntity.generateKey(moduleType, resourceId);

    return super.get(key);
  }
  
  /**
   * Fetch {@link ImportEntity} from DataStore.
   */
  public ImportJobEntity get(String importId) {
    Key<ImportJobEntity> key = ImportJobEntity.generateKey(importId);
    
    return super.get(key);
  }

  /**
   * Delete {@link ImportEntity} from DataStore.
   * TODO(arjuns) : See if there is a way to ensure task is over before deleting it.
   *  
   * @param id
   */
  public void delete(ModuleType moduleType, String resourceId) {
    Key<ImportJobEntity> key = ImportJobEntity.generateKey(moduleType, resourceId);

    super.delete(key);
  }
}
