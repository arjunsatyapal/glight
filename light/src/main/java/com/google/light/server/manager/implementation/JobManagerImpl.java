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
import static com.google.light.server.constants.LightConstants.JOB_BACK_OFF_FACTOR;
import static com.google.light.server.constants.LightConstants.JOB_BACK_OFF_SECONDS;
import static com.google.light.server.constants.LightConstants.JOB_MAX_ATTEMPTES;
import static com.google.light.server.constants.google.cloudstorage.GoogleCloudStorageBuckets.CONTENT;
import static com.google.light.server.exception.ExceptionType.SERVER_GUICE_INJECTION;
import static com.google.light.server.utils.GoogleCloudStorageUtils.writeFileOnGCS;
import static com.google.light.server.utils.GuiceUtils.getInstance;
import static com.google.light.server.utils.GuiceUtils.getRequestScopedValues;
import static com.google.light.server.utils.LightPreconditions.checkJobId;
import static com.google.light.server.utils.LightPreconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkTxnIsRunning;
import static com.google.light.server.utils.LightUtils.isListEmpty;

import com.google.appengine.api.datastore.Text;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileReadChannel;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.GSFileOptions;
import com.google.appengine.tools.pipeline.PipelineService;
import com.google.appengine.tools.pipeline.PipelineServiceFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.light.server.constants.FileExtensions;
import com.google.light.server.constants.google.cloudstorage.GoogleCloudStorageBuckets;
import com.google.light.server.constants.http.ContentTypeEnum;
import com.google.light.server.dto.module.GSBlobInfo;
import com.google.light.server.dto.module.ImportDestinationDto;
import com.google.light.server.dto.module.ModuleType;
import com.google.light.server.dto.pojo.ChangeLogEntryPojo;
import com.google.light.server.dto.pojo.GoogleDocArchivePojo;
import com.google.light.server.dto.pojo.GoogleDocArchivePojo.LightGDocArchiveStatus;
import com.google.light.server.dto.pojo.JobHandlerId;
import com.google.light.server.dto.pojo.LightJobContextPojo;
import com.google.light.server.dto.pojo.RequestScopedValues;
import com.google.light.server.dto.pojo.longwrapper.CollectionId;
import com.google.light.server.dto.pojo.longwrapper.JobId;
import com.google.light.server.dto.pojo.longwrapper.ModuleId;
import com.google.light.server.dto.pojo.longwrapper.PersonId;
import com.google.light.server.dto.pojo.longwrapper.Version;
import com.google.light.server.dto.pojo.tree.AbstractTreeNode.TreeNodeType;
import com.google.light.server.dto.pojo.tree.CollectionTreeNodeDto;
import com.google.light.server.dto.pojo.tree.GoogleDocTree;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocImportBatchJobContext;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocImportBatchJobContext.GoogleDocImportBatchJobState;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocImportJobContext;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocInfoDto;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocResourceId;
import com.google.light.server.exception.ExceptionType;
import com.google.light.server.exception.unchecked.GoogleDocException;
import com.google.light.server.exception.unchecked.pipelineexceptions.PipelineException;
import com.google.light.server.exception.unchecked.taskqueue.GoogleDocArchivalWaitingException;
import com.google.light.server.exception.unchecked.taskqueue.WaitForChildsToComplete;
import com.google.light.server.jobs.google.gdoc.GoogleDocJobs;
import com.google.light.server.jobs.importjobs.PipelineJobs;
import com.google.light.server.manager.interfaces.CollectionManager;
import com.google.light.server.manager.interfaces.ImportManager;
import com.google.light.server.manager.interfaces.JobManager;
import com.google.light.server.manager.interfaces.ModuleManager;
import com.google.light.server.manager.interfaces.NotificationManager;
import com.google.light.server.manager.interfaces.QueueManager;
import com.google.light.server.persistence.dao.JobDao;
import com.google.light.server.persistence.entity.collection.CollectionEntity;
import com.google.light.server.persistence.entity.collection.CollectionVersionEntity;
import com.google.light.server.persistence.entity.jobs.JobEntity;
import com.google.light.server.persistence.entity.jobs.JobEntity.JobHandlerType;
import com.google.light.server.persistence.entity.jobs.JobEntity.JobState;
import com.google.light.server.persistence.entity.jobs.JobEntity.JobType;
import com.google.light.server.persistence.entity.jobs.JobEntity.TaskType;
import com.google.light.server.persistence.entity.module.ModuleVersionEntity;
import com.google.light.server.persistence.entity.queue.importflow.ImportJobEntity;
import com.google.light.server.thirdparty.clients.google.gdata.gdoc.DocsServiceWrapper;
import com.google.light.server.utils.GoogleCloudStorageUtils;
import com.google.light.server.utils.GuiceUtils;
import com.google.light.server.utils.JsonUtils;
import com.google.light.server.utils.LightPreconditions;
import com.google.light.server.utils.LightUtils;
import com.google.light.server.utils.ObjectifyUtils;
import com.googlecode.objectify.Objectify;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

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
  private ImportManager importManager;
  private ModuleManager moduleManager;
  private NotificationManager notificationManager;
  private QueueManager queueManager;
  private GoogleDocJobs googleDocJobs;
  private Provider<DocsServiceWrapper> docsServiceProvider;

  @Inject
  public JobManagerImpl(JobDao jobDao, ImportManager importManager,
      CollectionManager collectionManager, ModuleManager moduleManager,
      NotificationManager notificationManager,
      QueueManager queueManager, GoogleDocJobs googleDocJobs,
      Provider<DocsServiceWrapper> docsServiceProvider) {
    this.jobDao = checkNotNull(jobDao, "jobDao");
    this.collectionManager =
        checkNotNull(collectionManager, SERVER_GUICE_INJECTION, "collectionManager");
    this.importManager = checkNotNull(importManager, SERVER_GUICE_INJECTION, "importManager");
    this.moduleManager = checkNotNull(moduleManager, SERVER_GUICE_INJECTION, "moduleManager");
    this.notificationManager = checkNotNull(notificationManager, SERVER_GUICE_INJECTION,
        "notificationManager");
    this.queueManager = checkNotNull(queueManager, SERVER_GUICE_INJECTION, "queueManager");
    this.googleDocJobs = checkNotNull(googleDocJobs, SERVER_GUICE_INJECTION, "googleDocJobs");
    this.docsServiceProvider = checkNotNull(docsServiceProvider,
        SERVER_GUICE_INJECTION, "docsServiceProvider");
  }

  @Deprecated
  @Override
  public JobEntity enqueueImportJob(ImportJobEntity importJobEntity, JobId parentJobId,
      JobId rootJobId, String promiseHandle) {
    Objectify ofy = ObjectifyUtils.initiateTransaction();
    try {
      // First persisting the importJob.
      ImportJobEntity persistedEntity = importManager.put(ofy, importJobEntity);
      checkNotNull(persistedEntity.getId(), "Import job was not persisted.");

      JobType jobType = rootJobId == null ? JobType.ROOT_JOB : JobType.CHILD_JOB;
      // Now creating a Job from ImportJob without PipelineId.
      JobEntity jobEntity = new JobEntity.Builder()
          .jobType(jobType)
          .parentJobId(parentJobId)
          .rootJobId(rootJobId)
          .jobHandlerType(JobHandlerType.PIPELINE)
          .taskType(TaskType.IMPORT)
          .taskId(importJobEntity.getId())
          .jobState(JobState.PRE_START)
          .build();
      JobEntity savedJobEntity = this.put(ofy, jobEntity,
          new ChangeLogEntryPojo("Enqueuing for Import."));
      ObjectifyUtils.commitTransaction(ofy);

      RequestScopedValues participants = getRequestScopedValues();

      LightJobContextPojo context = new LightJobContextPojo.Builder()
          .ownerId(participants.getOwnerId())
          .actorId(participants.getActorId())
          .jobId(savedJobEntity.getId())
          .rootJobId(savedJobEntity.getRootJobId())
          .promiseHandle(promiseHandle)
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
  @Deprecated
  public JobEntity findByJobHandlerId(JobHandlerType jobHandlerType, JobHandlerId jobHandlerId) {
    return jobDao.findByJobHandlerId(jobHandlerType, jobHandlerId);
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
      queueManager.enqueueGoogleDocInteractionJob(ofy, jobEntity.getId());
      ObjectifyUtils.commitTransaction(ofy);

      logger.info("Successfully created IMPORT_GOOGLE_DOC_BATCH Job[" + savedJobEntity.getId());
      return savedJobEntity;
    } finally {
      ObjectifyUtils.rollbackTransactionIfStillActive(ofy);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public JobEntity enqueueGoogleDocImportChildJob(Objectify ofy, GoogleDocImportJobContext context,
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
    queueManager.enqueueGoogleDocInteractionJob(ofy, jobEntity.getId());

    logger.info("Successfully created IMPORT_GOOGLE_DOC Job[" + savedJobEntity.getId());
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
  public void handleJob(JobId jobId) {
    checkNotNull(jobId, ExceptionType.CLIENT_PARAMETER, "jobId cannot be null");
    jobId.validate();

    JobEntity jobEntity = this.get(null, jobId);
    checkNotNull(jobId, ExceptionType.CLIENT_PARAMETER, "No job found for : " + jobId);

    System.out.println(jobEntity.getContext().getValue());
    switch (jobEntity.getTaskType()) {
      case IMPORT_GOOGLE_DOC_BATCH:
        handleImporgGoogleDocBatch(jobEntity);
        break;

      case IMPORT_GOOGLE_DOC1:
        handleGoogleDocImport(jobEntity);
        break;

      default:
        throw new IllegalStateException("Unsupported TaskType : " + jobEntity.getTaskType());

    }
  }

  /**
   * This is a linear job. So all stages will depend on JobContext.
   */
  private void handleGoogleDocImport(JobEntity jobEntity) {
    GoogleDocImportJobContext gdocImportContext = JsonUtils.getDto(
        jobEntity.getContext().getValue(), GoogleDocImportJobContext.class);

    switch (gdocImportContext.getState()) {
      case ENQUEUED:
        startArchivingGoogleDoc(gdocImportContext, jobEntity);
        break;

      case WAITING_FOR_ARCHIVE:
        downloadArchiveIfReady(gdocImportContext, jobEntity);
        break;

      case ARCHIVE_DOWNLOADED:
        publishModule(gdocImportContext, jobEntity);
        break;

      case COMPLETE:
        this.enqueueCompleteJob(jobEntity.getId(), "Successfully completed Google Doc Job.");
        break;

      default:
        throw new IllegalStateException("Unsupported State : " + gdocImportContext.getState());

    }
  }

  /**
   * @param gdocImportContext
   * @param jobEntity
   */
  private void publishModule(GoogleDocImportJobContext gdocImportContext, JobEntity jobEntity) {
    ModuleId moduleId = gdocImportContext.getModuleId();
    Version version = gdocImportContext.getVersion();
    try {
      FileService fileService = FileServiceFactory.getFileService();
      Map<String, GSBlobInfo> resourceMap = Maps.newConcurrentMap();

      AppEngineFile file = new AppEngineFile(gdocImportContext.getGCSArchiveLocation());
      FileReadChannel readChannel = fileService.openReadChannel(file, true /* lock */);

      InputStream inputStream = Channels.newInputStream(readChannel);
      ZipInputStream zipIn = new ZipInputStream(inputStream);

      ZipEntry zipEntry = null;
      do {
        zipEntry = zipIn.getNextEntry();
        if (zipEntry == null) {
          continue;
        }
        System.out.println(zipEntry.getName());

        // TODO(arjuns): Get contentType from File extension.
        // Storing file on Cloud Storage.
        String nameFromGoogleDoc = zipEntry.getName();

        checkArgument(nameFromGoogleDoc.contains("/"), "Unexpected name from GoogleDoc : "
            + nameFromGoogleDoc);
        String parts[] = nameFromGoogleDoc.split("/");

        String newFileName = "";
        // Parts[0] is always the folder whose name is same as prettified title for a GoogleDoc.
        if (parts.length == 2) {
          // This will be true for HTML file.
          newFileName = parts[1];
        } else {
          checkArgument(parts.length == 3, "Google Docs will return files " +
              "name of format <pretty title/images/image names>");
          // This will be true for images/resources.
          newFileName = parts[1] + "/" + parts[2];
        }

        if (newFileName.endsWith(FileExtensions.HTML.get())) {
          newFileName = moduleId.getValue() + "." + FileExtensions.HTML.get();
        }

        ContentTypeEnum contentType = FileExtensions.getFileExtension(newFileName).getContentType();
        String storeFileName = moduleId.getValue() + "/" + newFileName;
        String storeAbsFilePath =
            GoogleCloudStorageBuckets.CONTENT.getAbsoluteFilePath(storeFileName);

        GSBlobInfo gsBlobInfo = new GSBlobInfo.Builder()
            .contentType(contentType)
            .fileName(parts[parts.length - 1])
            .gsKey(storeAbsFilePath)
            .sizeInBytes(zipEntry.getSize())
            .build();
        resourceMap.put(newFileName, gsBlobInfo);

        GSFileOptions gsFileOptions = GoogleCloudStorageUtils.getGCSFileOptionsForCreate(
            GoogleCloudStorageBuckets.CONTENT, contentType, storeFileName);
        writeFileOnGCS(zipIn, gsFileOptions);
      } while (zipEntry != null);

      zipIn.close();
      inputStream.close();
      readChannel.close();

      logger.info("Finished unarchiving files");

      // Now lets add the moduleVersion. This will require adding html and resources.
      // First adding Html.

      logger.info("Adding html for moduleId[" + moduleId + "].");

      String filePath = GoogleCloudStorageUtils.getAbsoluteModuleHtmlPath(CONTENT, moduleId);

      fileService = FileServiceFactory.getFileService();
      file = new AppEngineFile(filePath);
      readChannel = fileService.openReadChannel(file, true /* lock */);
      inputStream = Channels.newInputStream(readChannel);
      String htmlContent = LightUtils.getInputStreamAsString(inputStream);

      ModuleManager moduleManager = getInstance(ModuleManager.class);
      ModuleVersionEntity moduleVersionEntity = moduleManager.publishModuleVersion(
          moduleId, version, htmlContent, gdocImportContext.getResourceInfo());

      for (String currKey : resourceMap.keySet()) {
        // We dont want to store HTML files as resources. They are handled separately.
        if (currKey.endsWith(FileExtensions.HTML.get())) {
          continue;
        }

        GSBlobInfo gsBlobInfo = resourceMap.get(currKey);
        logger.info(JsonUtils.toJson(gsBlobInfo));
        moduleManager.addModuleResource(moduleVersionEntity.getModuleId(),
            moduleVersionEntity.getVersion(), currKey, gsBlobInfo);
      }

      System.out.println("Successfully published [" + moduleId + ", " + version);
      Objectify ofy = ObjectifyUtils.initiateTransaction();
      try {
        gdocImportContext.setState(GoogleDocImportJobContext.GoogleDocImportJobState.COMPLETE);
        Text context = new Text(gdocImportContext.toJson());
        updateJobContext(ofy, jobEntity.getJobState(), context, jobEntity, "Published module");
        this.enqueueLightJob(ofy, jobEntity.getId());
        ObjectifyUtils.commitTransaction(ofy);
      } finally {
        ObjectifyUtils.rollbackTransactionIfStillActive(ofy);
      }
    } catch (Exception e) {
      throw new GoogleDocException(e);
    }
  }

  /**
   * @param gdocImportContext
   * @param jobEntity
   */
  private void downloadArchiveIfReady(GoogleDocImportJobContext gdocImportContext,
      JobEntity jobEntity) {
    String archiveId = gdocImportContext.getArchiveId();
    GoogleDocArchivePojo archivePojo = docsServiceProvider.get().getArchiveStatus(archiveId);

    if (archivePojo.getArchiveStatus() != LightGDocArchiveStatus.FINISHED) {
      throw new GoogleDocArchivalWaitingException();
    }

    String destinationFileName = GoogleCloudStorageUtils.getDestinationFileNameForGDoc();
    String gcsArchiveLocation = docsServiceProvider.get().downloadArchive(
        archivePojo.getContentLocation(), destinationFileName);

    gdocImportContext
        .setState(GoogleDocImportJobContext.GoogleDocImportJobState.ARCHIVE_DOWNLOADED);
    gdocImportContext.setGCSArchiveLocation(gcsArchiveLocation);

    Objectify ofy = ObjectifyUtils.initiateTransaction();
    try {
      Text context = new Text(gdocImportContext.toJson());
      updateJobContext(ofy, jobEntity.getJobState(), context, jobEntity, "archive downloaded.");
      this.enqueueLightJob(ofy, jobEntity.getId());
      ObjectifyUtils.commitTransaction(ofy);
    } finally {
      ObjectifyUtils.rollbackTransactionIfStillActive(ofy);
    }
  }

  /**
   * @param gdocImportContext
   * @param jobEntity
   */
  private void startArchivingGoogleDoc(GoogleDocImportJobContext gdocImportContext,
      JobEntity jobEntity) {
    GoogleDocInfoDto resourceInfo = gdocImportContext.getResourceInfo();
    // GoogleDocArchivePojo archiveInfo = docsServiceProvider.get().archiveResource(
    // resourceInfo.getGoogleDocsResourceId());
    // gdocImportContext.setArchiveId(archiveInfo.getArchiveId());

    String archiveId =
        "vax2Nuv9Oq4VIPQEBTcxlJDkNbRFv7KVNL55qVoitx7rtrJHru1T6ljtTjMW26L3ygWrvW1tMZHHg6ARCy3Uj2yisT1mrH5gSGOwR4aPv4ZY86T6L4_M-8kg97Ll_3a3satbRaEsmug";
    gdocImportContext.setArchiveId(archiveId);

    gdocImportContext
        .setState(GoogleDocImportJobContext.GoogleDocImportJobState.WAITING_FOR_ARCHIVE);

    Objectify ofy = ObjectifyUtils.initiateTransaction();
    try {
      Text context = new Text(gdocImportContext.toJson());
      updateJobContext(null, jobEntity.getJobState(), context, jobEntity, "Waiting for Archive");
      this.enqueueGoogleDocInteractionJob(ofy, jobEntity.getId());
      ObjectifyUtils.commitTransaction(ofy);
    } finally {
      ObjectifyUtils.rollbackTransactionIfStillActive(ofy);
    }
  }

  private void updateJobContext(Objectify ofy, JobState jobState, Text context,
      JobEntity jobEntity, String changeLogMsg) {
    jobEntity.setJobState(jobState);
    jobEntity.setContext(context);
    this.put(ofy, jobEntity, new ChangeLogEntryPojo(changeLogMsg));
  }

  /**
   * @param jobEntity
   */
  private void handleImporgGoogleDocBatch(JobEntity jobEntity) {
    GoogleDocImportBatchJobContext gdocImportBatchContext = JsonUtils.getDto(
        jobEntity.getContext().getValue(), GoogleDocImportBatchJobContext.class);

    switch (jobEntity.getJobState()) {
      case ENQUEUED:
      case CREATING_CHILDS:
        createChildJobs(gdocImportBatchContext, jobEntity);
        break;

      case WAITING_FOR_CHILD_COMPLETE_NOTIFICATION:
        logger.severe("Called for : " + jobEntity.getId());
        break;

      case POLLING_FOR_CHILDS:
        resumeImportGoogleDocBatchIfChildsAreComplete(gdocImportBatchContext, jobEntity);
        break;

      case ALL_CHILDS_COMPLETED_SUCCESSFULLY:
        publishCollectionIfRequired(gdocImportBatchContext, jobEntity);
        break;

      case COMPLETE:
        logger.severe("Inspite off being complete, it was called for : " + jobEntity.getId());
        System.out.println("All complete now.");
        break;
      default:
        throw new IllegalStateException("Unsupported state : " + jobEntity.getJobState());
    }
  }

  /**
   * 
   */
  private void publishCollectionIfRequired(GoogleDocImportBatchJobContext gdocImportBatchContext,
      JobEntity jobEntity) {
    CollectionVersionEntity cvEntity = null;
    if (gdocImportBatchContext.isEditCollection()) {
      cvEntity = appendChildsToCollectionIfRequired(gdocImportBatchContext, jobEntity);
    } else if (gdocImportBatchContext.isCreateCollection()) {
      cvEntity = createNewCollection(gdocImportBatchContext, jobEntity);
    }

    if (cvEntity != null) {
      System.out.println(cvEntity.getCollectionKey().getId() + ":"
          + cvEntity.getVersion().getValue() + "was published successfully");
    }

    this.enqueueCompleteJob(jobEntity.getId(), 
        "Successfully pulished collection. Now marking as done.");
  }

  private CollectionVersionEntity appendChildsToCollectionIfRequired(
      GoogleDocImportBatchJobContext gdocImportBatchContext, JobEntity jobEntity) {
    CollectionId collectionId = gdocImportBatchContext.getCollectionId();

    Objectify ofy = ObjectifyUtils.initiateTransaction();
    try {
      CollectionEntity collectionEntity = collectionManager.get(ofy, collectionId);
      Version version = collectionEntity.getLatestVersion();

      CollectionVersionEntity cvEntity = null;

      if (version.getValue() == 0) {
        cvEntity = createFirstCollectionVersion(ofy, gdocImportBatchContext, collectionEntity);
      } else {
        cvEntity = appendChildAndCreateNewVersionIfRequired(ofy, gdocImportBatchContext,
            collectionEntity);
      }
      ObjectifyUtils.commitTransaction(ofy);

      return cvEntity;
    } finally {
      ObjectifyUtils.rollbackTransactionIfStillActive(ofy);
    }
  }

  /**
   * @param ofy
   * @param gdocImportBatchContext
   * @param collectionEntity
   * @return
   */
  private CollectionVersionEntity appendChildAndCreateNewVersionIfRequired(Objectify ofy,
      GoogleDocImportBatchJobContext gdocImportBatchContext, CollectionEntity collectionEntity) {
    // This should be called in same transaction where collectionEntity was fetched."
    LightPreconditions.checkTxnIsRunning(ofy);

    CollectionVersionEntity cvEntity = collectionManager.getCollectionVersion(
        ofy, collectionEntity.getCollectionId(), collectionEntity.getLatestVersion());

    GoogleDocTree gdocTreeRoot = gdocImportBatchContext.getTreeRoot();
    CollectionTreeNodeDto collectionRoot = cvEntity.getCollectionTree();

    boolean isModified = appendChildsFromGDocTreeIfNotAlreadyPresent(gdocImportBatchContext,
        gdocTreeRoot, collectionRoot);

    if (isModified) {
      cvEntity = collectionManager.addCollectionVersionForGoogleDoc(
          ofy, collectionEntity, collectionRoot);
    }

    return cvEntity;
  }

  /**
   * @param possibleNewNode
   * @param collectionRoot
   */
  private boolean appendChildsFromGDocTreeIfNotAlreadyPresent(
      GoogleDocImportBatchJobContext gdocImportBatchContext, GoogleDocTree gdocTreeParent,
      CollectionTreeNodeDto collectionParent) {
    boolean isModified = false;
    System.out.println("GdocTree title : " + gdocTreeParent.getTitle());
    checkNotNull(gdocTreeParent, "gdocTreeParent");
    checkNotNull(collectionParent, "collectionParent");
    for (GoogleDocTree currGDocTreeNode : gdocTreeParent.getChildren()) {
      String externalId = currGDocTreeNode.getResourceInfo()
          .getGoogleDocsResourceId().getExternalId();

      CollectionTreeNodeDto.Builder newCollectionTreeNodeBuilder =
          new CollectionTreeNodeDto.Builder()
              .title(currGDocTreeNode.getTitle())
              .type(currGDocTreeNode.getType())
              .moduleType(currGDocTreeNode.getResourceInfo().getType())
              .externalId(externalId);

      switch (currGDocTreeNode.getType()) {
        case ROOT_NODE:
          throw new IllegalStateException("This should not be called for root node.");

        case LEAF_NODE:
          if (collectionParent.containsExternalIdAsChildren(externalId)) {
            /*
             * Since it already exists as one of the children for current parent, so no need to
             * add it again.
             */
            break;
          }
          appendLeafNodeToCollectionTree(gdocImportBatchContext, newCollectionTreeNodeBuilder,
              collectionParent, externalId);
          isModified = true;
          break;

        case INTERMEDIATE_NODE:
          CollectionTreeNodeDto newCollectionParent = null;
          if (collectionParent.containsExternalIdAsChildren(externalId)) {
            /*
             * Current sub collection already exists. So now we have to check all the childs to see
             * if there were any new modifications.
             */
            newCollectionParent =
                collectionParent.findChildByExternalId(externalId);
            isModified |= appendChildsFromGDocTreeIfNotAlreadyPresent(gdocImportBatchContext,
                currGDocTreeNode, newCollectionParent);
          } else {
            newCollectionParent = newCollectionTreeNodeBuilder.build();
            collectionParent.addChildren(newCollectionParent);
            handOverChilds(gdocImportBatchContext, currGDocTreeNode, newCollectionParent);
            isModified = true;
          }
          
          checkNotNull(newCollectionParent, "newCollectionParent");

          break;
      }
    }

    return isModified;
  }

  /**
   * @param possibleNewNode
   * @param collectionParentNode
   * @param externalId
   */
  private void appendLeafNodeToCollectionTree(
      GoogleDocImportBatchJobContext gdocImportBatchContext,
      CollectionTreeNodeDto.Builder newLeafNodeBuilder,
      CollectionTreeNodeDto collectionParentNode, String externalId) {
    ModuleId moduleId = gdocImportBatchContext.getModuleIdForExternalId(externalId);
    checkNotNull(moduleId, "This should not happen. "
        + "Could not find moduleId for externalId : " + externalId);
    CollectionTreeNodeDto newLeafNode = newLeafNodeBuilder.moduleId(moduleId).build();
    collectionParentNode.addChildren(newLeafNode);
  }

  private CollectionVersionEntity createNewCollection(
      GoogleDocImportBatchJobContext gdocImportBatchContext, JobEntity jobEntity) {

    Objectify ofy = ObjectifyUtils.initiateTransaction();
    try {
      CollectionEntity collectionEntity = collectionManager.reserveCollectionId(
          ofy, GuiceUtils.getOwnerId());
      gdocImportBatchContext.setCollectionId(collectionEntity.getCollectionId());

      Text context = new Text(gdocImportBatchContext.toJson());
      updateJobContext(ofy, jobEntity.getJobState(), context, jobEntity,
          "assigning new CollectionId :" + collectionEntity.getCollectionId());

      CollectionVersionEntity cvEntity = createFirstCollectionVersion(
          ofy, gdocImportBatchContext, collectionEntity);
      ObjectifyUtils.commitTransaction(ofy);

      return cvEntity;
    } finally {
      ObjectifyUtils.rollbackTransactionIfStillActive(ofy);
    }
  }

  /**
   * @param gdocImportBatchContext
   * @param ofy
   * @param collectionEntity
   * @return
   */
  private CollectionVersionEntity createFirstCollectionVersion(Objectify ofy,
      GoogleDocImportBatchJobContext gdocImportBatchContext,
      CollectionEntity collectionEntity) {
    CollectionTreeNodeDto collectionTree = createCollectionTree(gdocImportBatchContext);
    CollectionVersionEntity cvEntity = collectionManager.addCollectionVersionForGoogleDoc(
        ofy, collectionEntity, collectionTree);
    return cvEntity;
  }

  /**
   * @param gdocImportBatchContext
   * @return
   */
  private CollectionTreeNodeDto createCollectionTree(
      GoogleDocImportBatchJobContext gdocImportBatchContext) {
    String title = gdocImportBatchContext.getCollectionTitle();

    if (StringUtils.isEmpty(title)) {
      title = "Untitled : " + new DateTime(LightUtils.getNow());
    }

    CollectionTreeNodeDto collectionTreeRoot = new CollectionTreeNodeDto.Builder()
        .type(TreeNodeType.ROOT_NODE)
        .title(title)
        .moduleType(ModuleType.LIGHT_COLLECTION)
        .externalId("null")
        .build();

    handOverChilds(gdocImportBatchContext, gdocImportBatchContext.getTreeRoot(), collectionTreeRoot);

    return collectionTreeRoot;
  }

  /**
   * @param treeRoot
   * @param collectionRoot
   */
  private void handOverChilds(GoogleDocImportBatchJobContext gdocImportBatchContext,
      GoogleDocTree gdocTreeParent, CollectionTreeNodeDto collectionParent) {
    if (!gdocTreeParent.hasChildren()) {
      return;
    }

    for (GoogleDocTree currGDocTreeChild : gdocTreeParent.getChildren()) {
      CollectionTreeNodeDto.Builder treeNodeBuilder = new CollectionTreeNodeDto.Builder()
          .title(currGDocTreeChild.getTitle())
          .type(currGDocTreeChild.getType())
          .moduleType(currGDocTreeChild.getGoogleDocResourceId().getModuleType())
          .externalId(currGDocTreeChild.getGoogleDocResourceId().getExternalId());

      CollectionTreeNodeDto treeNode = null;

      switch (currGDocTreeChild.getType()) {
        case ROOT_NODE:
          throw new IllegalStateException("Found a child with type = ROOT_NODE");

        case INTERMEDIATE_NODE:
          treeNode = treeNodeBuilder.build();
          handOverChilds(gdocImportBatchContext, currGDocTreeChild, treeNode);
          break;

        case LEAF_NODE:
          if (currGDocTreeChild.getGoogleDocResourceId().getModuleType().isSupported()) {
            ModuleId moduleId = gdocImportBatchContext.getModuleIdForExternalId(
                currGDocTreeChild.getGoogleDocResourceId().getExternalId());
            System.out.println(moduleId);
            treeNode = treeNodeBuilder.moduleId(moduleId).build();
          } else {
            // Ignoring unsupported types.
            continue;
          }
          break;

        default:
          throw new IllegalStateException("Unsupporetd type : " + currGDocTreeChild.getType());
      }

      checkNotNull(treeNode, "treeNode should have been created by now.");
      collectionParent.addChildren(treeNode);
    }
  }

  /**
   * @param jobEntity
   */
  private void resumeImportGoogleDocBatchIfChildsAreComplete(
      GoogleDocImportBatchJobContext gdocImportBatchContext, JobEntity jobEntity) {
    List<ImportDestinationDto> listOfDestinations =
        gdocImportBatchContext.getListOfImportDestinations();
    List<JobId> listOfChildJobIds = Lists.newArrayList();

    for (ImportDestinationDto curr : listOfDestinations) {
      listOfChildJobIds.add(curr.getJobId());
    }

    Map<JobId, JobEntity> map = this.findListOfJobs(listOfChildJobIds);

    boolean allChildsComplete = true;
    JobId pendingChildJobId = null;

    for (JobEntity currChildJob : map.values()) {
      if (currChildJob.getJobState() != JobState.COMPLETE) {
        allChildsComplete = false;
        pendingChildJobId = currChildJob.getId();
        break;
      }
    }

    if (!allChildsComplete) {
      throw new WaitForChildsToComplete(pendingChildJobId);
    }

    Objectify ofy = ObjectifyUtils.initiateTransaction();
    try {
      jobEntity.setJobState(JobState.ALL_CHILDS_COMPLETED_SUCCESSFULLY);
      this.put(ofy, jobEntity, new ChangeLogEntryPojo("all childs completed successfully"));
      this.enqueueLightJob(ofy, jobEntity.getId());

      ObjectifyUtils.commitTransaction(ofy);
    } finally {
      ObjectifyUtils.rollbackTransactionIfStillActive(ofy);
    }
  }

  private void createChildJobs(GoogleDocImportBatchJobContext gdocBatchImportContext,
      JobEntity jobEntity) {
    switch (jobEntity.getJobState()) {
      case ENQUEUED:
        updateRootNode(jobEntity, gdocBatchImportContext);
        
        if(!gdocBatchImportContext.getTreeRoot().hasChildren()) {
          // There is no child that needs to be worked on. So marking as complete.
          logger.info("Since there is no child which needs to be imported, "
              + "so marking job as complete.");
          enqueueCompleteJob(jobEntity.getId(), "no work needs to be done, so marking as done.");
          break;
        }
        
        // else continue creating childs.
        //$FALL-THROUGH$

      case CREATING_CHILDS:
        createChildJobsIfRequired(jobEntity, gdocBatchImportContext);
        break;
      default:
        throw new IllegalArgumentException("Unsupported State : " + jobEntity.getJobState());
    }
    System.out.println("done");
  }

  /**
   * @param jobEntity
   * @param gdocBatchImportContext
   */
  private void createChildJobsIfRequired(JobEntity jobEntity,
      GoogleDocImportBatchJobContext gdocBatchImportBatchContext) {
    List<PersonId> owners = Lists.newArrayList(GuiceUtils.getOwnerId());

    Set<GoogleDocInfoDto> setOfSupportedGoogleDocs = getSetOfSupportedGoogleDocs(
        gdocBatchImportBatchContext.getTreeRoot().getInOrderTraversalOfLeafNodes());

    List<GoogleDocResourceId> listOfGDocsToImport = Lists.newArrayList();

    for (GoogleDocInfoDto currInfo : setOfSupportedGoogleDocs) {
      System.out.println("Etag = " + currInfo.getEtag());
      System.out.println("resourceId = " + currInfo.getGoogleDocsResourceId().getTypedResourceId());

      GoogleDocResourceId resourceId = currInfo.getGoogleDocsResourceId();
      ModuleId moduleId = moduleManager.reserveModuleIdForExternalId(
          currInfo.getType(), resourceId.getExternalId(), owners);
      System.out.println(moduleId);

      boolean childAlreadyCreated = gdocBatchImportBatchContext.isChildJobCreated(
          resourceId.getExternalId(), moduleId);

      if (childAlreadyCreated) {
        logger.info("Seems child is already created, So ignoring.");
        continue;
      }

      Version version = moduleManager.reserveModuleVersionForImport(
          moduleId, currInfo.getEtag(), currInfo.getLastEditTime());
      System.out.println(version);

      if (version != null) {
        // Now creating a child job and adding its reference in Job Context.
        Objectify ofy = ObjectifyUtils.initiateTransaction();
        try {
          // First enqueuing a child job for this resource.
          GoogleDocImportJobContext childJobContext = new GoogleDocImportJobContext.Builder()
              .resourceInfo(currInfo)
              .moduleId(moduleId)
              .version(version)
              .state(GoogleDocImportJobContext.GoogleDocImportJobState.ENQUEUED)
              .build();
          JobEntity childJob = this.enqueueGoogleDocImportChildJob(ofy,
              childJobContext, jobEntity.getId(), jobEntity.getId());

          ImportDestinationDto dest = new ImportDestinationDto.Builder()
              .moduleType(currInfo.getType())
              .externalId(resourceId.getExternalId())
              .moduleId(moduleId)
              .version(version)
              .jobId(childJob.getId())
              .build();
          gdocBatchImportBatchContext.addImportDestination(dest);

          Text context = new Text(gdocBatchImportBatchContext.toJson());
          updateJobContext(ofy, JobState.CREATING_CHILDS, context, jobEntity,
              "Adding childJob : " + childJob.getId());
          listOfGDocsToImport.add(resourceId);
          ObjectifyUtils.commitTransaction(ofy);
        } finally {
          ObjectifyUtils.rollbackTransactionIfStillActive(ofy);
        }
      }
    }

    Text context = new Text(gdocBatchImportBatchContext.toJson());
    if (isListEmpty(listOfGDocsToImport)) {
      updateJobContext(null, JobState.COMPLETE, context, jobEntity, "Marking job as complete.");
    } else {
      updateJobContext(null, JobState.WAITING_FOR_CHILD_COMPLETE_NOTIFICATION, context,
          jobEntity, "Marking job as Waiting for ChildJobs.");
    }

  }

  /**
   * @param gdocBatchImportContext
   */
  private void updateRootNode(JobEntity jobEntity,
      GoogleDocImportBatchJobContext gdocBatchImportContext) {
    GoogleDocTree rootNode = new GoogleDocTree.Builder()
        .title("root")
        .type(TreeNodeType.ROOT_NODE)
        .build();
    googleDocJobs.addChildTreeToParent(rootNode, gdocBatchImportContext.get());
    gdocBatchImportContext.setTreeRoot(rootNode);

    Text context = new Text(gdocBatchImportContext.toJson());
    updateJobContext(null, JobState.CREATING_CHILDS, context, jobEntity,
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
      if (currNode.getResourceInfo().getGoogleDocsResourceId().getModuleType().isSupported()) {
        map.put(currNode.getGoogleDocResourceId(), currNode.getResourceInfo());
      }
    }

    return Sets.newHashSet(map.values());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public JobEntity enqueueCompleteJob(JobId jobId, String message) {
    LightPreconditions.checkNotBlank(message, "message cannot be null");
    Objectify ofy = ObjectifyUtils.initiateTransaction();
    try {
      JobEntity jobEntity = this.get(ofy, jobId);
      jobEntity.setJobState(JobState.COMPLETE);
      this.put(ofy, jobEntity, new ChangeLogEntryPojo(message));

      if (!jobEntity.isRootJob()) {
        notificationManager.notifyChildCompletionEvent(ofy, jobEntity.getParentJobId(),
            jobEntity.getId());
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
}
