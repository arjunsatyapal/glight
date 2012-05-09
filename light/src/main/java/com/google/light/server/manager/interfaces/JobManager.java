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
package com.google.light.server.manager.interfaces;

import com.google.light.server.dto.pojo.ChangeLogEntryPojo;
import com.google.light.server.dto.pojo.longwrapper.JobId;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocImportBatchJobContext;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocImportJobContext;
import com.google.light.server.persistence.entity.jobs.JobEntity;
import com.googlecode.objectify.Objectify;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/**
 *
 * 
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */
public interface JobManager {
  /**
   * Enqueues a Batch job for importing Multiple Google Docs.
   */
  public JobEntity enqueueGoogleDocImportBatchJob(
      GoogleDocImportBatchJobContext docImportBatchJobRequest);
  
  /**
   * This is enqueued by a Batch Job for each google doc that needs to be downloaded and published.
   */
  public JobEntity enqueueGoogleDocImportChildJob(Objectify ofy, 
      GoogleDocImportJobContext docImportJobRequest, JobId parentJobId, JobId rootJobId);
  
  /**
   * This is used for complete Job. This will do two things
   * <br> 1. Mark the job Pointed by JobId as Finalized.
   * <br> 2. If it was a child Job, it will try to notify Parent Once. If it is successful, ok else
   * parents are expected to poll for their Child Job Status.
   * @param jobId
   * @return
   */
  public JobEntity enqueueCompleteJob(JobId jobId, String message);
  
  /**
   * This will enqueue a Job in {@link com.google.light.server.constants.QueueEnum#LIGHT_POLLING}
   * queue for polling for different resources.
   * 
   * @param jobId
   * @return
   */
  public void enqueueJobForPolling(Objectify ofy, JobId jobId);
  
  public void enqueueLightJob(Objectify ofy, JobId jobId);
  
  public void enqueueLightJobWithoutTxn(JobId jobId);

  
  public void enqueueGoogleDocInteractionJob(Objectify ofy, JobId jobId);
  
  public JobEntity put(@Nullable Objectify ofy, JobEntity jobEntity, ChangeLogEntryPojo changeLog);
  
  public JobEntity get(Objectify ofy, JobId jobId);
  
  public void handleJob(JobId jobId);
  
  public Map<JobId, JobEntity> findListOfJobs(List<JobId> listOfJobIds);
}
