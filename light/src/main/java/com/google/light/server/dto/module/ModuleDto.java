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
import static com.google.light.server.utils.LightPreconditions.checkPersonId;

import com.google.light.server.dto.pojo.longwrapper.Version;

import com.google.light.server.dto.pojo.longwrapper.PersonId;

import com.google.light.server.dto.AbstractDtoToPersistence;
import com.google.light.server.persistence.entity.module.ModuleEntity;
import org.codehaus.jackson.annotate.JsonCreator;

/**
 * DTO for Light Modules.
 * 
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class ModuleDto extends AbstractDtoToPersistence<ModuleDto, ModuleEntity, Long> {
  private String title;
  private ModuleState state;
  // PersonId for owner of this module.
  private PersonId ownerPersonId;
  private String etag;
  private Version latestVersion;

  /** 
   * {@inheritDoc}
   */
  @Override
  public ModuleDto validate() {
    checkNotBlank(title, "title");
    checkNotNull(state, "moduleState");
    checkPersonId(ownerPersonId);
    checkNotBlank(etag, "etag");
    // Version can be zero when module is in process of import.
    checkNotNull(latestVersion, "latestVersion");
    return this;
  }

  /** 
   * {@inheritDoc}
   */
  @Override
  public ModuleEntity toPersistenceEntity(Long id) {
    // Since there are many things that are required for Updating a module, so this method
    // is inappropriate.
    throw new UnsupportedOperationException();
  }

  public String getTitle() {
    return title;
  }

  public static class Builder extends AbstractDtoToPersistence.BaseBuilder<Builder> {
    private String title;
    private ModuleState state;
    private PersonId ownerPersonId;
    private String etag;
    private Version latestVersion;

    public Builder title(String title) {
      this.title = title;
      return this;
    }
    
    public Builder state(ModuleState state) {
      this.state = state;
      return this;
    }
    
    public Builder ownerPersonId(PersonId ownerPersonId) {
      this.ownerPersonId  = ownerPersonId;
      return this;
    }
    
    public Builder etag(String etag) {
      this.etag = etag;
      return this;
    }
    
    public Builder latestVersion(Version latestVersion) {
      this.latestVersion = latestVersion;
      return this;
    }

    @SuppressWarnings("synthetic-access")
    public ModuleDto build() {
      return new ModuleDto(this);
    }
  }

  @SuppressWarnings("synthetic-access")
  private ModuleDto(Builder builder) {
    super(builder);
    this.title = builder.title;
    this.state = builder.state;
    this.ownerPersonId = builder.ownerPersonId;
    this.etag = builder.etag;
    this.latestVersion = builder.latestVersion;
  }
  
  @JsonCreator
  private ModuleDto() {
    super(null);
  }
}
