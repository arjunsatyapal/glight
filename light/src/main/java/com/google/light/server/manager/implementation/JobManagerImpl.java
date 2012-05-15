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
import static com.google.light.server.jobs.JobUtils.updateJobContext;
import static com.google.light.server.utils.LightPreconditions.checkJobId;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.LightPreconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkTxnIsRunning;

import com.google.light.server.jobs.handlers.modulejobs.ImportModuleSyntheticModuleJobContext;

import com.google.appengine.api.datastore.Text;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.light.server.constants.QueueEnum;
import com.google.light.server.dto.AbstractDto;
import com.google.light.server.dto.importresource.ImportBatchWrapper;
import com.google.light.server.dto.importresource.ImportExternalIdDto;
import com.google.light.server.dto.pojo.ChangeLogEntryPojo;
import com.google.light.server.dto.pojo.tree.AbstractTreeNode.TreeNodeType;
import com.google.light.server.dto.pojo.tree.GoogleDocTree;
import com.google.light.server.dto.pojo.tree.collection.CollectionTreeNodeDto;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.JobId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.ModuleId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.PersonId;
import com.google.light.server.dto.pojo.typewrapper.stringwrapper.ExternalId;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocImportBatchJobContext;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocImportBatchJobContext.GoogleDocImportBatchJobState;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocInfoDto;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocResourceId;
import com.google.light.server.jobs.google.gdoc.GoogleDocJobs;
import com.google.light.server.jobs.handlers.collectionjobs.ImportCollectionGoogleDocContext;
import com.google.light.server.jobs.handlers.modulejobs.ImportModuleGoogleDocJobContext;
import com.google.light.server.manager.interfaces.CollectionManager;
import com.google.light.server.manager.interfaces.JobManager;
import com.google.light.server.manager.interfaces.ModuleManager;
import com.google.light.server.manager.interfaces.NotificationManager;
import com.google.light.server.manager.interfaces.QueueManager;
import com.google.light.server.persistence.dao.JobDao;
import com.google.light.server.persistence.entity.collection.CollectionEntity;
import com.google.light.server.persistence.entity.jobs.JobEntity;
import com.google.light.server.persistence.entity.jobs.JobEntity.JobHandlerType;
import com.google.light.server.persistence.entity.jobs.JobEntity.JobType;
import com.google.light.server.persistence.entity.jobs.JobEntity.TaskType;
import com.google.light.server.persistence.entity.jobs.JobState;
import com.google.light.server.thirdparty.clients.google.gdata.gdoc.DocsServiceWrapper;
import com.google.light.server.utils.GuiceUtils;
import com.google.light.server.utils.ObjectifyUtils;
import com.googlecode.objectify.Objectify;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
  private CollectionManager collectionManager;
  private ModuleManager moduleManager;
  private NotificationManager notificationManager;
  private QueueManager queueManager;
  private GoogleDocJobs googleDocJobs;
  private Provider<DocsServiceWrapper> docsServiceProvider;

  @Inject
  public JobManagerImpl(JobDao jobDao, CollectionManager collectionManager,
      ModuleManager moduleManager,
      NotificationManager notificationManager,
      QueueManager queueManager, GoogleDocJobs googleDocJobs,
      Provider<DocsServiceWrapper> docsServiceProvider) {
    this.jobDao = checkNotNull(jobDao, "jobDao");
    this.collectionManager =
        checkNotNull(collectionManager, SERVER_GUICE_INJECTION, "collectionManager");
    this.moduleManager = checkNotNull(moduleManager, SERVER_GUICE_INJECTION, "moduleManager");
    this.notificationManager = checkNotNull(notificationManager, SERVER_GUICE_INJECTION,
        "notificationManager");
    this.queueManager = checkNotNull(queueManager, SERVER_GUICE_INJECTION, "queueManager");
    this.googleDocJobs = checkNotNull(googleDocJobs, SERVER_GUICE_INJECTION, "googleDocJobs");
    this.docsServiceProvider = checkNotNull(docsServiceProvider,
        SERVER_GUICE_INJECTION, "docsServiceProvider");
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
  public JobEntity enqueueGoogleDocImportBatchJob(GoogleDocImportBatchJobContext jobRequest) {
    Objectify ofy = ObjectifyUtils.initiateTransaction();
    try {
      jobRequest.setState(GoogleDocImportBatchJobState.ENQUEUED);
      Text jobRequestText = new Text(jobRequest.toJson());

      JobEntity jobEntity = new JobEntity.Builder()
          .jobType(JobType.ROOT_JOB)
          .jobHandlerType(JobHandlerType.TASK_QUEUE)
          .taskType(TaskType.IMPORT_GOOGLE_DOC_BATCH)
          .jobState(JobState.ENQUEUED)
          .request(jobRequestText)
          .context(jobRequestText)
          .build();
      JobEntity savedJobEntity = this.put(ofy, jobEntity,
          new ChangeLogEntryPojo("Enqueuing Request."));
      queueManager.enqueueGoogleDocInteractionJob(ofy, jobEntity.getJobId());
      ObjectifyUtils.commitTransaction(ofy);

      logger.info("Successfully created IMPORT_GOOGLE_DOC_BATCH Job[" + savedJobEntity.getJobId());
      return savedJobEntity;
    } finally {
      ObjectifyUtils.rollbackTransactionIfStillActive(ofy);
    }
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
        .taskType(TaskType.IMPORT_GOOGLE_DOC1)
        .jobState(JobState.ENQUEUED)
        .request(text)
        .context(text)
        .build();
    JobEntity savedJobEntity = this.put(ofy, jobEntity,
        new ChangeLogEntryPojo("Enqueuing Request."));
    // queueManager.enqueueGoogleDocInteractionJob(ofy, jobEntity.getJobId());

    logger.info("Successfully created Job[" + savedJobEntity.getJobId() + "] for ["
        + TaskType.IMPORT_GOOGLE_DOC1 + "].");
    return savedJobEntity;
  }

  @Override
  public void enqueueGoogleDocInteractionJob(Objectify ofy, JobId jobId) {
    checkTxnIsRunning(ofy);
    queueManager.enqueueGoogleDocInteractionJob(ofy, jobId);
  }

  /**
   * @param possibleNewNode
   * @param collectionParentNode
   * @param externalId
   */
  private void appendLeafNodeToCollectionTree(
      GoogleDocImportBatchJobContext gdocImportBatchContext,
      CollectionTreeNodeDto.Builder newLeafNodeBuilder,
      CollectionTreeNodeDto collectionParentNode, ExternalId externalId) {
    ModuleId moduleId = gdocImportBatchContext.getModuleIdForExternalId(externalId);
    checkNotNull(moduleId, "This should not happen. "
        + "Could not find moduleId for externalId : " + externalId);
    CollectionTreeNodeDto newLeafNode = newLeafNodeBuilder.moduleId(moduleId).build();
    collectionParentNode.addChildren(newLeafNode);
  }

  private void createNewCollection(GoogleDocImportBatchJobContext gdocImportBatchContext,
      JobEntity jobEntity) {
    if (gdocImportBatchContext.getCollectionId() != null) {
      // No need to create new collection.
      return;
    }
    Objectify ofy = ObjectifyUtils.initiateTransaction();
    try {
      List<PersonId> owners = Lists.newArrayList(GuiceUtils.getOwnerId());

      CollectionEntity collectionEntity = collectionManager.reserveCollectionId(ofy,
          owners, gdocImportBatchContext.getCollectionTitle());
      gdocImportBatchContext.setCollectionId(collectionEntity.getCollectionId());
      gdocImportBatchContext.setState(GoogleDocImportBatchJobState.COLLECTION_CREATED);

      updateJobContext(ofy, this, jobEntity.getJobState(), gdocImportBatchContext, jobEntity,
          "assigning new CollectionId :" + collectionEntity.getCollectionId());
      ObjectifyUtils.commitTransaction(ofy);
    } finally {
      ObjectifyUtils.rollbackTransactionIfStillActive(ofy);
    }
  }

  /**

   * @param gdocBatchImportContext
   */
  private void updateRootNode(JobEntity jobEntity,
      GoogleDocImportBatchJobContext gdocBatchImportContext) {
    GoogleDocTree rootNode = new GoogleDocTree.Builder()
        .title("root")
        .nodeType(TreeNodeType.ROOT_NODE)
        .build();
    googleDocJobs.addChildTreeToParent(rootNode, gdocBatchImportContext.get());
    gdocBatchImportContext.setTreeRoot(rootNode);

    updateJobContext(null, this, JobState.CREATING_CHILDS, gdocBatchImportContext, jobEntity,
        "Updating root and setting state to CreateChilds.");
  }

  /**
   * Returns set of ResourceInfo for supported Google Doc Types.
   * 
   * @return
   */
  private Set<GoogleDocInfoDto> getSetOfSupportedGoogleDocs(
      List<GoogleDocTree> listOfLeaves) {
    Map<GoogleDocResourceId, GoogleDocInfoDto> map = Maps.newHashMap();

    for (GoogleDocTree currNode : listOfLeaves) {
      if (currNode.getResourceInfo().getModuleType().isSupported()) {
        map.put(currNode.getGoogleDocResourceId(), currNode.getResourceInfo());
      }
    }

    return Sets.newHashSet(map.values());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <D extends AbstractDto<D>> JobEntity enqueueCompleteJob(JobId jobId, D responseDto, String message) {
    checkNotBlank(message, "message cannot be null");
    checkNotNull(responseDto, "responseDto cannot be null");
    System.out.println("\n***Resposne = \n" + responseDto.toJson());
    
    
    Objectify ofy = ObjectifyUtils.initiateTransaction();

    try {
      JobEntity jobEntity = this.get(ofy, jobId);
      jobEntity.setJobState(JobState.COMPLETE);
      jobEntity.setResponse(responseDto);
      this.put(ofy, jobEntity, new ChangeLogEntryPojo(message));

      if (!jobEntity.isRootJob()) {
        notificationManager.notifyChildCompletionEvent(ofy, jobEntity.getParentJobId(),
            jobEntity.getJobId());
      }

      ObjectifyUtils.commitTransaction(ofy);
      return jobEntity;
    } finally {
      ObjectifyUtils.rollbackTransactionIfStillActive(ofy);
    }
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
  public Map<JobId, JobEntity> findListOfJobs(List<JobId> listOfJobIds) {
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
  public JobId createImportBatchJob(ImportBatchWrapper jobRequest) {
    Objectify ofy = ObjectifyUtils.initiateTransaction();
    try {
      Text jobRequestText = new Text(jobRequest.toJson());

      JobEntity jobEntity = new JobEntity.Builder()
          .jobType(JobType.ROOT_JOB)
          .jobHandlerType(JobHandlerType.TASK_QUEUE)
          .taskType(TaskType.IMPORT_BATCH)
          .jobState(JobState.PRE_START)
          .request(jobRequestText)
          .context(jobRequestText)
          .build();
      JobEntity savedJobEntity = this.put(ofy, jobEntity,
          new ChangeLogEntryPojo("PreStart Request."));
      ObjectifyUtils.commitTransaction(ofy);

      logger.info("Successfully created " + TaskType.IMPORT_BATCH + ""
          + "Job[" + savedJobEntity.getJobId() + "].");
      return savedJobEntity.getJobId();
    } finally {
      ObjectifyUtils.rollbackTransactionIfStillActive(ofy);
    }
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
    checkArgument(parentJob.getTaskType() == TaskType.IMPORT_BATCH ||
        parentJob.getTaskType() == TaskType.IMPORT_COLLECTION_GOOGLE_DOC,
        "Parent job was expected to be of type : " + TaskType.IMPORT_BATCH + "/"
            + TaskType.IMPORT_COLLECTION_GOOGLE_DOC);

    parentJob.addChildJob(childJobId);

    updateParentJobContext(externalIdDto, parentJob, childJobId);

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
    }
  }

  /**
   * @param externalIdDto
   * @param parentJob
   */
  private void updateParentJobContext(ImportExternalIdDto externalIdDto, JobEntity parentJob,
      JobId childJobId) {
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

      case IMPORT_COLLECTION_GOOGLE_DOC:

        // Get existing context from Parent Job Entity.
        ImportCollectionGoogleDocContext importCollectionGDocContext = parentJob.getContext(
                ImportCollectionGoogleDocContext.class);

        checkNotNull(importCollectionGDocContext, "importCollectionGDocContext should not be null.");
        // Now update context with new information and then persist it back.
        importCollectionGDocContext.addImportModuleDto(externalIdDto);
        parentJob.setContext(importCollectionGDocContext);
        System.out.println(parentJob.getContext().getValue());
        break;

      default:
        throw new IllegalStateException("Unsupported parentJob for : " + taskType);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public JobEntity createSyntheticModuleJob(Objectify ofy, ImportModuleSyntheticModuleJobContext context,
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
        .taskType(TaskType.IMPORT_SYNTHETIC_MODULE)
        .jobState(JobState.ENQUEUED)
        .request(text)
        .context(text)
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
        .taskType(TaskType.IMPORT_COLLECTION_GOOGLE_DOC)
        .jobState(JobState.ENQUEUED)
        .request(text)
        .context(text)
        .build();
    JobEntity savedJobEntity = this.put(ofy, jobEntity,
        new ChangeLogEntryPojo("Enqueuing " + TaskType.IMPORT_COLLECTION_GOOGLE_DOC + " job"));

    logger.info("Successfully created Job[" + savedJobEntity.getJobId() + "] for ["
        + TaskType.IMPORT_COLLECTION_GOOGLE_DOC + "].");
    return savedJobEntity;
  }
}
