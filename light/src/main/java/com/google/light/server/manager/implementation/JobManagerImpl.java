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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.exception.ExceptionType.SERVER_GUICE_INJECTION;
import static com.google.light.server.utils.LightPreconditions.checkJobId;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.LightPreconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkTxnIsRunning;
import static com.google.light.server.utils.LightUtils.getNow;
import static com.google.light.server.utils.ObjectifyUtils.repeatInTransaction;

import com.google.appengine.api.datastore.Text;
import com.google.inject.Inject;
import com.google.light.server.constants.QueueEnum;
import com.google.light.server.dto.AbstractDto;
import com.google.light.server.dto.importresource.ImportBatchWrapper;
import com.google.light.server.dto.importresource.ImportExternalIdDto;
import com.google.light.server.dto.pojo.ChangeLogEntryPojo;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.JobId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.PersonId;
import com.google.light.server.jobs.handlers.collectionjobs.gdoccollection.ImportCollectionGoogleDocContext;
import com.google.light.server.jobs.handlers.collectionjobs.youtubeplaylist.ImportCollectionYouTubePlaylistContext;
import com.google.light.server.jobs.handlers.modulejobs.gdocument.ImportModuleGoogleDocJobContext;
import com.google.light.server.jobs.handlers.modulejobs.synthetic.ImportModuleSyntheticModuleJobContext;
import com.google.light.server.manager.interfaces.JobManager;
import com.google.light.server.manager.interfaces.NotificationManager;
import com.google.light.server.manager.interfaces.QueueManager;
import com.google.light.server.persistence.dao.JobDao;
import com.google.light.server.persistence.entity.jobs.JobEntity;
import com.google.light.server.persistence.entity.jobs.JobEntity.JobHandlerType;
import com.google.light.server.persistence.entity.jobs.JobEntity.JobType;
import com.google.light.server.persistence.entity.jobs.JobEntity.TaskType;
import com.google.light.server.persistence.entity.jobs.JobState;
import com.google.light.server.serveronlypojos.GAEQueryWrapper;
import com.google.light.server.utils.GuiceUtils;
import com.google.light.server.utils.Transactable;
import com.googlecode.objectify.Objectify;
import java.util.Collection;
import java.util.Map;
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
  private NotificationManager notificationManager;
  private QueueManager queueManager;

  @Inject
  public JobManagerImpl(JobDao jobDao, NotificationManager notificationManager,
      QueueManager queueManager) {
    this.jobDao = checkNotNull(jobDao, "jobDao");
    this.notificationManager = checkNotNull(notificationManager, SERVER_GUICE_INJECTION,
        "notificationManager");
    this.queueManager = checkNotNull(queueManager, SERVER_GUICE_INJECTION, "queueManager");
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
  public JobEntity get(Objectify ofy, JobId jobId) {
    return jobDao.get(ofy, jobId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public JobEntity createGoogleDocImportChildJob(Objectify ofy,
      ImportModuleGoogleDocJobContext context,
      JobId parentJobId, JobId rootJobId) {
    checkTxnIsRunning(ofy);
    checkJobId(parentJobId);
    checkJobId(rootJobId);

    Text text = new Text(context.toJson());

    JobEntity jobEntity = new JobEntity.Builder()
        .jobType(JobType.CHILD_JOB)
        .parentJobId(parentJobId)
        .rootJobId(rootJobId)
        .jobHandlerType(JobHandlerType.TASK_QUEUE)
        .taskType(TaskType.IMPORT_GOOGLE_DOCUMENT)
        .jobState(JobState.ENQUEUED)
        .request(text)
        .context(text)
        .creationTime(getNow())
        .ownerId(GuiceUtils.getOwnerId())
        .build();
    JobEntity savedJobEntity = this.put(ofy, jobEntity,
        new ChangeLogEntryPojo("Enqueuing Request."));
    // queueManager.enqueueGoogleDocInteractionJob(ofy, jobEntity.getJobId());

    logger.info("Successfully created Job[" + savedJobEntity.getJobId() + "] for ["
        + TaskType.IMPORT_GOOGLE_DOCUMENT + "].");
    return savedJobEntity;
  }

  @Override
  public void enqueueGoogleDocInteractionJob(Objectify ofy, JobId jobId) {
    checkTxnIsRunning(ofy);
    queueManager.enqueueGoogleDocInteractionJob(ofy, jobId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <D extends AbstractDto<D>> JobEntity enqueueCompleteJob(final JobId jobId,
      final D responseDto,
      final String message) {
    checkNotBlank(message, "message cannot be null");
    checkNotNull(responseDto, "responseDto cannot be null");

    JobEntity jobEntity = repeatInTransaction(new Transactable<JobEntity>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public JobEntity run(Objectify ofy) {
        JobEntity jobEntity = get(ofy, jobId);
        jobEntity.setJobState(JobState.COMPLETE);
        
        checkNotNull(responseDto, "found null for " + jobEntity);
        jobEntity.setResponse(responseDto);
        
        put(ofy, jobEntity, new ChangeLogEntryPojo(message));

        if (!jobEntity.isRootJob()) {
          notificationManager.notifyChildCompletionEvent(ofy, jobEntity.getParentJobId(),
              jobEntity.getJobId());
        }

        return jobEntity;
      }
    });

    return jobEntity;

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void enqueueJobForPolling(Objectify ofy, JobId jobId) {
    checkTxnIsRunning(ofy);
    queueManager.enqueuePollingJob(ofy, jobId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Map<JobId, JobEntity> findListOfJobs(Collection<JobId> listOfJobIds) {
    return jobDao.findListOfJobs(listOfJobIds);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void enqueueLightJob(Objectify ofy, JobId jobId) {
    checkTxnIsRunning(ofy);
    queueManager.enqueueLightJob(ofy, jobId);
    logger.info("Enqueued LightJob[" + jobId + "].");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void enqueueLightJobWithoutTxn(JobId jobId) {
    queueManager.enqueueLightJobWithoutTxn(jobId);
    logger.info("Enqueued LightJob[" + jobId + "].");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public JobId createImportBatchJob(Objectify ofy, final ImportBatchWrapper jobRequest,
      JobState jobState) {
    checkTxnIsRunning(ofy);
    Text jobRequestText = new Text(jobRequest.toJson());
    JobEntity jobEntity = new JobEntity.Builder()
        .jobType(JobType.ROOT_JOB)
        .jobHandlerType(JobHandlerType.TASK_QUEUE)
        .taskType(TaskType.IMPORT_BATCH)
        .jobState(jobState)
        .request(jobRequestText)
        .context(jobRequestText)
        .creationTime(getNow())
        .ownerId(GuiceUtils.getOwnerId())
        .build();
    JobEntity savedJobEntity = put(ofy, jobEntity,
        new ChangeLogEntryPojo("Creating ImportBatchJob with JobState : " + jobState));

    logger.info("Successfully created " + TaskType.IMPORT_BATCH + ""
        + "Job[" + savedJobEntity.getJobId() + "].");
    return savedJobEntity.getJobId();

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void enqueueImportChildJob(Objectify ofy, JobId parentJobId, JobId childJobId,
      ImportExternalIdDto externalIdDto, QueueEnum queue) {
    checkTxnIsRunning(ofy);
    checkJobId(parentJobId);
    checkJobId(childJobId);

    JobEntity parentJob = this.get(ofy, parentJobId);
    checkNotNull(parentJob, "Parent[" + parentJobId + "] was not found.");
    checkArgument(parentJob.getTaskType() == TaskType.IMPORT_BATCH
        || parentJob.getTaskType() == TaskType.IMPORT_COLLECTION_GOOGLE_COLLECTION
        || parentJob.getTaskType() == TaskType.IMPORT_YOUTUBE_PLAYLIST,
        "Parent job was expected to be of type : " + TaskType.IMPORT_BATCH + "/"
            + TaskType.IMPORT_COLLECTION_GOOGLE_COLLECTION + "/"
            + TaskType.IMPORT_YOUTUBE_PLAYLIST);

    parentJob.addChildJob(childJobId);

    updateParentJobContext(externalIdDto, parentJob);

    this.put(ofy, parentJob, new ChangeLogEntryPojo("Adding child " + childJobId));

    switch (queue) {
      case GDOC_INTERACTION:
        queueManager.enqueueGoogleDocInteractionJob(ofy, childJobId);
        break;

      case LIGHT:
      case LIGHT_NOTIFICATIONS:
      case LIGHT_POLLING:
        queueManager.enqueueLightJob(ofy, childJobId);
        break;
      default:
        throw new IllegalArgumentException("Add more code for queue : " + queue);
    }
  }

  /**
   * @param externalIdDto
   * @param parentJob
   */
  @SuppressWarnings("deprecation")
  private void updateParentJobContext(ImportExternalIdDto externalIdDto, JobEntity parentJob) {
    TaskType taskType = parentJob.getTaskType();

    switch (taskType) {
      case IMPORT_BATCH:
        ImportBatchWrapper importBatchContext = null;

        // Get existing context from Parent Job Entity.
        if (parentJob.getContext() == null) {
          importBatchContext = new ImportBatchWrapper();
        } else {
          importBatchContext = parentJob.getContext(ImportBatchWrapper.class);
        }

        checkNotNull(importBatchContext, "externalIdListWrapper should not be null.");
        // Now update context with new information and then persist it back.
        importBatchContext.addImportModuleDto(externalIdDto);
        parentJob.setContext(importBatchContext);
        break;

      case IMPORT_COLLECTION_GOOGLE_COLLECTION:

        // Get existing context from Parent Job Entity.
        ImportCollectionGoogleDocContext importCollectionGDocContext = parentJob.getContext(
            ImportCollectionGoogleDocContext.class);

        checkNotNull(importCollectionGDocContext, "importCollectionGDocContext should not be null.");
        // Now update context with new information and then persist it back.
        importCollectionGDocContext.addImportModuleDto(externalIdDto);
        parentJob.setContext(importCollectionGDocContext);
        break;

      case IMPORT_YOUTUBE_PLAYLIST:
        // Get existing context from Parent Job Entity.
        ImportCollectionYouTubePlaylistContext ytPlaylistJobContext = parentJob.getContext(
            ImportCollectionYouTubePlaylistContext.class);

        checkNotNull(ytPlaylistJobContext, "ytPlaylistJobContext should not be null.");
        // Now update context with new information and then persist it back.
        ytPlaylistJobContext.addImportModuleDto(externalIdDto);
        parentJob.setContext(ytPlaylistJobContext);
        break;
      default:
        throw new IllegalStateException("Unsupported parentJob for : " + taskType);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public JobEntity createSyntheticModuleJob(Objectify ofy,
      ImportModuleSyntheticModuleJobContext context, TaskType taskType,
      JobId parentJobId, JobId rootJobId) {
    checkTxnIsRunning(ofy);
    checkJobId(parentJobId);
    checkJobId(rootJobId);

    Text text = new Text(context.toJson());

    JobEntity jobEntity = new JobEntity.Builder()
        .jobType(JobType.CHILD_JOB)
        .parentJobId(parentJobId)
        .rootJobId(rootJobId)
        .jobHandlerType(JobHandlerType.TASK_QUEUE)
        .taskType(taskType)
        .jobState(JobState.ENQUEUED)
        .request(text)
        .context(text)
        .creationTime(getNow())
        .ownerId(GuiceUtils.getOwnerId())
        .build();
    JobEntity savedJobEntity = this.put(ofy, jobEntity,
        new ChangeLogEntryPojo("Enqueuing Synthetic Module Request."));
    // queueManager.enqueueLightJob(ofy, jobEntity.getJobId());

    logger.info("Successfully created Job[" + savedJobEntity.getJobId() + "] for ["
        + TaskType.IMPORT_SYNTHETIC_MODULE + "].");
    return savedJobEntity;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public JobEntity createImportCollectionGoogleCollectionJob(Objectify ofy,
      ImportCollectionGoogleDocContext jobRequest, JobId parentJobId, JobId rootJobId) {
    checkTxnIsRunning(ofy);
    checkJobId(parentJobId);
    checkJobId(rootJobId);

    Text text = new Text(jobRequest.toJson());

    JobEntity jobEntity = new JobEntity.Builder()
        .jobType(JobType.CHILD_JOB)
        .parentJobId(parentJobId)
        .rootJobId(rootJobId)
        .jobHandlerType(JobHandlerType.TASK_QUEUE)
        .taskType(TaskType.IMPORT_COLLECTION_GOOGLE_COLLECTION)
        .jobState(JobState.ENQUEUED)
        .request(text)
        .context(text)
        .creationTime(getNow())
        .ownerId(GuiceUtils.getOwnerId())
        .build();
    JobEntity savedJobEntity =
        this.put(ofy, jobEntity,
            new ChangeLogEntryPojo("Enqueuing " + TaskType.IMPORT_COLLECTION_GOOGLE_COLLECTION
                + " job"));

    logger.info("Successfully created Job[" + savedJobEntity.getJobId() + "] for ["
        + TaskType.IMPORT_COLLECTION_GOOGLE_COLLECTION + "].");
    return savedJobEntity;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public JobEntity createImportCollectionYouTubePlaylistJob(Objectify ofy,
      ImportCollectionYouTubePlaylistContext jobRequest, JobId parentJobId, JobId rootJobId) {
    checkTxnIsRunning(ofy);
    checkJobId(parentJobId);
    checkJobId(rootJobId);

    Text text = new Text(jobRequest.toJson());

    JobEntity jobEntity = new JobEntity.Builder()
        .jobType(JobType.CHILD_JOB)
        .parentJobId(parentJobId)
        .rootJobId(rootJobId)
        .jobHandlerType(JobHandlerType.TASK_QUEUE)
        .taskType(TaskType.IMPORT_YOUTUBE_PLAYLIST)
        .jobState(JobState.ENQUEUED)
        .request(text)
        .context(text)
        .creationTime(getNow())
        .ownerId(GuiceUtils.getOwnerId())
        .build();
    JobEntity savedJobEntity = this.put(ofy, jobEntity,
        new ChangeLogEntryPojo("Enqueuing " + TaskType.IMPORT_YOUTUBE_PLAYLIST + " job"));

    logger.info("Successfully created Job[" + savedJobEntity.getJobId() + "] for ["
        + TaskType.IMPORT_YOUTUBE_PLAYLIST + "].");
    return savedJobEntity;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public GAEQueryWrapper<JobEntity> findRootJobsCreatedByMe(PersonId ownerId, String startIndex,
      int maxResults) {
    return jobDao.findRootJobsByOwnerId(ownerId, startIndex, maxResults);

  }
}
