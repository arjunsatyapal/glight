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
package com.google.light.server.jobs.handlers.collectionjobs.youtubeplaylist;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.dto.pojo.tree.collection.CollectionTreeUtils.generateCollectionNodeFromChildJob;
import static com.google.light.server.jersey.resources.thirdparty.mixed.ImportResource.updateImportExternalIdDtoForCollections;
import static com.google.light.server.utils.LightUtils.isCollectionEmpty;

import com.google.light.server.dto.pojo.typewrapper.longwrapper.PersonId;

import java.util.List;

import com.google.common.collect.Lists;

import com.google.light.server.utils.GuiceUtils;

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
import com.google.light.server.dto.thirdparty.google.youtube.YouTubePlaylistInfo;
import com.google.light.server.dto.thirdparty.google.youtube.YouTubeVideoInfo;
import com.google.light.server.jobs.handlers.JobHandlerInterface;
import com.google.light.server.jobs.handlers.modulejobs.synthetic.ImportModuleSyntheticModuleJobHandler;
import com.google.light.server.manager.interfaces.JobManager;
import com.google.light.server.persistence.entity.jobs.JobEntity;
import com.google.light.server.persistence.entity.jobs.JobEntity.TaskType;
import com.google.light.server.persistence.entity.jobs.JobState;
import com.google.light.server.utils.JsonUtils;
import com.google.light.server.utils.LightUtils;
import com.google.light.server.utils.ObjectifyUtils;
import com.google.light.server.utils.Transactable;
import com.googlecode.objectify.Objectify;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class ImportCollectionYouTubePlaylistHandler implements JobHandlerInterface {
  private JobManager jobManager;
  private ModuleManager moduleManager;
  private ImportModuleSyntheticModuleJobHandler importSyntheticModuleJobHandler;

  @Inject
  public ImportCollectionYouTubePlaylistHandler(JobManager jobManager, ModuleManager moduleManager,
      ImportModuleSyntheticModuleJobHandler importSyntheticModuleJobHandler) {
    this.jobManager = checkNotNull(jobManager, "jobManager");
    this.moduleManager = checkNotNull(moduleManager, "moduleManager");
    this.importSyntheticModuleJobHandler = checkNotNull(importSyntheticModuleJobHandler,
        "importSyntheticModuleJobHandler");
  }

  @Override
  public void handle(JobEntity jobEntity) {
    ImportCollectionYouTubePlaylistContext context =
        jobEntity.getContext(ImportCollectionYouTubePlaylistContext.class);
    checkArgument(context.getExternalId().getModuleType() == ModuleType.YOU_TUBE_PLAYLIST,
        "This handler can handle only " + ModuleType.YOU_TUBE_PLAYLIST);

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
        ImportCollectionYouTubePlaylistContext context = jobEntity.getContext(
            ImportCollectionYouTubePlaylistContext.class);
        System.out.println(context.toJson());
        YouTubePlaylistInfo playlistInfo = context.getYouTubePlaylistInfo();
        System.out.println("Childs = " + playlistInfo.getListOfVideos().size());

        JobId enqueuedJobId = null;
        int skipCounter = 0;
        for (YouTubeVideoInfo currChild : playlistInfo.getListOfVideos()) {
          ImportExternalIdDto existing = context.findImportExternalIdDtoByExternalId(
              currChild.getExternalId());
          
          if (existing != null && !existing.needsNewJobId()) {
            skipCounter++;
            // For current child, job is already created or not required.
            continue;
          }
          System.out.println("Skipped : " + skipCounter);
          skipCounter = 0;

          List<PersonId> owners = Lists.newArrayList(GuiceUtils.getOwnerId());
          ModuleType moduleType = currChild.getModuleType();
          ImportExternalIdDto importExternalIdDto = LightUtils.createImportExternalIdDto(
              playlistInfo.getContentLicenses(), currChild.getExternalId(),
              null /* jobId */, null /* jobState */, null /* moduleId */, moduleType,
              ModuleState.IMPORTING, currChild.getTitle(), LightUtils.LATEST_VERSION);

          switch (moduleType) {
            case YOU_TUBE_VIDEO:
              enqueuedJobId = importSyntheticModuleJobHandler.enqueueImportChildSyntheticModule(
                  ofy, importExternalIdDto, TaskType.IMPORT_YOUTUBE_VIDEO, jobEntity.getJobId(),
                  jobEntity.getRootJobId());
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
            System.out.println(context.toJson());
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
          System.out.println(context.toJson());
          System.out.println("\n****" + Iterables.toString(jobEntity.getChildJobs()));
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
  private void handleChildCompletions(ImportCollectionYouTubePlaylistContext context,
      JobEntity jobEntity) {
    YouTubePlaylistInfo playlistInfo = context.getYouTubePlaylistInfo();
    CollectionTreeNodeDto subCollection = new CollectionTreeNodeDto.Builder()
        .title(playlistInfo.getTitle())
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
      System.out.println("\n***SubCollection = " + JsonUtils.toJson(subCollection));
    }

    jobManager.enqueueCompleteJob(jobEntity.getJobId(), subCollection, "Marking job as complete");
  }
  
  // TODO(arjuns) : Move this to JobManager.
  public JobId enqueueCollectionYouTubePlaylistJob(Objectify ofy,
      final ImportExternalIdDto importExternalIdDto,
      YouTubePlaylistInfo youTubePlaylistInfo, final JobId parentJobId, final JobId rootJobId) {
    final String title = youTubePlaylistInfo.getTitle();

    final ImportCollectionYouTubePlaylistContext jobRequest =
        new ImportCollectionYouTubePlaylistContext.Builder()
            .contentLicenses(youTubePlaylistInfo.getContentLicenses())
            .youTubePlaylistInfo(youTubePlaylistInfo)
            .build();

    JobEntity childJob = jobManager.createImportCollectionYouTubePlaylistJob(
        ofy, jobRequest, parentJobId, rootJobId);
    updateImportExternalIdDtoForCollections(importExternalIdDto,
        ModuleType.YOU_TUBE_PLAYLIST, ModuleState.IMPORTING, title, childJob);

    jobManager.enqueueImportChildJob(ofy, parentJobId, childJob.getJobId(),
        importExternalIdDto,
        QueueEnum.LIGHT);
    return childJob.getJobId();
  }
}
