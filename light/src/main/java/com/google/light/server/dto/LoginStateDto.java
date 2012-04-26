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

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import com.google.common.base.Preconditions;
import com.google.light.server.utils.JsonUtils;

/**
 * Dto to hold any state information that should be
 * reflected during any login redirect flow
 * 
 * @author Walter Cacau
 */
@SuppressWarnings("serial")
@JsonSerialize(include = Inclusion.NON_NULL)
public class LoginStateDto implements DtoInterface<LoginStateDto> {
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
  public String toJson() {
    try {
      return JsonUtils.toJson(this);
    } catch (Exception e) {
      // TODO(waltercacau) : Add exception handling later.
      throw new RuntimeException(e);
    }
  }

  @Override
  public String toXml() {
    throw new UnsupportedOperationException();
  }

  @Override
  public LoginStateDto validate() {
    if (redirectPath != null) {
      Preconditions.checkState(redirectPath.startsWith("/"),
          "redirectPath should start with a slash (/)");
    }
    return this;
  }

  public static class Builder {
    private String redirectPath;

    public Builder redirectPath(String redirectPath) {
      this.redirectPath = redirectPath;
      return this;
    }

    @SuppressWarnings("synthetic-access")
    public LoginStateDto build() {
      return new LoginStateDto(this).validate();
    }
  }

  @SuppressWarnings("synthetic-access")
  private LoginStateDto(Builder builder) {
    this.redirectPath = builder.redirectPath;
  }

  // For JAXB
  private LoginStateDto() {
  }
}
