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
package com.google.light.server.jobs.lightjobs;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.jobs.importjobs.google.ImportGoogleDocJobs.updateChangeLog;
import static com.google.light.server.utils.GuiceUtils.getInstance;

import com.google.appengine.tools.pipeline.ImmediateValue;
import com.google.appengine.tools.pipeline.Value;
import com.google.light.jobs.LightJob2;
import com.google.light.jobs.LightJob4;
import com.google.light.jobs.LightJob6;
import com.google.light.server.dto.module.GSBlobInfo;
import com.google.light.server.dto.module.ModuleType;
import com.google.light.server.dto.pojo.ModuleId;
import com.google.light.server.dto.pojo.Version;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocInfoDto;
import com.google.light.server.manager.interfaces.ImportManager;
import com.google.light.server.manager.interfaces.ModuleManager;
import com.google.light.server.persistence.entity.queue.importflow.ImportJobEntity;
import com.google.light.server.utils.GuiceUtils;
import com.google.light.server.utils.JsonUtils;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class ModuleJobs {
  @SuppressWarnings("serial")
  public static class ReserveModule extends LightJob2<ModuleId, String> {
    /**
     * {@inheritDoc}
     */
    @Override
    public ImmediateValue<ModuleId> handler(String originId) {
      // TODO(arjuns): Add a check that for one Document, only one module is created.
      ModuleManager moduleManager = GuiceUtils.getInstance(ModuleManager.class);

      ModuleId reservedModuleId =
          moduleManager.reserveModuleIdForOriginId(originId, getContext().getOwnerId());

      return immediate(reservedModuleId);
    }
  }

  @SuppressWarnings("serial")
  public static class AddModuleVersion extends LightJob4<Version, ModuleId, String, String> {
    /**
     * {@inheritDoc}
     */
    @Override
    public Value<Version> handler(ModuleId moduleId, String importEntityId, String content) {
      ImportManager importManager = getInstance(ImportManager.class);
      ImportJobEntity importEntity = importManager.get(importEntityId);
      updateChangeLog(importManager, importEntity, "Adding new moduleVersion.");

      ModuleManager moduleManager = getInstance(ModuleManager.class);

      ModuleType moduleType = importEntity.getModuleType();
      Version moduleVersion = null;
      switch (moduleType) {
        case GOOGLE_DOC:
          GoogleDocInfoDto docInfoDto = JsonUtils.getDto(
              importEntity.getAdditionalJsonInfo(), GoogleDocInfoDto.class);
          moduleVersion = moduleManager.addModuleVersionForGoogleDoc(moduleId, content, docInfoDto);
          break;

        default:
          throw new IllegalArgumentException(
              "ModuleType[" + moduleType + "] is not supported.");
      }
      checkNotNull(moduleVersion, "moduleVersion should be positive.");

      updateChangeLog(importManager, importEntity, "Successfully added new moduleVersion["
          + moduleVersion + "].");

      return immediate(moduleVersion);
    }
  }

  @SuppressWarnings("serial")
  public static class AddModuleResources extends LightJob6<Void, ModuleId, Version, String, String, GSBlobInfo> {
    /**
     * {@inheritDoc}
     */
    @Override
    public Value<Void> handler(ModuleId moduleId, Version moduleVersion, String importEntityId,
        String resourceId, GSBlobInfo resourceInfo) {
      ImportManager importManager = getInstance(ImportManager.class);
      ImportJobEntity importEntity = importManager.get(importEntityId);
      updateChangeLog(importManager, importEntity, "Adding new resource : "
          + resourceInfo.getFileName() + " to Module[" + moduleId + ":" + moduleVersion + "].");

      ModuleManager moduleManager = getInstance(ModuleManager.class);
      immediate(moduleManager.addModuleResource(moduleId, moduleVersion, resourceId, resourceInfo));
      return null;
    }
  }

}