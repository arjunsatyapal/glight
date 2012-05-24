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
package com.google.light.server.jobs.handlers.collectionjobs.gdoccollection;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.constants.LightConstants.GDATA_GDOC_MAX_RESULTS;
import static com.google.light.server.dto.pojo.tree.collection.CollectionTreeUtils.generateCollectionNodeFromChildJob;
import static com.google.light.server.jersey.resources.thirdparty.mixed.ImportResource.updateImportExternalIdDtoForCollections;
import static com.google.light.server.utils.LightUtils.LATEST_VERSION;
import static com.google.light.server.utils.LightUtils.createCollectionNode;
import static com.google.light.server.utils.LightUtils.isCollectionEmpty;

import com.google.inject.Inject;
import com.google.light.server.constants.QueueEnum;
import com.google.light.server.dto.importresource.ImportExternalIdDto;
import com.google.light.server.dto.module.ModuleState;
import com.google.light.server.dto.module.ModuleType;
import com.google.light.server.dto.pojo.ChangeLogEntryPojo;
import com.google.light.server.dto.pojo.tree.AbstractTreeNode.TreeNodeType;
import com.google.light.server.dto.pojo.tree.collection.CollectionTreeNodeDto;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.JobId;
import com.google.light.server.dto.thirdparty.google.gdoc.GoogleDocInfoDto;
import com.google.light.server.dto.thirdparty.google.gdoc.GoogleDocResourceId;
import com.google.light.server.jobs.handlers.JobHandlerInterface;
import com.google.light.server.jobs.handlers.modulejobs.gdocument.ImportModuleGoogleDocJobHandler;
import com.google.light.server.manager.interfaces.JobManager;
import com.google.light.server.persistence.entity.jobs.JobEntity;
import com.google.light.server.persistence.entity.jobs.JobState;
import com.google.light.server.servlets.thirdparty.google.gdoc.DocsServiceWrapper;
import com.google.light.server.utils.GuiceUtils;
import com.google.light.server.utils.JsonUtils;
import com.google.light.server.utils.LightUtils;
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
        "This handler can handle only " + ModuleType.GOOGLE_COLLECTION);

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

        GoogleDocResourceId gdocResourceId = new GoogleDocResourceId(context.getExternalId());
        DocsServiceWrapper docsService = GuiceUtils.getInstance(DocsServiceWrapper.class);
        List<GoogleDocInfoDto> listOfChilds =
            docsService.getFolderContentWhichAreSupportedInAlphabeticalOrder(
                gdocResourceId, GDATA_GDOC_MAX_RESULTS,
                GoogleDocInfoDto.Configuration.DTO_FOR_DEBUGGING);

        JobId enqueuedJobId = null;
        for (GoogleDocInfoDto currChild : listOfChilds) {
          ImportExternalIdDto existing = context.findImportExternalIdDtoByExternalId(
              currChild.getExternalId());
          if (existing != null && !existing.needsNewJobId()) {
            // For current child, job is already created or not required.
            continue;
          }

          ModuleType moduleType = currChild.getModuleType();
          ImportExternalIdDto importExternalIdDto =
              LightUtils.createImportExternalIdDto(
                  context.getContentLicenses(), currChild.getExternalId(), null /* jobId */,
                  null /* jobState */,
                  null /* moduleId */, moduleType, ModuleState.IMPORTING, currChild.getTitle(),
                  LightUtils.LATEST_VERSION);

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

          context.addImportModuleDto(importExternalIdDto);
          jobEntity.setContext(context);

          if (enqueuedJobId != null) {
            // Update ParentJob Context and then exit for loop and retry this function for other
            // pending childs.
            context.validate();
            jobEntity.addChildJob(checkNotNull(importExternalIdDto.getJobId(),
                "jobId cannot be null."));
            jobManager.put(ofy, jobEntity, new ChangeLogEntryPojo(
                "enqueued child : " + importExternalIdDto.getJobId()));
          } else {
            jobManager.put(ofy, jobEntity, new ChangeLogEntryPojo(
                "child upto date : " + importExternalIdDto.getExternalId()));
          }
          return true;
        }

        if (enqueuedJobId == null) {

          // All possible childs are created. So this job is now ready for getting child
          // notifications.
          context.validate();
          JobState jobState = null;
          if (isCollectionEmpty(jobEntity.getChildJobs())) {
            // This means all the childs were ignored.
            jobState = JobState.ALL_CHILDS_COMPLETED;
            // So re-enqueue this job.
            jobManager.enqueueLightJob(ofy, jobEntity.getJobId());
          } else {
            jobState = JobState.WAITING_FOR_CHILD_COMPLETE_NOTIFICATION;
          }

          System.out.println(jobEntity.getContext().getValue());
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
    CollectionTreeNodeDto subCollection = createCollectionNode(
        null /* description */, context.getExternalId(),
        null/* moduleId */, ModuleType.LIGHT_SUB_COLLECTION, null /* nodeId */,
        TreeNodeType.INTERMEDIATE_NODE, context.getTitle(), LATEST_VERSION);

    for (ImportExternalIdDto curr : context.getList()) {
      CollectionTreeNodeDto child = null;
      if (curr.hasJobId()) {
        child = generateCollectionNodeFromChildJob(jobManager, curr.getJobId());
      } else {
        checkArgument(curr.getModuleType().getNodeType() == TreeNodeType.LEAF_NODE,
            "Intermediate nodes are expected to have created a job. Failed for : " +
                curr.getExternalId());
        child = createCollectionNode(null /* description */, 
            curr.getExternalId(), curr.getModuleId(), curr.getModuleType(),null /* nodeId */,
            TreeNodeType.LEAF_NODE, curr.getTitle(), LATEST_VERSION);
      }

      checkNotNull(child, "child should not be null.");
      subCollection.addChildren(child);
      System.out.println("\n***SubCollection = " + JsonUtils.toJson(subCollection));
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
            .contentLicenses(importExternalIdDto.getContentLicenses())
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
