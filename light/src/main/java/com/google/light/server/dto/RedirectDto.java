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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlElement;

import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Preconditions;

/**
 * Dto to hold any state information that should be
 * reflected during any login redirect flow
 * 
 * @author Walter Cacau
 */
@SuppressWarnings("serial")
@XmlRootElement(name = "redirect")
@XmlAccessorType(XmlAccessType.FIELD)
public class RedirectDto extends AbstractDto<RedirectDto> {
  @XmlElement(name = "redirectPath")
  @JsonProperty(value = "redirectPath")
  private String redirectPath;

  /**
   * Path to be redirected after the login flow (being it successful or not)
   * 
   * Example: if redirectPath="page.html", then at the end of the login flow
   * the user will be redirected to http://server:port/redirectPath
   */
  public String getRedirectPath() {
    return redirectPath;
  }

  public void setRedirectPath(String redirectPath) {
    this.redirectPath = redirectPath;
  }

  @Override
  public RedirectDto validate() {
    if (redirectPath != null) {
      Preconditions.checkState(redirectPath.startsWith("/"),
          "redirectPath should start with a slash (/)");
    }
    return this;
  }
  
  @Override
  public String toXml() {
    throw new UnsupportedOperationException("RedirectDto should only be used as JSON.");
  }

  public static class Builder extends AbstractDto.BaseBuilder<Builder> {
    private String redirectPath;

    public Builder redirectPath(String redirectPath) {
      this.redirectPath = redirectPath;
      return this;
    }

    @SuppressWarnings("synthetic-access")
    public RedirectDto build() {
      return new RedirectDto(this).validate();
    }
  }

  @SuppressWarnings("synthetic-access")
  private RedirectDto(Builder builder) {
    super(builder);
    this.redirectPath = builder.redirectPath;
  }

  // For JAXB
  private RedirectDto() {
    super(null);
  }
}
