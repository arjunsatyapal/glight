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
package com.google.light.server.utils;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.GuiceUtils.getInstance;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;

import com.google.appengine.tools.pipeline.JobInfo;
import com.google.appengine.tools.pipeline.PipelineService;
import com.google.appengine.tools.pipeline.PipelineServiceFactory;
import com.google.light.server.exception.unchecked.pipelineexceptions.PipelineException;
import com.google.light.server.manager.interfaces.JobManager;
import com.google.light.server.persistence.entity.jobs.JobEntity;
import com.google.light.server.persistence.entity.jobs.JobEntity.JobState;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class PipelineUtils {
  /**
   * Get status of a Pipeline.
   */
  public static JobInfo getPipelineJobInfo(String pipelineId) {
    checkNotNull(pipelineId, "pipelineId");

    try {
      PipelineService service = PipelineServiceFactory.newPipelineService();
      return service.getJobInfo(pipelineId);
    } catch (Exception e) {
      throw new PipelineException(e);
    }
  }

  public static void stopPipeline(String pipelineId, String stopReason) {
    checkNotBlank(stopReason, "stopReason");
    try {
      PipelineService service = PipelineServiceFactory.newPipelineService();
      service.stopPipeline(pipelineId);
    } catch (Exception e) {
      throw new PipelineException(e);
    }
  }

  public static void updateJobDetails(String pipelineId, JobEntity jobEntity) {
    checkNotNull(jobEntity, "jobEntity");

    JobManager jobManager = getInstance(JobManager.class);
    JobInfo jobInfo = getPipelineJobInfo(pipelineId);

    JobState jobState = JobState.fromPipelineJobState(jobInfo.getJobState());
    jobEntity.setJobState(jobState);

    switch (jobState) {
      case COMPLETED_SUCCESSFULLY:
        break;

      case STOPPED_BY_ERROR:
        jobEntity.setError(jobInfo.getError());
        break;

      case RUNNING:
        //$FALL-THROUGH$
      case STOPPED_BY_REQUEST:
        //$FALL-THROUGH$
      case WAITING_TO_RETRY:
        // Do nothing.
        break;
      default:
        String errorMessage = "Invalid jobState : " + jobState;
        stopPipeline(pipelineId, errorMessage);
        throw new IllegalArgumentException(errorMessage);
    }

    // Updating for non-default JobState.
    jobManager.put(jobEntity);
  }

  public static void deletePipeline(String pipelineId, boolean force) {
    checkNotBlank(pipelineId, "pipelineId");
    try {
      PipelineService service = PipelineServiceFactory.newPipelineService();
      service.deletePipelineRecords(pipelineId, force, false /* async */);
    } catch (Exception e) {
      throw new PipelineException(e);
    }
  }

  // Utility class.
  private PipelineUtils() {
  }
}
