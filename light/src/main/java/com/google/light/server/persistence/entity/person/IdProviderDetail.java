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
package com.google.light.server.persistence.entity.person;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkEmail;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;

import org.apache.commons.lang.builder.EqualsBuilder;

import org.apache.commons.lang.builder.HashCodeBuilder;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.google.light.server.constants.OpenIdAuthDomain;
import com.google.light.server.dto.person.PersonDto;
import com.google.light.server.persistence.PersistenceToDtoInterface;

/**
 *  We are storing these so that in future if we need to add support more Federated Providers
 *  then we can add it.
 *  TODO(arjuns): Update in context of OAuth2Provider.
 * @author Arjun Satyapal
 */
@SuppressWarnings({ "deprecation", "serial" })
public class IdProviderDetail implements PersistenceToDtoInterface<PersonEntity, PersonDto> {
  private OpenIdAuthDomain authDomain;
  private String email;
  private String federatedIdentity;

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

  // TODO(arjuns): Add a builder and make constructor private.
  public IdProviderDetail(OpenIdAuthDomain authDomain, String email, String federatedIdentity) {
    this.authDomain = checkNotNull(authDomain);
    this.email = checkEmail(email);
    this.federatedIdentity = checkNotBlank(federatedIdentity);
  }

  public OpenIdAuthDomain getAuthDomain() {
    return authDomain;
  }

  public String getEmail() {
    return email;
  }

  public String getFederatedIdentity() {
    return federatedIdentity;
  }

  // For Objectify.
  @SuppressWarnings("unused")
  private IdProviderDetail() {
  }

  /** 
   * {@inheritDoc}
   */
  @Override
  public PersonDto toDto() {
    // TODO(arjuns): Auto-generated method stub
    throw new UnsupportedOperationException();
  }
}
