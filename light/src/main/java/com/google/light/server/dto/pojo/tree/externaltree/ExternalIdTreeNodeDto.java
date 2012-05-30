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
package com.google.light.server.dto.pojo.tree.externaltree;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkNotEmptyCollection;

import com.google.light.server.dto.pojo.tree.AbstractTreeNode;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.ModuleId;
import com.google.light.server.dto.pojo.typewrapper.stringwrapper.ExternalId;
import com.google.light.server.dto.thirdparty.google.youtube.ContentLicense;
import java.util.List;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class ExternalIdTreeNodeDto extends AbstractTreeNode<ExternalIdTreeNodeDto> {
  private ExternalId externalId;
  private ModuleId moduleId;
  private List<ContentLicense> contentLicenses;

  public ExternalId getExternalId() {
    return externalId;
  }

  public ModuleId getModuleId() {
    return moduleId;
  }

  public void setModuleId(ModuleId moduleId) {
    this.moduleId = checkNotNull(moduleId, "moduleId");
  }

  public List<ContentLicense> getContentLicenses() {
    return contentLicenses;
  }

  public ExternalIdTreeNodeDto findFirstLevelChildByExternalId(ExternalId externalId) {
    if (!hasChildren()) {
      return null;
    }

    for (ExternalIdTreeNodeDto currChild : getChildren()) {
      if (currChild.getExternalId().equals(externalId)) {
        return currChild;
      }
    }

    return null;
  }

  @Override
  public ExternalIdTreeNodeDto validate() {
    super.validate();

    switch (getNodeType()) {
      case ROOT_NODE:
      case INTERMEDIATE_NODE:
        if (externalId != null) {
          checkArgument(externalId.getModuleType().mapsToCollection(),
              "Expected non-moduleType but found " + externalId);
        }
        break;

      case LEAF_NODE:
        checkNotNull(externalId, "externalId");
        checkNotEmptyCollection(contentLicenses, "contentLicenses");
        checkArgument(externalId.getModuleType().mapsToModule(),
            "Expected moduleType but found " + externalId);
        break;

      default:
        throw new IllegalStateException("Invalid nodeType : " + getNodeType());
    }

    return this;
  }

  public static class Builder extends AbstractTreeNode.Builder<ExternalIdTreeNodeDto, Builder> {
    private ExternalId externalId;
    private ModuleId moduleId;
    private List<ContentLicense> contentLicenses;

    public Builder externalId(ExternalId externalId) {
      this.externalId = externalId;
      return this;
    }

    public Builder moduleId(ModuleId moduleId) {
      this.moduleId = moduleId;
      return this;
    }

    public Builder contentLicenses(List<ContentLicense> contentLicenses) {
      this.contentLicenses = contentLicenses;
      return this;
    }

    @SuppressWarnings("synthetic-access")
    public ExternalIdTreeNodeDto build() {
      return new ExternalIdTreeNodeDto(this).validate();
    }
  }

  @SuppressWarnings("synthetic-access")
  private ExternalIdTreeNodeDto(Builder builder) {
    super(builder);
    this.externalId = builder.externalId;
    this.moduleId = builder.moduleId;
    this.contentLicenses = builder.contentLicenses;
  }

  // For JAXB.
  private ExternalIdTreeNodeDto() {
    super(null);
  }
}
