/*
 * Copyright (C) Google Inc.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * http://www.apache.org/licenses/LICENSE-2.0
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

import com.google.light.server.dto.AbstractDto;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

/**
 * Java representation for the JSON object returned by Google as a response to
 * {@link OldGoogleOAuth2Helper#getUserInfo(String)}
 * 
 * TODO(arjuns): Ensure that the original JSON is stored on Datastore instead of converted one.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
@XmlRootElement(name = "TokenInfo")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonSerialize(include = Inclusion.NON_NULL)
// TODO(arjuns): Add test for this and token info.
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleUserInfo extends AbstractDto<GoogleUserInfo> {
  private String id;
  private String email;

  @JsonProperty(value = "verified_email")
  private boolean verifiedEmail;

  private String name;

  @JsonProperty(value = "given_name")
  private String givenName;
  
  @JsonProperty(value = "family_name")
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

  public String getGivenName() {
    return givenName;
  }

  public String getFamilyName() {
    return familyName;
  }

  public String getEmail() {
    return email;
  }

  public boolean getVerifiedEmail() {
    return verifiedEmail;
  }

  public String getLocale() {
    return locale;
  }

  public void setLocale(String locale) {
    this.locale = locale;
  }

  /**
   * {@inheritDoc} TODO(arjuns): Add test for this.
   */
  @Override
  public GoogleUserInfo validate() {
    checkNotBlank(id, "id");
    checkEmail(email);
    checkNotBlank(name, "name");
    checkNotBlank(givenName, "givenName");
    checkNotBlank(familyName, "familyName");

    // TODO(arjuns) : Add more tests for Locale validation.
    if (locale != null) {
      checkNotBlank(locale, "locale");
    } else {
      this.locale = "en";
    }
    return this;
  }

  // For JAXB.
  @JsonCreator
  public GoogleUserInfo() {
    super(null);
  }

  public static class Builder extends AbstractDto.BaseBuilder<Builder> {
    private String id;
    private String email;
    private boolean verifiedEmail;
    private String name;
    private String givenName;
    private String familyName;
    private String locale;

    public Builder id(String id) {
      this.id = id;
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

    public Builder name(String name) {
      this.name = name;
      return this;
    }

    public Builder givenName(String givenName) {
      this.givenName = givenName;
      return this;
    }

    public Builder familyName(String familyName) {
      this.familyName = familyName;
      return this;
    }

    public Builder locale(String locale) {
      this.locale = locale;
      return this;
    }

    @SuppressWarnings("synthetic-access")
    public GoogleUserInfo build() {
      return new GoogleUserInfo(this).validate();
    }
  }

  @SuppressWarnings("synthetic-access")
  private GoogleUserInfo(Builder builder) {
    super(builder);
    this.id = builder.id;
    this.email = builder.email;
    this.verifiedEmail = builder.verifiedEmail;
    this.name = builder.name;
    this.givenName = builder.givenName;
    this.familyName = builder.familyName;
    this.locale = builder.locale;
  }
}
