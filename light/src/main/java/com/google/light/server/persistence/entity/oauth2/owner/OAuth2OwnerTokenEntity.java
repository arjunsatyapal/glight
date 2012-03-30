/*
 * Copyright 2012 Google Inc.
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
package com.google.light.server.persistence.entity.oauth2.owner;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.LightPreconditions.checkPersonId;
import static com.google.light.server.utils.LightPreconditions.checkPositiveLong;
import static com.google.light.server.utils.LightPreconditions.checkProviderUserId;

import com.google.common.annotations.VisibleForTesting;

import com.google.light.server.constants.OAuth2ProviderService;
import com.google.light.server.dto.oauth2.owner.OAuth2OwnerTokenDto;
import com.google.light.server.persistence.PersistenceToDtoInterface;
import javax.persistence.Id;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Persistence entity for OAuth2 Tokens.
 * Corresponding DTO is {@link OAuth2OwnerTokenDto}.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class OAuth2OwnerTokenEntity implements
    PersistenceToDtoInterface<OAuth2OwnerTokenEntity, OAuth2OwnerTokenDto> {
  @Id
  String id;
  long personId;
  
  // This is used in Objectify Query.
  public static final String OFY_PROVIDER= "providerService";
  OAuth2ProviderService providerService;
  
  // This is used in Objectify Query.
  public static final String OFY_PROVIDER_USER_ID = "providerUserId";
  String providerUserId;
  
  String accessToken;
  String refreshToken;
  long expiresInMillis;
  String tokenType;
  String tokenInfo;

  /** 
   * {@inheritDoc}
   */
  @Override
  public OAuth2OwnerTokenDto toDto() {
    return new OAuth2OwnerTokenDto.Builder()
      .personId(personId)
      .provider(providerService)
      .providerUserId(providerUserId)
      .accessToken(accessToken)
      .refreshToken(refreshToken)
      .expiresInMillis(expiresInMillis)
      .tokenType(tokenType)
      .tokenInfo(tokenInfo)
      .build();
  }
  
  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }
  
  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
  
  public String getId() {
    return id;
  }

  public long getPersonId() {
    return personId;
  }

  public OAuth2ProviderService getProviderService() {
    return providerService;
  }
  
  public String providerUserId() {
    return providerUserId;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public String getRefreshToken() {
    return refreshToken;
  }

  public long getExpiresInMillis() {
    return expiresInMillis;
  }

  public String getTokenType() {
    return tokenType;
  }

  public String getTokenInfo() {
    return tokenInfo;
  }

  public static class Builder {
    private long personId;
    private OAuth2ProviderService providerService;
    private String providerUserId;
    private String accessToken;
    private String refreshToken;
    private long expiresInMillis;
    private String tokenType;
    private String tokenInfo;

    public Builder personId(long personId) {
      this.personId = personId;
      return this;
    }

    public Builder providerService(OAuth2ProviderService providerService) {
      this.providerService = providerService;
      return this;
    }

    public Builder providerUserId(String providerUserId) {
      this.providerUserId = providerUserId;
      return this;
    }
    
    public Builder accessToken(String accessToken) {
      this.accessToken = accessToken;
      return this;
    }

    public Builder refreshToken(String refreshToken) {
      this.refreshToken = refreshToken;
      return this;
    }

    public Builder expiresInMillis(long expiresInMillis) {
      this.expiresInMillis = expiresInMillis;
      return this;
    }

    public Builder tokenType(String tokenType) {
      this.tokenType = tokenType;
      return this;
    }

    public Builder tokenInfo(String tokenInfo) {
      this.tokenInfo = tokenInfo;
      return this;
    }

    @SuppressWarnings("synthetic-access")
    public OAuth2OwnerTokenEntity build() {
      return new OAuth2OwnerTokenEntity(this);
    }
  }
  
  @VisibleForTesting
  protected String computeId(long personId, OAuth2ProviderService providerService) {
    return checkPersonId(personId) + "." + providerService.name();
  }

  @SuppressWarnings("synthetic-access")
  private OAuth2OwnerTokenEntity(Builder builder) {
    this.personId = checkPersonId(builder.personId);
    this.providerService = checkNotNull(builder.providerService, "provider");
    // In order to setId, personId, and provider needs to be set.
    this.id = computeId(personId, providerService);
    
    this.providerUserId = checkProviderUserId(builder.providerService, builder.providerUserId);
    this.accessToken = checkNotBlank(builder.accessToken, "accessToken");
    this.refreshToken = checkNotBlank(builder.refreshToken, "refreshToken");
    this.expiresInMillis = checkPositiveLong(builder.expiresInMillis, "expiresInMillis");
    this.tokenType = checkNotBlank(builder.tokenType, "tokenType");
    this.tokenInfo = checkNotBlank(builder.tokenInfo, "tokenInfo");
  }

  // For Objectify.
  private OAuth2OwnerTokenEntity() {
  }
}
