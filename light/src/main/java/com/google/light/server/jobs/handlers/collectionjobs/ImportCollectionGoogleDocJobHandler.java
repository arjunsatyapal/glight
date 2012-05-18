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
import static com.google.light.server.jersey.resources.thirdparty.mixed.ImportResource.updateImportExternalIdDtoForCollections;

import com.google.light.server.utils.LightUtils;

import com.google.light.server.utils.JsonUtils;

import com.google.light.server.manager.interfaces.ModuleManager;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.light.server.constants.QueueEnum;
import com.google.light.server.dto.importresource.ImportExternalIdDto;
import com.google.light.server.dto.module.ModuleState;
import com.google.light.server.dto.module.ModuleType;
import com.google.light.server.dto.pojo.ChangeLogEntryPojo;
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
import com.google.light.server.utils.Transactable;
import com.googlecode.objectify.Objectify;
import java.util.List;
import org.apache.commons.lang.StringUtils;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class ImportCollectionGoogleDocJobHandler implements JobHandlerInterface {
  private JobManager jobManager;
  private ModuleManager moduleManager;
  private ImportModuleGoogleDocJobHandler importGDocModuleJobHandler;

  @Inject
  public ImportCollectionGoogleDocJobHandler(JobManager jobManager,
      ImportModuleGoogleDocJobHandler importGoogleDocModuleJobHandler, ModuleManager moduleManager) {
    this.jobManager = checkNotNull(jobManager, "jobManager");
    this.importGDocModuleJobHandler = checkNotNull(importGoogleDocModuleJobHandler,
        "importGoogleDocModuleJobHandler");
    this.moduleManager = checkNotNull(moduleManager, "moduleManager");
  }

  @Override
  public void handle(JobEntity jobEntity) {
    ImportCollectionGoogleDocContext context =
        jobEntity.getContext(ImportCollectionGoogleDocContext.class);
    checkArgument(context.getExternalId().getModuleType() == ModuleType.GOOGLE_COLLECTION,
        "This handler can handle only Google Collections");

    JobState jobState = jobEntity.getJobState();
    switch (jobState) {
      case ALL_CHILDS_COMPLETED:
        handleChildCompletions(context, jobEntity);
        break;
        
      case ENQUEUED:
        createChilds(jobEntity.getJobId());
        break;

      default:
        throw new IllegalStateException("Unexpected " + jobState);
    }

  }

  /**
   * @param jobId
   */
  private void createChilds(final JobId jobId) {
    boolean retryThisMethod = ObjectifyUtils.repeatInTransaction(new Transactable<Boolean>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Boolean run(Objectify ofy) {
        JobEntity jobEntity = jobManager.get(ofy, jobId);
        ImportCollectionGoogleDocContext context = jobEntity.getContext(
            ImportCollectionGoogleDocContext.class);
        System.out.println(context.toJson());

        GoogleDocResourceId gdocResourceId = new GoogleDocResourceId(context.getExternalId());
        DocsServiceWrapper docsService = GuiceUtils.getInstance(DocsServiceWrapper.class);
        List<GoogleDocInfoDto> listOfChilds = docsService.getFolderContentWhichAreSupported(
            gdocResourceId, GDATA_GDOC_MAX_RESULTS);
        System.out.println(JsonUtils.toJson(listOfChilds));

        JobId enqueuedJobId = null;
        for (GoogleDocInfoDto currChild : listOfChilds) {
          ImportExternalIdDto existing = context.findImportExternalIdDtoByExternalId(
              currChild.getExternalId());
          if (existing != null && existing.hasJobId()) {
            // For current child, job is already created.
            continue;
          }

          ModuleType moduleType = currChild.getModuleType();
          ImportExternalIdDto importExternalIdDto = new ImportExternalIdDto.Builder()
              .externalId(currChild.getExternalId())
              .title(currChild.getTitle())
              .moduleType(currChild.getModuleType())
              .build();

          switch (moduleType) {
            case GOOGLE_COLLECTION:
              enqueuedJobId = enqueueCollectionGoogleDocJob(ofy, importExternalIdDto,
                  currChild, jobEntity.getJobId(), jobEntity.getRootJobId());
              break;

            case GOOGLE_DOCUMENT:
              enqueuedJobId = importGDocModuleJobHandler.enqueueModuleGoogleDocJob(ofy,
                  importExternalIdDto, currChild, jobEntity.getJobId(), jobEntity.getRootJobId());
              break;

            default:
              throw new IllegalStateException("We should not get this type : " + moduleType);
          }

          if (enqueuedJobId != null) {
            // Update ParentJob Context and then exit for loop and retry this function for other
            // pending childs.
            context.addImportModuleDto(importExternalIdDto);
            jobEntity.setContext(context);
            jobEntity.addChildJob(checkNotNull(importExternalIdDto.getJobId(),
                "jobId cannot be null."));
            System.out.println(context.toJson());
            jobManager.put(ofy, jobEntity, new ChangeLogEntryPojo(
                "enqueued child : " + importExternalIdDto.getJobId()));
            return true;
          }
        }

        if (enqueuedJobId == null) {
          
          // All possible childs are created. So this job is now ready for getting child notifications.
          System.out.println(context.toJson());
          System.out.println("\n****" + Iterables.toString(jobEntity.getChildJobs()));
          JobState jobState = null;
          if (LightUtils.isCollectionEmpty(jobEntity.getChildJobs())) {
            // This means all the childs were ignored.
            jobState = JobState.ALL_CHILDS_COMPLETED;
            // So re-enqueue this job.
            jobManager.enqueueLightJob(ofy, jobEntity.getJobId());
          } else {
            jobState = JobState.WAITING_FOR_CHILD_COMPLETE_NOTIFICATION;
          }
          
          jobEntity.setJobState(jobState);
          jobManager.put(ofy, jobEntity, new ChangeLogEntryPojo(
              "getting ready for " + jobState));
          
        }
        return false;
      }
    });

    if (retryThisMethod) {
      createChilds(jobId);
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
        .nodeType(TreeNodeType.INTERMEDIATE_NODE)
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
            .nodeType(TreeNodeType.LEAF_NODE)
            .moduleType(curr.getModuleType())
            .moduleId(curr.getModuleId())
            .version(new Version(Version.LATEST_VERSION))
            .build();
      }

      checkNotNull(child, "child should not be null.");
      subCollection.addChildren(child);
    }

    jobManager.enqueueCompleteJob(jobEntity.getJobId(), subCollection, "Marking job as complete");
  }

  // TODO(arjuns) : Move this to JobManager.
  public JobId enqueueCollectionGoogleDocJob(Objectify ofy,
      final ImportExternalIdDto importExternalIdDto,
      GoogleDocInfoDto gdocInfo, final JobId parentJobId, final JobId rootJobId) {
    final String title = calculateTitle(importExternalIdDto, gdocInfo);

    final ImportCollectionGoogleDocContext jobRequest =
        new ImportCollectionGoogleDocContext.Builder()
            .title(title)
            .externalId(importExternalIdDto.getExternalId())
            .gdocInfo(gdocInfo)
            .build();

    JobEntity childJob = jobManager.createImportCollectionGoogleCollectionJob(
        ofy, jobRequest, parentJobId, rootJobId);
    updateImportExternalIdDtoForCollections(importExternalIdDto,
        ModuleType.GOOGLE_COLLECTION, ModuleState.IMPORTING, title, childJob);

    jobManager.enqueueImportChildJob(ofy, parentJobId, childJob.getJobId(),
        importExternalIdDto,
        QueueEnum.LIGHT);
    return childJob.getJobId();
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