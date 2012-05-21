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
package com.google.light.server.jobs.google.gdoc;

import static com.google.light.server.constants.LightConstants.GDATA_GDOC_MAX_RESULTS;
import static com.google.light.server.utils.LightPreconditions.checkNotNull;
import static com.google.light.server.utils.LightUtils.isCollectionEmpty;

import com.google.light.server.dto.thirdparty.google.gdoc.GoogleDocInfoDto;
import com.google.light.server.dto.thirdparty.google.gdoc.GoogleDocResourceId;

import com.google.light.server.thirdparty.clients.google.gdoc.DocsServiceWrapper;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.light.server.dto.pojo.tree.AbstractTreeNode.TreeNodeType;
import com.google.light.server.dto.pojo.tree.GoogleDocTree;
import com.google.light.server.exception.ExceptionType;
import java.util.List;
import java.util.logging.Logger;

/**
 * Jobs related to Google Docs.
 * 
 * @author Arjun Satyapal
 */
public class GoogleDocJobs {
  private static final Logger logger = Logger.getLogger(GoogleDocJobs.class.getName());
  
  private Provider<DocsServiceWrapper> docsServiceProvider;

  @Inject
  public GoogleDocJobs(Provider<DocsServiceWrapper> docsServiceProvider) {
    this.docsServiceProvider = checkNotNull(docsServiceProvider,
        ExceptionType.SERVER_GUICE_INJECTION, "docsServiceProvider cannot be null.");
  }

  public GoogleDocTree addChildTreeToParent(GoogleDocTree parentNode, 
      List<GoogleDocInfoDto> listOfChilds) {
    checkNotNull(parentNode, ExceptionType.SERVER, "parentNode cannot be null");

    for (GoogleDocInfoDto currChild : listOfChilds) {
      GoogleDocTree gdocTreeNode = getTreeNode(currChild);
      
      if (gdocTreeNode != null) {
        parentNode.addChildren(gdocTreeNode);
      }
    }
    
    return parentNode;
  }

  /**
   * @param currResource
   */
  private GoogleDocTree getTreeNode(GoogleDocInfoDto resourceInfo) {
    GoogleDocResourceId resourceId = resourceInfo.getGoogleDocResourceId();
    
    GoogleDocTree.Builder nodeBuilder = new GoogleDocTree.Builder()
        .title(resourceInfo.getTitle())
        .resourceId(resourceId)
        .resourceInfo(resourceInfo);

    if (!resourceId.getModuleType().isSupported()) {
      // Since this type is not supported, so ignoring.
      logger.info("Ignoring  " + resourceId);
      return null;
    }
    
    if (!resourceId.getModuleType().mapsToCollection()) {
      nodeBuilder.nodeType(TreeNodeType.LEAF_NODE);
      return nodeBuilder.build();
    } 
    
    // This is a folder. So adding child Tree into current node.
    nodeBuilder.nodeType(TreeNodeType.INTERMEDIATE_NODE);
    List<GoogleDocInfoDto> listOfChilds = 
        docsServiceProvider.get().getFolderContentWhichAreSupportedInAlphabeticalOrder(
            resourceId, GDATA_GDOC_MAX_RESULTS);

    List<GoogleDocInfoDto> listOfSupportedChilds = Lists.newArrayList();
    for (GoogleDocInfoDto currChild : listOfChilds) {
      if (currChild.getModuleType().isSupported()) {
        listOfSupportedChilds.add(currChild);
      }
    }
    
    
    if (isCollectionEmpty(listOfSupportedChilds)) {
      return null;
    }
    
    
    return addChildTreeToParent(nodeBuilder.build(), listOfSupportedChilds);
  }
}
