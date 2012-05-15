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

import org.codehaus.jackson.annotate.JsonTypeName;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.codehaus.jackson.annotate.JsonProperty;

import javax.xml.bind.annotation.XmlElement;

import javax.xml.bind.annotation.XmlRootElement;

import com.google.light.server.constants.OAuth2ProviderEnum;
import com.google.light.server.dto.AbstractDto;
import com.google.light.server.dto.AbstractDtoToPersistence;
import com.google.light.server.persistence.entity.admin.OAuth2ConsumerCredentialEntity;

/**
 * DTO for OAuth2Consumer Credentials. Corresponding Entity is
 * {@link OAuth2ConsumerCredentialEntity}.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
@JsonTypeName(value = "oauth2ConsumerCredential")
@XmlRootElement(name = "oauth2ConsumerCredential")
@XmlAccessorType(XmlAccessType.FIELD)
public class OAuth2ConsumerCredentialDto extends
    AbstractDtoToPersistence<OAuth2ConsumerCredentialDto, OAuth2ConsumerCredentialEntity, String> {
  @XmlElement(name = "provider")
  @JsonProperty(value = "provider")
  private OAuth2ProviderEnum provider;
  
  @XmlElement(name = "clientId")
  @JsonProperty(value = "clientId")
  private String clientId;
  
  @XmlElement(name = "clientSecret")
  @JsonProperty(value = "clientSecret")
  private String clientSecret;

  @XmlElement(name = "extraData")
  @JsonProperty(value = "extraData")
  private String extraData;

  // /**
  // * Constructor.
  // *
  // * @param provider
  // * @param clientId
  // * @param clientSecret
  // */
  // protected OAuth2ConsumerCredentialDto(OAuth2ProviderEnum provider, String clientId,
  // String clientSecret) {
  // this.provider = provider;
  // this.clientId = clientId;
  // this.clientSecret = clientSecret;
  //
  // this.validate();
  // }

  /**
   * {@inheritDoc} This method should not be called.
   * 
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
        .extraData(extraData)
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

  public OAuth2ProviderEnum getProvider() {
    return provider;
  }

  public String getClientId() {
    return clientId;
  }

  public String getClientSecret() {
    return clientSecret;
  }

  public String getExtraData() {
    return extraData;
  }

  public static class Builder extends AbstractDto.BaseBuilder<Builder> {
    private OAuth2ProviderEnum provider;
    private String clientId;
    private String clientSecret;
    private String extraData;

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

    public Builder extraData(String extraData) {
      this.extraData = extraData;
      return this;
    }

    @SuppressWarnings("synthetic-access")
    public OAuth2ConsumerCredentialDto build() {
      return new OAuth2ConsumerCredentialDto(this).validate();
    }
  }

  @SuppressWarnings("synthetic-access")
  private OAuth2ConsumerCredentialDto(Builder builder) {
    super(builder);
    this.provider = builder.provider;
    this.clientId = builder.clientId;
    this.clientSecret = builder.clientSecret;
    this.extraData  = builder.extraData;
  }

  // For JAXB
  private OAuth2ConsumerCredentialDto() {
    super(null);
  }
}
