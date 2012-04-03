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
package com.google.light.server.dto.admin;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;

import com.google.light.server.utils.JsonUtils;

import com.google.light.server.constants.OAuth2ProviderEnum;
import com.google.light.server.dto.DtoToPersistenceInterface;
import com.google.light.server.persistence.entity.admin.OAuth2ConsumerCredentialEntity;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * DTO for OAuth2Consumer Credentials. Corresponding Entity is
 * {@link OAuth2ConsumerCredentialEntity}.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class OAuth2ConsumerCredentialDto implements
    DtoToPersistenceInterface<OAuth2ConsumerCredentialDto, OAuth2ConsumerCredentialEntity, String> {
  private OAuth2ProviderEnum provider;
  private String clientId;
  private String clientSecret;

  /**
   * Constructor.
   * 
   * @param provider
   * @param clientId
   * @param clientSecret
   */
  protected OAuth2ConsumerCredentialDto(OAuth2ProviderEnum provider, String clientId,
      String clientSecret) {
    this.provider = provider;
    this.clientId = clientId;
    this.clientSecret = clientSecret;

    this.validate();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toJson() {
    try {
    return JsonUtils.toJson(this);
    } catch (Exception e) {
      // TODO(arjuns): Add exception handling.
      throw new RuntimeException(e);
    }
  }

  /**
   * {@inheritDoc} This method should not be called. 
   * @deprecated call {@link #toPersistenceEntity()}.
   */
  @Deprecated
  @Override
  public OAuth2ConsumerCredentialEntity toPersistenceEntity(String id) {
    throw new UnsupportedOperationException("this should not be called.");
  }

  /**
   * Method to convert to {@link OAuth2ConsumerCredentialEntity}.
   * 
   * @return
   */
  public OAuth2ConsumerCredentialEntity toPersistenceEntity() {
    return new OAuth2ConsumerCredentialEntity.Builder()
        .clientId(clientId)
        .clientSecret(clientSecret)
        .providerName(provider.name())
        .build();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public OAuth2ConsumerCredentialDto validate() {
    checkNotNull(provider, "provider");
    checkNotBlank(clientId, "clientId");
    checkNotBlank(clientSecret, "clientSecret");

    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toXml() {
    throw new UnsupportedOperationException();
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

  public OAuth2ProviderEnum getProvider() {
    return provider;
  }

  public String getClientId() {
    return clientId;
  }

  public String getClientSecret() {
    return clientSecret;
  }

  public static class Builder {
    private OAuth2ProviderEnum provider;
    private String clientId;
    private String clientSecret;

    public Builder provider(OAuth2ProviderEnum provider) {
      this.provider = provider;
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
    public OAuth2ConsumerCredentialDto build() {
      return new OAuth2ConsumerCredentialDto(this).validate();
    }
  }

  @SuppressWarnings("synthetic-access")
  private OAuth2ConsumerCredentialDto(Builder builder) {
    this.provider = builder.provider;
    this.clientId = builder.clientId;
    this.clientSecret = builder.clientSecret;
  }

  // For JAXB
  @SuppressWarnings("unused")
  private OAuth2ConsumerCredentialDto() {
  }
}
