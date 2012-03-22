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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

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
 * {@link GoogleOAuth2Helper#getUserInfo(String)}
 * 
 * @author Arjun Satyapal
 */
@XmlRootElement(name = "TokenInfo")
@XmlAccessorType(XmlAccessType.PROPERTY)
@JsonSerialize(include = Inclusion.NON_NULL)
public class GoogleUserInfo {
  private String id;
  private String email;
  private boolean verifiedEmail;
  
  private String name;
  private String givenName;
  private String familyName;
  private String locale;

  public String getId() {
    return id;
  }
  
  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String audience) {
    this.name = audience;
  }

  @JsonProperty(value = "given_name")
  public String getGivenName() {
    return givenName;
  }
  @JsonProperty(value = "given_name")
  public void setGivenName(String userId) {
    this.givenName = userId;
  }

  @JsonProperty(value = "family_name")
  public String getFamilyName() {
    return familyName;
  }

  @JsonProperty(value = "family_name")
  public void setFamilyName(String scope) {
    this.familyName = scope;
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

  public String getLocale() {
    return locale;
  }

  public void setLocale(String accessType) {
    this.locale = accessType;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }
  
  // For JAXB.
  @JsonCreator
  public GoogleUserInfo() {
  }
}
