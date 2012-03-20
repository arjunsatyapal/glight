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
package com.google.light.server.dto.admin;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;

import javax.annotation.Nullable;

import com.google.light.server.constants.OAuth2Provider;
import com.google.light.server.dto.DtoToPersistenceInterface;
import com.google.light.server.persistence.entity.admin.OAuth2ConsumerCredentialEntity;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * DTO for OAuth2Consumer Credentials.
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class OAuth2ConsumerCredentialDto implements
    DtoToPersistenceInterface<OAuth2ConsumerCredentialDto, OAuth2ConsumerCredentialEntity, String> {
  private OAuth2Provider provider;
  private String clientId;
  private String clientSecret;

  /**
   * Constructor.
   * 
   * @param provider
   * @param clientId
   * @param clientSecret
   */
  protected OAuth2ConsumerCredentialDto(OAuth2Provider provider, String clientId, 
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
    // TODO(arjuns): Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   * Here id should be set as null as {@link OAuth2Provider#name()} is used as the id.
   */
  @Override
  public OAuth2ConsumerCredentialEntity toPersistenceEntity(@Nullable String id) {
    checkArgument(id == null);
    
    return new OAuth2ConsumerCredentialEntity.Builder()
      .clientId(clientId)
      .clientSecret(clientSecret)
      .provider(provider.name())
      .build();
    
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public OAuth2ConsumerCredentialDto validate() {
    checkNotNull(provider);
    checkNotBlank(clientId);
    checkNotBlank(clientSecret);
    
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toXml() {
    // TODO(arjuns): Auto-generated method stub
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

  public OAuth2Provider getProvider() {
    return provider;
  }

  public String getClientId() {
    return clientId;
  }

  public String getClientSecret() {
    return clientSecret;
  }

  public static class Builder {
    private OAuth2Provider provider;
    private String clientId;
    private String clientSecret;

    public Builder provider(OAuth2Provider provider) {
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

    public OAuth2ConsumerCredentialDto build() {
      return new OAuth2ConsumerCredentialDto(provider, clientId, clientSecret);
    }
  }
}
