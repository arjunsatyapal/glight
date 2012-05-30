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
import static com.google.light.server.dto.thirdparty.google.youtube.ContentLicense.DEFAULT_LIGHT_CONTENT_LICENSES;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.LightPreconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkPersonLoggedIn;
import static com.google.light.server.utils.LightUtils.createCollectionNode;
import static com.google.light.server.utils.LightUtils.createExternalIdTreeNode;
import static com.google.light.server.utils.LightUtils.wrapIntoRuntimeExceptionAndThrow;
import static com.google.light.server.utils.ModuleUtils.createExternalIdTree;
import static com.google.light.server.utils.ObjectifyUtils.repeatInTransaction;

import java.util.concurrent.Future;

import com.google.appengine.api.ThreadManager;
import com.google.common.base.Throwables;
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
import com.google.light.server.dto.pojo.tree.externaltree.ExternalIdTreeNodeDto;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.CollectionId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.JobId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.ModuleId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.PersonId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.Version;
import com.google.light.server.dto.pojo.typewrapper.stringwrapper.ExternalId;
import com.google.light.server.dto.thirdparty.google.youtube.ContentLicense;
import com.google.light.server.exception.ExceptionType;
import com.google.light.server.exception.unchecked.InvalidExternalIdException;
import com.google.light.server.exception.unchecked.httpexception.NotFoundException;
import com.google.light.server.jersey.resources.AbstractJerseyResource;
import com.google.light.server.jobs.runnables.ReserveModuleIdRunnable;
import com.google.light.server.manager.interfaces.CollectionManager;
import com.google.light.server.manager.interfaces.JobManager;
import com.google.light.server.manager.interfaces.ModuleManager;
import com.google.light.server.persistence.entity.collection.CollectionEntity;
import com.google.light.server.persistence.entity.collection.CollectionVersionEntity;
import com.google.light.server.persistence.entity.jobs.JobEntity;
import com.google.light.server.persistence.entity.jobs.JobState;
import com.google.light.server.persistence.entity.module.ModuleEntity;
import com.google.light.server.servlets.SessionManager;
import com.google.light.server.urls.LightUrl;
import com.google.light.server.utils.GuiceUtils;
import com.google.light.server.utils.JsonUtils;
import com.google.light.server.utils.LightUtils;
import com.google.light.server.utils.Transactable;
import com.google.light.server.utils.XmlUtils;
import com.googlecode.objectify.Objectify;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import org.apache.commons.lang.StringUtils;
import org.joda.time.Instant;

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

    // Doing basic validations depending on type of job.
    switch (type) {
      case APPEND_COLLECTION_JOB:
        checkNotNull(importBatchWrapper.getBaseVersion(), "BaseVersion cannot be null.");
        checkNotNull(importBatchWrapper.getCollectionId(), "CollectionId cannot be null.");
        CollectionId collectionId = importBatchWrapper.getCollectionId();
        CollectionEntity collectionEntity = collectionManager.get(null, collectionId);
        if (collectionEntity == null) {
          throw new NotFoundException("Collection [" + collectionId + "] was not found.");
        }
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
    // reserveModuleIdAndUpdateWrapper(importBatchWrapper, owners);

    Instant now = LightUtils.getNow();
    ExternalIdTreeNodeDto externalIdTreeRoot = prepareExternalIdTree(importBatchWrapper);
    logger.info("ExternalId Tree : " + externalIdTreeRoot.toJson());
    reserveWholeTreeAndUpdateWrapper(externalIdTreeRoot, importBatchWrapper, owners);
    Instant now2 = LightUtils.getNow();
    System.out.println(now2.minus(now.getMillis()).getMillis());

    return handleCollectionAndRelatedJob(externalIdTreeRoot, importBatchWrapper);
  }

  /**
   * @param importBatchWrapper
   * @param owners
   */
  private ExternalIdTreeNodeDto prepareExternalIdTree(ImportBatchWrapper importBatchWrapper) {
    ExternalIdTreeNodeDto rootNode = createExternalIdTreeNode(null, null, TreeNodeType.ROOT_NODE,
        null /* license */);

    for (ImportExternalIdDto currExternalIdDto : importBatchWrapper.getList()) {
      try {
        ExternalIdTreeNodeDto node = createExternalIdTree(currExternalIdDto.getExternalId());
        rootNode.addChildren(node);
      } catch (InvalidExternalIdException e) {
        logger.info("Ignoring externalId : " + currExternalIdDto.getExternalId() + " due to : "
            + Throwables.getStackTraceAsString(e));
        currExternalIdDto.setModuleState(ModuleState.FAILED);
      }
    }

    return rootNode;
  }

  /**
   * @param importBatchWrapper
   * @return
   */
  private ImportBatchWrapper handleCollectionAndRelatedJob(
      ExternalIdTreeNodeDto externalIdTreeRoot, final ImportBatchWrapper importBatchWrapper) {
    // Convering externalIdTree to Collection Tree.

    final CollectionTreeNodeDto dummyRoot =
        convertExternalIdTreeToCollectionTree(externalIdTreeRoot);

    repeatInTransaction("enqueueing a job for " + importBatchWrapper.getCollectionTitle(),
        new Transactable<Void>() {
          @SuppressWarnings("synthetic-access")
          @Override
          public Void run(Objectify ofy) {
            List<PersonId> owners = Lists.newArrayList(GuiceUtils.getOwnerId());

            // CollectionTreeNodeDto dummyRoot = null;
            CollectionEntity collectionEntity = null;
            switch (importBatchWrapper.getImportBatchType()) {
              case CREATE_COLLECTION_JOB:
                // dummyRoot = getDummyRootFromImportBatchWrapper(importBatchWrapper);
                collectionEntity = collectionManager.reserveCollectionId(ofy, owners, dummyRoot,
                    DEFAULT_LIGHT_CONTENT_LICENSES);

                Version reservedVersion = collectionManager.reserveCollectionVersionFirst(
                    ofy, collectionEntity);

                importBatchWrapper.setCollectionId(collectionEntity.getCollectionId());
                importBatchWrapper.setBaseVersion(collectionEntity.getLatestPublishVersion());
                importBatchWrapper.setVersion(reservedVersion);
                break;

              case APPEND_COLLECTION_JOB:
                // dummyRoot = getDummyRootFromImportBatchWrapper(importBatchWrapper);
                CollectionVersionEntity cvEntity = collectionManager.getLatestPublishedVersion(
                    ofy, importBatchWrapper.getCollectionId());
                checkNotNull(cvEntity, "cvEntity should not be null here.");
                CollectionTreeNodeDto rootToBeUpdated = cvEntity.getCollectionTree();
                rootToBeUpdated.importChildsFrom(dummyRoot);

                Version publishVersion = collectionManager.reserveAndPublishAsLatest(ofy,
                    importBatchWrapper.getCollectionId(), rootToBeUpdated,
                    CollectionState.PARTIALLY_PUBLISHED, DEFAULT_LIGHT_CONTENT_LICENSES);
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
   * @param externalIdTreeRoot
   * @return
   */
  private CollectionTreeNodeDto convertExternalIdTreeToCollectionTree(
      ExternalIdTreeNodeDto externalIdNode) {
    TreeNodeType nodeType = externalIdNode.getNodeType();
    ExternalId externalId = externalIdNode.getExternalId();

    ModuleType moduleType = null;

    if (nodeType == TreeNodeType.ROOT_NODE) {
      moduleType = ModuleType.LIGHT_COLLECTION;
    } else {
      moduleType = externalId.getModuleType();
    }

    CollectionTreeNodeDto collectionTreeNode = createCollectionNode(null/* description */,
        externalId, externalIdNode.getModuleId(), moduleType,
        null/* nodeId */, nodeType, externalIdNode.getTitle(), LightUtils.LATEST_VERSION);

    switch (nodeType) {
      case LEAF_NODE:
        break;

      case INTERMEDIATE_NODE:
      case ROOT_NODE:
        if (externalIdNode.hasChildren()) {
          for (ExternalIdTreeNodeDto currExternalIdNodeChild : externalIdNode.getChildren()) {
            CollectionTreeNodeDto childCollectionTreeNode = convertExternalIdTreeToCollectionTree(
                currExternalIdNodeChild);

            collectionTreeNode.addChildren(childCollectionTreeNode);
          }
        }
        break;

      default:
        throw new IllegalStateException("Unknown nodeType : " + nodeType);
    }
    return collectionTreeNode;
  }

  /**
   * @param importBatchWrapper
   * @return
   */
  private static CollectionTreeNodeDto getDummyRootFromImportBatchWrapper(
      final ImportBatchWrapper importBatchWrapper) {
    CollectionTreeNodeDto dummyRoot = LightUtils.createCollectionRootDummy(
        importBatchWrapper.getCollectionTitle(), null /* description */);

    for (ImportExternalIdDto currExternalIdDto : importBatchWrapper.getList()) {

      // Ignoring the ones that failed
      if (currExternalIdDto.getModuleState() == ModuleState.FAILED) {
        continue;
      }

      CollectionTreeNodeDto newChild = createCollectionNode(null/* description */,
          currExternalIdDto.getExternalId(), currExternalIdDto.getModuleId(),
          currExternalIdDto.getModuleType(), null/* nodeId */,
          currExternalIdDto.getModuleType().getNodeType(), currExternalIdDto.getTitle(),
          LightUtils.LATEST_VERSION);

      dummyRoot.addChildren(newChild);
    }
    return dummyRoot;
  }

  /**
   * @param importBatchWrapper
   */
  private void reserveWholeTreeAndUpdateWrapper(ExternalIdTreeNodeDto externalIdTree,
      ImportBatchWrapper importBatchWrapper, final List<PersonId> owners) {

    ThreadFactory factory = ThreadManager.currentRequestThreadFactory();
    ExecutorService threadExecutor = Executors.newFixedThreadPool(9, factory);

    List<ReserveModuleIdRunnable> listOfRunnables = Lists.newArrayList();
    for (ExternalIdTreeNodeDto currExternalIdNode : externalIdTree.getLeafNodes()) {
      ReserveModuleIdRunnable runnable = new ReserveModuleIdRunnable(currExternalIdNode, owners);
      Future<?> future = threadExecutor.submit(runnable);
      try {
        future.get();
      } catch (Exception e) {
        logger.severe("Failed for " + currExternalIdNode.getExternalId() + " due to : "
            + Throwables.getStackTraceAsString(e));
        wrapIntoRuntimeExceptionAndThrow(e);
      }
      listOfRunnables.add(runnable);
    }

    threadExecutor.shutdown();
    try {
      threadExecutor.awaitTermination(25, TimeUnit.SECONDS);
    } catch (Exception e) {
      LightUtils.wrapIntoRuntimeExceptionAndThrow(e);
    }

    // All ExternalIds are now reserved. So updating the moduleIds.

    for (final ImportExternalIdDto currExternalIdDto : importBatchWrapper.getList()) {
      if (currExternalIdDto.getModuleState() == ModuleState.FAILED) {
        continue;
      }

      ExternalId externalId = currExternalIdDto.getExternalId();
      final ExternalIdTreeNodeDto externalIdTreeNode =
          externalIdTree.findFirstLevelChildByExternalId(
              externalId);
      checkNotNull(externalIdTreeNode, "externalIdTreeNode");

      currExternalIdDto.setModuleType(currExternalIdDto.getExternalId().getModuleType());
      // try {
      // Predicting title does two things :
      // 1. Fetches the title.
      // 2. Ensures that its a valid module.

      String predictedTitle = externalIdTreeNode.getTitle();
      // ModuleUtils.getTitleForExternalId(
      // currExternalIdDto.getExternalId());
      // logger.info("For " + currExternalIdDto.getExternalId()
      // + ", Predicted Title : " + predictedTitle);
      if (StringUtils.isBlank(currExternalIdDto.getTitle())) {
        currExternalIdDto.setTitle(predictedTitle);
      }

      ModuleType moduleType = currExternalIdDto.getModuleType();
      final List<ContentLicense> contentLicenses = moduleType.getDefaultLicenses();

      if (moduleType.mapsToModule()) {
        if (currExternalIdDto.getModuleType() == ModuleType.LIGHT_HOSTED_MODULE) {
          // This will be true for Light URLs.
          LightUrl lightUrl = currExternalIdDto.getExternalId().getLightUrl();
          ModuleEntity moduleEntity = moduleManager.get(null, lightUrl.getModuleId());

          if (moduleEntity == null) {
            throw new InvalidExternalIdException(currExternalIdDto.getExternalId());
          }

          currExternalIdDto.setModuleId(lightUrl.getModuleId());
          currExternalIdDto.setModuleState(ModuleState.PUBLISHED);
          currExternalIdDto.setModuleType(moduleEntity.getModuleType());
          currExternalIdDto.setVersion(LightUtils.LATEST_VERSION);
        } else {
          ModuleId moduleId = externalIdTreeNode.getModuleId();
          if (moduleId == null) {
            System.out.println(externalIdTreeNode.toJson());
          }
          checkNotNull(moduleId, "ModuleId found null for : " + externalIdTreeNode.getExternalId());
          currExternalIdDto.setModuleId(externalIdTreeNode.getModuleId());
          currExternalIdDto.setContentLicenses(contentLicenses);
          currExternalIdDto.setModuleState(ModuleState.IMPORTING);
        }
      } else {
        // This is a type of External Collection. So changing state to importing.
        currExternalIdDto.setModuleState(ModuleState.IMPORTING);
      }

      currExternalIdDto.setContentLicenses(contentLicenses);
      // } catch (InvalidExternalIdException e) {
      // logger.info("Ignoring externalId : " + currExternalIdDto.getExternalId() + " due to : "
      // + Throwables.getStackTraceAsString(e));
      // currExternalIdDto.setModuleState(ModuleState.FAILED);
      // }
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
