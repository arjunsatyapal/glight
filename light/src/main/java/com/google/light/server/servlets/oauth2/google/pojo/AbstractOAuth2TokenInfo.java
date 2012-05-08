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

import static com.google.light.server.constants.LightConstants.TASK_QUEUUE_TIMEOUT_IN_SEC;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.LightPreconditions.checkPositiveLong;
import static com.google.light.server.utils.LightUtils.getCurrentTimeInMillis;

import com.google.light.server.dto.AbstractDto;
import com.google.light.server.dto.AbstractPojo;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.codehaus.jackson.annotate.JsonProperty;

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
public abstract class AbstractOAuth2TokenInfo<D> extends AbstractPojo<D> {
  @XmlElement(name = "issued_to")
  @JsonProperty(value = "issued_to")
  private String issuedTo;

  @XmlElement
  private String audience;

  @XmlElement
  private String scope;

  // This value is not of much use as it is in seconds.
  @XmlElement(name = "expires_in")
  @JsonProperty(value = "expires_in")
  private long expiresIn;

  // Hack to tell that value has not been set and then validate method updates this.
  @XmlElement(name = "expires_in_millis")
  @JsonProperty(value = "expires_in_millis")
  private long expiresInMillis = 0;

  @XmlElement(name = "access_type")
  @JsonProperty(value = "access_type")
  private String accessType;

  public String getIssuedTo() {
    return issuedTo;
  }
  
  public String getAudience() {
    return audience;
  }

  public String getScope() {
    return scope;
  }

  public long getExpiresIn() {
    return expiresIn;
  }

  public String getAccessType() {
    return accessType;
  }

  /**
   * Ensures that all values are set. This does not check whether token is expired or not.
   * 
   * @return
   */
  @SuppressWarnings("unchecked")
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

  public boolean hasAccessTokenExpired() {
    return getCurrentTimeInMillis() > expiresInMillis;
  }

  /**
   * From Google, we receive expiry in Seconds from time of Generation of AccessToken. We convert it
   * to absolute expiry time in millis so that calculation of expiry time becomes easier as we dont
   * have to store the token creationTime. In case its required then it can be reverse computed.
   * 
   * In addition to that, we are deliberately shortening the life of AccessToken by
   * {@link LightConstants#TASK_QUEUUE_TIMEOUT_IN_SEC} seconds. Reason for doing this is we have a
   * TimeOut of 30 seconds for FrontEnd and 10 minutes for TaskQueues. And we don't want to get any
   * access exception during those TaskQueue execution. In addition in future, once we start using
   * FrontEnds, still we may not want to run any task for more then 10 mins. If some task happens
   * to run for more then 10minutes, then at that time that task should take care of this.
   * 
   * TODO(arjuns) : Add support for automatic refreshing when token expires.
   * 
   * @param expiresInSecFromNow
   * @return
   */
  public static long calculateExpireInMillis(long expiresInSecFromNow) {
    long nowInMillis = getCurrentTimeInMillis();
    long modifiedExpiryInMillis = (expiresInSecFromNow - TASK_QUEUUE_TIMEOUT_IN_SEC) * 1000;
    if (modifiedExpiryInMillis < 0) {
      modifiedExpiryInMillis = 0;
    }

    long actualExpiryInMillis = nowInMillis + modifiedExpiryInMillis;
    return actualExpiryInMillis;
  }

  @SuppressWarnings("rawtypes")
  public static class Builder<T extends Builder> extends AbstractDto.BaseBuilder<Builder>{
    private String issuedTo;
    private String audience;
    private String scope;
    private long expiresIn;
    private long expiresInMillis;
    private String accessType;

    @SuppressWarnings("unchecked")
    public T issuedTo(String issuedTo) {
      this.issuedTo = issuedTo;
      return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T audience(String audience) {
      this.audience = audience;
      return ((T) this);
    }

    @SuppressWarnings("unchecked")
    public T scope(String scope) {
      this.scope = scope;
      return ((T) this);
    }

    @SuppressWarnings("unchecked")
    public T expiresIn(long expiresIn) {
      this.expiresIn = expiresIn;
      return ((T) this);
    }

    @SuppressWarnings("unchecked")
    public T expiresInMillis(long expiresInMillis) {
      this.expiresInMillis = expiresInMillis;
      return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T accessType(String accessType) {
      this.accessType = accessType;
      return ((T) this);
    }
  }

  @SuppressWarnings({ "rawtypes", "synthetic-access" })
  protected AbstractOAuth2TokenInfo(Builder builder) {
//    super(builder);
    
    if (builder == null) {
      return;
    }
    
    this.issuedTo = builder.issuedTo;
    this.audience = builder.audience;
    this.scope = builder.scope;
    this.expiresIn = builder.expiresIn;
    this.expiresInMillis = builder.expiresInMillis;
    this.accessType = builder.accessType;
  }
  
  // For JAXB
  private AbstractOAuth2TokenInfo() {
//    super(null);
  }
}
