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

import org.codehaus.jackson.annotate.JsonTypeName;

import javax.xml.bind.annotation.XmlAccessType;

import javax.xml.bind.annotation.XmlAccessorType;

import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocInfoDto;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocResourceId;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
@JsonTypeName(value = "googleDocTree")
@XmlRootElement(name = "googleDocTree")
@XmlAccessorType(XmlAccessType.FIELD)
public class GoogleDocTree extends AbstractTreeNode<GoogleDocTree> {
  @XmlElement(name = "resourceId")
  @JsonProperty(value = "resourceId")
  private GoogleDocResourceId resourceId;
  
  @XmlElement(name = "resourceInfo")
  @JsonProperty(value = "resourceInfo")
  private GoogleDocInfoDto resourceInfo;


  public GoogleDocResourceId getGoogleDocResourceId() {
    return resourceId;
  }
  
  public GoogleDocInfoDto getResourceInfo() {
    return resourceInfo;
  }

  @Override
  public GoogleDocTree validate() {
    if (getNodeType() != TreeNodeType.ROOT_NODE) {
      checkNotNull(resourceId, "resourceId");
      checkNotNull(resourceInfo, "resourceId");
    }
    
    return this;
  }

  public static class Builder extends AbstractTreeNode.Builder<GoogleDocTree, Builder> {
    private GoogleDocResourceId resourceId;
    private GoogleDocInfoDto resourceInfo;

    public Builder resourceId(GoogleDocResourceId resourceId) {
      this.resourceId = resourceId;
      return this;
    }
    
    public Builder resourceInfo(GoogleDocInfoDto resourceInfo) {
      this.resourceInfo = resourceInfo;
      return this;
    }

    @SuppressWarnings("synthetic-access")
    public GoogleDocTree build() {
      return new GoogleDocTree(this).validate();
    }
  }

  @SuppressWarnings("synthetic-access")
  private GoogleDocTree(Builder builder) {
    super(builder);
    this.resourceId = builder.resourceId;
    this.resourceInfo = builder.resourceInfo;
  }

  // For Objectify and GAEPi
  private GoogleDocTree() {
    super(null);
  }
}
