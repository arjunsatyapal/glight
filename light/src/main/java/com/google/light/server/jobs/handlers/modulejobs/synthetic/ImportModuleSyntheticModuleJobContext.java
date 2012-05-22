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
package com.google.light.server.jobs.handlers.modulejobs.synthetic;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkModuleId;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.LightPreconditions.checkVersion;

import com.google.light.server.dto.module.ModuleType;

import com.google.light.server.dto.AbstractDto;
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
@JsonTypeName(value = "importModuleSyntheticModuleJobContext")
@XmlRootElement(name = "importModuleSyntheticModuleJobContext")
@XmlAccessorType(XmlAccessType.FIELD)
public class ImportModuleSyntheticModuleJobContext extends AbstractDto<ImportModuleSyntheticModuleJobContext> {
  @XmlElement(name = "externalId")
  @JsonProperty(value = "externalId")
  private ExternalId externalId;
  
  @XmlElement(name = "title")
  @JsonProperty(value = "title")
  private String title;

  @XmlElement(name = "moduleId")
  @JsonProperty(value = "moduleId")
  private ModuleId moduleId;

  @XmlElement(name = "version")
  @JsonProperty(value = "version")
  private Version version;

  // At present hard coding as there is no other synthetic module. 
  @XmlElement(name = "moduleType")
  @JsonProperty(value = "moduleType")
  private ModuleType moduleType = ModuleType.LIGHT_SYNTHETIC_MODULE;



  /**
   * {@inheritDoc}
   */
  @Override
  public ImportModuleSyntheticModuleJobContext validate() {
    checkNotBlank(title, "title");
    checkNotNull(externalId, "externalId");
    externalId.validate();

    checkModuleId(getModuleId());
    checkVersion(getVersion());
    
    return this;
  }

  public ExternalId getExternalId() {
    return externalId;
  }

  public ModuleId getModuleId() {
    return moduleId;
  }

  public Version getVersion() {
    return version;
  }
  
  public ModuleType getModuleType() {
    return moduleType;
  }
  
  public String getTitle() {
    return title;
  }

  public static class Builder extends AbstractDto.BaseBuilder<Builder>{
    private ExternalId externalId;
    private ModuleId moduleId;
    private Version version;
    private String title;

    public Builder externalId(ExternalId externalId) {
      this.externalId = externalId;
      return this;
    }

    public Builder moduleId(ModuleId moduleId) {
      this.moduleId = moduleId;
      return this;
    }

    public Builder version(Version version) {
      this.version = version;
      return this;
    }
    
    public Builder title(String title) {
      this.title = title;
      return this;
    }

    @SuppressWarnings("synthetic-access")
    public ImportModuleSyntheticModuleJobContext build() {
      return new ImportModuleSyntheticModuleJobContext(this);
    }
  }

  @SuppressWarnings("synthetic-access")
  private ImportModuleSyntheticModuleJobContext(Builder builder) {
    super(builder);
    this.externalId = builder.externalId;
    this.moduleId = builder.moduleId;
    this.version = builder.version;
    this.title = builder.title;
  }
  
  private ImportModuleSyntheticModuleJobContext() {
    super(null);
  }
}
