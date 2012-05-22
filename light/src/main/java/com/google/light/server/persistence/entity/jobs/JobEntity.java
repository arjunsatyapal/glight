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
import static com.google.light.server.utils.LightUtils.convertListOfValuesToWrapperList;
import static com.google.light.server.utils.LightUtils.convertWrapperListToListOfValues;
import static com.google.light.server.utils.LightUtils.getWrapper;
import static com.google.light.server.utils.LightUtils.getWrapperValue;
import static com.google.light.server.utils.LightUtils.isCollectionEmpty;

import com.google.appengine.api.datastore.Text;
import com.google.common.collect.Lists;
import com.google.light.server.annotations.ObjectifyQueryField;
import com.google.light.server.annotations.ObjectifyQueryFieldName;
import com.google.light.server.dto.AbstractDto;
import com.google.light.server.dto.pojo.ChangeLogEntryPojo;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.JobId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.PersonId;
import com.google.light.server.persistence.entity.AbstractPersistenceEntity;
import com.google.light.server.utils.JsonUtils;
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
  private Long parentJobId;
  private Long rootJobId;
  private List<Long> childJobs;
  private List<Long> finishedChildJobs;

  @ObjectifyQueryFieldName("ownerId")
  public static final String OFY_JOB_OWNER_QUERY_STRING = "ownerId = ";

  @ObjectifyQueryField("OFY_JOB_OWNER_QUERY_STRING")
  private Long ownerId;

  // This is used in Objectify Query.
  private JobHandlerType jobHandlerType;

  @ObjectifyQueryFieldName("jobType")
  public static final String OFY_JOB_TYPE_QUERY_STRING = "jobType = ";

  @ObjectifyQueryField("OFY_JOB_TYPE_QUERY_STRING")
  private JobType jobType;

  private TaskType taskType;
  private String taskId;

  // Following values can
  private JobState jobState;

  @Unindexed
  private String stopReason;

  private Text request;
  private Text context;
  private Text response;

  @Unindexed
  @Embedded
  private List<ChangeLogEntryPojo> changeLogs;

  public static Key<JobEntity> generateKey(JobId jobId) {
    return new Key<JobEntity>(JobEntity.class, jobId.getValue());
  }

  @Override
  public Key<JobEntity> getKey() {
    return generateKey(getJobId());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object toDto() {
    // TODO(arjuns): Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  public JobId getJobId() {
    return getWrapper(jobId, JobId.class);
  }

  public JobId getParentJobId() {
    return getWrapper(parentJobId, JobId.class);
  }

  public boolean hasParent() {
    return getParentJobId() == null;
  }

  public JobId getRootJobId() {
    if (isRootJob()) {
      return getJobId();
    }

    return new JobId(rootJobId);
  }

  public boolean isRootJob() {
    return jobType == JobType.ROOT_JOB;
  }

  public JobHandlerType getJobHandlerType() {
    return jobHandlerType;
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

  public Text getRequest() {
    return request;
  }

  @Deprecated
  public Text getContext() {
    checkNotNull(context, "Context cannot be null.");
    return context;
  }

  public <D extends AbstractDto<D>> D getContext(Class<D> clazz) {
    checkNotNull(context, "Context cannot be null.");

    return JsonUtils.getDto(context.getValue(), clazz);
  }

  public void setContext(Text context) {
    this.context = checkNotNull(context, "context");
  }

  public <D extends AbstractDto<D>> void setContext(D dto) {
    checkNotNull(dto, "dto");
    setContext(new Text(dto.toJson()));
  }

  public Text getResponse() {
    return response;
  }

  public <D extends AbstractDto<D>> void setResponse(D responseDto) {
    Text responseText = new Text(checkNotNull(responseDto).toJson());
    this.response = responseText;
  }

  public <D extends AbstractDto<D>> D getResponse(Class<D> clazz) {
    checkNotNull(response, "response cannot be null.");

    return JsonUtils.getDto(response.getValue(), clazz);
  }

  public List<JobId> getPendingChildJobs() {
    if (isCollectionEmpty(childJobs)) {
      return Lists.newArrayList();
    }

    List<JobId> pendingChildJobs = convertListOfValuesToWrapperList(childJobs, JobId.class);
    pendingChildJobs.removeAll(getFinishedChildJobs());
    return pendingChildJobs;
  }

  public boolean hasPendingChildJobs() {
    return !isCollectionEmpty(getPendingChildJobs());
  }

  public List<JobId> getChildJobs() {
    return convertListOfValuesToWrapperList(childJobs, JobId.class);
  }

  public boolean hasChildJobs() {
    return !isCollectionEmpty(getChildJobs());
  }

  @SuppressWarnings("cast")
  public void addChildJob(JobId jobId) {
    if (childJobs == null) {
      childJobs = Lists.newArrayList();
    }

    if (!childJobs.contains(getWrapperValue(jobId))) {
      childJobs.add(((Long) getWrapperValue(jobId)));
    }
  }

  public List<JobId> getFinishedChildJobs() {
    return convertListOfValuesToWrapperList(finishedChildJobs, JobId.class);
  }

  @SuppressWarnings("cast")
  public void addFinishedChildJob(JobId jobId) {
    if (finishedChildJobs == null) {
      finishedChildJobs = Lists.newArrayList();
    }

    if (!finishedChildJobs.contains(getWrapperValue(jobId))) {
      finishedChildJobs.add(((Long) getWrapperValue(jobId)));
    }
  }

  public PersonId getOwnerId() {
    return getWrapper(ownerId, PersonId.class);
  }

  @Override
  public JobEntity validate() {
    checkNotNull(jobType, "jobType");
    checkNotNull(super.creationTimeInMillis, "For JobEntity, creationTime is mandatory.");
    checkNotNull(ownerId, "ownerId");

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
      case IMPORT_BATCH:
      case IMPORT_GOOGLE_DOCUMENT:
      case IMPORT_SYNTHETIC_MODULE:
      case IMPORT_YOUTUBE_VIDEO:
      case IMPORT_YOUTUBE_PLAYLIST:
      case IMPORT_COLLECTION_GOOGLE_COLLECTION:
        checkNotNull(request, "jobRequest cannot be empty for " + taskType);
        checkArgument(jobHandlerType == JobHandlerType.TASK_QUEUE, taskType
            + " is handled by only ." + JobHandlerType.TASK_QUEUE);
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
    private JobType jobType;
    private TaskType taskType;
    private String taskId;
    private JobState jobState;
    private String stopReason;
    private Text request;
    private Text context;
    private Text response;

    private List<JobId> childJobs;
    private List<JobId> finishedChildJobs;
    private PersonId ownerId;

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

    public Builder childJobs(List<JobId> childJobs) {
      this.childJobs = childJobs;
      return this;
    }

    public Builder finishedChildJobs(List<JobId> finishedChildJobs) {
      this.finishedChildJobs = finishedChildJobs;
      return this;
    }

    public Builder ownerId(PersonId ownerId) {
      this.ownerId = ownerId;
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

    this.parentJobId = getWrapperValue(builder.parentJobId);
    this.rootJobId = getWrapperValue(builder.rootJobId);

    this.jobType = builder.jobType;
    this.jobHandlerType = builder.jobHandlerType;

    this.taskType = builder.taskType;
    this.taskId = builder.taskId;

    this.jobState = builder.jobState;
    this.changeLogs = Lists.newArrayList();
    this.request = builder.request;
    this.context = builder.context;
    this.response = builder.response;

    this.stopReason = builder.stopReason;
    this.childJobs = convertWrapperListToListOfValues(builder.childJobs);
    this.finishedChildJobs = convertWrapperListToListOfValues(builder.finishedChildJobs);
    this.ownerId = getWrapperValue(builder.ownerId);
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
    // Task for importing a batch of ExternalIds.
    IMPORT_BATCH(false),

    // Task for Importing a Collection.
    IMPORT_COLLECTION(true),

    // Task for Importing a Google Collection.
    IMPORT_COLLECTION_GOOGLE_COLLECTION(true),

    // Task for Importing Google Document.
    IMPORT_GOOGLE_DOCUMENT(true),

    // Task for Importing a Synthetic Module.
    IMPORT_SYNTHETIC_MODULE(true),
    
 // Task for Importing a YouTube Module.
    IMPORT_YOUTUBE_VIDEO(true),
    
    IMPORT_YOUTUBE_PLAYLIST(true);
    
    private boolean needsResponse;
    
    private TaskType(boolean needsResponse) {
      this.needsResponse = needsResponse;
    }
    
    public boolean needsResponse() {
      return needsResponse;
    }
  }

  // For objectify.
  private JobEntity() {
    super(null, false);
  }
}
