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

import java.util.List;

import com.google.light.server.jobs.handlers.collectionjobs.youtubeplaylist.ImportCollectionYouTubePlaylistHandler;

import com.google.light.server.jobs.handlers.collectionjobs.gdoccollection.ImportCollectionGoogleDocJobHandler;

import com.google.light.server.jobs.handlers.modulejobs.youtube.ImportModuleYouTubeVideoJobHandler;

import com.google.light.server.jobs.handlers.modulejobs.gdocument.ImportModuleGoogleDocJobHandler;

import com.google.light.server.jobs.handlers.modulejobs.synthetic.ImportModuleSyntheticModuleJobHandler;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.light.server.dto.pojo.ChangeLogEntryPojo;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.JobId;
import com.google.light.server.exception.ExceptionType;
import com.google.light.server.exception.unchecked.taskqueue.WaitForChildsToComplete;
import com.google.light.server.jobs.handlers.batchjobs.ImportBatchJobHandler;
import com.google.light.server.manager.interfaces.JobManager;
import com.google.light.server.persistence.entity.jobs.JobEntity;
import com.google.light.server.persistence.entity.jobs.JobEntity.TaskType;
import com.google.light.server.persistence.entity.jobs.JobState;
import com.google.light.server.utils.Transactable;
import com.googlecode.objectify.Objectify;
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
    repeatInTransaction(new Transactable<Void>() {
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

  //
  // /**
  // * @param jobEntity
  // */
  // @Deprecated
  // private void handleImporgGoogleDocBatch(JobEntity jobEntity) {
  // GoogleDocImportBatchJobContext gdocImportBatchContext = JsonUtils.getDto(
  // jobEntity.getContext().getValue(), GoogleDocImportBatchJobContext.class);
  //
  // switch (jobEntity.getJobState()) {
  // case ENQUEUED:
  // case CREATING_CHILDS:
  // createChildJobs(gdocImportBatchContext, jobEntity);
  // break;
  //
  // case WAITING_FOR_CHILD_COMPLETE_NOTIFICATION:
  // logger.severe("This should not be called. But called for : " + jobEntity.getJobId());
  // break;
  //
  // case POLLING_FOR_CHILDS:
  // resumeImportGoogleDocBatchIfChildsAreComplete(gdocImportBatchContext, jobEntity);
  // break;
  //
  // case ALL_CHILDS_COMPLETED_SUCCESSFULLY:
  // publishCollectionIfRequired(gdocImportBatchContext, jobEntity);
  // break;
  //
  // case COMPLETE:
  // logger.severe("Inspite of being complete, it was called for : " + jobEntity.getJobId());
  // break;
  // default:
  // throw new IllegalStateException("Unsupported state : " + jobEntity.getJobState());
  // }
  // }
  //

  //
  //
  //
  // private void createChildJobs(GoogleDocImportBatchJobContext gdocBatchImportContext,
  // JobEntity jobEntity) {
  // switch (jobEntity.getJobState()) {
  // case ENQUEUED:
  // updateRootNode(jobEntity, gdocBatchImportContext);
  //
  // if (!gdocBatchImportContext.getTreeRoot().hasChildren()) {
  // // // There is no child that needs to be worked on. So marking as complete.
  // // logger.info("Since there is no child which needs to be imported, "
  // // + "so marking job as complete.");
  // // enqueueCompleteJob(jobEntity.getId(), "no work needs to be done, so marking as done.");
  // skipChildJobCreationProcess(gdocBatchImportContext, jobEntity);
  // break;
  // }
  //
  // // else continue creating childs.
  // //$FALL-THROUGH$
  //
  // case CREATING_CHILDS:
  // createChildJobsIfRequired(jobEntity, gdocBatchImportContext);
  // break;
  // default:
  // throw new IllegalArgumentException("Unsupported State : " + jobEntity.getJobState());
  // }
  // logger.info("done");
  // }
  //
  // /**
  // * @param jobEntity
  // * @param gdocBatchImportContext
  // */
  // private void createChildJobsIfRequired(JobEntity jobEntity,
  // GoogleDocImportBatchJobContext gdocBatchImportBatchContext) {
  // List<PersonId> owners = Lists.newArrayList(GuiceUtils.getOwnerId());
  //
  // Set<GoogleDocInfoDto> setOfSupportedGoogleDocs = getSetOfSupportedGoogleDocs(
  // gdocBatchImportBatchContext.getTreeRoot().getInOrderTraversalOfLeafNodes());
  //
  // List<GoogleDocResourceId> listOfGDocsToImport = Lists.newArrayList();
  //
  // for (GoogleDocInfoDto currInfo : setOfSupportedGoogleDocs) {
  // GoogleDocResourceId resourceId = currInfo.getGoogleDocResourceId();
  //
  // boolean childAlreadyCreated = gdocBatchImportBatchContext.isChildJobCreated(
  // resourceId.getExternalId());
  //
  // if (childAlreadyCreated) {
  // logger.info("Seems child is already created, So ignoring.");
  // continue;
  // }
  //
  // // First reserving a moduleId for externalId.
  // ModuleId moduleId = null;
  // Objectify ofy = ObjectifyUtils.initiateTransaction();
  // try {
  // moduleId = moduleManager.reserveModuleId(ofy, resourceId.getExternalId(),
  // owners, currInfo.getTitle());
  //
  // ObjectifyUtils.commitTransaction(ofy);
  // } finally {
  // ObjectifyUtils.rollbackTransactionIfStillActive(ofy);
  // }
  //
  // checkNotNull(moduleId, "moduleId should not be null here.");
  //
  // // Now reserving a module version.
  // ofy = ObjectifyUtils.initiateTransaction();
  // try {
  // Version reservedVersion = moduleManager.reserveModuleVersion(ofy, moduleId,
  // currInfo.getEtag(), currInfo.getLastEditTime());
  // logger.info("Reserved/created module : " + moduleId + " version : " + reservedVersion);
  //
  // if (reservedVersion != null) {
  // // Now creating a child job and adding its reference in Job Context.
  //
  // // First enqueuing a child job for this resource.
  // GoogleDocImportJobContext childJobContext = new GoogleDocImportJobContext.Builder()
  // .resourceInfo(currInfo)
  // .moduleId(moduleId)
  // .version(reservedVersion)
  // .state(GoogleDocImportJobContext.GoogleDocImportJobState.ENQUEUED)
  // .build();
  // JobEntity childJob = jobManager.createGoogleDocImportChildJob(ofy,
  // childJobContext, jobEntity.getJobId(), jobEntity.getJobId());
  //
  // /*
  // * Now adding a importDestination in the jobContext so that if job gets retried, no
  // * new version is created.
  // */
  // ImportModuleDto dest = new ImportModuleDto.Builder()
  // .externalId(resourceId.getExternalId())
  // .moduleId(moduleId)
  // .version(reservedVersion)
  // .jobId(childJob.getJobId())
  // .jobState(childJob.getJobState())
  // .build();
  // gdocBatchImportBatchContext.addImportDestination(dest);
  //
  // Text context = new Text(gdocBatchImportBatchContext.toJson());
  // updateJobContext(ofy, this, JobState.CREATING_CHILDS, context, jobEntity,
  // "Adding childJob : " + childJob.getJobId());
  // listOfGDocsToImport.add(resourceId);
  // ObjectifyUtils.commitTransaction(ofy);
  // }
  // } finally {
  // ObjectifyUtils.rollbackTransactionIfStillActive(ofy);
  // }
  // }
  //
  // if (isListEmpty(listOfGDocsToImport)) {
  // skipChildJobCreationProcess(gdocBatchImportBatchContext, jobEntity);
  // } else {
  // Text context = new Text(gdocBatchImportBatchContext.toJson());
  // updateJobContext(null, this, JobState.WAITING_FOR_CHILD_COMPLETE_NOTIFICATION, context,
  // jobEntity, "Marking job as Waiting for ChildJobs.");
  // }
  //
  // }
  //
  // /**
  // * @param jobEntity
  // * @param context
  // */
  // private void skipChildJobCreationProcess(
  // GoogleDocImportBatchJobContext gdocImportBatchJobContext,
  // JobEntity jobEntity) {
  // Text context = new Text(gdocImportBatchJobContext.toJson());
  // updateJobContext(null, this, JobState.ALL_CHILDS_COMPLETED_SUCCESSFULLY, context, jobEntity,
  // "Nothing to do. So directly changing state to "
  // + JobState.ALL_CHILDS_COMPLETED_SUCCESSFULLY);
  // enqueueLightJobWithoutTxn(jobEntity.getJobId());
  // }
  //
  // /**
  // *
  // */
  // private void publishCollectionIfRequired(GoogleDocImportBatchJobContext gdocImportBatchContext,
  // JobEntity jobEntity) {
  //
  // boolean requiresNewVersion = false;
  // String message = "";
  // CollectionTreeNodeDto collectionRoot = null;
  // switch (gdocImportBatchContext.getType()) {
  // case CREATE_COLLECTION_JOB:
  // createNewCollection(gdocImportBatchContext, jobEntity);
  // collectionRoot = createCollectionTree(gdocImportBatchContext);
  // requiresNewVersion = true;
  // message = "Successfully created " + gdocImportBatchContext.getCollectionId() + ".";
  // break;
  //
  // case EDIT_COLLECTION_JOB:
  // CollectionId collectionId = gdocImportBatchContext.getCollectionId();
  // CollectionEntity collectionEntity = collectionManager.get(null, collectionId);
  // Version publishedVersion = collectionEntity.getLatestPublishVersion();
  //
  // if (publishedVersion.isNoVersion()) {
  // // Seems like there is a collection with no published version. So this is mis tagged as
  // // EDIT_COLLECTION_JOB. So instead fix this, and retag as createNewCollectionJob.
  // gdocImportBatchContext.setType(ImportBatchType.CREATE_COLLECTION_JOB);
  // Text context = new Text(gdocImportBatchContext.toJson());
  // Objectify ofy = ObjectifyUtils.initiateTransaction();
  // try {
  // updateJobContext(ofy, this, jobEntity.getJobState(), context, jobEntity,
  // "reclassifying current job for EDIT_COLLECTION_JOB");
  // ObjectifyUtils.commitTransaction(ofy);
  // } finally {
  // ObjectifyUtils.rollbackTransactionIfStillActive(ofy);
  // }
  // throw new IllegalArgumentException("Throwing for retry");
  // }
  //
  // CollectionVersionEntity cvEntity = collectionManager.getCollectionVersion(null,
  // collectionId, publishedVersion);
  //
  // GoogleDocTree gdocTreeRoot = gdocImportBatchContext.getTreeRoot();
  // collectionRoot = cvEntity.getCollectionTree();
  //
  // requiresNewVersion = appendChildsFromGDocTreeIfNotAlreadyPresent(gdocImportBatchContext,
  // gdocTreeRoot, collectionRoot);
  //
  // if (!requiresNewVersion) {
  // // Comparre title. If it has changed, we will still need another version.
  // String possibleNewTitle = gdocImportBatchContext.getCollectionTitle();
  // requiresNewVersion = !collectionEntity.getTitle().equals(possibleNewTitle);
  // collectionRoot.setTitle(possibleNewTitle);
  // }
  //
  // message = "Successfully appended childs to "
  // + gdocImportBatchContext.getCollectionId() + ".";
  // break;
  //
  // case MODULE_JOB:
  // // Child jobs would have completed the responsibility of publishing modules. So nothing
  // // to do here.
  // message = "successfully completed ModuleJob.";
  // break;
  //
  // default:
  // throw new IllegalStateException("Unsupported type : " + gdocImportBatchContext.getType());
  // }
  //
  // if (requiresNewVersion) {
  // reserverCollectionVersion(gdocImportBatchContext, jobEntity);
  // publishCollectionVersion(gdocImportBatchContext, jobEntity, collectionRoot);
  // }
  //
  // this.enqueueCompleteJob(jobEntity.getJobId(), message);
  // }
  //
  //
  //
  //
  // /**
  // * @param possibleNewNode
  // * @param collectionRoot
  // */
  // private boolean appendChildsFromGDocTreeIfNotAlreadyPresent(
  // GoogleDocImportBatchJobContext gdocImportBatchContext, GoogleDocTree gdocTreeParent,
  // CollectionTreeNodeDto collectionParent) {
  // boolean isModified = false;
  // checkNotNull(gdocTreeParent, "gdocTreeParent");
  // checkNotNull(collectionParent, "collectionParent");
  // for (GoogleDocTree currGDocTreeNode : gdocTreeParent.getChildren()) {
  // ExternalId externalId = currGDocTreeNode.getResourceInfo().getExternalId();
  //
  // CollectionTreeNodeDto.Builder newCollectionTreeNodeBuilder =
  // new CollectionTreeNodeDto.Builder()
  // .title(currGDocTreeNode.getTitle())
  // .type(currGDocTreeNode.getType())
  // .moduleType(currGDocTreeNode.getResourceInfo().getModuleType())
  // .externalId(externalId);
  //
  // switch (currGDocTreeNode.getType()) {
  // case ROOT_NODE:
  // throw new IllegalStateException("This should not be called for root node.");
  //
  // case LEAF_NODE:
  // if (collectionParent.containsExternalIdAsChildren(externalId)) {
  // /*
  // * Since it already exists as one of the children for current parent, so no need to
  // * add it again.
  // */
  // break;
  // }
  // appendLeafNodeToCollectionTree(gdocImportBatchContext, newCollectionTreeNodeBuilder,
  // collectionParent, externalId);
  // isModified = true;
  // break;
  //
  // case INTERMEDIATE_NODE:
  // CollectionTreeNodeDto newCollectionParent = null;
  // if (collectionParent.containsExternalIdAsChildren(externalId)) {
  // /*
  // * Current sub collection already exists. So now we have to check all the childs to see
  // * if there were any new modifications.
  // */
  // newCollectionParent =
  // collectionParent.findChildByExternalId(externalId);
  // isModified |= appendChildsFromGDocTreeIfNotAlreadyPresent(gdocImportBatchContext,
  // currGDocTreeNode, newCollectionParent);
  // } else {
  // newCollectionParent = newCollectionTreeNodeBuilder.build();
  // collectionParent.addChildren(newCollectionParent);
  // handOverChilds(gdocImportBatchContext, currGDocTreeNode, newCollectionParent);
  // isModified = true;
  // }
  //
  // checkNotNull(newCollectionParent, "newCollectionParent");
  //
  // break;
  // }
  // }
  //
  // return isModified;
  // }
  //
  // /**
  // * @param gdocImportBatchContext
  // * @param jobEntity
  // * @param collectionTree
  // */
  // private void publishCollectionVersion(GoogleDocImportBatchJobContext gdocImportBatchContext,
  // JobEntity jobEntity, CollectionTreeNodeDto collectionTree) {
  // Objectify ofy = ObjectifyUtils.initiateTransaction();
  // try {
  // CollectionVersionEntity cvEntity = collectionManager.publishCollectionVersion(
  // ofy, gdocImportBatchContext.getCollectionId(), gdocImportBatchContext.getVersion(),
  // collectionTree);
  //
  // gdocImportBatchContext.setVersion(gdocImportBatchContext.getVersion());
  // gdocImportBatchContext.setState(GoogleDocImportBatchJobState.COLLECTION_VERSION_PUBLISHED);
  //
  // Text context = new Text(gdocImportBatchContext.toJson());
  // updateJobContext(ofy, this, jobEntity.getJobState(), context, jobEntity,
  // "assigning new CollectionId :" + cvEntity.getCollectionId());
  //
  // ObjectifyUtils.commitTransaction(ofy);
  // } finally {
  // ObjectifyUtils.rollbackTransactionIfStillActive(ofy);
  // }
  // }
  //
  // /**
  // * @param gdocImportBatchContext
  // * @param jobEntity
  // */
  // private void reserverCollectionVersion(GoogleDocImportBatchJobContext gdocImportBatchContext,
  // JobEntity jobEntity) {
  // Objectify ofy = ObjectifyUtils.initiateTransaction();
  // try {
  //
  // Version reservedVersion = collectionManager.reserveCollectionVersion(
  // ofy, gdocImportBatchContext.getCollectionId());
  //
  // gdocImportBatchContext.setVersion(reservedVersion);
  // gdocImportBatchContext.setState(GoogleDocImportBatchJobState.COLLECTION_VERSION_RESERVED);
  // Text context = new Text(gdocImportBatchContext.toJson());
  // updateJobContext(ofy, this, jobEntity.getJobState(), context, jobEntity,
  // "assigning new CollectionId :" + gdocImportBatchContext.getCollectionId());
  //
  // ObjectifyUtils.commitTransaction(ofy);
  // } finally {
  // ObjectifyUtils.rollbackTransactionIfStillActive(ofy);
  // }
  // }
  //
  // /**
  // * @param gdocImportBatchContext
  // * @return
  // */
  // private CollectionTreeNodeDto createCollectionTree(
  // GoogleDocImportBatchJobContext gdocImportBatchContext) {
  // String title = gdocImportBatchContext.getCollectionTitle();
  //
  // if (StringUtils.isEmpty(title)) {
  // title = "Untitled : " + new DateTime(LightUtils.getNow());
  // }
  //
  // CollectionTreeNodeDto collectionTreeRoot = new CollectionTreeNodeDto.Builder()
  // .type(TreeNodeType.ROOT_NODE)
  // .title(title)
  // .moduleType(ModuleType.LIGHT_COLLECTION)
  // .externalId(null)
  // .build();
  //
  // handOverChilds(gdocImportBatchContext, gdocImportBatchContext.getTreeRoot(),
  // collectionTreeRoot);
  //
  // return collectionTreeRoot;
  // }
  //
  // /**
  // * @param treeRoot
  // * @param collectionRoot
  // */
  // private void handOverChilds(GoogleDocImportBatchJobContext gdocImportBatchContext,
  // GoogleDocTree gdocTreeParent, CollectionTreeNodeDto collectionParent) {
  // if (!gdocTreeParent.hasChildren()) {
  // return;
  // }
  //
  // for (GoogleDocTree currGDocTreeChild : gdocTreeParent.getChildren()) {
  // CollectionTreeNodeDto.Builder treeNodeBuilder = new CollectionTreeNodeDto.Builder()
  // .title(currGDocTreeChild.getTitle())
  // .type(currGDocTreeChild.getType())
  // .moduleType(currGDocTreeChild.getGoogleDocResourceId().getModuleType())
  // .externalId(currGDocTreeChild.getGoogleDocResourceId().getExternalId());
  //
  // CollectionTreeNodeDto treeNode = null;
  //
  // switch (currGDocTreeChild.getType()) {
  // case ROOT_NODE:
  // throw new IllegalStateException("Found a child with type = ROOT_NODE");
  //
  // case INTERMEDIATE_NODE:
  // treeNode = treeNodeBuilder.build();
  // handOverChilds(gdocImportBatchContext, currGDocTreeChild, treeNode);
  // break;
  //
  // case LEAF_NODE:
  // if (currGDocTreeChild.getGoogleDocResourceId().getModuleType().isSupported()) {
  // ModuleId moduleId = moduleManager.findModuleIdByExternalId(
  // null, currGDocTreeChild.getGoogleDocResourceId().getExternalId());
  // treeNode = treeNodeBuilder.moduleId(moduleId).build();
  // } else {
  // // Ignoring unsupported types.
  // continue;
  // }
  // break;
  //
  // default:
  // throw new IllegalStateException("Unsupporetd type : " + currGDocTreeChild.getType());
  // }
  //
  // checkNotNull(treeNode, "treeNode should have been created by now.");
  // collectionParent.addChildren(treeNode);
  // }
  // }
  //
  // /**
  // * @param jobEntity
  // */
  // @Deprecated
  // private void resumeImportGoogleDocBatchIfChildsAreComplete(
  // GoogleDocImportBatchJobContext gdocImportBatchContext, JobEntity jobEntity) {
  // List<ImportModuleDto> listOfDestinations =
  // gdocImportBatchContext.getListOfImportDestinations();
  // List<JobId> listOfChildJobIds = Lists.newArrayList();
  //
  // for (ImportModuleDto curr : listOfDestinations) {
  // listOfChildJobIds.add(curr.getJobId());
  // }
  //
  // Map<JobId, JobEntity> map = this.findListOfJobs(listOfChildJobIds);
  //
  // boolean allChildsComplete = true;
  // JobId pendingChildJobId = null;
  //
  // for (JobEntity currChildJob : map.values()) {
  // if (currChildJob.getJobState() != JobState.COMPLETE) {
  // allChildsComplete = false;
  // pendingChildJobId = currChildJob.getJobId();
  // break;
  // }
  // }
  //
  // if (!allChildsComplete) {
  // throw new WaitForChildsToComplete(Lists.newArrayList(pendingChildJobId));
  // }
  //
  // Objectify ofy = ObjectifyUtils.initiateTransaction();
  // try {
  // jobEntity.setJobState(JobState.ALL_CHILDS_COMPLETED_SUCCESSFULLY);
  // this.put(ofy, jobEntity, new ChangeLogEntryPojo("all childs completed successfully"));
  // this.enqueueLightJob(ofy, jobEntity.getJobId());
  //
  // ObjectifyUtils.commitTransaction(ofy);
  // } finally {
  // ObjectifyUtils.rollbackTransactionIfStillActive(ofy);
  // }
  // }
}
