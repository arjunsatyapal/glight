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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkModuleId;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;

import com.google.light.server.dto.module.ModuleType;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.ModuleId;
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

  @XmlElement(name = "moduleType")
  @JsonProperty(value = "moduleType")
  private ModuleType moduleType;

  @XmlElement(name = "externalId")
  @JsonProperty(value = "externalId")
  private String externalId;

  @Override
  public CollectionTreeNodeDto validate() {
    super.validate();

    switch (getType()) {
      case LEAF_NODE:
        checkModuleId(getModuleId());
        break;

      default:
        // do nothing.
    }

    checkNotNull(moduleType, "moduleType");
    checkNotBlank(externalId, "externalId");

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

  public ModuleType getModuleType() {
    return moduleType;
  }

  public String getExternalId() {
    return externalId;
  }

  public boolean containsModuleIdAsChildren(ModuleId moduleId) {
    return findChildWithModuleId(moduleId) != null;
  }

  public CollectionTreeNodeDto findChildWithModuleId(ModuleId moduleId) {
    switch (getType()) {
      case LEAF_NODE:
        break;

      case ROOT_NODE:
      case INTERMEDIATE_NODE:
        for (CollectionTreeNodeDto currChild : getChildren()) {
          if (currChild.getModuleId().equals(moduleId)) {
            return currChild;
          }
        }

        break;
      default:
        throw new IllegalStateException("Unsupported type : " + getType());
    }

    return null;
  }

  public boolean containsExternalIdAsChildren(String externalId) {
    return findChildByExternalId(externalId) != null;
  }

  public CollectionTreeNodeDto findChildByExternalId(String externalId) {
    switch (getType()) {
      case LEAF_NODE:
        break;

      case ROOT_NODE:
      case INTERMEDIATE_NODE:
        for (CollectionTreeNodeDto currChild : getChildren()) {
          if (currChild.getExternalId().equals(externalId)) {
            return currChild;
          }
        }
        break;

      default:
        throw new IllegalStateException("Unsupported type : " + getType());
    }

    return null;
  }

  public static class Builder extends AbstractTreeNode.Builder<CollectionTreeNodeDto, Builder> {
    private ModuleId moduleId;
    private ModuleType moduleType;
    private String externalId;

    public Builder moduleId(ModuleId moduleId) {
      this.moduleId = moduleId;
      return this;
    }

    public Builder moduleType(ModuleType moduleType) {
      this.moduleType = moduleType;
      return this;
    }

    public Builder externalId(String externalId) {
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
    this.moduleType = builder.moduleType;
    this.externalId = builder.externalId;
  }

  // For Objectify and GAEPi
  private CollectionTreeNodeDto() {
    super(null);
  }
}
