/*
 * Copyright (C) Google Inc.
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
package com.google.light.server.dto;

import static com.google.light.server.utils.LightPreconditions.checkNotBlank;

import org.codehaus.jackson.annotate.JsonTypeName;

import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.google.light.server.dto.person.PersonDto;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.ModuleId;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Dto to represent javascript variables that will be inject
 * in the page before the actual client side code is loaded.
 * TODO(arjuns): See if this gets affected.
 * @author Walter Cacau
 */
@SuppressWarnings("serial")
@JsonTypeName(value = "jsVariablesPreload")
@XmlRootElement(name = "jsVariablesPreload")
@XmlAccessorType(XmlAccessType.FIELD)
public class JSVariablesPreloadDto extends AbstractDto<JSVariablesPreloadDto> {
  @XmlElement(name = "locale")
  @JsonProperty(value = "locale")
  private String locale;
  
  @XmlElement(name = "moduleId")
  @JsonProperty(value = "moduleId")
  private ModuleId moduleId;
  
  @XmlElement(name = "person")
  @JsonProperty(value = "person")
  private PersonDto person;

  @Override
  public JSVariablesPreloadDto validate() {
    checkNotBlank(locale, "locale");
    return this;
  }
  
  public String toHtml() {
    // TODO(waltercacau): Add test
    StringBuilder htmlBuilder = new StringBuilder();
    htmlBuilder.append("<script>\n");
    htmlBuilder.append("var lightPreload = ");
    htmlBuilder.append(toJson());
    htmlBuilder.append(";\n</script>");
    return htmlBuilder.toString();
  }

  public String getLocale() {
    return locale;
  }

  public void setLocale(String locale) {
    this.locale = locale;
  }

  public PersonDto getPerson() {
    return person;
  }

  public void setPerson(PersonDto person) {
    this.person = person;
  }

  // For JAXB
  private JSVariablesPreloadDto() {
    super(null);
  }

  public static class Builder extends AbstractDto.BaseBuilder<Builder> {
    private String locale;
    private PersonDto person;
    private ModuleId moduleId;

    public Builder locale(String locale) {
      this.locale = locale;
      return this;
    }

    public Builder moduleId(ModuleId moduleId) {
      this.moduleId = moduleId;
      return this;
    }

    public Builder person(PersonDto person) {
      this.person = person;
      return this;
    }

    @SuppressWarnings("synthetic-access")
    public JSVariablesPreloadDto build() {
      return new JSVariablesPreloadDto(this).validate();
    }
  }

  @SuppressWarnings("synthetic-access")
  private JSVariablesPreloadDto(Builder builder) {
    super(builder);
    this.locale = builder.locale;
    this.person = builder.person;
    this.moduleId = builder.moduleId;
  }

  public ModuleId getModuleId() {
    return moduleId;
  }

}
