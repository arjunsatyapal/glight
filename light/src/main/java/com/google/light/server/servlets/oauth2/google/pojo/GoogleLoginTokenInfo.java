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
import com.google.light.server.exception.unchecked.JsonException;
import com.google.light.server.utils.JsonUtils;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

/**
 * Java representation for the JSON object returned by Google as a response to
 * {@link OldGoogleOAuth2Helper#getTokenInfo(String)}
 * 
 * TODO(arjuns): Update tests. and package for test.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
@XmlRootElement(name = "TokenInfo")
@XmlAccessorType(XmlAccessType.PROPERTY)
@JsonSerialize(include = Inclusion.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleLoginTokenInfo extends AbstractGoogleOAuth2TokenInfo<GoogleLoginTokenInfo>{
  private String userId;
  private String email;
  private boolean verifiedEmail;
  

  @JsonProperty(value = "user_id")
  public String getUserId() {
    return userId;
  }

  @JsonProperty(value = "user_id")
  public void setUserId(String userId) {
    this.userId = userId;
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

  /**
   * {@inheritDoc} TODO(arjuns) : Add test for this.
   */
  @Override
  public String toJson() {
    try {
      return JsonUtils.toJson(this);
    } catch (Exception e) {
      throw new JsonException("Conversion of " + getClass().getSimpleName() + " to Json failed.", e);
    }
  }

  // For JAXB.
  @JsonCreator
  public GoogleLoginTokenInfo() {
  }
}
