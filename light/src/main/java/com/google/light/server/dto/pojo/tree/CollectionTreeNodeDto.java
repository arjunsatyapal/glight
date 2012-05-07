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

import org.codehaus.jackson.annotate.JsonProperty;

import com.google.light.server.dto.pojo.longwrapper.ModuleId;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
@XmlRootElement(name = "collection_tree")
public class CollectionTreeNodeDto extends TreeNode<CollectionTreeNodeDto> {
  @XmlElement(name = "module_id")
  @JsonProperty(value = "module_id")
  private ModuleId moduleId;

  // TODO(arjuns): Add validation logic here.
  // @Override
  // public CollectionTreeNode validate() {
  // super.validate();
  //
  // if (getType() == TreeNodeType.LEAF_NODE) {
  // checkNotNull(moduleId, "moduleId");
  // }
  // return this;
  // }

  public ModuleId getModuleId() {
    return moduleId;
  }

  public static class Builder extends TreeNode.Builder<CollectionTreeNodeDto, Builder> {
    private ModuleId moduleId;

    public Builder moduleId(ModuleId moduleId) {
      this.moduleId = moduleId;
      return this;
    }

    @SuppressWarnings("synthetic-access")
    public CollectionTreeNodeDto build() {
      return new CollectionTreeNodeDto(this).validate();
    }
  }

  @SuppressWarnings("synthetic-access")
  private CollectionTreeNodeDto(Builder builder) {
    super(builder);
    this.moduleId = builder.moduleId;
  }

  // For Objectify and GAEPi
  private CollectionTreeNodeDto() {
    super(null);
  }
}
