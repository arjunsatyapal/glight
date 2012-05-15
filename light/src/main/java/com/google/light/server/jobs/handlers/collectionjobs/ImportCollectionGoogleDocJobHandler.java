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
package com.google.light.server.jobs.handlers.collectionjobs;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.constants.LightConstants.GDATA_GDOC_MAX_RESULTS;
import static com.google.light.server.dto.pojo.tree.collection.CollectionTreeUtils.generateCollectionNodeFromChildJob;
import static com.google.light.server.jersey.resources.thirdparty.mixed.ImportResource.updateCurrentJobAccordingToChildJobs;
import static com.google.light.server.jersey.resources.thirdparty.mixed.ImportResource.updateImportExternalIdDtoForCollections;

import org.apache.commons.lang.StringUtils;

import com.google.inject.Inject;
import com.google.light.server.constants.QueueEnum;
import com.google.light.server.dto.importresource.ImportExternalIdDto;
import com.google.light.server.dto.module.ModuleState;
import com.google.light.server.dto.module.ModuleType;
import com.google.light.server.dto.pojo.tree.AbstractTreeNode.TreeNodeType;
import com.google.light.server.dto.pojo.tree.collection.CollectionTreeNodeDto;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.JobId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.Version;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocInfoDto;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocResourceId;
import com.google.light.server.jobs.handlers.JobHandlerInterface;
import com.google.light.server.jobs.handlers.modulejobs.ImportModuleGoogleDocJobHandler;
import com.google.light.server.manager.interfaces.JobManager;
import com.google.light.server.persistence.entity.jobs.JobEntity;
import com.google.light.server.persistence.entity.jobs.JobState;
import com.google.light.server.thirdparty.clients.google.gdata.gdoc.DocsServiceWrapper;
import com.google.light.server.utils.GuiceUtils;
import com.google.light.server.utils.ObjectifyUtils;
import com.googlecode.objectify.Objectify;
import java.util.List;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class ImportCollectionGoogleDocJobHandler implements JobHandlerInterface {
  private JobManager jobManager;
  private ImportModuleGoogleDocJobHandler importGDocModuleJobHandler;

  @Inject
  public ImportCollectionGoogleDocJobHandler(JobManager jobManager,
      ImportModuleGoogleDocJobHandler importGoogleDocModuleJobHandler) {
    this.jobManager = checkNotNull(jobManager, "jobManager");
    this.importGDocModuleJobHandler = checkNotNull(importGoogleDocModuleJobHandler,
        "importGoogleDocModuleJobHandler");
  }

  @Override
  public void handle(JobEntity jobEntity) {
    ImportCollectionGoogleDocContext context =
        jobEntity.getContext(ImportCollectionGoogleDocContext.class);
    checkArgument(context.getExternalId().getModuleType() == ModuleType.GOOGLE_COLLECTION,
        "This handler can handle only Google Collections");
    System.out.println(context.toJson());

    JobState jobState = jobEntity.getJobState();
    switch (jobState) {
      case ALL_CHILDS_COMPLETED:
        handleChildCompletions(context, jobEntity);
        break;
      case ENQUEUED:
        handleChildCreations(context, jobEntity);
        break;

      default:
        throw new IllegalStateException("Unexpected " + jobState);
    }

  }

  /**
   * @param context
   * @param jobEntity
   */
  private void handleChildCompletions(ImportCollectionGoogleDocContext context,
      JobEntity jobEntity) {
    CollectionTreeNodeDto subCollection = new CollectionTreeNodeDto.Builder()
        .title(context.getTitle())
        .externalId(context.getExternalId())
        .type(TreeNodeType.INTERMEDIATE_NODE)
        .moduleType(ModuleType.LIGHT_SUB_COLLECTION)
        .build();

    for (ImportExternalIdDto curr : context.getList()) {
      CollectionTreeNodeDto child = null;
      if (curr.hasJobId()) {
        child = generateCollectionNodeFromChildJob(jobManager, curr.getJobId());
      } else {
        checkArgument(curr.getModuleType().getNodeType() == TreeNodeType.LEAF_NODE,
            "Intermediate nodes are expected to have created a job. Failed for : " + 
        curr.getExternalId());
        child = new CollectionTreeNodeDto.Builder()
            .title(curr.getTitle())
            .externalId(curr.getExternalId())
            .type(TreeNodeType.LEAF_NODE)
            .moduleType(curr.getModuleType())
            .moduleId(curr.getModuleId())
            .version(new Version(Version.LATEST_VERSION))
            .build();
      }
      
      checkNotNull(child, "child should not be null.");
      System.out.println("\n***child = " + child.toJson());
      subCollection.addChildren(child);
    }
    
    System.out.println("\n*** subcollectionRoot = " + subCollection.toJson());
    jobManager.enqueueCompleteJob(jobEntity.getJobId(), subCollection, "Marking job as complete");
  }

  /**
   * @param context
   * @param jobEntity
   */
  private void handleChildCreations(ImportCollectionGoogleDocContext context, JobEntity jobEntity) {
    GoogleDocResourceId gdocResourceId = new GoogleDocResourceId(context.getExternalId());

    DocsServiceWrapper docsService = GuiceUtils.getInstance(DocsServiceWrapper.class);
    List<GoogleDocInfoDto> listOfChilds = docsService.getFolderContentWhichAreSupported(
        gdocResourceId, GDATA_GDOC_MAX_RESULTS);

    for (GoogleDocInfoDto currChild : listOfChilds) {
      ModuleType moduleType = currChild.getModuleType();
      ImportExternalIdDto importExternalIdDto = new ImportExternalIdDto.Builder()
          .externalId(currChild.getExternalId())
          .title(currChild.getTitle())
          .moduleType(currChild.getModuleType())
          .build();

      switch (moduleType) {
        case GOOGLE_COLLECTION:
          this.enqueueCollectionGoogleDocJob(importExternalIdDto,
              currChild, jobEntity.getJobId(), jobEntity.getRootJobId());
          break;

        case GOOGLE_DOCUMENT:
          importGDocModuleJobHandler.enqueueModuleGoogleDocJob(
              importExternalIdDto, currChild, jobEntity.getJobId(), jobEntity.getRootJobId());
          break;

        default:
          throw new IllegalStateException("We should not get this type : " + moduleType);
      }

    }

    Objectify ofy = ObjectifyUtils.initiateTransaction();
    try {
      updateCurrentJobAccordingToChildJobs(ofy, jobManager, jobEntity.getJobId());
      ObjectifyUtils.commitTransaction(ofy);
    } finally {
      ObjectifyUtils.rollbackTransactionIfStillActive(ofy);
    }
  }

  // TODO(arjuns) : Move this to JobManager.
  public void enqueueCollectionGoogleDocJob(ImportExternalIdDto importExternalIdDto,
      GoogleDocInfoDto gdocInfo, JobId parentJobId, JobId rootJobId) {
    String title = calculateTitle(importExternalIdDto, gdocInfo);
    
    ImportCollectionGoogleDocContext jobRequest = new ImportCollectionGoogleDocContext.Builder()
        .title(title)
        .externalId(importExternalIdDto.getExternalId())
        .gdocInfo(gdocInfo)
        .build();

    Objectify ofy = ObjectifyUtils.initiateTransaction();
    try {
      JobEntity childJob = jobManager.createImportCollectionGoogleCollectionJob(
          ofy, jobRequest, parentJobId, rootJobId);
      updateImportExternalIdDtoForCollections(importExternalIdDto,
          ModuleType.GOOGLE_COLLECTION, ModuleState.IMPORTING, title, childJob);

      jobManager.enqueueImportChildJob(ofy, parentJobId, childJob.getJobId(), importExternalIdDto,
          QueueEnum.LIGHT);
      ObjectifyUtils.commitTransaction(ofy);
    } finally {
      ObjectifyUtils.rollbackTransactionIfStillActive(ofy);
    }
  }

  /**
   * @param importExternalIdDto
   * @param gdocInfo
   * @return
   */
  private String calculateTitle(ImportExternalIdDto importExternalIdDto, GoogleDocInfoDto gdocInfo) {
    checkNotNull(importExternalIdDto, "importExternalIdDto");
    checkNotNull(gdocInfo, "gdocInfo");
    
    if (StringUtils.isNotBlank(importExternalIdDto.getTitle())) {
      return importExternalIdDto.getTitle();
    } else {
      return gdocInfo.getTitle();
    }
  }

}
