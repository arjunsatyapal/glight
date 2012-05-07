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
package com.google.light.server.persistence.entity.jobs;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.LightPreconditions.checkNull;

import com.google.light.server.constants.PlacementOrder;

import com.google.appengine.api.datastore.Text;
import com.google.appengine.tools.pipeline.JobInfo;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.light.server.constants.RequestParamKeyEnum;
import com.google.light.server.dto.pojo.ChangeLogEntryPojo;
import com.google.light.server.dto.pojo.JobHandlerId;
import com.google.light.server.dto.pojo.longwrapper.JobId;
import com.google.light.server.persistence.entity.AbstractPersistenceEntity;
import com.google.light.server.servlets.path.ServletPathEnum;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Unindexed;
import java.util.List;
import javax.persistence.Embedded;
import javax.persistence.Id;

/**
 * 
 * TODO(arjuns): Add child jobs.
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class JobEntity extends AbstractPersistenceEntity<JobEntity, Object> {
  @Id
  private Long jobId;
  @Embedded
  private JobId parentJobId;
  @Embedded
  private JobId rootJobId;

  // This is used in Objectify Query.
  public static final String OFY_JOB_HANDLER_TYPE = "jobHandlerType";
  private JobHandlerType jobHandlerType;

  // This is used in Objectify Query.
  public static final String OFY_JOB_HANDLER_ID = "jobHandlerId.id";
  @Embedded
  private JobHandlerId jobHandlerId;
  private JobType jobType;

  private TaskType taskType;
  private String taskId;

  // Following values can be modified.
  private JobState jobState;
  
  @Unindexed
  private String stopReason;

  private Text request;
  private Text context;

  @Unindexed
  @Embedded
  private List<ChangeLogEntryPojo> changeLogs;

  public static Key<JobEntity> generateKey(JobId jobId) {
    return new Key<JobEntity>(JobEntity.class, jobId.getValue());
  }

  @Override
  public Key<JobEntity> getKey() {
    return generateKey(getId());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object toDto() {
    // TODO(arjuns): Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  public JobId getId() {
    return new JobId(jobId);
  }

  public JobId getParentJobId() {
    return parentJobId;
  }

  public boolean hasParent() {
    return getParentJobId() == null;
  }

  public JobId getRootJobId() {
    if (isRootJob()) {
      return getId();
    }

    return rootJobId;
  }

  public boolean isRootJob() {
    return jobType == JobType.ROOT_JOB;
  }

  public JobHandlerType getJobHandlerType() {
    return jobHandlerType;
  }

  public JobHandlerId getJobHandlerId() {
    return jobHandlerId;
  }

  public void setJobHandlerId(JobHandlerId jobHandlerId) {
    this.jobHandlerId = checkNotNull(jobHandlerId, "jobHandlerId");
  }

  public JobType getJobType() {
    return jobType;
  }

  public TaskType getTaskType() {
    return taskType;
  }

  public String getTaskId() {
    return taskId;
  }

  public JobState getJobState() {
    return jobState;
  }

  public void setJobState(JobState jobState) {
    this.jobState = checkNotNull(jobState, "jobState");
  }

  public String getStopReason() {
    return stopReason;
  }

  public void setStopReason(String stopReason) {
    this.stopReason = checkNotBlank(stopReason, "stopReason");
  }

  public void setError(String stopReason) {
    this.stopReason = checkNotBlank(stopReason, "stopReason string is blank");
  }

  public List<ChangeLogEntryPojo> getChangeLogs() {
    return changeLogs;
  }

  public void addToChangeLog(ChangeLogEntryPojo changeLog) {
    changeLogs.add(changeLog);
  }

  public String getLocation() {
    String encodedId = Long.toString(jobId);
    String locationUrl = ServletPathEnum.IMPORT_STAGE_DETAIL_SERVLET.get() + "?"
        + RequestParamKeyEnum.JOB_ID.get() + "=" + encodedId;

    return locationUrl;
  }

  public Text getRequest() {
    return request;
  }
  
  public Text getContext() {
    return context;
  }
  
  public void setContext(Text context) {
    this.context = Preconditions.checkNotNull(context, "context");
  }

  @Override
  public JobEntity validate() {
    checkNotNull(jobType, "jobType");

    switch (jobType) {
      case ROOT_JOB:
        checkNull(rootJobId, "For rootJob, rootJobId should be null.");
        break;

      case CHILD_JOB:
        checkNotNull(rootJobId, "For childJob, rootJobId cannot be null.");
        checkNotNull(parentJobId, "For childJob, rootJobId cannot be null.");
        
        if (jobId != null) {
          checkArgument(!jobId.equals(rootJobId),
              "For childJob, jobId and rootJobId should be different.");
        }
        
        break;

      default:
        throw new IllegalArgumentException("Add more checks for Unsupportede JobType : " + jobType);
    }

    checkNotNull(jobHandlerType, "jobHandlerType");

    checkNotNull(taskType, "taskType");
    switch (taskType) {
      case IMPORT:
        checkNotNull(taskId, "taskId");
        checkArgument(jobHandlerType == JobHandlerType.PIPELINE, 
            "Import is handled by only Pipeline.");
        break;

      case DELETE:
        break;

      case IMPORT_GOOGLE_DOC1 :
      case IMPORT_GOOGLE_DOC_BATCH:
        checkNotNull(request, "jobRequest cannot be empty for IMPORT_GOOGLE_DOC");
        checkArgument(jobHandlerType == JobHandlerType.TASK_QUEUE, 
            "IMPORT_GOOGLE_DOC_BATCH is handled by only ." + JobHandlerType.TASK_QUEUE);
        break;
      default:
        throw new IllegalArgumentException("Add more validations for Unsupported JobType : "
            + taskType);
    }

    checkNotNull(jobState, "jobState");

    switch (jobState) {
      case STOPPED_BY_ERROR:
      case STOPPED_BY_REQUEST:
        checkNotBlank(stopReason, "stopReason cannot be blank.");
        break;

      default:
        // TODO(arjuns) : Add more validations here.
        break;
    }

    return this;
  }

  public static class Builder extends AbstractPersistenceEntity.BaseBuilder<Builder> {
    private JobId jobId;
    private JobId parentJobId;
    private JobId rootJobId;

    private JobHandlerType jobHandlerType;
    private JobHandlerId jobHandlerId;
    private JobType jobType;
    private TaskType taskType;
    private String taskId;
    private JobState jobState;
    private String stopReason;
    private Text request;
    private Text context;

    public Builder jobId(JobId jobId) {
      this.jobId = jobId;
      return this;
    }

    public Builder parentJobId(JobId parentJobId) {
      this.parentJobId = parentJobId;
      return this;
    }

    public Builder rootJobId(JobId rootJobId) {
      this.rootJobId = rootJobId;
      return this;
    }

    public Builder jobHandlerType(JobHandlerType jobHandlerType) {
      this.jobHandlerType = jobHandlerType;
      return this;
    }

    public Builder jobHandlerId(JobHandlerId jobHandlerId) {
      this.jobHandlerId = jobHandlerId;
      return this;
    }

    public Builder jobType(JobType jobType) {
      this.jobType = jobType;
      return this;
    }

    public Builder taskType(TaskType taskType) {
      this.taskType = taskType;
      return this;
    }

    public Builder taskId(String taskId) {
      this.taskId = taskId;
      return this;
    }

    public Builder jobState(JobState jobState) {
      this.jobState = jobState;
      return this;
    }

    public Builder stopReason(String stopReason) {
      this.stopReason = stopReason;
      return this;
    }

    public Builder request(Text request) {
      this.request = request;
      return this;
    }
    
    public Builder context(Text context) {
      this.context = context;
      return this;
    }

    @SuppressWarnings("synthetic-access")
    public JobEntity build() {
      return new JobEntity(this).validate();
    }
  }

  @SuppressWarnings("synthetic-access")
  private JobEntity(Builder builder) {
    super(builder, false);

    if (builder.jobId != null) {
      this.jobId = builder.jobId.getValue();
    }

    this.parentJobId = builder.parentJobId;
    this.rootJobId = builder.rootJobId;

    this.jobType = builder.jobType;
    this.jobHandlerType = builder.jobHandlerType;

    this.taskType = builder.taskType;
    this.taskId = builder.taskId;
    
    this.jobState = builder.jobState;
    this.changeLogs = Lists.newArrayList();
    this.request = builder.request;
    this.context = builder.context;
    this.jobHandlerId = builder.jobHandlerId;
    this.stopReason = builder.stopReason;
  }

  public static enum JobHandlerType {
    PIPELINE,
    TASK_QUEUE;
  }

  public static enum JobType {
    CHILD_JOB,
    ROOT_JOB;

  }

  public static enum TaskType {
    DELETE,
    IMPORT,
    IMPORT_GOOGLE_DOC_BATCH,
    IMPORT_GOOGLE_DOC1;
  }

  /**
   * Copy of {@link com.google.appengine.tools.pipeline.JobInfo.State}.
   * Enum for Encapsulating different JobStates.
   * TODO(arjuns): Add test for this.
   * TODO(arjuns): Ensure enum values match.
   */
  public static enum JobState {
    @Deprecated
    RUNNING(1),
    @Deprecated
    COMPLETED_SUCCESSFULLY(2),
    @Deprecated
    STOPPED_BY_REQUEST(3),
    @Deprecated
    STOPPED_BY_ERROR(4),
    @Deprecated
    WAITING_TO_RETRY(5),

    // Additional States.
    @Deprecated
    PRE_START(6),
    ENQUEUED(100),
    CREATING_CHILDS(200),
    WAITING_FOR_CHILD_COMPLETE_NOTIFICATION(300),
    POLLING_FOR_CHILDS(400),
    ALL_CHILDS_COMPLETED_SUCCESSFULLY(500),
    COMPLETE(950),
    READY_FOR_CLEANUP(1000);

    private int level;
    
    private JobState(int level) {
      this.level = level;
    }
    
    public int getLevel() {
      return level;
    }
    
    public PlacementOrder getPlacement(JobState jobState) {
      if (this == jobState) {
        return PlacementOrder.EQUAL;
      } else if (this.getLevel() < jobState.getLevel()) {
        return PlacementOrder.BEFORE;
      } else {
        return PlacementOrder.AFTER;
      }
    }
    
    
    public static JobState fromPipelineJobState(JobInfo.State pipelineJobState) {
      for (JobState curr : JobState.values()) {
        if (curr.name().equals(pipelineJobState.name())) {
          return curr;
        }
      }

      throw new IllegalArgumentException(pipelineJobState.name());
    }
  }

  // For objectify.
  private JobEntity() {
    super(null, false);
  }
}
