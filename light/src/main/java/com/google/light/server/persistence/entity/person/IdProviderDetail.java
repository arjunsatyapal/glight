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
import static com.google.light.server.utils.LightPreconditions.checkNotEmptyString;

import com.google.light.server.constants.OpenIdAuthDomain;

/**
 *  We are storing these so that in future if we need to add support more Federated Providers
 *  then we can add it.
 *  
 * @author Arjun Satyapal
 */
public class IdProviderDetail {
  private OpenIdAuthDomain authDomain;
  private String email;
  private String federatedIdentity;

  @Override
  public String toString() {
    return "IdentityProviderDetail [authDomain=" + authDomain + ", email=" + email
        + ", federatedIdentity=" + federatedIdentity + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((authDomain == null) ? 0 : authDomain.hashCode());
    result = prime * result + ((email == null) ? 0 : email.hashCode());
    result = prime * result + ((federatedIdentity == null) ? 0 : federatedIdentity.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    IdProviderDetail other = (IdProviderDetail) obj;
    if (authDomain != other.authDomain)
      return false;
    if (email == null) {
      if (other.email != null)
        return false;
    } else if (!email.equals(other.email))
      return false;
    if (federatedIdentity == null) {
      if (other.federatedIdentity != null)
        return false;
    } else if (!federatedIdentity.equals(other.federatedIdentity))
      return false;
    return true;
  }

  public IdProviderDetail(OpenIdAuthDomain authDomain, String email, String federatedIdentity) {
    this.authDomain = checkNotNull(authDomain);
    this.email = checkNotEmptyString(email);
    this.federatedIdentity = checkNotEmptyString(federatedIdentity);
    this.federatedIdentity = checkNotEmptyString(federatedIdentity);
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

  // For Objecfity.
  @SuppressWarnings("unused")
  private IdProviderDetail() {
  }
}
