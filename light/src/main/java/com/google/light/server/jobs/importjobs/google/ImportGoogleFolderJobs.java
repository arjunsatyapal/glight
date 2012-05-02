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
package com.google.light.server.jobs.importjobs.google;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.jersey.resources.thirdparty.google.GoogleDocIntegrationUtils.importGDocResource;
import static com.google.light.server.jobs.JobUtils.updateChangeLog;
import static com.google.light.server.utils.GuiceUtils.getInstance;

import java.util.ArrayList;

import com.google.common.collect.Lists;

import com.google.appengine.tools.pipeline.FutureValue;
import com.google.appengine.tools.pipeline.PromisedValue;
import com.google.appengine.tools.pipeline.Value;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.light.jobs.LightJob2;
import com.google.light.jobs.LightJob3;
import com.google.light.server.dto.module.ModuleType;
import com.google.light.server.dto.pojo.LightJobContextPojo;
import com.google.light.server.dto.pojo.longwrapper.CollectionId;
import com.google.light.server.dto.pojo.longwrapper.ModuleId;
import com.google.light.server.dto.pojo.longwrapper.PersonId;
import com.google.light.server.dto.pojo.tree.CollectionTreeNode;
import com.google.light.server.dto.pojo.tree.TreeNode.TreeNodeType;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocInfoDto;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocResourceId;
import com.google.light.server.jobs.importjobs.PipelineJobs;
import com.google.light.server.manager.interfaces.CollectionManager;
import com.google.light.server.manager.interfaces.JobManager;
import com.google.light.server.manager.interfaces.ModuleManager;
import com.google.light.server.persistence.entity.collection.CollectionEntity;
import com.google.light.server.persistence.entity.jobs.JobEntity;
import com.google.light.server.persistence.entity.jobs.JobEntity.JobState;
import com.google.light.server.thirdparty.clients.google.gdata.gdoc.DocsServiceWrapper;
import com.google.light.server.utils.GuiceUtils;
import com.google.light.server.utils.JsonUtils;
import com.google.light.server.utils.ObjectifyUtils;
import com.googlecode.objectify.Objectify;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class ImportGoogleFolderJobs {
  static final Logger logger = Logger.getLogger(ImportGoogleFolderJobs.class.getName());

  @SuppressWarnings("serial")
  public static class InitiateGoogleCollectionImport extends
      LightJob2<LightJobContextPojo, GoogleDocResourceId> {
    /**
     * {@inheritDoc}
     */
    @Override
    public Value<LightJobContextPojo> handler(GoogleDocResourceId gdocFolderResourceId) {
      checkArgument(gdocFolderResourceId.getModuleType() == ModuleType.GOOGLE_COLLECTION,
          "Invalid ModuleType[" + gdocFolderResourceId.getModuleType() + "] for resourceId["
              + gdocFolderResourceId + ".");

      updateChangeLog(null, getContext().getJobId(), "Fetching Folder Tree from Google.");
      DocsServiceWrapper docsService = GuiceUtils.getInstance(DocsServiceWrapper.class);
      List<GoogleDocInfoDto> folderContents = docsService.getFolderContentAll(gdocFolderResourceId);
      Map<PromisedValue<Boolean>, Long> mapOfPvs = Maps.newHashMap();

      Map<GoogleDocResourceId, Long> mapOfResourceToJob = Maps.newHashMap();

      // Now creating Child Jobs for each of the nodes.
      for (GoogleDocInfoDto curr : folderContents) {
        GoogleDocResourceId resourceId = curr.getGoogleDocsResourceId();

        JobEntity jobEntity = null;
        PromisedValue<Boolean> pv = null;
        switch (curr.getModuleType()) {
          case GOOGLE_DOC:
            pv = newPromise(Boolean.class);
            jobEntity = importGDocResource(resourceId, docsService,
                getContext().getJobId(), getContext().getRootJobId(), pv.getHandle());
            updateChangeLog(null, getContext().getJobId(),
                "Creating a childJob[" + jobEntity.getId() + "], to download ["
                    + curr.getGoogleDocsResourceId() + "].");
            mapOfResourceToJob.put(resourceId, jobEntity.getId());
            mapOfPvs.put(pv, jobEntity.getId());
            break;

          case GOOGLE_COLLECTION:
            pv = newPromise(Boolean.class);
            jobEntity = importGDocResource(resourceId, docsService,
                getContext().getJobId(), getContext().getRootJobId(), pv.getHandle());
            updateChangeLog(null, getContext().getJobId(),
                "Creating a childJob[" + jobEntity.getId() + "], to download ["
                    + curr.getGoogleDocsResourceId() + "].");
            mapOfResourceToJob.put(resourceId, jobEntity.getId());
            mapOfPvs.put(pv, jobEntity.getId());
            break;

          default:
            logger.severe("Ignoring Unsupported ModuleType : " + curr.getResourceId());
            break;
        }
      } 
      
      FutureValue<LightJobContextPojo> completeMarker1 = null;
      FutureValue<LightJobContextPojo> completeMarkerS = null;
      
      int counter = 1;
      
      ArrayList<PromisedValue<Boolean>> newlist = Lists.newArrayList(mapOfPvs.keySet());
      for (int i = 0; i < newlist.size() - 1; i++) {
        PromisedValue<Boolean> currPv = newlist.get(i);
        futureCall(new PipelineJobs.DummyJob(),
            immediate(getContext()), immediate("Completed Job[" + mapOfPvs.get(currPv) + "]."),
            waitFor(currPv));
      }

      
      FutureValue<LightJobContextPojo> completeMarker2 = futureCall(new PipelineJobs.DummyJob(),
          immediate(getContext()), immediate("all child jobs are completed."),
          waitFor(newlist.get(newlist.size() - 1)));

      System.out.println("Created following jobs : " + Iterables.toString(
          mapOfResourceToJob.keySet()));

      FutureValue<CollectionId> collectionIdFV =
          futureCall(new CreateCollectionJob(), immediate(getContext()),
              immediate(gdocFolderResourceId), immediate(mapOfResourceToJob), 
              waitFor(completeMarker2));

      FutureValue<LightJobContextPojo> completeMarker3 = futureCall(new PipelineJobs.DummyJob(),
          immediate(getContext()), immediate("collectionId Created"),
          waitFor(collectionIdFV));
      return completeMarker3;
    }
  }

  @SuppressWarnings("serial")
  public static class CreateCollectionJob extends
      LightJob3<CollectionId, GoogleDocResourceId, Map<GoogleDocResourceId, Long>> {

    @Override
    public Value<CollectionId> handler(GoogleDocResourceId gdocFolderResourceId,
        Map<GoogleDocResourceId, Long> map) {
      JobManager jobManager = getInstance(JobManager.class);
      
      for (Long currJobId : map.values()) {
        JobEntity jobEntity = jobManager.get(null, currJobId);
        if (jobEntity.getJobState() != JobState.COMPLETED_SUCCESSFULLY) {
          throw new RuntimeException("Waiting for jobs to complete."); 
        }
      }

      ModuleManager moduleManager = getInstance(ModuleManager.class);
      DocsServiceWrapper docsService = getInstance(DocsServiceWrapper.class);

      GoogleDocInfoDto folderInfo = docsService.getDocumentEntryWithAcl(gdocFolderResourceId);
      List<GoogleDocInfoDto> folderContents = docsService.getFolderContentAll(gdocFolderResourceId);

      CollectionTreeNode root = new CollectionTreeNode.Builder()
          .type(TreeNodeType.ROOT_NODE)
          .title(folderInfo.getTitle())
          .build();

      createLightCollection(root, folderContents, moduleManager, docsService);
      System.out.println(JsonUtils.toJson(root));

      // TODO(arjuns) : Update External Entity mapping.
      CollectionManager collectionManager = GuiceUtils.getInstance(CollectionManager.class);

      String originId =
          gdocFolderResourceId.getModuleType() + ":" + gdocFolderResourceId.getTypedResourceId();
      PersonId ownerId = getContext().getOwnerId();

      CollectionEntity collectionEntity = null;
      Objectify ofy = ObjectifyUtils.initiateTransaction();
      try {
        collectionEntity = collectionManager.reserveCollectionIdForOriginId(ofy, originId, ownerId);
        collectionManager.addCollectionVersionForGoogleDoc(ofy, collectionEntity, root);
        ObjectifyUtils.commitTransaction(ofy);
      } finally {
        if (ofy.getTxn().isActive()) {
          ObjectifyUtils.rollbackTransaction(ofy);
        }
      }

      checkNotNull(collectionEntity, "At this point collectionEntity should not be null");

      updateChangeLog(null, getContext().getJobId(),
          "Created collection : " + collectionEntity.getCollectionId());

      return immediate(collectionEntity.getCollectionId());
    }

    /**
     * @param listOfGoogleDocResources
     */
    private void createLightCollection(CollectionTreeNode parent,
        List<GoogleDocInfoDto> listOfGoogleDocResources,
        ModuleManager moduleManager, DocsServiceWrapper docsService) {

      for (GoogleDocInfoDto currResource : listOfGoogleDocResources) {
        switch (currResource.getModuleType()) {
          case GOOGLE_DOC:
            String originId = currResource.getModuleType() + ":" + currResource.getResourceId();
            ModuleId moduleId = moduleManager.findModuleIdByOriginId(null, originId);

            CollectionTreeNode node = new CollectionTreeNode.Builder()
                .moduleId(moduleId)
                .title(currResource.getTitle())
                .type(TreeNodeType.LEAF_NODE)
                .build();

            parent.addChildren(node);
            break;

          case GOOGLE_COLLECTION:
            CollectionTreeNode interMediate = new CollectionTreeNode.Builder()
                .type(TreeNodeType.INTERMEDIATE_NODE)
                .title(currResource.getTitle())
                .build();
            List<GoogleDocInfoDto> listOfChilds =
                docsService.getFolderContentAll(currResource.getGoogleDocsResourceId());
            createLightCollection(interMediate, listOfChilds, moduleManager, docsService);

            parent.addChildren(interMediate);
            break;

          default:
            logger.severe("Ignoring Unsupported ModuleType : " + currResource.getResourceId());
            break;
        }
      }
    }
  }
}