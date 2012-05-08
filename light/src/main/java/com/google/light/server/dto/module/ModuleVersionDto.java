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

import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.LightPreconditions.checkPositiveLong;

import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;

import javax.xml.bind.annotation.XmlAccessorType;

import javax.xml.bind.annotation.XmlRootElement;

import javax.xml.bind.annotation.XmlElement;

import com.google.light.server.dto.pojo.longwrapper.Version;

import com.google.light.server.dto.AbstractDtoToPersistence;
import com.google.light.server.persistence.entity.module.ModuleVersionEntity;

/**
 * DTO for Light Modules.
 * 
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
@XmlRootElement(name = "moduleVersion")
@XmlAccessorType(XmlAccessType.FIELD)
public class ModuleVersionDto extends AbstractDtoToPersistence<ModuleVersionDto, ModuleVersionEntity, Long> {
  @XmlElement(name = "version")
  @JsonProperty(value = "version")
  private Long version;
  
  @XmlElement(name = "content")
  @JsonProperty(value = "content")
  private String content;

  /** 
   * {@inheritDoc}
   */
  @Override
  public ModuleVersionDto validate() {
    checkPositiveLong(version, "version");
    checkNotBlank(content, "content");
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

  public static class Builder extends AbstractDtoToPersistence.BaseBuilder<Builder> {
    private Long version;
    private String content;

    public Builder version(Long version) {
      this.version = version;
      return this;
    }

    public Builder content(String content) {
      this.content = content;
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
    this.version = builder.version;
    this.content = builder.content;
  }

  // For JAXB
  private ModuleVersionDto() {
    super(null);
  }
}
