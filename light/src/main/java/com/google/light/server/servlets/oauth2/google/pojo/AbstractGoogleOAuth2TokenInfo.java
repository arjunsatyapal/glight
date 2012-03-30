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

import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.LightPreconditions.checkPositiveLong;
import static com.google.light.server.utils.LightUtils.getCurrentTimeInMillis;

import com.google.light.server.dto.DtoInterface;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnore;
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
public abstract class AbstractGoogleOAuth2TokenInfo<D> implements DtoInterface<D> {
  private String issuedTo;
  private String audience;
  private String scope;
  // This value is not of much use as it is in seconds.
  private long expiresIn;
  private long expiresInMillis = 0; // Hack to tell that value has not been set.
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

  public String getScope() {
    return scope;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }

  @JsonProperty(value = "expires_in")
  public long getExpiresIn() {
    return expiresIn;
  }

  @JsonProperty(value = "expires_in")
  public void setExpiresIn(long expiresInMillis) {
    this.expiresIn = expiresInMillis;
  }

  @JsonProperty(value = "access_type")
  public String getAccessType() {
    return accessType;
  }

  @JsonProperty(value = "access_type")
  public void setAccessType(String accessType) {
    this.accessType = accessType;
  }

  /**
   * Ensures that all values are set. This does not check whether token is expired or not.
   * 
   * @return
   */
  @Override
  public D validate() {
    checkNotBlank(issuedTo, "issuedTo");
    checkNotBlank(audience, "audience");
    checkNotBlank(scope, "scope");
    checkNotBlank(issuedTo, "issuedTo");
    checkPositiveLong(expiresIn, "expiresIn");

    if (expiresInMillis == 0) {
      // This means object was created when parsing the TokenInfo from Google.
      // So now we will calculate the actual expiry time and set it.
      expiresInMillis = calculateExpireInMillis(expiresIn);

    } else {
      // expiresInMillis was already stored. So nothing to do.
    }
    checkPositiveLong(expiresInMillis, "expiresInMillis");

    checkNotBlank(issuedTo, "issuedTo");
    checkNotBlank(accessType, "accessType");

    return (D) this;
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

  @JsonIgnore(value = true)
  public boolean hasAccessTokenExpired() {
    return getCurrentTimeInMillis() > expiresInMillis;
  }

  /**
   * {@inheritDoc} TODO(arjuns) : Add test for this.
   */
  @Override
  public abstract String toJson();

  /**
   * {@inheritDoc} TODO(arjuns) : Add test for this.
   */
  @Override
  public String toXml() {
    // TODO(arjuns): Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  /**
   * From Google, we receive expiry in Seconds from time of Generation of AccessToken. We convert it
   * to absolute expiry time in millis so that calculation of expiry time becomes easier as we dont
   * have to store the token creationTime. In case its required then it can be reverse computed.
   * 
   * In addition to that, we are deliberately shortening the life of AccessToken by 10 minutes.
   * Reason for doing this is we have a TimeOut of 30seconds for FrontEnd and 10 minutes for
   * TaskQueues. And we don't want to get any access exception during those tasks. In addition in
   * future, once we start using FrontEnds, still we may not want to run any task for more then 10
   * mins. If some task happens to run for more then 10minutes, then at that time that task should
   * take care of this.
   * 
   * TODO(arjuns) : Add support for automatic refreshing when token expires.
   * 
   * @param expiresInSecFromNow
   * @return
   */
  @JsonIgnore(value = true)
  public static long calculateExpireInMillis(long expiresInSecFromNow) {
    long nowInMillis = getCurrentTimeInMillis();
    long modifiedExpiryInMillis = (expiresInSecFromNow - 10 * 60 /* 10mins */) * 1000;
    if (modifiedExpiryInMillis < 0) {
      modifiedExpiryInMillis = 0;
    }

    long actualExpiryInMillis = nowInMillis + modifiedExpiryInMillis;
    return actualExpiryInMillis;
  }

  // For JAXB.
  @JsonCreator
  public AbstractGoogleOAuth2TokenInfo() {
  }
}
