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
package com.google.light.server.jersey.resources.thirdparty.mixed;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.constants.http.ContentTypeEnum.getContentTypeByString;
import static com.google.light.server.httpclient.LightHttpClient.getHeaderValueFromResponse;
import static com.google.light.server.jobs.handlers.modulejobs.ModuleJobUtils.reserveModuleId;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.LightPreconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkPersonLoggedIn;
import static com.google.light.server.utils.LightUtils.getURI;

import com.google.light.server.jobs.handlers.modulejobs.ImportModuleSyntheticModuleJobContext;

import com.google.light.server.dto.pojo.ChangeLogEntryPojo;

import com.google.api.client.http.HttpResponse;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.light.server.constants.HttpHeaderEnum;
import com.google.light.server.constants.JerseyConstants;
import com.google.light.server.constants.QueueEnum;
import com.google.light.server.constants.http.ContentTypeConstants;
import com.google.light.server.constants.http.ContentTypeEnum;
import com.google.light.server.dto.importresource.ImportBatchType;
import com.google.light.server.dto.importresource.ImportBatchWrapper;
import com.google.light.server.dto.importresource.ImportExternalIdDto;
import com.google.light.server.dto.module.ModuleState;
import com.google.light.server.dto.module.ModuleType;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.JobId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.ModuleId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.PersonId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.Version;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocInfoDto;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocResourceId;
import com.google.light.server.exception.ExceptionType;
import com.google.light.server.httpclient.LightHttpClient;
import com.google.light.server.jersey.resources.AbstractJerseyResource;
import com.google.light.server.jobs.handlers.collectionjobs.ImportCollectionGoogleDocJobHandler;
import com.google.light.server.jobs.handlers.modulejobs.ImportModuleGoogleDocJobHandler;
import com.google.light.server.manager.interfaces.CollectionManager;
import com.google.light.server.manager.interfaces.JobManager;
import com.google.light.server.manager.interfaces.ModuleManager;
import com.google.light.server.persistence.entity.collection.CollectionEntity;
import com.google.light.server.persistence.entity.jobs.JobEntity;
import com.google.light.server.persistence.entity.jobs.JobState;
import com.google.light.server.persistence.entity.module.ModuleEntity;
import com.google.light.server.servlets.SessionManager;
import com.google.light.server.thirdparty.clients.google.gdata.gdoc.DocsServiceWrapper;
import com.google.light.server.utils.GuiceUtils;
import com.google.light.server.utils.JsonUtils;
import com.google.light.server.utils.ObjectifyUtils;
import com.google.light.server.utils.XmlUtils;
import com.googlecode.objectify.Objectify;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import org.apache.commons.lang.StringUtils;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@Path(JerseyConstants.RESOURCE_IMPORT)
public class ImportResource extends AbstractJerseyResource {
  private static final Logger logger = Logger.getLogger(ImportResource.class.getName());

  private LightHttpClient httpClient;
  private JobManager jobManager;
  private ModuleManager moduleManager;
  private CollectionManager collectionManager;
  private ImportCollectionGoogleDocJobHandler importCollectionGDocJobHandler;
  private ImportModuleGoogleDocJobHandler importGoogleDocModuleJobHandler;

  @Inject
  public ImportResource(Injector injector, HttpServletRequest request,
      HttpServletResponse response, JobManager jobManager, CollectionManager collectionManager,
      ModuleManager moduleManager,
      ImportCollectionGoogleDocJobHandler importCollectionGDocJobHandler,
      ImportModuleGoogleDocJobHandler importGoogleDocModuleJob,
      LightHttpClient httpClient) {
    super(injector, request, response);
    this.jobManager = checkNotNull(jobManager, ExceptionType.SERVER_GUICE_INJECTION, "jobManager");
    this.collectionManager = checkNotNull(collectionManager, "collectionManager");
    this.moduleManager = checkNotNull(
        moduleManager, ExceptionType.SERVER_GUICE_INJECTION, "moduleManager");

    this.importCollectionGDocJobHandler = checkNotNull(importCollectionGDocJobHandler,
        ExceptionType.SERVER_GUICE_INJECTION, "importCollectionGDocJobHandler");
    
    this.importGoogleDocModuleJobHandler = checkNotNull(importGoogleDocModuleJob,
        ExceptionType.SERVER_GUICE_INJECTION, "importGoogleDocModuleJob");
    
    this.httpClient = checkNotNull(httpClient, ExceptionType.SERVER_GUICE_INJECTION, "httpClient");
  }

  @POST
  @Consumes(ContentTypeConstants.APPLICATION_URL_ENCODED)
  @Produces({ ContentTypeConstants.APPLICATION_JSON, ContentTypeConstants.APPLICATION_XML })
  @Path(JerseyConstants.PATH_IMPORT_BATCH)
  public ImportBatchWrapper importBatchFormPost(
      @FormParam("json_body") String body) {
    return importBatchPost(body);
  }

  @POST
  @Consumes({ ContentTypeConstants.APPLICATION_JSON, ContentTypeConstants.APPLICATION_XML })
  @Produces({ ContentTypeConstants.APPLICATION_JSON, ContentTypeConstants.APPLICATION_XML })
  @Path(JerseyConstants.PATH_IMPORT_BATCH)
  public ImportBatchWrapper importBatchPost(String body) {
    SessionManager sessionManager = GuiceUtils.getInstance(SessionManager.class);
    checkPersonLoggedIn(sessionManager);

    ContentTypeEnum contentType = getContentTypeByString(request.getContentType());

    ImportBatchWrapper importBatchWrapper = null;
    if (contentType == ContentTypeEnum.APPLICATION_JSON
        || contentType == ContentTypeEnum.APPLICATION_URL_ENCODED) {
      importBatchWrapper = JsonUtils.getDto(body, ImportBatchWrapper.class);
    } else if (contentType == ContentTypeEnum.APPLICATION_XML) {
      importBatchWrapper = XmlUtils.getDto(body);
    } else {
      throw new IllegalArgumentException("Invalid contentType : " + contentType);
    }

    // Validate all child requests.
    importBatchWrapper.requestValidation();
    ImportBatchType type = importBatchWrapper.getImportBatchType();

    JobId rootJobId = jobManager.createImportBatchJob(importBatchWrapper);
    JobId parentJobId = rootJobId;
    switch (type) {
      case APPEND_COLLECTION_JOB:
        checkNotNull(importBatchWrapper.getBaseVersion(), "BaseVersion cannot be null.");
        //$FALL-THROUGH$
        
      case CREATE_COLLECTION_JOB:
        reserveCollectionVersion(importBatchWrapper);
        //$FALL-THROUGH$
        
      case MODULE_JOB:
        for (ImportExternalIdDto importModuleDto : importBatchWrapper.getList()) {
          enqueueImportChildJob(importModuleDto, parentJobId, rootJobId);
        }
        break;

      default:
        throw new IllegalArgumentException("Unsupported type :" + type);
    }
    Objectify ofy = ObjectifyUtils.initiateTransaction();
    try {
      JobEntity rootJobEntity = updateCurrentJobAccordingToChildJobs(ofy, jobManager, 
          rootJobId);
      
      rootJobEntity.setContext(importBatchWrapper);
      jobManager.put(ofy, rootJobEntity, new ChangeLogEntryPojo("Updating state and context."));

      ObjectifyUtils.commitTransaction(ofy);
      
      importBatchWrapper.setJobDetails(rootJobEntity);
      importBatchWrapper.responseValidation();
      
      return importBatchWrapper;
    } finally {
      ObjectifyUtils.rollbackTransactionIfStillActive(ofy);
    }
  }

  /**
   * @param importBatchWrapper
   * @param rootJobId
   * @param ofy
   * @return
   */
  public static  JobEntity updateCurrentJobAccordingToChildJobs(Objectify ofy, 
      JobManager jobManager, JobId jobId) {
    
    // Now set the status of Root Job depending on childs.
    JobEntity jobEntity = jobManager.get(ofy, jobId);
    JobState jobState = null;
    if (jobEntity.hasPendingChildJobs()) {
      jobState = JobState.WAITING_FOR_CHILD_COMPLETE_NOTIFICATION;
    } else {
      jobState = JobState.ALL_CHILDS_COMPLETED;
      jobManager.enqueueLightJob(ofy, jobId);
    }
    jobEntity.setJobState(jobState);
    logger.info("Setting currenJob[" + jobId + "] to " + jobState);
    jobManager.put(ofy, jobEntity, new ChangeLogEntryPojo("Updating JobState to : " + jobState));
    return jobEntity;
  }

  private void reserveCollectionVersion(ImportBatchWrapper importBatchWrapper) {
    PersonId ownerId = GuiceUtils.getOwnerId();
    ImportBatchType type = importBatchWrapper.getImportBatchType();

    Objectify ofy = null;
    CollectionEntity collectionEntity = null;
    switch (type) {
      case MODULE_JOB:
        throw new IllegalStateException("Code should not reach here but reached for "
            + importBatchWrapper.toJson());

      case CREATE_COLLECTION_JOB:
        ofy = ObjectifyUtils.initiateTransaction();
        try {
          collectionEntity = collectionManager.reserveCollectionId(
              ofy, Lists.newArrayList(ownerId), importBatchWrapper.getCollectionTitle());
          importBatchWrapper.setCollectionId(collectionEntity.getCollectionId());
          ObjectifyUtils.commitTransaction(ofy);
        } finally {
          ObjectifyUtils.rollbackTransactionIfStillActive(ofy);
        }
        
        importBatchWrapper.setBaseVersion(new Version(Version.NO_VERSION));
        
        //$FALL-THROUGH$

      case APPEND_COLLECTION_JOB:
        ofy = ObjectifyUtils.initiateTransaction();
        try {
          if (collectionEntity == null) {
            collectionEntity = collectionManager.get(ofy, importBatchWrapper.getCollectionId());
          }
          checkNotNull(collectionEntity, "collectionEntity should not be null here.");
          Version version = collectionManager.reserveCollectionVersion(
              ofy, importBatchWrapper.getCollectionId());

          importBatchWrapper.setVersion(version);
          ObjectifyUtils.commitTransaction(ofy);
        } finally {
          ObjectifyUtils.rollbackTransactionIfStillActive(ofy);
        }
    }
  }

  /**
   * @param parentJob
   * @param currExternalId
   */
  private void enqueueImportChildJob(
      ImportExternalIdDto importModuleDto, JobId parentJobId, JobId rootJobId) {
    ModuleType moduleType = importModuleDto.getExternalId().getModuleType();

    if (moduleType.mapsToCollection()) {
      handleChildsMappingToCollection(importModuleDto, moduleType, parentJobId, rootJobId);
    } else {
      handlineChildsMappingToModules(importModuleDto, moduleType, parentJobId, rootJobId);
    }
  }

  /**
   * 
   */
  private void handlineChildsMappingToModules(ImportExternalIdDto importModuleDto,
      ModuleType moduleType, JobId parentJobId, JobId rootJobId) {
    switch (moduleType) {
      case GOOGLE_DOCUMENT:
        enqueueImportChildGoogleDocJob(importModuleDto, parentJobId, rootJobId);
        break;

      case GOOGLE_DRAWING:
      case GOOGLE_FILE:
      case GOOGLE_FORM:
      case GOOGLE_PRESENTATION:
      case GOOGLE_SPREADSHEET:
      case LIGHT_SYNTHETIC_MODULE:
        try {
          enqueueImportChildSyntheticModule(importModuleDto, parentJobId, rootJobId);
        } catch (Exception e) {
          logger.info("Failed to import : " + importModuleDto.getExternalId() + ". Reason : "
              + Throwables.getStackTraceAsString(e));
          updateImportExternalIdDtoForModulesForFailure(importModuleDto, ModuleState.FAILED);
        }
        break;

      default:
        throw new UnsupportedOperationException(moduleType + " is currently not supported.");
    }
  }

  /**
   * @param importModuleDto
   * @return
   */
  private GoogleDocInfoDto getGDocInfo(ImportExternalIdDto importModuleDto) {
    DocsServiceWrapper docsService = GuiceUtils.getInstance(DocsServiceWrapper.class);
    GoogleDocResourceId resourceId = new GoogleDocResourceId(importModuleDto.getExternalId());
    GoogleDocInfoDto gdocInfo = docsService.getGoogleDocInfo(resourceId);
    return gdocInfo;
  }

  /**
   * @param externalIdDto
   * @param parentJobId
   * @param rootJobId
   */
  public void enqueueImportChildSyntheticModule(ImportExternalIdDto importModuleDto,
      JobId parentJobId, JobId rootJobId) throws IOException {

    ModuleId moduleId = moduleManager.findModuleIdByExternalId(null,
        importModuleDto.getExternalId());

    if (moduleId != null) {
      ModuleEntity existingModule = moduleManager.get(null, moduleId);
      if (existingModule != null && existingModule.getModuleState() == ModuleState.PUBLISHED) {
        // No need to publish new version for this synthetic module.
        updateImportExternalIdDtoForModules(importModuleDto, moduleId,
            existingModule.getLatestPublishVersion(),
            ModuleState.PUBLISHED, existingModule.getModuleType(), existingModule.getTitle(), null);
        return;
      }
    }
    
    // First see if this ExternalId is suitable for Synthetic Module.
    URI uri = getURI(importModuleDto.getExternalId().getValue());
    HttpResponse response = httpClient.get(uri);

    if (!response.isSuccessStatusCode()) {
      updateImportExternalIdDtoForModulesForFailure(importModuleDto, ModuleState.FAILED);
    }

    String xFrameHeader = getHeaderValueFromResponse(response, HttpHeaderEnum.X_FRAME_OPTIONS);
    if (xFrameHeader != null) {
      // This module cannot be embedded inside iFrame so it is not allowed for synthetic module.
      updateImportExternalIdDtoForModulesForFailure(importModuleDto, ModuleState.NOT_SUPPORTED);
      return;
    }


    // Current ExternalId is suitable for Synthetic Module.
    PersonId ownerId = GuiceUtils.getOwnerId();
    checkNotNull(ownerId, "ownerId should not be null here.");

    if (moduleId == null) {
      moduleId = reserveModuleId(moduleManager, importModuleDto, ownerId);
    }
    checkNotNull(moduleId, "moduleId should not be null here.");
    
//    if (moduleId != null) {
//      throw new IllegalStateException();
//    }
    

    /*
     * Now reserving a module version and creating a Synthetic Module Child job that will
     * publish this version.
     */
    Objectify ofy = ObjectifyUtils.initiateTransaction();
    try {
      Version reservedVersion = moduleManager.reserveModuleVersion(ofy, moduleId, null /* etag */,
          null /* last edit time */);

      if (reservedVersion == null) {
        // Since this is a synthetic module, so only one version is enough.
        updateImportExternalIdDtoForModules(importModuleDto, moduleId, new Version(Version.LATEST_VERSION),
            ModuleState.PUBLISHED, ModuleType.LIGHT_SYNTHETIC_MODULE, importModuleDto.getTitle(),
            null);
        return;
      }

      String title = null;

      if (StringUtils.isNotBlank(importModuleDto.getTitle())) {
        title = importModuleDto.getTitle();
      } else {
        title = httpClient.getTitle(response, importModuleDto.getExternalId());
      }

      ImportModuleSyntheticModuleJobContext jobContext = new ImportModuleSyntheticModuleJobContext.Builder()
          .title(title)
          .externalId(importModuleDto.getExternalId())
          .moduleId(moduleId)
          .version(reservedVersion)
          .build();

      JobEntity childJob = jobManager.createSyntheticModuleJob(
          ofy, jobContext, parentJobId, rootJobId);

      /*
       * Now saving details about ModuleId and version as part of ExternalIdDto that will be
       * eventually sent to the client. Also this will be stored as part of the parent Batch Job
       * which can be useful for Debugging later.
       */
      updateImportExternalIdDtoForModules(importModuleDto, moduleId, reservedVersion,
          ModuleState.IMPORTING, ModuleType.LIGHT_SYNTHETIC_MODULE, title, childJob);
      jobManager.enqueueImportChildJob(ofy, parentJobId, childJob.getJobId(), importModuleDto,
          QueueEnum.LIGHT);
      ObjectifyUtils.commitTransaction(ofy);
    } finally {
      ObjectifyUtils.rollbackTransactionIfStillActive(ofy);
    }
  }

  /**
   * @param parentJob
   * @param externalIdDto
   */
  private void enqueueImportChildGoogleDocJob(ImportExternalIdDto importExternalIdDto,
      JobId parentJobId,
      JobId rootJobId) {
    GoogleDocInfoDto gdocInfo = getGDocInfo(importExternalIdDto);

    ModuleType moduleType = gdocInfo.getModuleType();
    switch (moduleType) {
      case GOOGLE_DOCUMENT:
        importGoogleDocModuleJobHandler.enqueueModuleGoogleDocJob(
            importExternalIdDto, gdocInfo, parentJobId, rootJobId);
        break;

      case GOOGLE_COLLECTION:
        throw new IllegalStateException("Code should not reach here but reached for "
            + gdocInfo.toJson());

      default:
        throw new IllegalArgumentException("Unsupported type " + moduleType);
    }
  }
  
  private void handleChildsMappingToCollection(ImportExternalIdDto importModuleDto,
      ModuleType moduleType, JobId parentJobId, JobId rootJobId) {
    switch (moduleType) {
      case GOOGLE_COLLECTION:
        GoogleDocInfoDto gdocInfo = getGDocInfo(importModuleDto);
        importCollectionGDocJobHandler.enqueueCollectionGoogleDocJob(
            importModuleDto, gdocInfo, parentJobId, rootJobId);
        break;

      default:
        throw new IllegalStateException("Add support for " + moduleType);
    }
  }

  /**
   * @param externalIdDto
   * @param moduleId
   * @param reservedVersion
   * @param childJob
   */
  public static void updateImportExternalIdDtoForModules(ImportExternalIdDto importExternalIdDto,
      ModuleId moduleId, Version version, ModuleState moduleState, ModuleType moduleType,
      String title, JobEntity childJob) {
    importExternalIdDto.setModuleId(moduleId);
    if (moduleId != null) {
      importExternalIdDto.setVersion(version);
    }

    importExternalIdDto.setModuleState(checkNotNull(moduleState, "moduleState"));
    importExternalIdDto.setModuleType(checkNotNull(moduleType, "moduleType"));
    importExternalIdDto.setTitle(checkNotBlank(title, "title"));
    if (childJob != null) {
      importExternalIdDto.setJobDetails(childJob);
    }
  }

  public static void updateImportExternalIdDtoForModulesForFailure(ImportExternalIdDto externalIdDto,
      ModuleState moduleState) {
    externalIdDto.setModuleState(moduleState);
  }
  
  public static void updateImportExternalIdDtoForCollections(ImportExternalIdDto importExternalIdDto,
      ModuleType moduleType, ModuleState moduleState, String title, JobEntity childJob) {
    importExternalIdDto.setModuleType(checkNotNull(moduleType, "moduleType"));
    importExternalIdDto.setTitle(checkNotBlank(title, "title"));
    importExternalIdDto.setJobDetails(childJob);
    importExternalIdDto.setModuleState(moduleState);
  }
}
