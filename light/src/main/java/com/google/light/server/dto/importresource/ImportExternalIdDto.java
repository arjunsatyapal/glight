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
package com.google.light.server.dto.importresource;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.LightPreconditions.checkNotEmptyCollection;
import static com.google.light.server.utils.LightPreconditions.checkNull;

import com.google.light.server.constants.LightConstants;

import com.google.light.server.dto.AbstractDto;
import com.google.light.server.dto.module.ModuleState;
import com.google.light.server.dto.module.ModuleStateCategory;
import com.google.light.server.dto.module.ModuleType;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.JobId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.ModuleId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.Version;
import com.google.light.server.dto.pojo.typewrapper.stringwrapper.ExternalId;
import com.google.light.server.dto.thirdparty.google.youtube.ContentLicense;
import com.google.light.server.persistence.entity.jobs.JobEntity;
import com.google.light.server.persistence.entity.jobs.JobState;
import com.google.light.server.persistence.entity.jobs.JobStateCategory;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
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
@JsonTypeName(value = "importExternalId")
@XmlRootElement(name = "importExternalId")
@XmlAccessorType(XmlAccessType.FIELD)
public class ImportExternalIdDto extends AbstractDto<ImportExternalIdDto> {
  @XmlElement(name = "externalId")
  @JsonProperty(value = "externalId")
  private ExternalId externalId;

  @XmlElement(name = "moduleId")
  @JsonProperty(value = "moduleId")
  private ModuleId moduleId;

  @XmlElement(name = "version")
  @JsonProperty(value = "version")
  private Version version;

  @XmlElement(name = "moduleState")
  @JsonProperty(value = "moduleState")
  private ModuleState moduleState;

  @XmlElement(name = "moduleStateCategory")
  @JsonProperty(value = "moduleStateCategory")
  private ModuleStateCategory moduleStateCategory;

  @XmlElement(name = "moduleType")
  @JsonProperty(value = "moduleType")
  private ModuleType moduleType;

  @XmlElementWrapper(name = "contentLicenses")
  @XmlAnyElement
  @JsonProperty(value = "contentLicenses")
  private List<ContentLicense> contentLicenses;

  @XmlElement(name = "title")
  @JsonProperty(value = "title")
  private String title;

  @XmlElement(name = "jobId")
  @JsonProperty(value = "jobId")
  private JobId jobId;

  @XmlElement(name = "jobState")
  @JsonProperty(value = "jobState")
  private JobState jobState;

  @XmlElement(name = "jobStateCategory")
  @JsonProperty(value = "jobStateCategory")
  private JobStateCategory jobStateCategory;

  public ExternalId getExternalId() {
    return externalId;
  }

  public ModuleId getModuleId() {
    return moduleId;
  }

  public ModuleState getModuleState() {
    return moduleState;
  }
  
  public void setModuleState(ModuleState moduleState) {
    this.moduleState = checkNotNull(moduleState, "moduleState");
    this.moduleStateCategory = moduleState.getCategory();
  }
  
  public ModuleStateCategory getModuleStateCategory() {
    return moduleStateCategory;
  }
  
  /**
   * This method is only for documentation.
   * @deprecated Use setModuleState.
   * @param moduleStateCategory
   */
  @SuppressWarnings("unused")
  @Deprecated 
  public void setModuleStateCategory(ModuleStateCategory moduleStateCategory) {
    throw new UnsupportedOperationException("Use setModuleState.");
  }

  public ModuleType getModuleType() {
    return moduleType;
  }

  public void setModuleType(ModuleType moduleType) {
    this.moduleType = checkNotNull(moduleType, "moduleType");
  }

  public List<ContentLicense> getContentLicenses() {
    return contentLicenses;
  }

  public void setContentLicenses(List<ContentLicense> contentLicenses) {
    this.contentLicenses = contentLicenses;
  }

  public void setModuleId(ModuleId moduleId) {
    this.moduleId = checkNotNull(moduleId, "moduleId");
  }

  public Version getVersion() {
    return version;
  }

  public void setVersion(Version version) {
    this.version = checkNotNull(version, "version");
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = checkNotBlank(title, "title");
  }

  public JobId getJobId() {
    return jobId;
  }
  
  public boolean hasJobId() {
    return getJobId() != null;
  }
  
  public boolean needsNewJobId() {
    boolean alreadyComplete = 
        this.getModuleState() == ModuleState.FAILED
        || this.getModuleState() == ModuleState.PUBLISHED
        || this.hasJobId();
    
    return !alreadyComplete;
  }

  public JobState getJobState() {
    return jobState;
  }
  
  public JobStateCategory getJobStateCategory() {
    return jobStateCategory;
  }

  public void setJobDetails(JobEntity jobEntity) {
    checkNotNull(jobEntity, "jobEntity");
    this.jobId = jobEntity.getJobId();
    this.jobState = jobEntity.getJobState();
    this.jobStateCategory = jobState.getCategory();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ImportExternalIdDto validate() {
    return this;
  }
  
  public static void doValidationForList(List<ImportExternalIdDto> list, boolean isRequest) {

    if (isRequest) {
      // Request size is limited. Response can be bigger.
      checkArgument(list.size() <= LightConstants.IMPORT_BATCH_SIZE_MAX);
    }
    
    for (ImportExternalIdDto curr : list) {
      if (isRequest) {
        curr.requestValidation();
      } else {
        curr.responseValidation();
      }
    }
  }

  /**
   * This method is used to validate the client Request.
   * 
   * @return
   */
  public ImportExternalIdDto requestValidation() {
    return doValidation(true);
  }

  /**
   * This method is used to validate the client Request.
   * 
   * @return
   */
  public ImportExternalIdDto responseValidation() {
    return doValidation(false);
  }

  /**
   * @param b
   */
  private ImportExternalIdDto doValidation(boolean isRequest) {
    validate();
    checkNotNull(externalId, "externalId");
    
    if (isRequest) {
      checkNull(moduleState, "moduleState should be null.");
      checkNull(moduleStateCategory, "moduleStateCategory should be null.");
      checkNull(version, "version should be null.");
      checkNull(jobId, "jobId should be null.");
      checkNull(jobState, "jobState should be null.");
    } else {
      checkNotNull(moduleState, "moduleState");
      checkNotNull(moduleStateCategory, "moduleStateCategory");
      
      if (moduleStateCategory == ModuleStateCategory.HOSTED) {
        checkNotNull(moduleType, "moduleType");
        checkNotEmptyCollection(contentLicenses, "contentLicenses");
      }
      
      
//      if (moduleType != null && moduleType.mapsToCollection()) {
//        checkNotNull(jobId, "jobId");
//        checkNotNull(jobState, "jobState");
//        return this;
//      }

//      switch (moduleState) {
//        case FAILED:
//        case NOT_SUPPORTED:
//          checkNull(version, "version should be null.");
//          //$FALL-THROUGH$
//
//        case PUBLISHED:
//          checkNull(jobId, "jobId should be null.");
//          checkNull(jobState, "jobState should be null.");
//
//          break;
//
//        case IMPORTING:
////        case UNKNOWN:
//        case REFRESHING:
//          checkNotNull(version, "version");
//          checkNotNull(jobId, "jobId");
//          checkNotNull(jobState, "jobState");
//          break;
//
//        case RESERVED:
//          throw new IllegalStateException("Server is not expected to return "
//              + moduleState + " immediately.");
//      }
    }
    
    return this;
  }

  public static class Builder extends AbstractDto.BaseBuilder<Builder> {
    private ExternalId externalId;
    private ModuleId moduleId;
    private ModuleState moduleState;
    private ModuleType moduleType;
    private List<ContentLicense> contentLicenses;
    private String title;
    private Version version;
    private JobId jobId;
    private JobState jobState;

    public Builder contentLicenses(List<ContentLicense> contentLicenses) {
      this.contentLicenses = contentLicenses;
      return this;
    }

    
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
    
    public Builder moduleState(ModuleState moduleState) {
      this.moduleState = moduleState;
      return this;
    }
    
    public Builder moduleType(ModuleType moduleType) {
      this.moduleType = moduleType;
      return this;
    }

    public Builder title(String title) {
      this.title = title;
      return this;
    }

    public Builder jobId(JobId jobId) {
      this.jobId = jobId;
      return this;
    }

    public Builder jobState(JobState jobState) {
      this.jobState = jobState;
      return this;
    }

    @SuppressWarnings("synthetic-access")
    public ImportExternalIdDto build1() {
      return new ImportExternalIdDto(this).validate();
    }
  }

  @SuppressWarnings("synthetic-access")
  private ImportExternalIdDto(Builder builder) {
    super(builder);
    this.externalId = builder.externalId;
    this.moduleId = builder.moduleId;
    this.version = builder.version;
    
    if (builder.moduleState != null) {
      setModuleState(builder.moduleState);
    }
    
    this.moduleType = builder.moduleType;
    this.contentLicenses = builder.contentLicenses;
    this.title = builder.title;
    this.jobId = builder.jobId;
    this.jobState = builder.jobState;
  }

  // For JAXB.
  private ImportExternalIdDto() {
    super(null);
  }
}
