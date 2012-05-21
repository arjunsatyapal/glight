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
import static com.google.light.server.utils.LightPreconditions.checkNotEmptyCollection;

import com.google.light.server.dto.AbstractDtoToPersistence;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.ModuleId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.PersonId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.Version;
import com.google.light.server.dto.pojo.typewrapper.stringwrapper.ExternalId;
import com.google.light.server.dto.thirdparty.google.youtube.ContentLicense;
import com.google.light.server.persistence.entity.module.ModuleEntity;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * DTO for Light Modules.
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
@JsonTypeName(value = "module")
@XmlRootElement(name = "module")
@XmlAccessorType(XmlAccessType.FIELD)
public class ModuleDto extends AbstractDtoToPersistence<ModuleDto, ModuleEntity, ModuleId> {
  @XmlElement(name = "moduleId")
  @JsonProperty(value = "moduleId")
  private ModuleId moduleId;

  @XmlElement(name = "title")
  @JsonProperty(value = "title")
  private String title;

  @XmlElement(name = "moduleState")
  @JsonProperty(value = "moduleState")
  private ModuleState moduleState;

  @XmlElement(name = "moduleType")
  @JsonProperty(value = "moduleType")
  private ModuleType moduleType;

  @XmlElementWrapper(name = "owners")
  @XmlElement(name = "personId")
  @JsonProperty(value = "owners")
  private List<PersonId> owners;
  
  @XmlElementWrapper(name = "contentLicenses")
  @XmlAnyElement
  @JsonProperty(value = "contentLicenses")
  private List<ContentLicense> contentLicenses;

  @XmlElement(name = "latestPublishVersion")
  @JsonProperty(value = "latestPublishVersion")
  private Version latestPublishVersion;
  
  @XmlElement(name = "nextVersion")
  @JsonProperty(value = "nextVersion")
  private Version nextVersion;
  
  @XmlElement(name = "externalId")
  @JsonProperty(value = "externalId")
  private ExternalId externalId;

  /**
   * {@inheritDoc}
   */
  @Override
  public ModuleDto validate() {
    checkNotBlank(title, "title");
    checkNotNull(moduleState, "moduleState");
    checkNotNull(moduleType, "moduleType");
    checkNotEmptyCollection(owners, "owner list cannot be empty");
    checkNotEmptyCollection(contentLicenses, "contentLicenses list cannot be empty");
    
    // Version can be zero when module is in process of import.
    checkNotNull(latestPublishVersion, "version");
    checkNotNull(externalId, "externalId");
    return this;
  }

  public ModuleId getModuleId() {
    return moduleId;
  }

  public ModuleType getModuleType() {
    return moduleType;
  }
  
  public ModuleState getModuleState() {
    return moduleState;
  }

  public List<PersonId> getOwners() {
//    return convertListOfValuesToWrapperList(owners, PersonId.class);
    return owners;
  }

  public List<ContentLicense> getContentLicenses() {
    return contentLicenses;
  }

  public Version getLatestPublishVersion() {
    return latestPublishVersion;
  }
  
  public Version getNextVersion() {
    return nextVersion;
  }
  
  public ExternalId getExternalId() {
    return externalId;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ModuleEntity toPersistenceEntity(ModuleId id) {
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
    private ModuleType moduleType;
    private ModuleState moduleState;
    private List<PersonId> owners;
    private List<ContentLicense> contentLicenses;
    private Version latestPublishVersion;
    private Version nextVersion;
    private ExternalId externalId;

    public Builder id(ModuleId id) {
      this.id = id;
      return this;
    }

    public Builder title(String title) {
      this.title = title;
      return this;
    }

    public Builder moduleType(ModuleType moduleType) {
      this.moduleType = moduleType;
      return this;
    }
    
    public Builder moduleState(ModuleState moduleState) {
      this.moduleState = moduleState;
      return this;
    }

    public Builder owners(List<PersonId> owners) {
      this.owners = owners;
      return this;
    }
    
    public Builder contentLicenses(List<ContentLicense> contentLicenses) {
      this.contentLicenses = contentLicenses;
      return this;
    }

    public Builder latestPublishVersion(Version latestPublishVersion) {
      this.latestPublishVersion = latestPublishVersion;
      return this;
    }
    
    public Builder nextVersion(Version nextVersion) {
      this.nextVersion = nextVersion;
      return this;
    }
    
    public Builder externalId(ExternalId externalId) {
      this.externalId = externalId;
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
    this.moduleId = builder.id;
    this.title = builder.title;
    this.moduleType = builder.moduleType;
    this.moduleState = builder.moduleState;
    this.owners = builder.owners;
    this.contentLicenses = builder.contentLicenses;
    this.latestPublishVersion = builder.latestPublishVersion;
    this.nextVersion = builder.nextVersion;
    this.externalId = builder.externalId;
  }

  @JsonCreator
  private ModuleDto() {
    super(null);
  }
}
