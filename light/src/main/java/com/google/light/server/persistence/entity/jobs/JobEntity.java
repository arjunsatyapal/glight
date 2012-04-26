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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;

import com.google.light.server.dto.pojo.JobHandlerId;

import com.google.appengine.tools.pipeline.JobInfo;
import com.google.light.server.constants.RequestParamKeyEnum;
import com.google.light.server.persistence.entity.AbstractPersistenceEntity;
import com.google.light.server.servlets.path.ServletPathEnum;
import com.googlecode.objectify.Key;
import javax.persistence.Embedded;
import javax.persistence.Id;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class JobEntity extends AbstractPersistenceEntity<JobEntity, Object> {
  @Id
  private Long jobId;

  // This is used in Objectify Query.
  public static final String OFY_JOB_HANDLER_TYPE = "jobHandlerType";
  private JobHandlerType jobHandlerType;

  // This is used in Objectify Query.
  public static final String OFY_JOB_HANDLER_ID = "jobHandlerId.id";
  @Embedded
  private JobHandlerId jobHandlerId;

  private JobType jobType;
  private String taskId;

  // Following values can be modified.
  private JobState jobState;
  private String stopReason;
  private String error;

  public static Key<JobEntity> generateKey(Long jobId) {
    return new Key<JobEntity>(JobEntity.class, jobId);
  }

  @Override
  public Key<JobEntity> getKey() {
    return generateKey(jobId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object toDto() {
    // TODO(arjuns): Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  public Long getJobId() {
    return jobId;
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

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = checkNotBlank(error, "error string is blank");
  }

  public String getLocation() {
    String encodedId = Long.toString(jobId);
    String locationUrl = ServletPathEnum.IMPORT_STAGE_DETAIL_SERVLET.get() + "?"
        + RequestParamKeyEnum.JOB_ID.get() + "=" + encodedId;

    return locationUrl;
  }

  public static class Builder {
    private Long jobId;
    private JobHandlerType jobHandlerType;
    private JobHandlerId jobHandlerId;
    private JobType jobType;
    private String taskId;
    private JobState jobState;
    private String error;
    private String stopReason;

    public Builder jobId(Long jobId) {
      this.jobId = jobId;
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

    public Builder taskId(String taskId) {
      this.taskId = taskId;
      return this;
    }

    public Builder jobState(JobState jobState) {
      this.jobState = jobState;
      return this;
    }

    public Builder error(String error) {
      this.error = error;
      return this;
    }

    public Builder stopReason(String stopReason) {
      this.stopReason = stopReason;
      return this;
    }

    @SuppressWarnings("synthetic-access")
    public JobEntity build() {
      return new JobEntity(this);
    }
  }

  @SuppressWarnings("synthetic-access")
  private JobEntity(Builder builder) {
    this.jobHandlerType = checkNotNull(builder.jobHandlerType, "jobHandler");

    this.jobId = builder.jobId;

    this.jobType = checkNotNull(builder.jobType, "jobType");
    this.taskId = checkNotBlank(builder.taskId, "taskId");
    this.jobState = checkNotNull(builder.jobState, "jobState");

    switch (jobState) {
      case PRE_START:
        this.jobHandlerId = builder.jobHandlerId;
        break;

      case COMPLETED_SUCCESSFULLY:
        setJobHandlerId(builder.jobHandlerId);
        break;

      case STOPPED_BY_ERROR:
        setError(builder.error);
        setJobHandlerId(builder.jobHandlerId);
        break;

      case STOPPED_BY_REQUEST:
        setStopReason(builder.stopReason);
        setJobHandlerId(builder.jobHandlerId);
        break;

      default:
        setJobHandlerId(builder.jobHandlerId);
        // do nothing.
    }
  }

  public static enum JobHandlerType {
    PIPELINE;
  }

  public static enum JobType {
    DELETE,
    IMPORT;
  }

  /**
   * Copy of {@link com.google.appengine.tools.pipeline.JobInfo.State}.
   * Enum for Encapsulating different JobStates.
   * TODO(arjuns): Add test for this.
   * TODO(arjuns): Ensure enum values match.
   */
  public static enum JobState {
    RUNNING,
    COMPLETED_SUCCESSFULLY,
    STOPPED_BY_REQUEST,
    STOPPED_BY_ERROR,
    WAITING_TO_RETRY,

    // Additional States.
    PRE_START;

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
  }
}
