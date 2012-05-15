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

import com.google.inject.Inject;
import com.google.light.server.dto.importresource.ImportBatchType;
import com.google.light.server.dto.importresource.ImportBatchWrapper;
import com.google.light.server.dto.importresource.ImportExternalIdDto;
import com.google.light.server.dto.module.ModuleStateCategory;
import com.google.light.server.dto.module.ModuleType;
import com.google.light.server.dto.pojo.tree.AbstractTreeNode.TreeNodeType;
import com.google.light.server.dto.pojo.tree.collection.CollectionTreeNodeDto;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.CollectionId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.JobId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.Version;
import com.google.light.server.jobs.handlers.JobHandlerInterface;
import com.google.light.server.manager.interfaces.CollectionManager;
import com.google.light.server.manager.interfaces.JobManager;
import com.google.light.server.persistence.entity.collection.CollectionVersionEntity;
import com.google.light.server.persistence.entity.jobs.JobEntity;
import com.google.light.server.persistence.entity.jobs.JobState;
import com.google.light.server.utils.ObjectifyUtils;
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
  private CollectionManager collectionManager;

  @Inject
  public ImportBatchJobHandler(JobManager jobManager, CollectionManager collectionManager) {
    this.jobManager = checkNotNull(jobManager, "jobManager");
    this.collectionManager = checkNotNull(collectionManager, "collectionManager");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void handle(JobEntity jobEntity) {
    ImportBatchWrapper importBatchJobContext = jobEntity.getContext(ImportBatchWrapper.class);
    ImportBatchType type = importBatchJobContext.getImportBatchType();
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
      ImportBatchWrapper importBatchJobContext, JobEntity jobEntity) {
    CollectionId collectionId = importBatchJobContext.getCollectionId();
    Version version = importBatchJobContext.getVersion();

    CollectionTreeNodeDto collectionRoot = null;

    if (importBatchJobContext.getBaseVersion().isNoVersion()) {
      collectionRoot = new CollectionTreeNodeDto.Builder()
          .type(TreeNodeType.ROOT_NODE)
          .title(importBatchJobContext.getCollectionTitle())
          .moduleType(ModuleType.LIGHT_COLLECTION)
          .build();
    } else {
      CollectionVersionEntity collectionEntity = collectionManager.getCollectionVersion(
          null, collectionId, importBatchJobContext.getBaseVersion());
      collectionRoot = collectionEntity.getCollectionTree();
    }

    for (ImportExternalIdDto currDto : importBatchJobContext.getList()) {
      CollectionTreeNodeDto childNode = null;
      System.out.println(currDto.toJson());

      if (currDto.getModuleStateCategory() != ModuleStateCategory.HOSTED) {
        logger.info("Ignoring as it is not hosted : " + currDto.toJson());
        // Since this module is not going to be hosted, so continue.
        continue;
      }

      if (currDto.getModuleType().mapsToCollection()) {
        childNode = createChildNodeFromChildJobs(currDto);
      } else {
        childNode = createChildNodeFromExistingDetails(currDto);
      }

      checkNotNull(childNode, "childNode cannot be null here.");
      addChildToParent(collectionRoot, childNode);
    }

    Objectify ofy = ObjectifyUtils.initiateTransaction();
    try {
      collectionManager.publishCollectionVersion(ofy, collectionId, version, collectionRoot);
      updateJobContext(ofy, jobManager, JobState.COMPLETE, importBatchJobContext,
          jobEntity, "Published collection successfully.");
      ObjectifyUtils.commitTransaction(ofy);
    } finally {
      ObjectifyUtils.rollbackTransactionIfStillActive(ofy);
    }
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
  private CollectionTreeNodeDto createChildNodeFromChildJobs(
      ImportExternalIdDto importExternalIdDto) {
    checkArgument(
        importExternalIdDto.getModuleType().getNodeType() == TreeNodeType.INTERMEDIATE_NODE,
        "This should be called only for " + TreeNodeType.INTERMEDIATE_NODE);

    CollectionTreeNodeDto collectionRoot = new CollectionTreeNodeDto.Builder()
        .type(TreeNodeType.INTERMEDIATE_NODE)
        .title(importExternalIdDto.getTitle())
        .externalId(importExternalIdDto.getExternalId())
        .moduleType(ModuleType.LIGHT_SUB_COLLECTION)
        .build();

    JobEntity jobEntity = jobManager.get(null, importExternalIdDto.getJobId());
    for (JobId childJobId : jobEntity.getChildJobs()) {
      CollectionTreeNodeDto child = generateCollectionNodeFromChildJob(jobManager, childJobId);
      System.out.println(child.toJson());
      collectionRoot.addChildren(child);
    }

    return collectionRoot;
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
        .type(TreeNodeType.LEAF_NODE)
        .title(importExternalIdDto.getTitle())
        .externalId(importExternalIdDto.getExternalId())
        .moduleId(importExternalIdDto.getModuleId())
        .moduleType(importExternalIdDto.getModuleType())
        .version(new Version(Version.LATEST_VERSION))
        .build();
  }
}
