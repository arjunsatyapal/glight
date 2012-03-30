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
package com.google.light.server.persistence.entity.admin;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;

import com.google.light.server.constants.OAuth2ProviderEnum;

import com.google.light.server.dto.admin.OAuth2ConsumerCredentialDto;
import com.google.light.server.persistence.PersistenceToDtoInterface;
import javax.persistence.Id;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * PersistenceEntity for OAuth2Consumer Credentials.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class OAuth2ConsumerCredentialEntity implements
    PersistenceToDtoInterface<OAuth2ConsumerCredentialEntity, OAuth2ConsumerCredentialDto> {
  @Id
  private String oAuth2ProviderKey;
  private String clientId;
  private String clientSecret;

  /**
   * {@inheritDoc}
   */
  @Override
  public OAuth2ConsumerCredentialDto toDto() {
    return new OAuth2ConsumerCredentialDto.Builder()
        .provider(OAuth2ProviderEnum.valueOf(oAuth2ProviderKey))
        .clientId(clientId)
        .clientSecret(clientSecret)
        .build();
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

  public String getOAuth2ProviderKey() {
    return oAuth2ProviderKey;
  }

  public String getClientId() {
    return clientId;
  }

  public String getClientSecret() {
    return clientSecret;
  }

  public static class Builder {
    private String oAuth2ProviderKey;
    private String clientId;
    private String clientSecret;

    public Builder oAuth2ProviderKey(String oAuth2ProviderKey) {
      this.oAuth2ProviderKey = oAuth2ProviderKey;
      return this;
    }

    public Builder clientId(String clientId) {
      this.clientId = clientId;
      return this;
    }

    public Builder clientSecret(String clientSecret) {
      this.clientSecret = clientSecret;
      return this;
    }

    @SuppressWarnings("synthetic-access")
    public OAuth2ConsumerCredentialEntity build() {
      return new OAuth2ConsumerCredentialEntity(this);
    }
  }

  @SuppressWarnings("synthetic-access")
  private OAuth2ConsumerCredentialEntity(Builder builder) {
    // Ensures that oAuth2ProviderKey is valid.
    checkNotNull(OAuth2ProviderEnum.valueOf(builder.oAuth2ProviderKey), "invalid ProviderKey["
        + oAuth2ProviderKey + "].");
    this.oAuth2ProviderKey = checkNotBlank(builder.oAuth2ProviderKey, "oAuth2ProviderKey");
    this.clientId = checkNotBlank(builder.clientId, "clientId is blank");
    this.clientSecret = checkNotBlank(builder.clientSecret, "clientSecret");
  }
  
  // For Objectify.
  private OAuth2ConsumerCredentialEntity() {
  }
}
