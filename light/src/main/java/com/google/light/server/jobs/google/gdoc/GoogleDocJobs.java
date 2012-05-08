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

import static com.google.light.server.utils.LightPreconditions.checkNotNull;

import com.google.inject.Inject;

import com.google.inject.Provider;
import com.google.light.server.dto.pojo.tree.GoogleDocTree;
import com.google.light.server.dto.pojo.tree.AbstractTreeNode.TreeNodeType;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocInfoDto;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocResourceId;
import com.google.light.server.exception.ExceptionType;
import com.google.light.server.thirdparty.clients.google.gdata.gdoc.DocsServiceWrapper;
import java.util.List;

/**
 * Jobs related to Google Docs.
 * 
 * @author Arjun Satyapal
 */
public class GoogleDocJobs {
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
      parentNode.addChildren(getTreeNode(currChild));
    }
    
    return parentNode;
  }

  /**
   * @param currResource
   */
  private GoogleDocTree getTreeNode(GoogleDocInfoDto resourceInfo) {
    GoogleDocResourceId resourceId = resourceInfo.getGoogleDocsResourceId();
    
    GoogleDocTree.Builder nodeBuilder = new GoogleDocTree.Builder()
        .title(resourceInfo.getTitle())
        .resourceId(resourceId)
        .resourceInfo(resourceInfo);
    

    
    if (!resourceId.isFolder()) {
      nodeBuilder.type(TreeNodeType.LEAF_NODE);
      return nodeBuilder.build();
    } 
    
    // This is a folder. So adding child Tree into current node.
    nodeBuilder.type(TreeNodeType.INTERMEDIATE_NODE);
    List<GoogleDocInfoDto> listOfChilds = docsServiceProvider.get().getFolderContentAll(resourceId);
    
    return addChildTreeToParent(nodeBuilder.build(), listOfChilds);
  }
}
