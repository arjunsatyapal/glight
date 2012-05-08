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
import static com.google.light.server.utils.LightPreconditions.checkNonEmptyList;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;

import com.google.light.server.dto.pojo.longwrapper.ModuleId;

import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;

import javax.xml.bind.annotation.XmlAccessorType;

import com.google.light.server.dto.AbstractDtoToPersistence;
import com.google.light.server.dto.pojo.longwrapper.PersonId;
import com.google.light.server.dto.pojo.longwrapper.Version;
import com.google.light.server.persistence.entity.module.ModuleEntity;
import com.google.light.server.utils.LightPreconditions;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.codehaus.jackson.annotate.JsonCreator;

/**
 * DTO for Light Modules.
 * 
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
@XmlRootElement(name="module")
@XmlAccessorType(XmlAccessType.FIELD)
public class ModuleDto extends AbstractDtoToPersistence<ModuleDto, ModuleEntity, Long> {
  @XmlElement(name = "id")
  @JsonProperty(value = "id")
  private ModuleId id;
  
  @XmlElement(name = "title")
  @JsonProperty(value = "title")
  private String title;
  
  @XmlElement(name = "state")
  @JsonProperty(value = "state")
  private ModuleState state;
  
  @XmlElement(name = "owners")
  @JsonProperty(value = "owners")
  private List<PersonId> owners;

  @XmlElement(name = "version")
  @JsonProperty(value = "version")
  private Version version;

  /** 
   * {@inheritDoc}
   */
  @Override
  public ModuleDto validate() {
    checkNotBlank(title, "title");
    checkNotNull(state, "moduleState");
    checkNonEmptyList(owners, "owner list cannot be empty");
    // Version can be zero when module is in process of import.
    checkNotNull(version, "version");
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
    private ModuleId id;
    private String title;
    private ModuleState state;
    private List<PersonId> owners;
    private Version version;
    
    public Builder id(ModuleId id) {
      this.id = id;
      return this;
    }

    public Builder title(String title) {
      this.title = title;
      return this;
    }
    
    public Builder state(ModuleState state) {
      this.state = state;
      return this;
    }
    
    public Builder owners(List<PersonId> owners) {
      this.owners = owners;
      return this;
    }
    
    public Builder latestVersion(Version version) {
      this.version = version;
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
    this.id = builder.id;
    this.title = builder.title;
    this.state = builder.state;
    this.owners = builder.owners;
    this.version = builder.version;
  }
  
  @JsonCreator
  private ModuleDto() {
    super(null);
  }
}
