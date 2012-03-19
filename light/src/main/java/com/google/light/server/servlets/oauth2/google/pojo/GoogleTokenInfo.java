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

import com.google.light.server.servlets.oauth2.google.GoogleOAuth2Helper;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

/**
 * Java representation for the JSON object returned by Google as a response to
 * {@link GoogleOAuth2Helper#getTokenInfo(String)}
 * 
 * @author Arjun Satyapal
 */
@XmlRootElement(name = "TokenInfo")
@XmlAccessorType(XmlAccessType.PROPERTY)
@JsonSerialize(include = Inclusion.NON_NULL)
public class GoogleTokenInfo {
  private String issuedTo;
  private String audience;
  private String userId;
  private String scope;
  private long expiresInSeconds;
  private String email;
  private boolean verifiedEmail;
  private String accessType;

  @JsonProperty(value = "issued_to")
  public String getIssuedTo() {
    return issuedTo;
  }
  
  @JsonProperty(value = "issued_to")
  public void setIssuedTo(String issuedTo) {
    this.issuedTo = issuedTo;
  }

  public String getAudience() {
    return audience;
  }

  public void setAudience(String audience) {
    this.audience = audience;
  }

  @JsonProperty(value = "user_id")
  public String getUserId() {
    return userId;
  }
  @JsonProperty(value = "user_id")
  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getScope() {
    return scope;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }

  @JsonProperty(value = "expires_in")
  public long getExpiresInSeconds() {
    return expiresInSeconds;
  }

  @JsonProperty(value = "expires_in")
  public void setExpiresInSeconds(long expiresInSeconds) {
    this.expiresInSeconds = expiresInSeconds;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  @JsonProperty(value = "verified_email")
  public boolean getVerifiedEmail() {
    return verifiedEmail;
  }

  @JsonProperty(value = "verified_email")
  public void setVerifiedEmail(boolean verifiedEmail) {
    this.verifiedEmail = verifiedEmail;
  }

  @JsonProperty(value = "access_type")
  public String getAccessType() {
    return accessType;
  }

  @JsonProperty(value = "access_type")
  public void setAccessType(String accessType) {
    this.accessType = accessType;
  }

  // For JAXB.
  @JsonCreator
  public GoogleTokenInfo() {
  }
}
