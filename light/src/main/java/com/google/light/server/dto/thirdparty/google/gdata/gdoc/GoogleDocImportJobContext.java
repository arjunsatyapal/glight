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
package com.google.light.server.dto.thirdparty.google.gdata.gdoc;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;

import com.google.light.server.dto.AbstractDto;
import com.google.light.server.dto.pojo.longwrapper.ModuleId;
import com.google.light.server.dto.pojo.longwrapper.Version;
import com.google.light.server.exception.ExceptionType;
import com.google.light.server.utils.LightPreconditions;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class GoogleDocImportJobContext extends AbstractDto<GoogleDocImportJobContext> {
  private GoogleDocInfoDto resourceInfo;
  private ModuleId moduleId;
  private Version version;
  private String archiveId;
  private String gcsArchiveLocation;
  private State state;

  /**
   * {@inheritDoc}
   */
  @Override
  public GoogleDocImportJobContext validate() {
    checkNotNull(resourceInfo, "resourceInfo");
    resourceInfo.validate();

    checkNotNull(moduleId, "moduleId");
    moduleId.validate();

    checkNotNull(version, "version");
    version.validate();

    checkNotNull(state, "state");

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

  public ModuleId getModuleId() {
    return moduleId;
  }

  public Version getVersion() {
    return version;
  }

  public State getState() {
    return state;
  }

  public void setState(State state) {
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
    private ModuleId moduleId;
    private Version version;
    public String archiveId;
    public String gcsArchiveLocation;
    private State state;

    public Builder resourceInfo(GoogleDocInfoDto resourceInfo) {
      this.resourceInfo = resourceInfo;
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

    public Builder state(State state) {
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
    public GoogleDocImportJobContext build() {
      return new GoogleDocImportJobContext(this).validate();
    }
  }

  @SuppressWarnings("synthetic-access")
  private GoogleDocImportJobContext(Builder builder) {
    super(builder);
    this.resourceInfo = builder.resourceInfo;
    this.moduleId = builder.moduleId;
    this.version = builder.version;
    this.state = builder.state;
    this.archiveId = builder.archiveId;
    this.gcsArchiveLocation = builder.gcsArchiveLocation;
  }

  // For Jaxb.
  private GoogleDocImportJobContext() {
    super(null);
  }

  public static enum State {
    ENQUEUED,
    WAITING_FOR_ARCHIVE,
    ARCHIVE_DOWNLOADED,
    COMPLETE;
  }
}
