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
package com.google.light.server.dto.pojo.tree.collection;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkModuleId;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;

import com.google.light.server.dto.module.ModuleType;
import com.google.light.server.dto.pojo.tree.AbstractTreeNode;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.ModuleId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.Version;
import com.google.light.server.dto.pojo.typewrapper.stringwrapper.ExternalId;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
@JsonTypeName(value = "collectionTree")
@XmlRootElement(name = "collectionTree")
@XmlAccessorType(XmlAccessType.FIELD)
public class CollectionTreeNodeDto extends AbstractTreeNode<CollectionTreeNodeDto> {
  @XmlElement(name = "moduleId")
  @JsonProperty(value = "moduleId")
  private ModuleId moduleId;
  
  @XmlElement(name = "version")
  @JsonProperty(value = "version")
  private Version version;
  
  @XmlElement(name = "moduleType")
  @JsonProperty(value = "moduleType")
  private ModuleType moduleType;

  @XmlElement(name = "externalId")
  @JsonProperty(value = "externalId")
  private ExternalId externalId;

  @Override
  public CollectionTreeNodeDto validate() {
    super.validate();

    switch (getNodeType()) {
      case LEAF_NODE:
        checkModuleId(getModuleId());
        checkNotNull(moduleType, "moduleType");
        checkNotNull(externalId, "externalId");
        break;
        
      default:
        // do nothing.
    }

    return this;
  }

  public boolean hasModuleId() {
    return moduleId != null;
  }
  
  public ModuleId getModuleId() {
    return moduleId;
  }

  public void setModuleId(ModuleId moduleId) {
    this.moduleId = moduleId;
  }
  
  public Version getVersion() {
    return version;
  }
  
  public void setVersion(Version version) {
    this.version = checkNotNull(version, "version");
  }

  public ModuleType getModuleType() {
    return moduleType;
  }

  public ExternalId getExternalId() {
    return externalId;
  }
  
  public boolean hasExternalId() {
    return externalId != null;
  }
  
  public void setTitle(String title) {
    this.title = checkNotBlank(title, "title");
  }

  public boolean containsFirstLevelChildWithExternalId(ExternalId externalId) {
    if (findFirstLevelChildByExternalId(externalId) != null) {
      return true;
    }
    
    return false;
  }
  
  public CollectionTreeNodeDto findFirstLevelChildByExternalId(ExternalId externalId) {
    if (!hasChildren()) {
      return null;
    }
    
    for (CollectionTreeNodeDto currChild : getChildren()) {
      if (currChild.hasExternalId() && currChild.getExternalId().equals(externalId)) {
        return currChild;
      }
    }

    return null;
  }
  
  public void importChildsFrom(CollectionTreeNodeDto source) {
    if (!source.hasChildren()) {
      return;
    }
    
    System.out.println(this.toJson());
    for (CollectionTreeNodeDto currChild : source.getChildren()) {
      if (currChild.isLeafNode()) {
        checkNotNull(currChild.getExternalId(), "leaf nodes must have externalId");
        if(containsFirstLevelChildWithExternalId(currChild.getExternalId())) {
          // continue as its already present.
          continue;
        } else {
          addChildren(currChild);
        }
      } else {
        // Intermediate node
        if (currChild.hasExternalId()) {
          if(containsFirstLevelChildWithExternalId(currChild.getExternalId())) {
            CollectionTreeNodeDto dest = findFirstLevelChildByExternalId(currChild.getExternalId());
            dest.importChildsFrom(currChild);
          } else {
            addChildren(currChild);
          }
        } else {
          // Source does not have this child. So add curr Chid to dest.
          addChildren(currChild);
        }
      }
    }
  }

  public static class Builder extends AbstractTreeNode.Builder<CollectionTreeNodeDto, Builder> {
    private ModuleId moduleId;
    private Version version;
    private ModuleType moduleType;
    private ExternalId externalId;

    public Builder moduleId(ModuleId moduleId) {
      this.moduleId = moduleId;
      return this;
    }
    
    public Builder version(Version version) {
      this.version = version;
      return this;
    }

    public Builder moduleType(ModuleType moduleType) {
      this.moduleType = moduleType;
      return this;
    }

    public Builder externalId(ExternalId externalId) {
      this.externalId = externalId;
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
    this.version = builder.version;
    this.moduleType = builder.moduleType;
    this.externalId = builder.externalId;
  }

  // For Objectify and GAEPi
  private CollectionTreeNodeDto() {
    super(null);
  }
}
