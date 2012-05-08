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
package com.google.light.server.servlets.oauth2.google.pojo;

import static com.google.light.server.utils.LightPreconditions.checkEmail;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;

import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Java representation for the JSON object returned by Google as a response to
 * {@link OldGoogleOAuth2Helper#getTokenInfo(String)}
 * 
 * TODO(arjuns): Update tests. and package for test.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
@XmlRootElement(name = "google_login_token_info")
public class GoogleLoginTokenInfo extends AbstractOAuth2TokenInfo<GoogleLoginTokenInfo> {
  @XmlElement(name = "user_id")
  @JsonProperty(value = "user_id")
  private String userId;
  
  @XmlElement
  private String email;
  
  @XmlElement(name = "verified_email")
  @JsonProperty(value = "verified_email")
  private boolean verifiedEmail;

  public String getUserId() {
    return userId;
  }

  public String getEmail() {
    return email;
  }
  
  public boolean getVerifiedEmail() {
    return verifiedEmail;
  }

  /**
   * Ensures that all values are set. This does not check whether token is expired or not.
   * 
   * @return
   */
  @Override
  public GoogleLoginTokenInfo validate() {
    super.validate();
    checkNotBlank(userId, "userId");
    checkEmail(email);

    return this;
  }

  public static class Builder extends AbstractOAuth2TokenInfo.Builder<Builder> {
    private String userId;
    private String email;
    private boolean verifiedEmail;

    public Builder userId(String userId) {
      this.userId = userId;
      return this;
    }

    public Builder email(String email) {
      this.email = email;
      return this;
    }

    public Builder verifiedEmail(boolean verifiedEmail) {
      this.verifiedEmail = verifiedEmail;
      return this;
    }

    @SuppressWarnings("synthetic-access")
    public GoogleLoginTokenInfo build() {
      return new GoogleLoginTokenInfo(this).validate();
    }
  }

  @SuppressWarnings("synthetic-access")
  private GoogleLoginTokenInfo(Builder builder) {
    super(builder);
    
    this.userId = builder.userId;
    this.email = builder.email;
    this.verifiedEmail = builder.verifiedEmail;
  }
  
  // For JAXB
  private GoogleLoginTokenInfo() {
    super(null);
  }
}
