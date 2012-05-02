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
package com.google.light.server.jersey.resources.thirdparty.google;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocInfoDto;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocResourceId;
import com.google.light.server.manager.interfaces.JobManager;
import com.google.light.server.persistence.entity.jobs.JobEntity;
import com.google.light.server.persistence.entity.queue.importflow.ImportJobEntity;
import com.google.light.server.thirdparty.clients.google.gdata.gdoc.DocsServiceWrapper;
import com.google.light.server.utils.GuiceUtils;
import java.util.logging.Logger;

/**
 *
 * 
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */
public class GoogleDocIntegrationUtils {
  private static final Logger logger = Logger.getLogger(GoogleDocIntegrationUtils.class.getName());
  
  public static JobEntity importGDocResource(GoogleDocResourceId resourceId, 
      DocsServiceWrapper docsService, Long parentJobId, Long rootJobId, String promiseHandle) {
    GoogleDocInfoDto docInfoDto = docsService.getDocumentEntryWithAcl(resourceId);
    checkNotNull(docInfoDto, "DocInfoDto for ResourceId[" + resourceId + "] was null.");

    // TODO(arjuns): Add more logic to filter out what docs can be imported.
    logger.info(docInfoDto.toJson());

    ImportJobEntity importJobEntity = new ImportJobEntity.Builder()
        .moduleType(resourceId.getModuleType())
        .resourceId(resourceId.getTypedResourceId())
        .personId(GuiceUtils.getOwnerId())
        .additionalJsonInfo(docInfoDto.toJson())
        .build();

    JobManager jobManager = GuiceUtils.getInstance(JobManager.class);
    JobEntity jobEntity = jobManager.enqueueImportJob(
        importJobEntity, parentJobId, rootJobId, promiseHandle);
    return jobEntity;
  }
}
