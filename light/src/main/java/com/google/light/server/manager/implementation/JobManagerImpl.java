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
import static com.google.light.server.utils.GuiceUtils.getParticipants;

import com.google.appengine.tools.pipeline.PipelineService;
import com.google.appengine.tools.pipeline.PipelineServiceFactory;
import com.google.inject.Inject;
import com.google.light.server.dto.pojo.JobHandlerId;
import com.google.light.server.dto.pojo.LightJobContextPojo;
import com.google.light.server.dto.pojo.RequestScopedValues;
import com.google.light.server.exception.unchecked.pipelineexceptions.PipelineException;
import com.google.light.server.jobs.importjobs.PipelineJobs;
import com.google.light.server.manager.interfaces.JobManager;
import com.google.light.server.persistence.dao.JobDao;
import com.google.light.server.persistence.entity.jobs.JobEntity;
import com.google.light.server.persistence.entity.jobs.JobEntity.JobHandlerType;
import com.google.light.server.persistence.entity.jobs.JobEntity.JobState;
import com.google.light.server.persistence.entity.jobs.JobEntity.JobType;
import com.google.light.server.persistence.entity.queue.importflow.ImportJobEntity;
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

  @Inject
  public JobManagerImpl(JobDao jobDao) {
    this.jobDao = checkNotNull(jobDao, "jobDao");
  }

  @Override
  public JobEntity enqueueImportJob(ImportJobEntity importJobEntity) {
    try {
      checkNotNull(importJobEntity.getId(), "Import job needs to be pre-persisted.");
      
      // First creating a Job from ImportJob without PipelineId.
      JobEntity jobEntity = new JobEntity.Builder()
          .jobHandlerType(JobHandlerType.PIPELINE)
          .jobType(JobType.IMPORT)
          .taskId(importJobEntity.getId())
          .jobState(JobState.PRE_START)
          .build();
      JobEntity savedJobEntity = this.put(jobEntity);

      RequestScopedValues participants = getParticipants();

      LightJobContextPojo context = new LightJobContextPojo.Builder()
          .ownerId(participants.getOwnerId())
          .actorId(participants.getActorId())
          .jobEntity(savedJobEntity)
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
  public JobEntity put(JobEntity jobEntity) {
    return jobDao.put(jobEntity);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public JobEntity get(Long id) {
    return jobDao.get(id);
  }

  /** 
   * {@inheritDoc}
   */
  @Override
  public JobEntity findByJobHandlerId(JobHandlerType jobHandlerType, JobHandlerId jobHandlerId) {
    return jobDao.findByJobHandlerId(jobHandlerType, jobHandlerId);
  }
}
