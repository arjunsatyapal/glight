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

import com.google.light.server.dto.AbstractDtoToPersistence;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.ModuleId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.Version;
import com.google.light.server.dto.pojo.typewrapper.stringwrapper.ExternalId;
import com.google.light.server.persistence.entity.module.ModuleVersionEntity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
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
@JsonTypeName(value = "moduleVersion")
@XmlRootElement(name = "moduleVersion")
@XmlAccessorType(XmlAccessType.FIELD)
public class ModuleVersionDto extends AbstractDtoToPersistence<ModuleVersionDto, ModuleVersionEntity, Long> {
  @XmlElement(name = "version")
  @JsonProperty(value = "version")
  private ModuleId moduleId;
  
  @XmlElement(name = "version")
  @JsonProperty(value = "version")
  private Version version;
  
  @XmlElement(name = "content")
  @JsonProperty(value = "content")
  private String content;
  
  @XmlElement(name = "externalId")
  @JsonProperty(value = "externalId")
  private ExternalId externalId;

  /** 
   * {@inheritDoc}
   */
  @Override
  public ModuleVersionDto validate() {
    checkNotNull(moduleId, "moduleId");
    checkNotNull(version, "version");
    checkNotBlank(content, "content");
    checkNotNull(externalId, "externalId");
    return this;
  }

  /** 
   * {@inheritDoc}
   */
  @Override
  public ModuleVersionEntity toPersistenceEntity(Long version) {
    ModuleVersionEntity entity = new ModuleVersionEntity.Builder()
        .version(new Version(version))
        .content(content)
        .build();

    return entity;
  }

  public String getContent() {
    return content;
  }

  public Version getVersion() {
    return version;
  }
  
  public ExternalId getExternalId() {
    return externalId;
  }

  public static class Builder extends AbstractDtoToPersistence.BaseBuilder<Builder> {
    private ModuleId moduleId;
    private Version version;
    private String content;
    private ExternalId externalId;

    public Builder moduleId(ModuleId moduleId) {
      this.moduleId = moduleId;
      return this;
    }
    
    public Builder version(Version version) {
      this.version = version;
      return this;
    }

    public Builder content(String content) {
      this.content = content;
      return this;
    }
    
    public Builder externalId(ExternalId externalId) {
      this.externalId = externalId;
      return this;
    }

    @SuppressWarnings("synthetic-access")
    public ModuleVersionDto build() {
      return new ModuleVersionDto(this).validate();
    }
  }

  @SuppressWarnings("synthetic-access")
  private ModuleVersionDto(Builder builder) {
    super(builder);
    this.moduleId = builder.moduleId;
    this.version = builder.version;
    this.content = builder.content;
    this.externalId = builder.externalId;
  }

  // For JAXB
  private ModuleVersionDto() {
    super(null);
  }
}
