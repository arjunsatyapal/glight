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
package com.google.light.server.dto.oauth2.owner;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.LightPreconditions.checkPositiveLong;
import static com.google.light.server.utils.LightPreconditions.checkProviderUserId;

import com.google.light.server.constants.OAuth2ProviderService;
import com.google.light.server.dto.DtoToPersistenceInterface;
import com.google.light.server.persistence.entity.oauth2.owner.OAuth2OwnerTokenEntity;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * DTO for OAuth2 Tokens. Corresponding TokenEntity is {@link OAuth2OwnerTokenEntity}
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class OAuth2OwnerTokenDto implements
    DtoToPersistenceInterface<OAuth2OwnerTokenDto, OAuth2OwnerTokenEntity, String> {
  private long personId;
  private OAuth2ProviderService providerService;
  private String providerUserId;
  private String accessToken;
  private String refreshToken;
  private long expiresInMillis;
  private String tokenType;
  private String tokenInfo;

  /**
   * {@inheritDoc}
   * TODO(arjuns): Implement this method.
   */
  @Override
  public String toJson() {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toXml() {
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   * TODO(arjuns): Update test.
   */
  @Override
  public OAuth2OwnerTokenDto validate() {
    checkPositiveLong(personId, "personId");
    checkNotNull(providerService, "provider");
    checkProviderUserId(providerService, providerUserId);
    checkNotBlank(accessToken, "accessToken");
    checkNotBlank(refreshToken, "refreshToken");
    checkPositiveLong(expiresInMillis, "expiresInMillis");
    checkNotBlank(tokenType, "tokenType");
    checkNotBlank(tokenInfo, "tokenInfo");
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public OAuth2OwnerTokenEntity toPersistenceEntity(String id) {
    throw new UnsupportedOperationException("should not be called.");
  }

  public OAuth2OwnerTokenEntity toPersistenceEntity() {
    return new OAuth2OwnerTokenEntity.Builder()
        .personId(personId)
        .providerService(providerService)
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

  public long getPersonId() {
    return personId;
  }

  public OAuth2ProviderService getProviderService() {
    return providerService;
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

    public Builder provider(OAuth2ProviderService providerService) {
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
    public OAuth2OwnerTokenDto build() {
      return new OAuth2OwnerTokenDto(this).validate();
    }
  }

  @SuppressWarnings("synthetic-access")
  private OAuth2OwnerTokenDto(Builder builder) {
    this.personId = builder.personId;
    this.providerService = builder.providerService;
    this.providerUserId = builder.providerUserId;
    this.accessToken = builder.accessToken;
    this.refreshToken = builder.refreshToken;
    this.expiresInMillis = builder.expiresInMillis;
    this.tokenType = builder.tokenType;
    this.tokenInfo = builder.tokenInfo;
  }
}
