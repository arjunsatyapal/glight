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
package com.google.light.server.dto.thirdparty.google.gdoc;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.LightUtils.decodeFromUrlEncodedString;

import com.google.light.server.urls.GoogleDocUrl;


import com.google.light.server.dto.pojo.typewrapper.stringwrapper.ExternalId;

import com.google.light.server.dto.AbstractDto;
import com.google.light.server.dto.module.ModuleType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * Creating a type for GoogleDoc ResourceId.
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
@JsonTypeName(value = "googleDocResourceId")
@XmlRootElement(name = "googleDocResourceId")
@XmlAccessorType(XmlAccessType.FIELD)
public class GoogleDocResourceId extends AbstractDto<GoogleDocResourceId> {
  @XmlElement(name = "moduleType")
  @JsonProperty(value = "moduleType")
  private ModuleType moduleType;

  @XmlElement(name = "typedResourceId")
  @JsonProperty(value = "typedResourceId")
  private String typedResourceId;

  @XmlElement(name = "externalId")
  @JsonProperty(value = "externalId")
  private ExternalId externalId;

  // public GoogleDocResourceId(ModuleType moduleType, String key) {
  // this(moduleType.getCategory() + ":" + key);
  // }

  public GoogleDocResourceId(ExternalId externalId) {
    super(null);
    this.externalId = checkNotNull(externalId, "externalId");
    checkArgument(externalId.isGoogleDocResource(), "Invalid GoogleDocResource : " + externalId);
    GoogleDocUrl gdocUrl = externalId.getGoogleDocURL();

    this.moduleType = gdocUrl.getModuleType();
    this.typedResourceId = checkNotBlank(gdocUrl.getTypedResourceId(), 
        "Invalid GoogleDocResource : " + externalId);

    String decodedString = decodeFromUrlEncodedString(typedResourceId);
    String errMsg = "invalid typedResourceId[" + typedResourceId + "].";
    checkArgument(decodedString.contains(":"), errMsg);

    String[] parts = decodedString.split(":");
    checkArgument(parts.length == 2, errMsg);

    validate();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public GoogleDocResourceId validate() {
    checkNotBlank(typedResourceId, "typedResourceId");
    checkNotNull(moduleType, "moduleType");

    return this;
  }

  @Override
  public String toString() {
    return getTypedResourceId();
  }

  public String getTypedResourceId() {
    return typedResourceId;
  }

  public String getTypeResourceId() {
    return typedResourceId;
  }

  public ExternalId getExternalId() {
    return externalId;
  }

  public ModuleType getModuleType() {
    return moduleType;
  }

  // For JAXB and Pipeline and Guice.
  private GoogleDocResourceId() {
    super(null);
  }
}
