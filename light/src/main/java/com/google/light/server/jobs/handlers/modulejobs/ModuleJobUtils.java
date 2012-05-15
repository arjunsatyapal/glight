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
package com.google.light.server.jobs.handlers.modulejobs;

import static com.google.common.collect.Lists.newArrayList;

import java.util.logging.Logger;

import com.google.light.server.manager.interfaces.ModuleManager;

import com.google.light.server.dto.importresource.ImportExternalIdDto;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.ModuleId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.PersonId;
import com.google.light.server.utils.ObjectifyUtils;
import com.googlecode.objectify.Objectify;

/**
 *
 * 
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */
public class ModuleJobUtils {
  private static final Logger logger = Logger.getLogger(ModuleJobUtils.class.getName());
  

  public static ModuleId reserveModuleId(ModuleManager moduleManager, 
      ImportExternalIdDto externalIdDto, PersonId ownerId) {
    ModuleId moduleId = null;
    Objectify ofy = ObjectifyUtils.initiateTransaction();
    try {

      moduleId = moduleManager.reserveModuleId(ofy, externalIdDto.getExternalId(),
          newArrayList(ownerId), externalIdDto.getTitle());
      ObjectifyUtils.commitTransaction(ofy);
    } finally {
      ObjectifyUtils.rollbackTransactionIfStillActive(ofy);
    }
    logger.info("Found/Created new Module[" + moduleId + "].");
    return moduleId;
  }
  
  // Utility class.
  private ModuleJobUtils() {
  }
}
