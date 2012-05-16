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
package com.google.light.server.jobs.handlers.modulejobs;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.constants.google.cloudstorage.GoogleCloudStorageBuckets.CONTENT;
import static com.google.light.server.exception.ExceptionType.SERVER_GUICE_INJECTION;
import static com.google.light.server.jersey.resources.thirdparty.mixed.ImportResource.updateImportExternalIdDtoForModules;
import static com.google.light.server.jobs.JobUtils.updateJobContext;
import static com.google.light.server.jobs.handlers.modulejobs.ModuleJobUtils.reserveModuleId;
import static com.google.light.server.utils.GoogleCloudStorageUtils.writeFileOnGCS;
import static com.google.light.server.utils.GuiceUtils.getInstance;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.LightPreconditions.checkNotNull;

import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileReadChannel;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileServiceFactory;
import com.google.appengine.api.files.GSFileOptions;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.light.server.constants.FileExtensions;
import com.google.light.server.constants.QueueEnum;
import com.google.light.server.constants.google.cloudstorage.GoogleCloudStorageBuckets;
import com.google.light.server.constants.http.ContentTypeEnum;
import com.google.light.server.dto.importresource.ImportExternalIdDto;
import com.google.light.server.dto.module.GSBlobInfo;
import com.google.light.server.dto.module.ModuleState;
import com.google.light.server.dto.module.ModuleType;
import com.google.light.server.dto.pojo.GoogleDocArchivePojo;
import com.google.light.server.dto.pojo.GoogleDocArchivePojo.LightGDocArchiveStatus;
import com.google.light.server.dto.pojo.tree.AbstractTreeNode.TreeNodeType;
import com.google.light.server.dto.pojo.tree.collection.CollectionTreeNodeDto;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.JobId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.ModuleId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.PersonId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.Version;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocInfoDto;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocResourceId;
import com.google.light.server.exception.unchecked.GoogleDocException;
import com.google.light.server.exception.unchecked.taskqueue.GoogleDocArchivalWaitingException;
import com.google.light.server.jobs.handlers.JobHandlerInterface;
import com.google.light.server.manager.interfaces.JobManager;
import com.google.light.server.manager.interfaces.ModuleManager;
import com.google.light.server.persistence.entity.jobs.JobEntity;
import com.google.light.server.persistence.entity.module.ModuleEntity;
import com.google.light.server.thirdparty.clients.google.gdata.gdoc.DocsServiceWrapper;
import com.google.light.server.utils.GoogleCloudStorageUtils;
import com.google.light.server.utils.GuiceUtils;
import com.google.light.server.utils.JsonUtils;
import com.google.light.server.utils.LightUtils;
import com.google.light.server.utils.ObjectifyUtils;
import com.googlecode.objectify.Objectify;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.util.Map;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.lang.StringUtils;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class ImportModuleGoogleDocJobHandler implements JobHandlerInterface {
  private static final Logger logger = Logger.getLogger(ImportModuleGoogleDocJobHandler.class
      .getName());

  private JobManager jobManager;
  private ModuleManager moduleManager;

  @Inject
  public ImportModuleGoogleDocJobHandler(JobManager jobManager,
      ModuleManager moduleManager) {
    this.jobManager = checkNotNull(jobManager, SERVER_GUICE_INJECTION, "jobManager");
    this.moduleManager = checkNotNull(moduleManager, SERVER_GUICE_INJECTION, "moduleManager");
  }

  /**
   * This is a linear job. So all stages will depend on JobContext.
   */
  @Override
  public void handle(JobEntity jobEntity) {
    ImportModuleGoogleDocJobContext gdocImportContext =
        jobEntity.getContext(ImportModuleGoogleDocJobContext.class);

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
        handleImportModuleGDocComplete(gdocImportContext, jobEntity);
        break;

      default:
        throw new IllegalStateException("Unsupported State : " + gdocImportContext.getState());
    }
  }

  /**
   * @param gdocImportContext
   * @param jobEntity
   */
  private void handleImportModuleGDocComplete(ImportModuleGoogleDocJobContext gdocImportContext,
      JobEntity jobEntity) {
    ModuleEntity moduleEntity = moduleManager.get(null, gdocImportContext.getModuleId());

    CollectionTreeNodeDto collectionNode = new CollectionTreeNodeDto.Builder()
        .title(moduleEntity.getTitle())
        .nodeType(TreeNodeType.LEAF_NODE)
        .moduleId(gdocImportContext.getModuleId())
        .version(gdocImportContext.getVersion())
        .moduleType(moduleEntity.getModuleType())
        .externalId(moduleEntity.getExternalId())
        .build();
    jobManager.enqueueCompleteJob(jobEntity.getJobId(), collectionNode,
        "Successfully completed Google Doc Job.");
  }

  public void handleGoogleDocModuleJob(JobEntity jobEntity, GoogleDocInfoDto gdocInfo,
      JobId parentJobId, JobId rootJobId) {
    ModuleType moduleType = gdocInfo.getModuleType();

    switch (moduleType) {
      case GOOGLE_DOCUMENT:
        ImportExternalIdDto importModuleDto = new ImportExternalIdDto.Builder()
            .title(gdocInfo.getTitle())
            .externalId(gdocInfo.getExternalId())
            .build();
        enqueueModuleGoogleDocJob(importModuleDto, gdocInfo, parentJobId, rootJobId);
        break;

      default:
        throw new IllegalStateException("Unsupported moduleType " + moduleType + " for "
            + jobEntity.getJobId());

    }
  }

  // TODO(arjuns): Move this inside jobManager.
  public void enqueueModuleGoogleDocJob(ImportExternalIdDto importModuleDto,
      GoogleDocInfoDto gdocInfo, JobId parentJobId, JobId rootJobId) {
    PersonId ownerId = GuiceUtils.getOwnerId();

    ModuleId moduleId = reserveModuleId(moduleManager, importModuleDto, ownerId);
    checkNotNull(moduleId, "moduleId should not be null here.");

    // Now reserving a module version and creating a GDoc Child job that will publish this version.
    Objectify ofy = ObjectifyUtils.initiateTransaction();
    try {
      Version reservedVersion = moduleManager.reserveModuleVersion(ofy, moduleId,
          gdocInfo.getEtag(), gdocInfo.getLastEditTime());
      logger.info("Reserved/created module : [" + moduleId + "], version : " + reservedVersion);

      JobEntity childJob = null;
      ModuleState moduleState = null;
      ModuleType moduleType = importModuleDto.getModuleType();
      String title = null;
      if (reservedVersion != null) {
        // Now creating a child job and adding its reference in Job Context.

        // First enqueuing a child job for this resource.
        title = calculateTitleFromContext(importModuleDto, gdocInfo, null, true);
        ImportModuleGoogleDocJobContext childJobContext =
            new ImportModuleGoogleDocJobContext.Builder()
                .title(title)
                .resourceInfo(gdocInfo)
                .moduleId(moduleId)
                .version(reservedVersion)
                .state(ImportModuleGoogleDocJobContext.GoogleDocImportJobState.ENQUEUED)
                .build();
        childJob = jobManager.createGoogleDocImportChildJob(ofy,
            childJobContext, parentJobId, rootJobId);
        if (reservedVersion.isFirstVersion()) {
          moduleState = ModuleState.IMPORTING;
        } else {
          moduleState = ModuleState.REFRESHING;
        }
        moduleType = gdocInfo.getModuleType();

        logger.info("Module State = " + moduleState);
        logger.info("ModuleType = " + moduleType);
      } else {
        // Google Document is up to date. So use the latest version as the reserved version.
        ModuleEntity moduleEntity = moduleManager.get(ofy, moduleId);
        reservedVersion = moduleEntity.getLatestPublishVersion();
        moduleState = ModuleState.PUBLISHED;
        title = calculateTitleFromContext(importModuleDto, gdocInfo, moduleEntity, false);
        moduleType = moduleEntity.getModuleType();
        logger.info("Module State = " + moduleState);
        logger.info("ModuleType = " + moduleType);
      }

      // Verify that values are initialized.
      checkNotNull(moduleState, "moduleState cannot be null here.");
      checkNotNull(moduleType, "moduleType cannot be null here.");
      checkNotBlank(title, "title cannot be empty here.");
      /*
       * Now saving details about ModuleId and version as part of ExternalIdDto that will be
       * eventually sent to the client. Also this will be stored as part of the parent Batch Job
       * which can be useful for Debugging later.
       */
      updateImportExternalIdDtoForModules(importModuleDto, moduleId, reservedVersion, moduleState,
          moduleType, title, childJob);

      if (childJob != null) {
        jobManager.enqueueImportChildJob(ofy, parentJobId, childJob.getJobId(), importModuleDto,
            QueueEnum.GDOC_INTERACTION);
      }
      ObjectifyUtils.commitTransaction(ofy);
    } finally {
      ObjectifyUtils.rollbackTransactionIfStillActive(ofy);
    }
  }

  private String calculateTitleFromContext(ImportExternalIdDto importModuleDto,
      GoogleDocInfoDto gdocInfo, ModuleEntity moduleEntity, boolean publishingNewVersion) {
    if (publishingNewVersion) {
      checkNotNull(importModuleDto, "importModuleDto");
      checkNotNull(gdocInfo, "gdocInfo");
      if (StringUtils.isNotBlank(importModuleDto.getTitle())) {
        return importModuleDto.getTitle();
      } else {
        return gdocInfo.getTitle();
      }
    } else {
      // Assumption is moduleEntity has latest title.
      checkNotNull(moduleEntity, "moduleEntity");
      return moduleEntity.getTitle();
    }
  }

  /**
   * @param gdocImportContext
   * @param jobEntity
   */
  private void startArchivingGoogleDoc(ImportModuleGoogleDocJobContext gdocImportContext,
      JobEntity jobEntity) {
    DocsServiceWrapper docsService = GuiceUtils.getInstance(DocsServiceWrapper.class);

    GoogleDocInfoDto resourceInfo = gdocImportContext.getResourceInfo();

    GoogleDocResourceId gdocResourceId = new GoogleDocResourceId(resourceInfo.getExternalId());
     GoogleDocArchivePojo archiveInfo = docsService.archiveResource(gdocResourceId);
     gdocImportContext.setArchiveId(archiveInfo.getArchiveId());

//    String archiveId =
//        "nTSZLtpDRSP5IP11Sol04pDkNbRFv7KVNL55qVoitx7rtrJHru1T6ljtTjMW26L3ygWrvW1tMZHHg6ARCy3UjxZRafUSMAgIFesW90teEkzsHCQ6FK96YLFEU7tKurf2hsM-tVKSUYw";
//    gdocImportContext.setArchiveId(archiveId);

    gdocImportContext
        .setState(ImportModuleGoogleDocJobContext.GoogleDocImportJobState.WAITING_FOR_ARCHIVE);

    Objectify ofy = ObjectifyUtils.initiateTransaction();
    try {
      updateJobContext(null, jobManager, jobEntity.getJobState(), gdocImportContext, jobEntity,
          "Waiting for Archive");
      jobManager.enqueueGoogleDocInteractionJob(ofy, jobEntity.getJobId());
      ObjectifyUtils.commitTransaction(ofy);
    } finally {
      ObjectifyUtils.rollbackTransactionIfStillActive(ofy);
    }
  }

  /**
   * @param gdocImportContext
   * @param jobEntity
   */
  private void downloadArchiveIfReady(ImportModuleGoogleDocJobContext gdocImportContext,
      JobEntity jobEntity) {
    DocsServiceWrapper docsService = GuiceUtils.getInstance(DocsServiceWrapper.class);

    String archiveId = gdocImportContext.getArchiveId();
    GoogleDocArchivePojo archivePojo = docsService.getArchiveStatus(archiveId);

    if (archivePojo.getArchiveStatus() != LightGDocArchiveStatus.FINISHED) {
      throw new GoogleDocArchivalWaitingException();
    }

    String destinationFileName = GoogleCloudStorageUtils.getDestinationFileNameForGDoc();
    String gcsArchiveLocation = docsService.downloadArchive(
        archivePojo.getContentLocation(), destinationFileName);

    gdocImportContext
        .setState(ImportModuleGoogleDocJobContext.GoogleDocImportJobState.ARCHIVE_DOWNLOADED);
    gdocImportContext.setGCSArchiveLocation(gcsArchiveLocation);

    Objectify ofy = ObjectifyUtils.initiateTransaction();
    try {
      updateJobContext(ofy, jobManager, jobEntity.getJobState(), gdocImportContext, jobEntity,
          "archive downloaded.");
      jobManager.enqueueLightJob(ofy, jobEntity.getJobId());
      ObjectifyUtils.commitTransaction(ofy);
    } finally {
      ObjectifyUtils.rollbackTransactionIfStillActive(ofy);
    }
  }

  /**
   * @param gdocImportContext
   * @param jobEntity
   */
  private void
      publishModule(ImportModuleGoogleDocJobContext gdocImportContext, JobEntity jobEntity) {
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

      Objectify ofy = ObjectifyUtils.initiateTransaction();
      try {
        ModuleManager moduleManager = getInstance(ModuleManager.class);

        GoogleDocInfoDto resourceInfo = gdocImportContext.getResourceInfo();
        // First publish HTML on Light.
        moduleManager.publishModuleVersion(ofy, moduleId, version,
            resourceInfo.getExternalId(), gdocImportContext.getTitle(), htmlContent,
            resourceInfo.getEtag(), resourceInfo.getLastEditTime());

        // Now publishing associated resources.
        for (String currKey : resourceMap.keySet()) {
          // We dont want to store HTML files as resources. They are handled separately.
          if (currKey.endsWith(FileExtensions.HTML.get())) {
            continue;
          }

          GSBlobInfo gsBlobInfo = resourceMap.get(currKey);
          logger.info(JsonUtils.toJson(gsBlobInfo));
          moduleManager.publishModuleResource(ofy, moduleId, version, currKey,
              gsBlobInfo);
        }

        gdocImportContext
            .setState(ImportModuleGoogleDocJobContext.GoogleDocImportJobState.COMPLETE);

        // Now generating result for this job.
        CollectionTreeNodeDto node = new CollectionTreeNodeDto.Builder()
            .title(gdocImportContext.getResourceInfo().getTitle())
            .nodeType(TreeNodeType.LEAF_NODE)
            .moduleId(gdocImportContext.getModuleId())
            .externalId(gdocImportContext.getResourceInfo().getExternalId())
            .moduleType(gdocImportContext.getResourceInfo().getModuleType())
            .build();
        jobEntity.setResponse(node);

        updateJobContext(ofy, jobManager, jobEntity.getJobState(), gdocImportContext, jobEntity,
            "Published module[" + moduleId + ":" + version);
        jobManager.enqueueLightJob(ofy, jobEntity.getJobId());
        ObjectifyUtils.commitTransaction(ofy);
      } finally {
        ObjectifyUtils.rollbackTransactionIfStillActive(ofy);
      }
    } catch (Exception e) {
      throw new GoogleDocException(e);
    }
  }
}
