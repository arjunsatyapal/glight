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
package com.google.light.server.jobs.handlers.modulejobs.gdocument;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkModuleId;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.LightPreconditions.checkVersion;

import com.google.light.server.dto.thirdparty.google.gdoc.GoogleDocInfoDto;


import com.google.light.server.dto.AbstractDto;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.ModuleId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.Version;
import com.google.light.server.exception.ExceptionType;
import com.google.light.server.utils.LightPreconditions;
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
@JsonTypeName(value = "googleDocImportJobContext")
@XmlRootElement(name = "googleDocImportJobContext")
@XmlAccessorType(XmlAccessType.FIELD)
public class ImportModuleGoogleDocJobContext extends AbstractDto<ImportModuleGoogleDocJobContext> {
  @XmlElement(name = "resourceInfo")
  @JsonProperty(value = "resourceInfo")
  private GoogleDocInfoDto resourceInfo;
  
  @XmlElement(name = "title")
  @JsonProperty(value = "title")
  private String title;
  
  
  @XmlElement(name = "moduleId")
  @JsonProperty(value = "moduleId")
  private ModuleId moduleId;
  
  @XmlElement(name = "version")
  @JsonProperty(value = "version")
  private Version version;
  
  @XmlElement(name = "archiveId")
  @JsonProperty(value = "archiveId")
  private String archiveId;
  
  @XmlElement(name = "gcsArchiveLocation")
  @JsonProperty(value = "gcsArchiveLocation")
  private String gcsArchiveLocation;
  
  @XmlElement(name = "state")
  @JsonProperty(value = "state")
  private GoogleDocImportJobState state;

  /**
   * {@inheritDoc}
   */
  @Override
  public ImportModuleGoogleDocJobContext validate() {
    checkNotNull(resourceInfo, "resourceInfo");
    resourceInfo.validate();

    checkModuleId(getModuleId());
    checkVersion(getVersion());
    checkNotNull(state, "state");
    checkNotBlank(title, "title");

    switch (state) {
      case ARCHIVE_DOWNLOADED:
        checkNotBlank(gcsArchiveLocation, ExceptionType.SERVER, "gcsArchiveLocation");
        //$FALL-THROUGH$
      case WAITING_FOR_ARCHIVE:
        checkNotBlank(archiveId, ExceptionType.SERVER, "archiveId");
        break;

      default:
        // TODO(arjuns): Add validation for other states.
    }

    return this;
  }

  public GoogleDocInfoDto getResourceInfo() {
    return resourceInfo;
  }

  public String getTitle() {
    return title;
  }
  
  public ModuleId getModuleId() {
    return moduleId;
  }

  public Version getVersion() {
    return version;
  }

  public GoogleDocImportJobState getState() {
    return state;
  }

  public void setState(GoogleDocImportJobState state) {
    this.state = LightPreconditions.checkNotNull(state, ExceptionType.SERVER, "state");
  }

  public String getArchiveId() {
    return archiveId;
  }
  
  public String getGCSArchiveLocation() {
    return gcsArchiveLocation;
  }
  
  public void setGCSArchiveLocation(String gcsArchiveLocation) {
    this.gcsArchiveLocation = checkNotBlank(gcsArchiveLocation, 
        ExceptionType.SERVER, "gcsArchiveLocation");
  }

  public void setArchiveId(String archiveId) {
    this.archiveId = LightPreconditions.checkNotBlank(archiveId, ExceptionType.SERVER, "archiveId");
  }

  public static class Builder extends AbstractDto.BaseBuilder<Builder> {
    private GoogleDocInfoDto resourceInfo;
    private String title;
    private ModuleId moduleId;
    private Version version;
    public String archiveId;
    public String gcsArchiveLocation;
    private GoogleDocImportJobState state;

    public Builder resourceInfo(GoogleDocInfoDto resourceInfo) {
      this.resourceInfo = resourceInfo;
      return this;
    }

    public Builder title(String title) {
      this.title = title;
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

    public Builder state(GoogleDocImportJobState state) {
      this.state = state;
      return this;
    }

    public Builder archiveId(String archiveId) {
      this.archiveId = archiveId;
      return this;
    }

    public Builder gcsArchiveLocation(String gcsArchiveLocation) {
      this.gcsArchiveLocation = gcsArchiveLocation;
      return this;
    }

    @SuppressWarnings("synthetic-access")
    public ImportModuleGoogleDocJobContext build() {
      return new ImportModuleGoogleDocJobContext(this).validate();
    }
  }

  @SuppressWarnings("synthetic-access")
  private ImportModuleGoogleDocJobContext(Builder builder) {
    super(builder);
    this.resourceInfo = builder.resourceInfo;
    this.title = builder.title;
    this.moduleId = builder.moduleId;
    this.version = builder.version;
    this.state = builder.state;
    this.archiveId = builder.archiveId;
    this.gcsArchiveLocation = builder.gcsArchiveLocation;
  }

  // For Jaxb.
  private ImportModuleGoogleDocJobContext() {
    super(null);
  }

  public static enum GoogleDocImportJobState {
    ENQUEUED,
    WAITING_FOR_ARCHIVE,
    ARCHIVE_DOWNLOADED,
    MODULE_CREATED,
    MODULE_VERSION_RESERVED,
    MODULE_VERSION_PUBLISHED,
    MODULE_INDEXED,
    COMPLETE;
  }
}
