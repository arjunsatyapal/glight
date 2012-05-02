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
package com.google.light.server.dto.pojo.tree;

import javax.xml.bind.annotation.XmlElement;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.light.server.dto.pojo.longwrapper.ModuleId;

/**
 *
 * 
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
@XmlRootElement(name = "collection_tree")
@XmlAccessorType(XmlAccessType.FIELD)
public class CollectionTreeNode extends TreeNode<CollectionTreeNode> {
  @XmlElement private ModuleId moduleId;
  
//  @Override
//  public CollectionTreeNode validate() {
//    super.validate();
//    
//    if (getType() == TreeNodeType.LEAF_NODE) {
//      checkNotNull(moduleId, "moduleId");
//    }
//    return this;
//  }
  
  public ModuleId getModuleId() {
    return moduleId;
  }
  
  public static class Builder extends TreeNode.Builder<CollectionTreeNode, Builder> {
    private ModuleId moduleId;
    
    public Builder moduleId(ModuleId moduleId) {
      this.moduleId = moduleId;
      return this;
    }
    
    @SuppressWarnings("synthetic-access")
    public CollectionTreeNode build() {
      return new CollectionTreeNode(this).validate();
    }
  }
  
  @SuppressWarnings("synthetic-access")
  private CollectionTreeNode(Builder builder) {
    super(builder);
    this.moduleId = builder.moduleId;
  }
  
  // For Objectify and GAEPi
  private CollectionTreeNode() {
    super(null);
  }
}
