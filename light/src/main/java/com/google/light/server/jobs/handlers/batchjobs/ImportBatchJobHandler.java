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
package com.google.light.server.jobs.handlers.batchjobs;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.dto.pojo.tree.collection.CollectionTreeUtils.generateCollectionNodeFromChildJob;
import static com.google.light.server.jobs.JobUtils.updateJobContext;
import static com.google.light.server.utils.ObjectifyUtils.repeatInTransaction;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.light.server.dto.collection.CollectionState;
import com.google.light.server.dto.importresource.ImportBatchType;
import com.google.light.server.dto.importresource.ImportBatchWrapper;
import com.google.light.server.dto.importresource.ImportExternalIdDto;
import com.google.light.server.dto.module.ModuleStateCategory;
import com.google.light.server.dto.module.ModuleType;
import com.google.light.server.dto.pojo.ChangeLogEntryPojo;
import com.google.light.server.dto.pojo.tree.AbstractTreeNode.TreeNodeType;
import com.google.light.server.dto.pojo.tree.collection.CollectionTreeNodeDto;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.CollectionId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.JobId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.Version;
import com.google.light.server.dto.thirdparty.google.gdoc.GoogleDocInfoDto;
import com.google.light.server.dto.thirdparty.google.gdoc.GoogleDocResourceId;
import com.google.light.server.dto.thirdparty.google.youtube.YouTubePlaylistInfo;
import com.google.light.server.jobs.handlers.JobHandlerInterface;
import com.google.light.server.jobs.handlers.collectionjobs.gdoccollection.ImportCollectionGoogleDocJobHandler;
import com.google.light.server.jobs.handlers.collectionjobs.youtubeplaylist.ImportCollectionYouTubePlaylistHandler;
import com.google.light.server.jobs.handlers.modulejobs.gdocument.ImportModuleGoogleDocJobHandler;
import com.google.light.server.jobs.handlers.modulejobs.synthetic.ImportModuleSyntheticModuleJobHandler;
import com.google.light.server.manager.interfaces.CollectionManager;
import com.google.light.server.manager.interfaces.JobManager;
import com.google.light.server.manager.interfaces.ModuleManager;
import com.google.light.server.persistence.entity.collection.CollectionVersionEntity;
import com.google.light.server.persistence.entity.jobs.JobEntity;
import com.google.light.server.persistence.entity.jobs.JobEntity.TaskType;
import com.google.light.server.persistence.entity.jobs.JobState;
import com.google.light.server.servlets.thirdparty.google.gdoc.DocsServiceWrapper;
import com.google.light.server.servlets.thirdparty.google.youtube.YouTubeServiceWrapper;
import com.google.light.server.urls.YouTubeUrl;
import com.google.light.server.utils.GuiceUtils;
import com.google.light.server.utils.LightUtils;
import com.google.light.server.utils.Transactable;
import com.googlecode.objectify.Objectify;
import java.util.logging.Logger;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class ImportBatchJobHandler implements JobHandlerInterface {
  private static final Logger logger = Logger.getLogger(ImportBatchJobHandler.class.getName());

  private JobManager jobManager;
  private ModuleManager moduleManager;
  private CollectionManager collectionManager;
  private ImportModuleGoogleDocJobHandler importModuleGDocJobHandler;
  private ImportCollectionGoogleDocJobHandler importCollectionGDocJobHandler;
  private ImportCollectionYouTubePlaylistHandler importYouTubePlaylistHandler;
  private ImportModuleSyntheticModuleJobHandler importSyntheticModuleJobHandler;

  @Inject
  public ImportBatchJobHandler(JobManager jobManager, CollectionManager collectionManager,
      ModuleManager moduleManager, ImportModuleGoogleDocJobHandler importModuleGDocJobHandler,
      ImportCollectionGoogleDocJobHandler importCollectionGDocJobHandler,
      ImportModuleSyntheticModuleJobHandler importSyntheticModuleJobHandler,
      ImportCollectionYouTubePlaylistHandler importYouTubePlaylistHandler) {
    this.jobManager = checkNotNull(jobManager, "jobManager");
    this.collectionManager = checkNotNull(collectionManager, "collectionManager");
    this.moduleManager = checkNotNull(moduleManager, "moduleManager");
    this.importModuleGDocJobHandler = checkNotNull(importModuleGDocJobHandler,
        "importModuleGDocJobHandler");
    this.importCollectionGDocJobHandler = checkNotNull(importCollectionGDocJobHandler,
        "importCollectionGDocJobHandler");
    this.importYouTubePlaylistHandler = checkNotNull(importYouTubePlaylistHandler,
        "importYouTubePlaylistHandler");
    this.importSyntheticModuleJobHandler = checkNotNull(importSyntheticModuleJobHandler,
        "importSyntheticModuleJobHandler");

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void handle(JobEntity jobEntity) {
    ImportBatchWrapper importBatchJobContext = jobEntity.getContext(ImportBatchWrapper.class);
    ImportBatchType type = importBatchJobContext.getImportBatchType();

    JobState jobState = jobEntity.getJobState();
    switch (jobState) {
      case ENQUEUED:
      case CREATING_CHILDS:
        createChilds(jobEntity.getJobId());
        return;
      default:
        break;
    }

    switch (type) {
      case MODULE_JOB:
        handleModuleJob(importBatchJobContext, jobEntity);
        break;

      case CREATE_COLLECTION_JOB:
        throw new IllegalStateException("This should not happen because all the "
            + ImportBatchType.CREATE_COLLECTION_JOB + "should be converted to "
            + ImportBatchType.APPEND_COLLECTION_JOB + " by now.");

      case APPEND_COLLECTION_JOB:
        handleAppendCollection(importBatchJobContext, jobEntity);
        return;
      default:
        throw new IllegalStateException("Add more logic for " + type);
    }
  }

  /**
   * @param jobId
   */
  private void createChilds(final JobId jobId) {
    boolean hasChildsNeedingJob = false;

    do {
      hasChildsNeedingJob = repeatInTransaction(new Transactable<Boolean>() {
        @SuppressWarnings("synthetic-access")
        @Override
        public Boolean run(Objectify ofy) {
          boolean foundChildNeedingJob = false;
          JobEntity jobEntity = jobManager.get(ofy, jobId);
          ImportBatchWrapper importBatchJobContext = jobEntity.getContext(ImportBatchWrapper.class);

          JobId enqueuedJobId = null;
          for (ImportExternalIdDto curr : importBatchJobContext.getList()) {
            if (!curr.needsNewJobId()) {
              continue;
            }

            foundChildNeedingJob = true;

            enqueuedJobId = createAndEnqueueChildJob(
                ofy, jobEntity, importBatchJobContext, enqueuedJobId, curr);
            /*
             * Break from for loop so that other child can be created in next iteration else
             * datastore will complain about operating on too many entities.
             */
            break;
          }
          return foundChildNeedingJob;
        }

      });
    } while (hasChildsNeedingJob);

    /*
     * All possible childs are created. So this job is now ready for getting child notifications.
     */
    repeatInTransaction(new Transactable<Void>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public Void run(Objectify ofy) {
        JobEntity jobEntity = jobManager.get(ofy, jobId);
        ImportBatchWrapper importBatchJobContext = jobEntity.getContext(ImportBatchWrapper.class);

        System.out.println(importBatchJobContext.toJson());
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

        return null;
      }
    });
  }

  private JobId createAndEnqueueChildJob(Objectify ofy, JobEntity jobEntity,
      ImportBatchWrapper importBatchJobContext, JobId enqueuedJobId,
      ImportExternalIdDto importExternalIdDto) {
    switch (importExternalIdDto.getModuleType()) {
      case LIGHT_SYNTHETIC_MODULE:
        enqueuedJobId = importSyntheticModuleJobHandler.enqueueImportChildSyntheticModule(
            ofy, importExternalIdDto, 
            TaskType.IMPORT_SYNTHETIC_MODULE, jobEntity.getJobId(), jobEntity.getRootJobId());
        break;
        
      case YOU_TUBE_VIDEO :
        enqueuedJobId = importSyntheticModuleJobHandler.enqueueImportChildSyntheticModule(
            ofy, importExternalIdDto, 
            TaskType.IMPORT_YOUTUBE_VIDEO, jobEntity.getJobId(), jobEntity.getRootJobId());
        break;
        
      case YOU_TUBE_PLAYLIST:
        enqueuedJobId = enqueueImportCollectionYouTubePlaylist(ofy, importExternalIdDto, 
            jobEntity.getJobId(), jobEntity.getRootJobId());
        break;

      case GOOGLE_DOCUMENT:
        enqueuedJobId =
            enqueueImportModuleGoogleDocument(ofy, importExternalIdDto, jobEntity.getJobId(),
                jobEntity.getRootJobId(), GoogleDocInfoDto.Configuration.DTO_FOR_DEBUGGING);
        System.out.println(importExternalIdDto.toJson());
        break;

      case GOOGLE_COLLECTION:
        enqueuedJobId = enqueueImportCollectionGDoc(ofy, importExternalIdDto, jobEntity.getJobId(),
            jobEntity.getRootJobId(), GoogleDocInfoDto.Configuration.DTO_FOR_DEBUGGING);
        break;
      default:
        return enqueuedJobId;
    }

    // Update ParentJob Context and then exit for loop and retry this function for other
    // pending childs.
    System.out.println(importBatchJobContext.toJson());
    System.out.println("\n*** ExternalIdDto = " + importExternalIdDto.toJson());
    jobEntity.setContext(importBatchJobContext);

    if (enqueuedJobId != null) {
      jobEntity.addChildJob(checkNotNull(importExternalIdDto.getJobId(), "jobId cannot be null."));
    }

    jobManager.put(ofy, jobEntity, new ChangeLogEntryPojo(
        "Created child : " + importExternalIdDto.getJobId() + " for "
            + importExternalIdDto.getExternalId()));

    return enqueuedJobId;
  }

  private JobId enqueueImportCollectionYouTubePlaylist(
      Objectify ofy, ImportExternalIdDto importExternalIdDto,
      JobId parentJobId, JobId rootJobId) {
    YouTubeServiceWrapper ytService = GuiceUtils.getInstance(YouTubeServiceWrapper.class);
    YouTubeUrl ytUrl = new YouTubeUrl(importExternalIdDto.getExternalId());
    YouTubePlaylistInfo ytPlaylistInfo = ytService.getYouTubePlayListDetailedInfo(ytUrl);

    return importYouTubePlaylistHandler.enqueueCollectionYouTubePlaylistJob(
        ofy, importExternalIdDto, ytPlaylistInfo, parentJobId, rootJobId);
  }

  private JobId enqueueImportCollectionGDoc(Objectify ofy, ImportExternalIdDto importExternalIdDto,
      JobId parentJobId, JobId rootJobId, GoogleDocInfoDto.Configuration config) {
    GoogleDocInfoDto gdocInfo = getGDocInfo(importExternalIdDto, config);
    return importCollectionGDocJobHandler.enqueueCollectionGoogleDocJob(ofy,
        importExternalIdDto, gdocInfo, parentJobId, rootJobId);
  }

  private JobId enqueueImportModuleGoogleDocument(Objectify ofy,
      ImportExternalIdDto importExternalIdDto, JobId parentJobId, JobId rootJobId,
      GoogleDocInfoDto.Configuration config) {
    GoogleDocInfoDto gdocInfo = getGDocInfo(importExternalIdDto, config);
    return importModuleGDocJobHandler.enqueueModuleGoogleDocJob(ofy,
        importExternalIdDto, gdocInfo, parentJobId, rootJobId);
  }

  /**
   * @param importModuleDto
   * @return
   */
  private GoogleDocInfoDto getGDocInfo(ImportExternalIdDto importModuleDto,
      GoogleDocInfoDto.Configuration config) {
    DocsServiceWrapper docsService = GuiceUtils.getInstance(DocsServiceWrapper.class);
    GoogleDocResourceId resourceId = new GoogleDocResourceId(importModuleDto.getExternalId());
    GoogleDocInfoDto gdocInfo = docsService.getGoogleDocInfo(resourceId, config);
    return gdocInfo;
  }

  /**
   * @param importBatchJobContext
   * @param jobEntity
   */
  private void handleModuleJob(ImportBatchWrapper importBatchJobContext, JobEntity jobEntity) {
    jobManager.enqueueCompleteJob(jobEntity.getJobId(), importBatchJobContext,
        "Marking " + jobEntity.getJobId() + " as complete");
  }

  /**
   * @param importBatchJobContext
   * @param jobEntity
   */
  private void handleAppendCollection(
      final ImportBatchWrapper importBatchJobContext, final JobEntity jobEntity) {
    final CollectionId collectionId = importBatchJobContext.getCollectionId();
    final Version version = importBatchJobContext.getVersion();

    CollectionTreeNodeDto collectionRoot = null;

    if (importBatchJobContext.getBaseVersion().isNoVersion()) {
      collectionRoot = new CollectionTreeNodeDto.Builder()
          .nodeType(TreeNodeType.ROOT_NODE)
          .title(importBatchJobContext.getCollectionTitle())
          .moduleType(ModuleType.LIGHT_COLLECTION)
          .build();
    } else {
      CollectionVersionEntity collectionEntity = collectionManager.getCollectionVersion(
          null, collectionId, importBatchJobContext.getBaseVersion());
      collectionRoot = collectionEntity.getCollectionTree();
    }
    System.out.println("\n***CollectionRoot = " + collectionRoot.toJson());

    for (ImportExternalIdDto currDto : importBatchJobContext.getList()) {
      CollectionTreeNodeDto childNode = null;

      if (currDto.getModuleStateCategory() != ModuleStateCategory.HOSTED) {
        logger.info("Ignoring as it is not hosted : " + currDto.toJson());
        // Since this module is not going to be hosted, so continue.
        continue;
      }

      if (currDto.getModuleType().mapsToCollection()) {
        childNode = getChildNodeFromJobResult(currDto);
      } else {
        childNode = createChildNodeFromExistingDetails(currDto);
      }

      checkNotNull(childNode, "childNode cannot be null here.");
      addChildToParent(collectionRoot, childNode);
    }

    final CollectionTreeNodeDto finalCollectionRoot = collectionRoot;
    repeatInTransaction(new Transactable<Void>() {
      @SuppressWarnings("synthetic-access")
      @Override
      public Void run(Objectify ofy) {
        collectionManager.publishCollectionVersion(ofy, collectionId, version, finalCollectionRoot,
            CollectionState.PUBLISHED);
        updateJobContext(ofy, jobManager, JobState.COMPLETE, importBatchJobContext,
            jobEntity, "Published collection successfully.");
        return null;
      }
    });
  }

  /**
   * @param collectionRoot
   * @param childNode
   */
  private void addChildToParent(CollectionTreeNodeDto parent, CollectionTreeNodeDto childNode) {
    checkNotNull(parent, "collectionRoot");
    checkNotNull(childNode, "childNode");

    if (!parent.containsFirstLevelChildWithExternalId(childNode.getExternalId())) {
      parent.addChildren(childNode);
      return;
    }

    // Now child is part of the parent.
    if (childNode.isLeafNode()) {
      // Nothing to be doen here as parent already has this leaf node.
      return;
    }

    // Child node is a intermediate node.
    checkArgument(childNode.isIntermediateNode(), "expected node is intermdediate node");

    CollectionTreeNodeDto existingNode = parent.findFirstLevelChildByExternalId(
        childNode.getExternalId());

    merge(existingNode, childNode);
  }

  /**
   * @param existingNode
   * @param childNode
   */
  private void merge(CollectionTreeNodeDto existingNode, CollectionTreeNodeDto newNode) {
    checkNotNull(existingNode, "existingNode");
    checkNotNull(newNode, "childNode");
    checkArgument(existingNode.getNodeType() == newNode.getNodeType(),
        "Node Type for existingNode : " + existingNode.getNodeType()
            + " was not equal to childNode : " + newNode.getNodeType());
    checkArgument(!existingNode.isLeafNode(), "merge operation is not allowed for leaf nodes.");

    for (CollectionTreeNodeDto child : newNode.getChildren()) {
      addChildToParent(existingNode, child);
    }
  }

  /**
   * @param currDto
   * @return
   */
  private CollectionTreeNodeDto getChildNodeFromJobResult(
      ImportExternalIdDto importExternalIdDto) {
    checkArgument(
        importExternalIdDto.getModuleType().getNodeType() == TreeNodeType.INTERMEDIATE_NODE,
        "This should be called only for " + TreeNodeType.INTERMEDIATE_NODE);

    // CollectionTreeNodeDto collectionRoot = new CollectionTreeNodeDto.Builder()
    // .nodeType(TreeNodeType.INTERMEDIATE_NODE)
    // .title(importExternalIdDto.getTitle())
    // .externalId(importExternalIdDto.getExternalId())
    // .moduleType(ModuleType.LIGHT_SUB_COLLECTION)
    // .build();

    CollectionTreeNodeDto child = generateCollectionNodeFromChildJob(jobManager,
        importExternalIdDto.getJobId());
    // collectionRoot.addChildren(child);

    return child;
  }

  /**
   * @param currDto
   * @return
   */
  private CollectionTreeNodeDto createChildNodeFromExistingDetails(
      ImportExternalIdDto importExternalIdDto) {
    checkArgument(importExternalIdDto.getModuleType().getNodeType() == TreeNodeType.LEAF_NODE,
        "This should be called only for " + TreeNodeType.LEAF_NODE);
    return new CollectionTreeNodeDto.Builder()
        .nodeType(TreeNodeType.LEAF_NODE)
        .title(importExternalIdDto.getTitle())
        .externalId(importExternalIdDto.getExternalId())
        .moduleId(importExternalIdDto.getModuleId())
        .moduleType(importExternalIdDto.getModuleType())
        .version(new Version(Version.LATEST_VERSION))
        .build();
  }
}
