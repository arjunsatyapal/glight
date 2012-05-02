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
import static com.google.light.server.constants.LightConstants.JOB_BACK_OFF_FACTOR;
import static com.google.light.server.constants.LightConstants.JOB_BACK_OFF_SECONDS;
import static com.google.light.server.constants.LightConstants.JOB_MAX_ATTEMPTES;
import static com.google.light.server.utils.GuiceUtils.getRequestScopedValues;

import com.google.appengine.tools.pipeline.PipelineService;
import com.google.appengine.tools.pipeline.PipelineServiceFactory;
import com.google.inject.Inject;
import com.google.light.server.dto.pojo.ChangeLogEntryPojo;
import com.google.light.server.dto.pojo.JobHandlerId;
import com.google.light.server.dto.pojo.LightJobContextPojo;
import com.google.light.server.dto.pojo.RequestScopedValues;
import com.google.light.server.exception.unchecked.pipelineexceptions.PipelineException;
import com.google.light.server.jobs.importjobs.PipelineJobs;
import com.google.light.server.manager.interfaces.ImportManager;
import com.google.light.server.manager.interfaces.JobManager;
import com.google.light.server.persistence.dao.JobDao;
import com.google.light.server.persistence.entity.jobs.JobEntity;
import com.google.light.server.persistence.entity.jobs.JobEntity.JobHandlerType;
import com.google.light.server.persistence.entity.jobs.JobEntity.JobState;
import com.google.light.server.persistence.entity.jobs.JobEntity.JobType;
import com.google.light.server.persistence.entity.jobs.JobEntity.TaskType;
import com.google.light.server.persistence.entity.queue.importflow.ImportJobEntity;
import com.google.light.server.utils.ObjectifyUtils;
import com.googlecode.objectify.Objectify;
import java.util.logging.Logger;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class JobManagerImpl implements JobManager {
  private static final Logger logger = Logger.getLogger(JobManagerImpl.class.getName());

  private JobDao jobDao;
  private ImportManager importManager;

  @Inject
  public JobManagerImpl(JobDao jobDao, ImportManager importManager) {
    this.jobDao = checkNotNull(jobDao, "jobDao");
    this.importManager = checkNotNull(importManager, "importManager");
  }

  @Override
  public JobEntity enqueueImportJob(ImportJobEntity importJobEntity, Long parentJobId,
      Long rootJobId, String promiseHandle) {
    Objectify ofy = ObjectifyUtils.initiateTransaction();
    try {
      // First persisting the importJob.
      ImportJobEntity persistedEntity = importManager.put(ofy, importJobEntity);
      checkNotNull(persistedEntity.getId(), "Import job was not persisted.");

      JobType jobType = rootJobId == null ? JobType.ROOT_JOB : JobType.CHILD_JOB;
      // Now creating a Job from ImportJob without PipelineId.
      JobEntity jobEntity = new JobEntity.Builder()
          .jobType(jobType)
          .parentJobId(parentJobId)
          .rootJobId(rootJobId)
          .jobHandlerType(JobHandlerType.PIPELINE)
          .taskType(TaskType.IMPORT)
          .taskId(importJobEntity.getId())
          .jobState(JobState.PRE_START)
          .build();
      JobEntity savedJobEntity = this.put(ofy, jobEntity,
          new ChangeLogEntryPojo("Enqueuing for Import."));
      ObjectifyUtils.commitTransaction(ofy);

      RequestScopedValues participants = getRequestScopedValues();

      LightJobContextPojo context = new LightJobContextPojo.Builder()
          .ownerId(participants.getOwnerId())
          .actorId(participants.getActorId())
          .jobId(savedJobEntity.getId())
          .rootJobId(savedJobEntity.getRootJobId())
          .promiseHandle(promiseHandle)
          .build();

      /*
       * Enqueuing a Import Job on Pipeline. PipelineId will be updated in the job.
       */
      PipelineService service = PipelineServiceFactory.newPipelineService();
      String pipelineId = service.startNewPipeline(new PipelineJobs.StartJob(), context,
          JOB_BACK_OFF_FACTOR, JOB_BACK_OFF_SECONDS, JOB_MAX_ATTEMPTES);

      logger.info("For ImportJobEntity [" + importJobEntity.getId() + "], created a PipelineId["
          + pipelineId + "] and saved as Job[" + savedJobEntity.getId() + "].");

      return savedJobEntity;
    } catch (Exception e) {
      throw new PipelineException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public JobEntity put(Objectify ofy, JobEntity jobEntity, ChangeLogEntryPojo changeLog) {
    jobEntity.addToChangeLog(changeLog);
    return jobDao.put(ofy, jobEntity);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public JobEntity get(Objectify ofy, Long id) {
    return jobDao.get(ofy, id);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public JobEntity findByJobHandlerId(JobHandlerType jobHandlerType, JobHandlerId jobHandlerId) {
    return jobDao.findByJobHandlerId(jobHandlerType, jobHandlerId);
  }
}
