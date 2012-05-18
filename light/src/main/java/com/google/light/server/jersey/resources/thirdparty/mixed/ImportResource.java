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
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.LightPreconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkPersonLoggedIn;
import static com.google.light.server.utils.ObjectifyUtils.repeatInTransaction;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.light.server.constants.JerseyConstants;
import com.google.light.server.constants.http.ContentTypeConstants;
import com.google.light.server.constants.http.ContentTypeEnum;
import com.google.light.server.dto.collection.CollectionState;
import com.google.light.server.dto.importresource.ImportBatchType;
import com.google.light.server.dto.importresource.ImportBatchWrapper;
import com.google.light.server.dto.importresource.ImportExternalIdDto;
import com.google.light.server.dto.module.ModuleState;
import com.google.light.server.dto.module.ModuleType;
import com.google.light.server.dto.pojo.tree.AbstractTreeNode.TreeNodeType;
import com.google.light.server.dto.pojo.tree.collection.CollectionTreeNodeDto;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.JobId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.ModuleId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.PersonId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.Version;
import com.google.light.server.exception.ExceptionType;
import com.google.light.server.exception.unchecked.InvalidExternalIdException;
import com.google.light.server.jersey.resources.AbstractJerseyResource;
import com.google.light.server.manager.interfaces.CollectionManager;
import com.google.light.server.manager.interfaces.JobManager;
import com.google.light.server.manager.interfaces.ModuleManager;
import com.google.light.server.persistence.entity.collection.CollectionEntity;
import com.google.light.server.persistence.entity.collection.CollectionVersionEntity;
import com.google.light.server.persistence.entity.jobs.JobEntity;
import com.google.light.server.persistence.entity.jobs.JobState;
import com.google.light.server.servlets.SessionManager;
import com.google.light.server.utils.GuiceUtils;
import com.google.light.server.utils.JsonUtils;
import com.google.light.server.utils.ModuleUtils;
import com.google.light.server.utils.Transactable;
import com.google.light.server.utils.XmlUtils;
import com.googlecode.objectify.Objectify;
import java.util.List;
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

  private JobManager jobManager;
  private ModuleManager moduleManager;
  private CollectionManager collectionManager;

  @Inject
  public ImportResource(Injector injector, HttpServletRequest request,
      HttpServletResponse response, JobManager jobManager, CollectionManager collectionManager,
      ModuleManager moduleManager) {
    super(injector, request, response);
    this.jobManager = checkNotNull(jobManager, ExceptionType.SERVER_GUICE_INJECTION, "jobManager");
    this.collectionManager = checkNotNull(collectionManager, "collectionManager");
    this.moduleManager = checkNotNull(
        moduleManager, ExceptionType.SERVER_GUICE_INJECTION, "moduleManager");
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
    //
    // JobId parentJobId = rootJobId;
    switch (type) {
      case APPEND_COLLECTION_JOB:
        checkNotNull(importBatchWrapper.getBaseVersion(), "BaseVersion cannot be null.");
        checkNotNull(importBatchWrapper.getCollectionId(), "CollectionId cannot be null.");
        break;

      case CREATE_COLLECTION_JOB:
        checkNotBlank(importBatchWrapper.getCollectionTitle(), "collectionTitle cannot be null.");
        break;

      case MODULE_JOB:
        break;

      default:
        throw new IllegalArgumentException("Unsupported type :" + type);
    }

    // Now reserve ModuleIds for each of the externalIds.
    List<PersonId> owners = Lists.newArrayList(GuiceUtils.getOwnerId());
    reserveModuleIdAndUpdateWrapper(importBatchWrapper, owners);

    return handleCollectionAndRelatedJob(importBatchWrapper);
  }

  /**
   * @param importBatchWrapper
   * @return
   */
  private ImportBatchWrapper handleCollectionAndRelatedJob(
      final ImportBatchWrapper importBatchWrapper) {
    repeatInTransaction(new Transactable<Void>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public Void run(Objectify ofy) {
        List<PersonId> owners = Lists.newArrayList(GuiceUtils.getOwnerId());

        CollectionTreeNodeDto dummyRoot = getDummyRootFromImportBatchWrapper(importBatchWrapper);
        CollectionEntity collectionEntity = null;
        switch (importBatchWrapper.getImportBatchType()) {
          case CREATE_COLLECTION_JOB:
            collectionEntity = collectionManager.createEmptyCollection(ofy, owners, dummyRoot);
            Version reservVersion = collectionEntity.reserveVersion();
            collectionManager.update(ofy, collectionEntity);

            importBatchWrapper.setCollectionId(collectionEntity.getCollectionId());
            importBatchWrapper.setBaseVersion(collectionEntity.getLatestPublishVersion());
            importBatchWrapper.setVersion(reservVersion);
            break;

          case APPEND_COLLECTION_JOB:
            CollectionVersionEntity cvEntity = collectionManager.getLatestPublishedVersion(
                ofy, importBatchWrapper.getCollectionId());
            checkNotNull(cvEntity, "cvEntity should not be null here.");
            CollectionTreeNodeDto rootToBeUpdated = cvEntity.getCollectionTree();
            rootToBeUpdated.importChildsFrom(dummyRoot);

            Version publishVersion = collectionManager.reserveAndPublishAsLatest(ofy,
                importBatchWrapper.getCollectionId(), rootToBeUpdated,
                CollectionState.PARTIALLY_PUBLISHED);
            importBatchWrapper.setBaseVersion(publishVersion);
            importBatchWrapper.setVersion(publishVersion);
            break;

          case MODULE_JOB:
            break;

          default:
            throw new IllegalStateException("Add support for : "
                + importBatchWrapper.getImportBatchType());
        }

        JobState jobState = JobState.ENQUEUED;
        JobId rootJobId = jobManager.createImportBatchJob(
            ofy, importBatchWrapper, JobState.ENQUEUED);
        importBatchWrapper.setJobState(jobState);
        importBatchWrapper.setJobId(rootJobId);
        jobManager.enqueueLightJob(ofy, rootJobId);

        return null;
      }
    });

    importBatchWrapper.responseValidation();
    return importBatchWrapper;
  }

  /**
   * @param importBatchWrapper
   * @return
   */
  private static CollectionTreeNodeDto getDummyRootFromImportBatchWrapper(
      final ImportBatchWrapper importBatchWrapper) {
    CollectionTreeNodeDto dummyRoot = new CollectionTreeNodeDto.Builder()
        .title(importBatchWrapper.getCollectionTitle())
        .nodeType(TreeNodeType.ROOT_NODE)
        .moduleType(ModuleType.LIGHT_COLLECTION)
        .build();

    for (ImportExternalIdDto currExternalIdDto : importBatchWrapper.getList()) {

      // Ignoring the ones that failed
      if (currExternalIdDto.getModuleState() == ModuleState.FAILED) {
        continue;
      }

      CollectionTreeNodeDto newChild = new CollectionTreeNodeDto.Builder()
          .title(currExternalIdDto.getTitle())
          .moduleType(currExternalIdDto.getModuleType())
          .externalId(currExternalIdDto.getExternalId())
          .nodeType(currExternalIdDto.getModuleType().getNodeType())
          .moduleId(currExternalIdDto.getModuleId())
          .build();

      dummyRoot.addChildren(newChild);
    }
    return dummyRoot;
  }

  /**
   * @param importBatchWrapper
   */
  private void reserveModuleIdAndUpdateWrapper(ImportBatchWrapper importBatchWrapper,
      final List<PersonId> owners) {
    // TODO(arjuns) : Replace this with multithreaded once available.
    for (final ImportExternalIdDto currExternalIdDto : importBatchWrapper.getList()) {
      currExternalIdDto.setModuleType(currExternalIdDto.getExternalId().getModuleType());
      try {
        // Predicting title does two things :
        // 1. Fetches the title.
        // 2. Ensures that its a valid module.
        final String predictedTitle = ModuleUtils.getTitleForExternalId(
            currExternalIdDto.getExternalId());
        if (StringUtils.isBlank(currExternalIdDto.getTitle())) {
          currExternalIdDto.setTitle(predictedTitle);
        }

        if (currExternalIdDto.getModuleType().mapsToModule()) {
          // Now reserve ModuleIds.
          repeatInTransaction(new Transactable<Void>() {
            @SuppressWarnings("synthetic-access")
            @Override
            public Void run(Objectify ofy) {
              ModuleId moduleId = moduleManager.reserveModuleId
                  (ofy, currExternalIdDto.getExternalId(), owners, currExternalIdDto.getTitle());
              currExternalIdDto.setModuleId(moduleId);
              return null;
            }
          });
        }

        currExternalIdDto.setModuleState(ModuleState.IMPORTING);
      } catch (InvalidExternalIdException e) {
        logger.info("Ignoring externalId : " + currExternalIdDto.getExternalId());
        currExternalIdDto.setModuleState(ModuleState.FAILED);
      }
    }
  }

//  /**
//   * @param importBatchWrapper
//   * @param collectionEntity
//   */
//  private void reserveCollectionVersionForAppendCollectionJob(
//      final ImportBatchWrapper importBatchWrapper) {
//    ObjectifyUtils.repeatInTransaction(new Transactable<Void>() {
//
//      @SuppressWarnings("synthetic-access")
//      @Override
//      public Void run(Objectify ofy) {
//        CollectionEntity collectionEntity = collectionManager.get(
//            ofy, importBatchWrapper.getCollectionId());
//        checkNotNull(collectionEntity, "collectionEntity should not be null here.");
//        Version reservedVersion = collectionManager.reserveCollectionVersion(
//            ofy, importBatchWrapper.getCollectionId());
//        importBatchWrapper.setVersion(reservedVersion);
//
//        Version baseVersion = collectionEntity.determineBaseVersionForAppend(
//            importBatchWrapper.getBaseVersion(), reservedVersion, LightClientType.BROWSER);
//
//        importBatchWrapper.setBaseVersion(baseVersion);
//        return null;
//      }
//    });
//  }

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

  public static void updateImportExternalIdDtoForModulesForFailure(
      ImportExternalIdDto externalIdDto,
      ModuleState moduleState) {
    externalIdDto.setModuleState(moduleState);
  }

  public static void updateImportExternalIdDtoForCollections(
      ImportExternalIdDto importExternalIdDto,
      ModuleType moduleType, ModuleState moduleState, String title, JobEntity childJob) {
    importExternalIdDto.setModuleType(checkNotNull(moduleType, "moduleType"));
    importExternalIdDto.setTitle(checkNotBlank(title, "title"));
    importExternalIdDto.setJobDetails(childJob);
    importExternalIdDto.setModuleState(moduleState);
  }
}
