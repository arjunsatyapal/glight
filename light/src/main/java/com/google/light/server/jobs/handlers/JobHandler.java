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
package com.google.light.server.jobs.handlers;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkNotNull;
import static com.google.light.server.utils.ObjectifyUtils.repeatInTransaction;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.light.server.dto.pojo.ChangeLogEntryPojo;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.JobId;
import com.google.light.server.exception.ExceptionType;
import com.google.light.server.exception.unchecked.taskqueue.WaitForChildsToComplete;
import com.google.light.server.jobs.handlers.batchjobs.ImportBatchJobHandler;
import com.google.light.server.jobs.handlers.collectionjobs.gdoccollection.ImportCollectionGoogleDocJobHandler;
import com.google.light.server.jobs.handlers.collectionjobs.youtubeplaylist.ImportCollectionYouTubePlaylistHandler;
import com.google.light.server.jobs.handlers.modulejobs.gdocument.ImportModuleGoogleDocJobHandler;
import com.google.light.server.jobs.handlers.modulejobs.synthetic.ImportModuleSyntheticModuleJobHandler;
import com.google.light.server.jobs.handlers.modulejobs.youtube.ImportModuleYouTubeVideoJobHandler;
import com.google.light.server.manager.interfaces.JobManager;
import com.google.light.server.persistence.entity.jobs.JobEntity;
import com.google.light.server.persistence.entity.jobs.JobEntity.TaskType;
import com.google.light.server.persistence.entity.jobs.JobState;
import com.google.light.server.utils.Transactable;
import com.googlecode.objectify.Objectify;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class JobHandler {
  private static final Logger logger = Logger.getLogger(JobHandler.class.getName());

  private JobManager jobManager;
  private ImportModuleGoogleDocJobHandler importModuleGDocHandler;
  private ImportModuleSyntheticModuleJobHandler importModuleSyntheticJobHandler;
  private ImportModuleYouTubeVideoJobHandler importModuleYouTubeVideoJobHandler;
  private ImportBatchJobHandler importBatchJobHandler;
  private ImportCollectionGoogleDocJobHandler importCollectionGDocHandler;
  private ImportCollectionYouTubePlaylistHandler importYouTubePlaylistHandler;

  @Inject
  public JobHandler(JobManager jobManager, ImportModuleGoogleDocJobHandler importModuleGDocHandler,
      ImportModuleSyntheticModuleJobHandler importModuleSyntheticJobHandler,
      ImportModuleYouTubeVideoJobHandler importModuleYouTubeVideoJobHandler,
      ImportBatchJobHandler importBatchJobHandler,
      ImportCollectionGoogleDocJobHandler importCollectionGDocHandler,
      ImportCollectionYouTubePlaylistHandler importYouTubePlaylistHandler) {
    this.jobManager = checkNotNull(jobManager, "jobManager");
    this.importModuleGDocHandler = checkNotNull(importModuleGDocHandler,
        "importModuleGDocHandler");
    this.importModuleSyntheticJobHandler = checkNotNull(importModuleSyntheticJobHandler,
        "importModuleSyntheticJobHandler");
    this.importModuleYouTubeVideoJobHandler = checkNotNull(importModuleYouTubeVideoJobHandler,
        "importModuleYouTubeVideoJobHandler");
    this.importCollectionGDocHandler = checkNotNull(importCollectionGDocHandler,
        "importCollectionGDocHandler");
    this.importBatchJobHandler = checkNotNull(importBatchJobHandler, "importBatchJobHandler");
    this.importYouTubePlaylistHandler = checkNotNull(importYouTubePlaylistHandler,
        "importYouTubePlaylistHandler");

  }

  @SuppressWarnings("deprecation")
  public void handleJob(JobId jobId) {
    checkNotNull(jobId, ExceptionType.CLIENT_PARAMETER, "jobId cannot be null");
    jobId.validate();

    JobEntity jobEntity = jobManager.get(null, jobId);
    checkNotNull(jobEntity, ExceptionType.CLIENT_PARAMETER, "No job found for : " + jobId);

    logger.info("Inside job[" + jobId + "], jobState[" + jobEntity.getJobState()
        + "], taskType[" + jobEntity.getTaskType() + "].");
    if (jobEntity.getContext() != null) {
      logger.info(jobEntity.getContext().getValue());
    }

    JobState jobState = jobEntity.getJobState();
    switch (jobState) {
      case POLLING_FOR_CHILDS:
        pollForChilds(jobEntity);
        return;

      case WAITING_FOR_CHILD_COMPLETE_NOTIFICATION:
      case PRE_START:
        throw new IllegalStateException("This should not be called but was called for "
            + jobState + " for " + jobId);

      case ENQUEUED:
      case ALL_CHILDS_COMPLETED:
        // Handling is done by type of handler.
        break;
      //
      case COMPLETE:
        // Do nothing.
        return;

      default:
        throw new IllegalStateException("Unsupported state : " + jobEntity.getJobState());
    }

    TaskType taskType = jobEntity.getTaskType();
    switch (taskType) {
      case IMPORT_BATCH:
        importBatchJobHandler.handle(jobEntity);
        break;

      case IMPORT_GOOGLE_DOCUMENT:
        importModuleGDocHandler.handle(jobEntity);
        break;

      case IMPORT_SYNTHETIC_MODULE:
        importModuleSyntheticJobHandler.handle(jobEntity);
        break;

      case IMPORT_YOUTUBE_VIDEO:
        importModuleYouTubeVideoJobHandler.handle(jobEntity);
        break;

      case IMPORT_YOUTUBE_PLAYLIST:
        importYouTubePlaylistHandler.handle(jobEntity);
        break;

      case IMPORT_COLLECTION_GOOGLE_COLLECTION:
        importCollectionGDocHandler.handle(jobEntity);
        break;

      default:
        throw new IllegalStateException("Unsupported TaskType : " + jobEntity.getTaskType());
    }
  }

  private void pollForChilds(final JobEntity jobEntity) {
    List<JobId> listOfChildJobIds = jobEntity.getPendingChildJobs();
    Map<JobId, JobEntity> map = jobManager.findListOfJobs(listOfChildJobIds);

    boolean allChildsComplete = true;
    for (JobEntity currChildJob : map.values()) {
      if (currChildJob.getJobState() == JobState.COMPLETE) {
        jobEntity.addFinishedChildJob(currChildJob.getJobId());
      } else {
        allChildsComplete = false;
      }
    }

    final boolean markAllChildsComplete = allChildsComplete;
    repeatInTransaction("Polling for childs for " + jobEntity.getJobId(),
        new Transactable<Void>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public Void run(Objectify ofy) {
        String changeLog = null;
        if (markAllChildsComplete) {
          jobEntity.setJobState(JobState.ALL_CHILDS_COMPLETED);
          jobManager.enqueueLightJob(ofy, jobEntity.getJobId());
          changeLog = "All Childs complete";
        } else {
          changeLog = "Waiting for childs : " + Iterables.toString(jobEntity.getPendingChildJobs());
        }

        jobManager.put(ofy, jobEntity, new ChangeLogEntryPojo(changeLog));
        return null;
      }
    });

    if (!allChildsComplete) {
      throw new WaitForChildsToComplete(jobEntity.getPendingChildJobs());
    }
  }
}
