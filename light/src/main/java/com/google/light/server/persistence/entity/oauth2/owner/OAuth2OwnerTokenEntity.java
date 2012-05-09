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
import static com.google.light.server.utils.LightPreconditions.checkPersonKey;
import static com.google.light.server.utils.LightPreconditions.checkPositiveLong;
import static com.google.light.server.utils.LightPreconditions.checkProviderUserId;
import static com.google.light.server.utils.LightUtils.getCurrentTimeInMillis;

import com.google.light.server.dto.pojo.typewrapper.longwrapper.PersonId;

import com.google.light.server.annotations.ObjectifyQueryField;

import com.google.light.server.annotations.ObjectifyQueryFieldName;


import com.google.light.server.constants.OAuth2ProviderService;
import com.google.light.server.dto.oauth2.owner.OAuth2OwnerTokenDto;
import com.google.light.server.persistence.entity.AbstractPersistenceEntity;
import com.google.light.server.persistence.entity.person.PersonEntity;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.annotation.Parent;
import javax.persistence.Id;

/**
 * Persistence entity for OAuth2 Tokens.
 * Corresponding DTO is {@link OAuth2OwnerTokenDto}.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class OAuth2OwnerTokenEntity extends
    AbstractPersistenceEntity<OAuth2OwnerTokenEntity, OAuth2OwnerTokenDto> {
  @Id
  private String providerServiceName;
  @Parent
  private Key<PersonEntity> personKey;

  // This is used in Objectify Query.
  @ObjectifyQueryFieldName("providerUserId")
  public static final String OFY_PROVIDER_USER_ID = "providerUserId";
  @ObjectifyQueryField("OFY_PROVIDER_USER_ID")
  private String providerUserId;

  private String accessToken;
  private String refreshToken;
  private long expiresInMillis;
  private String tokenType;
  private String tokenInfo;

  /**
   * {@inheritDoc}
   */
  @Override
  public OAuth2OwnerTokenDto toDto() {
    return new OAuth2OwnerTokenDto.Builder()
        .personId(new PersonId(personKey.getId()))
        .provider(OAuth2ProviderService.valueOf(providerServiceName))
        .providerUserId(providerUserId)
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .expiresInMillis(expiresInMillis)
        .tokenType(tokenType)
        .tokenInfo(tokenInfo)
        .build();
  }

  public Key<PersonEntity> getPersonKey() {
    return personKey;
  }

  /**
   * TODO(arjuns): Add test for this.
   * 
   * @return
   */
  public PersonId getPersonId() {
    return new PersonId(getPersonKey().getId());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Key<OAuth2OwnerTokenEntity> getKey() {
    return generateKey(personKey, providerServiceName);
  }

  /**
   * Method to generate Objectify key for {@link OAuth2OwnerTokenEntity}.
   */
  public static Key<OAuth2OwnerTokenEntity> generateKey(Key<PersonEntity> personKey,
      String providerServiceName) {
    // check that providerServiceName is valid.
    try {
      checkNotBlank(providerServiceName, "providerServiceName");
      checkNotNull(OAuth2ProviderService.valueOf(providerServiceName));
    } catch (Exception e) {
      throw new EnumConstantNotPresentException(OAuth2ProviderService.class, providerServiceName);
    }

    return new Key<OAuth2OwnerTokenEntity>(personKey, OAuth2OwnerTokenEntity.class,
        providerServiceName);
  }

  public OAuth2ProviderService getProviderService() {
    return OAuth2ProviderService.valueOf(providerServiceName);
  }

  public String getProviderUserId() {
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

  // TODO(arjuns): Add test for this.
  public boolean hasExpired() {
    return getCurrentTimeInMillis() > expiresInMillis;
  }

  public String getTokenType() {
    return tokenType;
  }

  public String getTokenInfo() {
    return tokenInfo;
  }

  public String getAuthorizationToken() {
    OAuth2ProviderService providerService = OAuth2ProviderService.valueOf(providerServiceName);

    switch (providerService) {
      case GOOGLE_DOC:
        //$FALL-THROUGH$
      case GOOGLE_LOGIN:
        return getTokenType() + " " + getAccessToken();

      default:
        throw new UnsupportedOperationException("This method is not supported for "
            + providerService);
    }
  }

  public static class Builder extends AbstractPersistenceEntity.BaseBuilder<Builder>{
    private Key<PersonEntity> personKey;
    private OAuth2ProviderService providerService;
    private String providerUserId;
    private String accessToken;
    private String refreshToken;
    private long expiresInMillis;
    private String tokenType;
    private String tokenInfo;

    public Builder personKey(Key<PersonEntity> personKey) {
      this.personKey = personKey;
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
      return new OAuth2OwnerTokenEntity(this).validate();
    }
  }

  @SuppressWarnings("synthetic-access")
  private OAuth2OwnerTokenEntity(Builder builder) {
    super(builder, false);
    this.personKey = checkPersonKey(builder.personKey);
    checkNotNull(builder.providerService, "provider");
    this.providerServiceName = builder.providerService.name();

    this.providerUserId = checkProviderUserId(builder.providerService, builder.providerUserId);
    this.accessToken = checkNotBlank(builder.accessToken, "accessToken");
    this.refreshToken = checkNotBlank(builder.refreshToken, "refreshToken");
    this.expiresInMillis = checkPositiveLong(builder.expiresInMillis, "expiresInMillis");
    this.tokenType = checkNotBlank(builder.tokenType, "tokenType");
    this.tokenInfo = checkNotBlank(builder.tokenInfo, "tokenInfo");
  }

  // For Objectify.
  private OAuth2OwnerTokenEntity() {
    super(null, false);
  }
}
