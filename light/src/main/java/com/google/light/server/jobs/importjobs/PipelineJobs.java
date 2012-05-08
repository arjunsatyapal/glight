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
package com.google.light.server.jobs.importjobs;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.jobs.JobUtils.updateChangeLog;
import static com.google.light.server.utils.GuiceUtils.getInstance;

import com.google.light.pipeline_jobs.LightJob1;
import com.google.light.pipeline_jobs.LightJob2;

import com.google.appengine.tools.pipeline.PipelineService;

import com.google.appengine.tools.pipeline.PipelineServiceFactory;

import com.google.appengine.tools.pipeline.FutureValue;
import com.google.appengine.tools.pipeline.ImmediateValue;
import com.google.appengine.tools.pipeline.Value;
import com.google.light.server.dto.pojo.ChangeLogEntryPojo;
import com.google.light.server.dto.pojo.JobHandlerId;
import com.google.light.server.dto.pojo.LightJobContextPojo;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocResourceId;
import com.google.light.server.exception.unchecked.pipelineexceptions.PipelineException;
import com.google.light.server.exception.unchecked.pipelineexceptions.pause.PipelineStopException;
import com.google.light.server.jobs.importjobs.google.ImportGoogleDocJobs;
import com.google.light.server.jobs.importjobs.google.ImportGoogleFolderJobs;
import com.google.light.server.manager.interfaces.ImportManager;
import com.google.light.server.manager.interfaces.JobManager;
import com.google.light.server.persistence.entity.jobs.JobEntity;
import com.google.light.server.persistence.entity.jobs.JobEntity.JobState;
import com.google.light.server.persistence.entity.queue.importflow.ImportJobEntity;
import com.google.light.server.utils.GuiceUtils;
import com.google.light.server.utils.PipelineUtils;
import org.apache.commons.lang.StringUtils;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class PipelineJobs {
  public static class StartJob extends LightJob1<Integer> {
    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("synthetic-access")
    @Override
    public FutureValue<Integer> handler() {
      JobManager jobManager = GuiceUtils.getInstance(JobManager.class);
      JobEntity jobEntity = jobManager.get(null, getContext().getJobId());

      jobEntity.setJobHandlerId(new JobHandlerId(getPipelineId()));
      PipelineUtils.updateJobDetails(getPipelineId(), jobEntity);

      FutureValue<LightJobContextPojo> lastJobContext = null;
      switch (jobEntity.getTaskType()) {
        case IMPORT:
          lastJobContext = futureCall(new ImportJob(),
              immediate(getContext()), immediate(jobEntity.getTaskId()));
          break;

        default:
          throw new PipelineStopException(getPipelineId(), "Invalid JobType : "
              + jobEntity.getTaskType());
      }

      FutureValue<Integer> returnValue =
          futureCall(new EndJob(), immediate(getContext()), waitFor(lastJobContext));
      return returnValue;
    }
  }

  private static class ImportJob extends LightJob2<LightJobContextPojo, String> {
    /**
     * {@inheritDoc}
     */
    @Override
    public Value<LightJobContextPojo> handler(String importEntityId) {
      ImportManager importManager = getInstance(ImportManager.class);
      ImportJobEntity importJobEntity = importManager.get(importEntityId);
      checkNotNull(importJobEntity, "ImportJobEntity for Id[" + importEntityId
          + "] could not be fetched.");

      updateChangeLog(null, getContext().getJobId(),
          "Attaching to Pipeline[" + getPipelineId() + "].");

      switch (importJobEntity.getModuleType()) {
        case GOOGLE_DOC:
          return futureCall(new ImportGoogleDocJobs.InitiateGoogleDocImport(),
              immediate(getContext()), immediate(importJobEntity.getId()));

        case GOOGLE_COLLECTION:
          return futureCall(new ImportGoogleFolderJobs.InitiateGoogleCollectionImport(),
              immediate(getContext()),
              immediate(new GoogleDocResourceId(importJobEntity.getResourceId())));

        default:
          throw new IllegalArgumentException("ModuleType[" + importJobEntity.getModuleType()
              + "] is currently not supported.");
      }
    }
  }

  private static class EndJob extends LightJob1<Integer> {
    /**
     * {@inheritDoc}
     */
    @Override
    public Value<Integer> handler() {
      try {
        JobManager jobManager = GuiceUtils.getInstance(JobManager.class);
        JobEntity jobEntity = jobManager.get(null, getContext().getJobId());

        jobEntity.setJobState(JobState.COMPLETED_SUCCESSFULLY);
        jobManager.put(null, jobEntity, new ChangeLogEntryPojo("Finishing Job."));
        
        if (getContext().getPromiseHandle() != null) {
          PipelineService service = PipelineServiceFactory.newPipelineService();
          service.submitPromisedValue(getContext().getPromiseHandle(), new Boolean("true"));
        }
        
        return immediate(0);
      } catch (Exception e) {
        throw new PipelineException(e);
      }
    }
  }

  public static class DummyJob extends LightJob2<LightJobContextPojo, String> {
    /**
     * {@inheritDoc}
     */
    @Override
    public ImmediateValue<LightJobContextPojo> handler(String message) {
      if (!StringUtils.isBlank(message)) {
        updateChangeLog(null, getContext().getJobId(), message);
      }
      return immediate(getContext());
    }
  }
}
