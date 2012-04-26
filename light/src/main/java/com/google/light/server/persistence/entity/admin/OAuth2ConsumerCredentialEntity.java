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

import com.google.light.server.persistence.entity.AbstractPersistenceEntity;

import com.google.light.server.constants.OAuth2ProviderEnum;
import com.google.light.server.dto.admin.OAuth2ConsumerCredentialDto;
import com.google.light.server.persistence.PersistenceToDtoInterface;
import com.googlecode.objectify.Key;
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
public class OAuth2ConsumerCredentialEntity extends
    AbstractPersistenceEntity<OAuth2ConsumerCredentialEntity, OAuth2ConsumerCredentialDto> {
  @Id
  private String providerName;
  private String clientId;
  private String clientSecret;

  /**
   * {@inheritDoc}
   */
  @Override
  public OAuth2ConsumerCredentialDto toDto() {
    return new OAuth2ConsumerCredentialDto.Builder()
        .provider(OAuth2ProviderEnum.valueOf(providerName))
        .clientId(clientId)
        .clientSecret(clientSecret)
        .build();
  }

  public OAuth2ProviderEnum getOAuth2Provider() {
    return OAuth2ProviderEnum.valueOf(providerName);
  }

  public String getClientId() {
    return clientId;
  }

  public String getClientSecret() {
    return clientSecret;
  }

  public static class Builder extends AbstractPersistenceEntity.BaseBuilder<Builder>{
    private String providerName;
    private String clientId;
    private String clientSecret;

    public Builder providerName(String providerName) {
      this.providerName = providerName;
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
      return new OAuth2ConsumerCredentialEntity(this).validate();
    }
  }

  @SuppressWarnings("synthetic-access")
  private OAuth2ConsumerCredentialEntity(Builder builder) {
    super(builder, false);
    // Ensures that oAuth2ProviderKey is valid.
    checkNotNull(OAuth2ProviderEnum.valueOf(builder.providerName), "invalid providerName["
        + providerName + "].");
    this.providerName = checkNotBlank(builder.providerName, "providerName");
    this.clientId = checkNotBlank(builder.clientId, "clientId is blank");
    this.clientSecret = checkNotBlank(builder.clientSecret, "clientSecret");
  }
  
  // For Objectify.
  private OAuth2ConsumerCredentialEntity() {
    super(null, false);
  }

  /** 
   * {@inheritDoc}
   */
  @Override
  public Key<OAuth2ConsumerCredentialEntity> getKey() {
    return generateKey(providerName);
  }
  
  /**
   * Method to generate Objectify key for {@link OAuth2ConsumerCredentialEntity}.
   * 
   * @param providerName
   * @return
   */
  public static Key<OAuth2ConsumerCredentialEntity> generateKey(String providerName) {
    // Ensure that Provider is valid.
    try {
      checkNotBlank(providerName, "provider");
      checkNotNull(OAuth2ProviderEnum.valueOf(providerName), "providerEnum");
    } catch (Exception e) {
      throw new EnumConstantNotPresentException(OAuth2ProviderEnum.class, providerName);
    }
    
    return new Key<OAuth2ConsumerCredentialEntity>(OAuth2ConsumerCredentialEntity.class,
        providerName);
  }
}
