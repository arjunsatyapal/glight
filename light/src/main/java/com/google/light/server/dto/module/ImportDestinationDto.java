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
package com.google.light.server.dto.module;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.LightUtils.getWrapperValue;

import com.google.light.server.dto.AbstractDto;
import com.google.light.server.dto.pojo.longwrapper.JobId;
import com.google.light.server.dto.pojo.longwrapper.ModuleId;
import com.google.light.server.dto.pojo.longwrapper.Version;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * This DTO is used to wrap information about an Import operation.
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
@JsonTypeName(value = "importDestination")
@XmlRootElement(name = "importDestination")
@XmlAccessorType(XmlAccessType.FIELD)
public class ImportDestinationDto extends AbstractDto<ImportDestinationDto> {
  @XmlElement(name = "externalId")
  @JsonProperty(value = "externalId")
  private String externalId;

  @XmlElement(name = "moduleType")
  @JsonProperty(value = "moduleType")
  private ModuleType moduleType;

  @XmlElement(name = "moduleId")
  @JsonProperty(value = "moduleId")
  private Long moduleId;

  @XmlElement(name = "version")
  @JsonProperty(value = "version")
  private Long version;

  @XmlElement(name = "jobId")
  @JsonProperty(value = "jobId")
  private Long jobId;

  /**
   * {@inheritDoc}
   */
  @Override
  public ImportDestinationDto validate() {
    checkNotBlank(externalId, "externalId");
    checkNotNull(moduleType, "moduleType");
    checkNotNull(moduleId, "moduleId");
    checkNotNull(version, "version");
    checkNotNull(jobId, "jobId");

    return this;
  }

  public String getExternalId() {
    return externalId;
  }

  public ModuleType getModuleType() {
    return moduleType;
  }

  public ModuleId getModuleId() {
    return new ModuleId(moduleId);
  }

  public Version getVersion() {
    return new Version(version);
  }

  public JobId getJobId() {
    return new JobId(jobId);
  }

  public static class Builder extends AbstractDto.BaseBuilder<Builder> {
    private String externalId;
    private ModuleType moduleType;
    private ModuleId moduleId;
    private Version version;
    private JobId jobId;

    public Builder externalId(String externalId) {
      this.externalId = externalId;
      return this;
    }

    public Builder moduleType(ModuleType moduleType) {
      this.moduleType = moduleType;
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

    public Builder jobId(JobId jobId) {
      this.jobId = jobId;
      return this;
    }

    @SuppressWarnings("synthetic-access")
    public ImportDestinationDto build() {
      return new ImportDestinationDto(this);
    }
  }

  @SuppressWarnings("synthetic-access")
  private ImportDestinationDto(Builder builder) {
    super(builder);
    this.externalId = builder.externalId;
    this.moduleType = builder.moduleType;
    this.moduleId = getWrapperValue(builder.moduleId);
    this.version = getWrapperValue(builder.version);
    this.jobId = getWrapperValue(builder.jobId);
  }

  // For Jaxb.
  private ImportDestinationDto() {
    super(null);
  }
}
