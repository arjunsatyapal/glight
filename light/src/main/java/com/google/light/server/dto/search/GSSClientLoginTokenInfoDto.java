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
package com.google.light.server.dto.search;

import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.LightPreconditions.checkPositiveLong;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

import com.google.light.server.dto.AbstractDto;
import com.google.light.server.utils.JsonUtils;

/**
 * Dto for storing the information about ClientLogin Authentication Token
 * used with CSE Control API for controlling GSS.
 * 
 * It is meant to be serialized as JSON string and stored in
 * {@link ConsumerCredentialDto#getExtraData()} for the {@link ConsumerCredentialDto} instance
 * regarding {@link ConsumerAuthenticationEnum#GSS} .
 * 
 * @author Walter Cacau
 */
@SuppressWarnings("serial")
@JsonTypeName(value = "gssClientLoginTokenInfo")
@XmlRootElement(name = "gssClientLoginTokenInfo")
@XmlAccessorType(XmlAccessType.FIELD)
public class GSSClientLoginTokenInfoDto extends
    AbstractDto<GSSClientLoginTokenInfoDto> {

  /**
   * Authorization token to be sent as the HTTP Authorization Header.
   */
  @XmlElement(name = "token")
  @JsonProperty(value = "token")
  private String token;

  /**
   * Expire date in milliseconds as expressed by {@link Date#getTime()}
   */
  @XmlElement(name = "expiresInMillis")
  @JsonProperty(value = "expiresInMillis")
  private Long expiresInMillis;

  @Override
  public String toXml() {
    throw new UnsupportedOperationException();
  }

  @Override
  public GSSClientLoginTokenInfoDto validate() {
    checkNotBlank(token, "token");
    checkPositiveLong(expiresInMillis,
        "expiresInMillis should be a positive long");
    return this;
  }

  public String getToken() {
    return token;
  }

  public Long getExpiresInMillis() {
    return expiresInMillis;
  }

  public static class Builder extends AbstractDto.BaseBuilder<Builder> {
    private String token;
    private Long expiresInMillis;

    public Builder token(String token) {
      this.token = token;
      return this;
    }

    public Builder expiresInMillis(Long expiresInMillis) {
      this.expiresInMillis = expiresInMillis;
      return this;
    }

    public GSSClientLoginTokenInfoDto build() {
      return new GSSClientLoginTokenInfoDto(this).validate();
    }
  }

  private GSSClientLoginTokenInfoDto(Builder builder) {
    super(builder);
    this.token = builder.token;
    this.expiresInMillis = builder.expiresInMillis;
  }

  // For JAXB
  private GSSClientLoginTokenInfoDto() {
    super(null);
  }
}
