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
import static com.google.light.server.servlets.oauth2.google.pojo.AbstractOAuth2TokenInfo.calculateExpireInMillis;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.LightPreconditions.checkPersonId;
import static com.google.light.server.utils.LightPreconditions.checkPositiveLong;
import static com.google.light.server.utils.LightPreconditions.checkProviderUserId;
import static com.google.light.server.utils.LightUtils.getWrapperValue;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.light.server.constants.OAuth2ProviderService;
import com.google.light.server.dto.AbstractDto;
import com.google.light.server.dto.AbstractDtoToPersistence;
import com.google.light.server.dto.pojo.longwrapper.PersonId;
import com.google.light.server.persistence.entity.oauth2.owner.OAuth2OwnerTokenEntity;
import com.google.light.server.persistence.entity.person.PersonEntity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * DTO for OAuth2 Tokens. Corresponding TokenEntity is {@link OAuth2OwnerTokenEntity}
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
@JsonTypeName(value = "oauth2OwnerToken")
@XmlRootElement(name="oauth2OwnerToken")
@XmlAccessorType(XmlAccessType.FIELD)
public class OAuth2OwnerTokenDto extends
    AbstractDtoToPersistence<OAuth2OwnerTokenDto, OAuth2OwnerTokenEntity, String> {
  @XmlElement(name = "personId")
  @JsonProperty(value = "personId")
  private Long personId;
  
  @XmlElement(name = "providerService")
  @JsonProperty(value = "providerService")
  private OAuth2ProviderService providerService;
  
  @XmlElement(name = "providerUserId")
  @JsonProperty(value = "providerUserId")
  private String providerUserId;
  
  @XmlElement(name = "accessToken")
  @JsonProperty(value = "accessToken")
  private String accessToken;
  
  @XmlElement(name = "refreshToken")
  @JsonProperty(value = "refreshToken")
  private String refreshToken;
  
  @XmlElement(name = "expiresInMillis")
  @JsonProperty(value = "expiresInMillis")
  private long expiresInMillis;
  
  @XmlElement(name = "tokenType")
  @JsonProperty(value = "tokenType")
  private String tokenType;
  
  @XmlElement(name = "tokenInfo")
  @JsonProperty(value = "tokenInfo")
  private String tokenInfo;

  /**
   * {@inheritDoc} TODO(arjuns): Update test.
   */
  @Override
  public OAuth2OwnerTokenDto validate() {
    checkPersonId(getPersonId());
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
        .personKey(PersonEntity.generateKey(getPersonId()))
        .providerService(providerService)
        .providerUserId(providerUserId)
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .expiresInMillis(expiresInMillis)
        .tokenType(tokenType)
        .tokenInfo(tokenInfo)
        .build();
  }

  public PersonId getPersonId() {
    return new PersonId(personId);
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

  // TODO(arjuns): Add test for this.
  /**
   * Helper method to get {@link OAuth2OwnerTokenDto} for the most frequent use-case.
   * 
   * @param personId
   * @param refreshToken
   * @param tokenResponse
   * @param providerService
   * @param providerUserId
   * @param tokenInfoInJson
   * @return
   */
  public static OAuth2OwnerTokenDto getOAuth2OwnerTokenDto(PersonId personId, String refreshToken,
      TokenResponse tokenResponse, OAuth2ProviderService providerService, String providerUserId,
      String tokenInfoInJson) {
    /*
     * Refresh token from TokenResponss is not reliable as it is set only when user has given 
     * access manually.
     */
    checkNotBlank(refreshToken, "refreshToken");
    
    long expiresInMillis = calculateExpireInMillis(tokenResponse.getExpiresInSeconds());

    OAuth2OwnerTokenDto tokenDto = new OAuth2OwnerTokenDto.Builder()
        .personId(personId)
        .provider(providerService)
        .providerUserId(providerUserId)
        .accessToken(tokenResponse.getAccessToken())
        .refreshToken(refreshToken)
        .expiresInMillis(expiresInMillis)
        .tokenType(tokenResponse.getTokenType())
        .tokenInfo(tokenInfoInJson)
        .build();

    return tokenDto;
  }

  public static class Builder extends AbstractDto.BaseBuilder<Builder> {
    private PersonId personId;
    private OAuth2ProviderService providerService;
    private String providerUserId;
    private String accessToken;
    private String refreshToken;
    private long expiresInMillis;
    private String tokenType;
    private String tokenInfo;

    public Builder personId(PersonId personId) {
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
    super(builder);
    this.personId = getWrapperValue(builder.personId);
    this.providerService = builder.providerService;
    this.providerUserId = builder.providerUserId;
    this.accessToken = builder.accessToken;
    this.refreshToken = builder.refreshToken;
    this.expiresInMillis = builder.expiresInMillis;
    this.tokenType = builder.tokenType;
    this.tokenInfo = builder.tokenInfo;
  }
  
  // For JAXB
  private OAuth2OwnerTokenDto() {
    super(null);
  }
}
