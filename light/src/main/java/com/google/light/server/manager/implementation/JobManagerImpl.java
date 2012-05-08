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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.constants.LightConstants.JOB_BACK_OFF_FACTOR;
import static com.google.light.server.constants.LightConstants.JOB_BACK_OFF_SECONDS;
import static com.google.light.server.constants.LightConstants.JOB_MAX_ATTEMPTES;
import static com.google.light.server.exception.ExceptionType.SERVER_GUICE_INJECTION;
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
import com.google.appengine.tools.pipeline.PipelineService;
import com.google.appengine.tools.pipeline.PipelineServiceFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.light.server.dto.module.ImportDestinationDto;
import com.google.light.server.dto.pojo.ChangeLogEntryPojo;
import com.google.light.server.dto.pojo.GoogleDocArchivePojo;
import com.google.light.server.dto.pojo.GoogleDocArchivePojo.LightGDocArchiveStatus;
import com.google.light.server.dto.pojo.JobHandlerId;
import com.google.light.server.dto.pojo.LightJobContextPojo;
import com.google.light.server.dto.pojo.RequestScopedValues;
import com.google.light.server.dto.pojo.longwrapper.JobId;
import com.google.light.server.dto.pojo.longwrapper.ModuleId;
import com.google.light.server.dto.pojo.longwrapper.PersonId;
import com.google.light.server.dto.pojo.longwrapper.Version;
import com.google.light.server.dto.pojo.tree.GoogleDocTree;
import com.google.light.server.dto.pojo.tree.AbstractTreeNode.TreeNodeType;
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
import com.google.light.server.manager.interfaces.ImportManager;
import com.google.light.server.manager.interfaces.JobManager;
import com.google.light.server.manager.interfaces.ModuleManager;
import com.google.light.server.manager.interfaces.NotificationManager;
import com.google.light.server.manager.interfaces.QueueManager;
import com.google.light.server.persistence.dao.JobDao;
import com.google.light.server.persistence.entity.jobs.JobEntity;
import com.google.light.server.persistence.entity.jobs.JobEntity.JobHandlerType;
import com.google.light.server.persistence.entity.jobs.JobEntity.JobState;
import com.google.light.server.persistence.entity.jobs.JobEntity.JobType;
import com.google.light.server.persistence.entity.jobs.JobEntity.TaskType;
import com.google.light.server.persistence.entity.queue.importflow.ImportJobEntity;
import com.google.light.server.thirdparty.clients.google.gdata.gdoc.DocsServiceWrapper;
import com.google.light.server.utils.GoogleCloudStorageUtils;
import com.google.light.server.utils.GuiceUtils;
import com.google.light.server.utils.JsonUtils;
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
  private ImportManager importManager;
  private ModuleManager moduleManager;
  private NotificationManager notificationManager;
  private QueueManager queueManager;
  private GoogleDocJobs googleDocJobs;
  private Provider<DocsServiceWrapper> docsServiceProvider;

  @Inject
  public JobManagerImpl(JobDao jobDao, ImportManager importManager, ModuleManager moduleManager,
      NotificationManager notificationManager,
      QueueManager queueManager, GoogleDocJobs googleDocJobs,
      Provider<DocsServiceWrapper> docsServiceProvider) {
    this.jobDao = checkNotNull(jobDao, "jobDao");
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
        this.enqueueCompleteJob(jobEntity.getId());
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
    try {
      FileService fileService = FileServiceFactory.getFileService();

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

        // // TODO(arjuns): Get contentType from File extension.
        // // Storing file on Cloud Storage.
        // String nameFromGoogleDoc = zipEntry.getName();
        //
        // checkArgument(nameFromGoogleDoc.contains("/"), "Unexpected name from GoogleDoc : "
        // + nameFromGoogleDoc);
        // String parts[] = nameFromGoogleDoc.split("/");
        //
        // String newFileName = "";
        // // Parts[0] is always the folder whose name is same as prettified title for a GoogleDoc.
        // if (parts.length == 2) {
        // // This will be true for HTML file.
        // newFileName = parts[1];
        // } else {
        // Preconditions.checkArgument(parts.length == 3, "Google Docs will return files " +
        // "name of format <pretty title/images/image names>");
        // // This will be true for images/resources.
        // newFileName = parts[1] + "/" + parts[2];
        // }
        //
        // if (newFileName.endsWith(FileExtensions.HTML.get())) {
        // newFileName = moduleId.getValue() + "." + FileExtensions.HTML.get();
        // }
        //
        // ContentTypeEnum contentType =
        // FileExtensions.getFileExtension(newFileName).getContentType();
        // String storeFileName = moduleId.getValue() + "/" + newFileName;
        // String storeAbsFilePath = CONTENT.getAbsoluteFilePath(storeFileName);
        //
        // GSBlobInfo gsBlobInfo = new GSBlobInfo.Builder()
        // .contentType(contentType)
        // .fileName(parts[parts.length - 1])
        // .gsKey(storeAbsFilePath)
        // .sizeInBytes(zipEntry.getSize())
        // .build();
        // resourceMap.put(newFileName, gsBlobInfo);
        //
        // updateChangeLog(null, getContext().getJobId(), "Storing [" + zipEntry.getName()
        // + "] as [" +
        // storeAbsFilePath + "].");
        //
        // GSFileOptions gsFileOptions = GoogleCloudStorageUtils.getGCSFileOptionsForCreate(
        // GoogleCloudStorageBuckets.CONTENT, contentType, storeFileName);
        // writeFileOnGCS(zipIn, gsFileOptions);
        //
        // updateChangeLog(null, getContext().getJobId(), "Successfully stored");
      } while (zipEntry != null);

      zipIn.close();
      inputStream.close();
      readChannel.close();

      // updateChangeLog(null, getContext().getJobId(),
      // "Finished unarchiving files from [" + fileName + "].");
      //
      // // Now lets add the moduleVersion. This will require adding html and resources.
      // // First adding Html.
      // FutureValue<ModuleVersionEntity> moduleVersionEntityFV = futureCall(new AddContent(),
      // immediate(getContext()), immediate(importEntityId), immediate(moduleId));
      //
      // futureCall(new PipelineJobs.DummyJob(), immediate(getContext()),
      // immediate("module version created"),
      // waitFor(moduleVersionEntityFV));
      //
      // int htmlCount = 0;
      // @SuppressWarnings("rawtypes")
      // List<FutureValue> jobsToWait = Lists.newArrayList();
      // // Now moduleVersion is created. So adding resources for that.
      // for (String currKey : resourceMap.keySet()) {
      // // We dont want to store HTML files as resources. They are handled separately.
      // if (currKey.endsWith(FileExtensions.HTML.get())) {
      // checkArgument(htmlCount == 0, "Found more then one HtmlFiles for ImportId["
      // + importEntityId + "].");
      // htmlCount++;
      // continue;
      // }
      //
      // GSBlobInfo gsBlobInfo = resourceMap.get(currKey);
      // logger.info(JsonUtils.toJson(gsBlobInfo));
      //
      // FutureValue<Void> fv = futureCall(
      // new ModuleJobs.AddModuleResources(),
      // immediate(getContext()),
      // moduleVersionEntityFV,
      // immediate(importEntityId),
      // immediate(currKey),
      // immediate(gsBlobInfo));
      // jobsToWait.add(fv);
      // }
      //
      // // Hacky way so that we wait for all the resources to be created before returning.
      // // TODO(arjuns): Find a better way to do this.
      // FutureValue<LightJobContextPojo> context = null;
      // for (FutureValue<Long> curr : jobsToWait) {
      // context =
      // futureCall(new PipelineJobs.DummyJob(), immediate(getContext()), immediate(""),
      // waitFor(curr));
      // }
      //
      // return context;

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

    gdocImportContext.setState(GoogleDocImportJobContext.GoogleDocImportJobState.ARCHIVE_DOWNLOADED);
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
    GoogleDocArchivePojo archiveInfo = docsServiceProvider.get().archiveResource(
        resourceInfo.getGoogleDocsResourceId());
    gdocImportContext.setArchiveId(archiveInfo.getArchiveId());

    // String archiveId =
    // "vax2Nuv9Oq4VIPQEBTcxlJDkNbRFv7KVNL55qVoitx7rtrJHru1T6ljtTjMW26L3ygWrvW1tMZHHg6ARCy3Uj_n3moSCE8uDDX6WKaTD-0nw0zcwfBiiFEufFSITnTO06n0TsXIIHSQ";
    // gdocImportContext.setArchiveId(archiveId);

    gdocImportContext.setState(GoogleDocImportJobContext.GoogleDocImportJobState.WAITING_FOR_ARCHIVE);

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
        finishJob(jobEntity);
        break;

    // default:
    // throw new IllegalStateException("Unsupported state : " + state);
    }
  }

  /**
   * 
   */
  private void publishCollectionIfRequired(
      GoogleDocImportBatchJobContext gdocImportBatchContext, JobEntity jobEntity) {
    System.out.println("Eventually add collection. Right now do nothing.");
    this.enqueueCompleteJob(jobEntity.getId());
  }

  //
  // /**
  // * @param jobEntity
  // */
  // private void publishGoogleDocs(GoogleDocBatchImportJobContext gdocImportContext, JobEntity
  // jobEntity) {
  //
  // FileService fileService = FileServiceFactory.getFileService();
  // Map<String, GSBlobInfo> resourceMap = Maps.newConcurrentMap();
  //
  // try {
  // String readFileName = gdocImportContext.getGCSArchiveLocation();
  //
  // AppEngineFile file = new AppEngineFile(readFileName);
  // FileReadChannel readChannel = fileService.openReadChannel(file, true /* lock */);
  //
  // InputStream inputStream = Channels.newInputStream(readChannel);
  // ZipInputStream zipIn = new ZipInputStream(inputStream);
  //
  // ZipEntry zipEntry = null;
  // do {
  // zipEntry = zipIn.getNextEntry();
  // if (zipEntry == null) {
  // continue;
  // }
  // System.out.println(zipEntry.getName());
  //
  // // // TODO(arjuns): Get contentType from File extension.
  // // // Storing file on Cloud Storage.
  // // String nameFromGoogleDoc = zipEntry.getName();
  // //
  // // checkArgument(nameFromGoogleDoc.contains("/"), "Unexpected name from GoogleDoc : "
  // // + nameFromGoogleDoc);
  // // String parts[] = nameFromGoogleDoc.split("/");
  // //
  // // String newFileName = "";
  // // // Parts[0] is always the folder whose name is same as prettified title for a GoogleDoc.
  // // if (parts.length == 2) {
  // // // This will be true for HTML file.
  // // newFileName = parts[1];
  // // } else {
  // // Preconditions.checkArgument(parts.length == 3, "Google Docs will return files " +
  // // "name of format <pretty title/images/image names>");
  // // // This will be true for images/resources.
  // // newFileName = parts[1] + "/" + parts[2];
  // // }
  // //
  // // if (newFileName.endsWith(FileExtensions.HTML.get())) {
  // // newFileName = moduleId.getValue() + "." + FileExtensions.HTML.get();
  // // }
  // //
  // // ContentTypeEnum contentType =
  // // FileExtensions.getFileExtension(newFileName).getContentType();
  // // String storeFileName = moduleId.getValue() + "/" + newFileName;
  // // String storeAbsFilePath = CONTENT.getAbsoluteFilePath(storeFileName);
  // //
  // // GSBlobInfo gsBlobInfo = new GSBlobInfo.Builder()
  // // .contentType(contentType)
  // // .fileName(parts[parts.length - 1])
  // // .gsKey(storeAbsFilePath)
  // // .sizeInBytes(zipEntry.getSize())
  // // .build();
  // // resourceMap.put(newFileName, gsBlobInfo);
  // //
  // // updateChangeLog(null, getContext().getJobId(), "Storing [" + zipEntry.getName()
  // // + "] as [" +
  // // storeAbsFilePath + "].");
  // //
  // // GSFileOptions gsFileOptions = GoogleCloudStorageUtils.getGCSFileOptionsForCreate(
  // // GoogleCloudStorageBuckets.CONTENT, contentType, storeFileName);
  // // writeFileOnGCS(zipIn, gsFileOptions);
  // } while (zipEntry != null);
  //
  // zipIn.close();
  // inputStream.close();
  // readChannel.close();
  // } catch (Exception e) {
  // throw new GoogleDocException(e);
  // }
  // }

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

  /**
   * @param jobEntity
   */
  private void finishJob(JobEntity jobEntity) {
    jobEntity.setJobState(JobState.COMPLETE);
    this.put(null, jobEntity, new ChangeLogEntryPojo("Finished successfully."));
  }

  private void createChildJobs(GoogleDocImportBatchJobContext gdocBatchImportContext,
      JobEntity jobEntity) {
    switch (jobEntity.getJobState()) {
      case ENQUEUED:
        updateRootNode(jobEntity, gdocBatchImportContext);
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

    Objectify ofy = ObjectifyUtils.initiateTransaction();
    try {
      ChangeLogEntryPojo changeLog = null;
      if (isListEmpty(listOfGDocsToImport)) {
        jobEntity.setJobState(JobState.COMPLETE);
        changeLog = new ChangeLogEntryPojo("Marking job as complete.");
      } else {
        jobEntity.setJobState(JobState.WAITING_FOR_CHILD_COMPLETE_NOTIFICATION);
        changeLog = new ChangeLogEntryPojo("Marking job as Waiting for ChildJobs.");
      }

      System.out.println("*** State = " + jobEntity.getJobState());
      System.out.println(changeLog.getDetail());
      this.put(ofy, jobEntity, changeLog);

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
        .type(TreeNodeType.ROOT_NODE)
        .build();
    googleDocJobs.addChildTreeToParent(rootNode, gdocBatchImportContext.get());
    gdocBatchImportContext.setTreeRoot(rootNode);

    jobEntity.setJobState(JobState.CREATING_CHILDS);
    this.put(null, jobEntity,
        new ChangeLogEntryPojo("Updating root and setting state to CreateChilds."));
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
  public JobEntity enqueueCompleteJob(JobId jobId) {
    Objectify ofy = ObjectifyUtils.initiateTransaction();
    try {
      JobEntity jobEntity = this.get(ofy, jobId);
      jobEntity.setJobState(JobState.COMPLETE);
      this.put(ofy, jobEntity, new ChangeLogEntryPojo("Marking job as complete"));

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
