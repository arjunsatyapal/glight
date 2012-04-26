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
package com.google.light.server.manager.interfaces;

import com.google.light.server.dto.pojo.ChangeLogEntryPojo;

import com.google.light.server.dto.module.ModuleType;
import com.google.light.server.persistence.entity.queue.importflow.ImportJobEntity;
import com.google.light.server.persistence.entity.queue.importflow.ImportStageDetailEntity;
import java.util.List;

/**
 * Manager for importing different {@link ModuleType} to Light.
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public interface ImportManager {
  /**
   * Method to put Import entity on both DataStore and Queue in single transaction.
   */
  ImportJobEntity put(ImportJobEntity entity, ChangeLogEntryPojo changeLog);

  /**
   * Get import entity from DataStore.
   * 
   * @param moduleType
   * @param resourceId
   * @return
   */
  ImportJobEntity get(ModuleType moduleType, String resourceId);
  
  /**
   * Get import entity from DataStore using importId.
   * 
   * @param importId
   * @return
   */
  ImportJobEntity get(String importId);

  /**
   * Delete import entity from DataStore.
   * 
   * @param moduleType
   * @param resourceId
   */
  void delete(ModuleType moduleType, String resourceId);
  
  /**
   * Get Details of ImportStages.
   * 
   * @return
   */
  List<ImportStageDetailEntity> getImportStageDetails(String importId);

}
