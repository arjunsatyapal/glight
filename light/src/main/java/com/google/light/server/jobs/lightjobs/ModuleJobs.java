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
import static com.google.light.server.jobs.JobUtils.updateChangeLog;
import static com.google.light.server.utils.GuiceUtils.getInstance;

import com.google.appengine.tools.pipeline.ImmediateValue;
import com.google.appengine.tools.pipeline.Value;
import com.google.common.collect.Lists;
import com.google.light.pipeline_jobs.LightJob2;
import com.google.light.pipeline_jobs.LightJob4;
import com.google.light.pipeline_jobs.LightJob5;
import com.google.light.server.dto.module.GSBlobInfo;
import com.google.light.server.dto.module.ModuleType;
import com.google.light.server.dto.pojo.longwrapper.ModuleId;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocInfoDto;
import com.google.light.server.manager.interfaces.ImportManager;
import com.google.light.server.manager.interfaces.ModuleManager;
import com.google.light.server.persistence.entity.module.ModuleVersionEntity;
import com.google.light.server.persistence.entity.queue.importflow.ImportJobEntity;
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
      ModuleManager moduleManager = getInstance(ModuleManager.class);

      ModuleId reservedModuleId = moduleManager.reserveModuleIdForExternalId(ModuleType.GOOGLE_DOC,
          originId, Lists.newArrayList(getContext().getOwnerId()));

      return immediate(reservedModuleId);
    }
  }

  @SuppressWarnings("serial")
  public static class AddModuleVersion extends
      LightJob4<ModuleVersionEntity, ModuleId, String, String> {
    /**
     * {@inheritDoc}
     */
    @Override
    public Value<ModuleVersionEntity> handler(ModuleId moduleId, String importEntityId,
        String content) {
      ImportManager importManager = getInstance(ImportManager.class);
      ImportJobEntity importEntity = importManager.get(importEntityId);
      updateChangeLog(null, getContext().getJobId(), "Adding new moduleVersion.");

      ModuleManager moduleManager = getInstance(ModuleManager.class);

      ModuleType moduleType = importEntity.getModuleType();
      ModuleVersionEntity moduleVersionEntity = null;
      switch (moduleType) {
        case GOOGLE_DOC:
          GoogleDocInfoDto docInfoDto = JsonUtils.getDto(
              importEntity.getAdditionalJsonInfo(), GoogleDocInfoDto.class);

          moduleVersionEntity =
              moduleManager.addModuleVersionForGoogleDoc(moduleId, content, docInfoDto);
          break;

        default:
          throw new IllegalArgumentException(
              "ModuleType[" + moduleType + "] is not supported.");
      }
      checkNotNull(moduleVersionEntity, "moduleVersionEntity should not be null.");

      updateChangeLog(null, getContext().getJobId(), "Successfully added new moduleVersion["
          + moduleVersionEntity.getVersion() + "].");

      return immediate(moduleVersionEntity);
    }
  }

  @SuppressWarnings("serial")
  public static class AddModuleResources extends
      LightJob5<Void, ModuleVersionEntity, String, String, GSBlobInfo> {
    /**
     * {@inheritDoc}
     */
    @Override
    public Value<Void> handler(ModuleVersionEntity moduleVersionEntity, String importEntityId,
        String resourceId, GSBlobInfo resourceInfo) {
      updateChangeLog(null, getContext().getJobId(), "Adding new resource : "
          + resourceInfo.getFileName() + " to Module[" + moduleVersionEntity.getModuleId() + ":"
          + moduleVersionEntity.getVersion() + "].");

      ModuleManager moduleManager = getInstance(ModuleManager.class);
      immediate(moduleManager.addModuleResource(moduleVersionEntity.getModuleId(), 
          moduleVersionEntity.getVersion(), resourceId, resourceInfo));
      return null;
    }
  }
}
