/*
 * Copyright 2012 Google Inc.
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
package com.google.light.server.manager.implementation.oauth2.owner;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.light.server.constants.OAuth2ProviderService;
import com.google.light.server.manager.interfaces.OAuth2OwnerTokenManager;
import com.google.light.server.persistence.dao.OAuth2OwnerTokenDao;
import com.google.light.server.persistence.entity.oauth2.owner.OAuth2OwnerTokenEntity;

/**
 * Implementation for {@link OAuth2OwnerTokenManager}.
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class OAuth2OwnerTokenManagerImpl implements OAuth2OwnerTokenManager {
  private OAuth2ProviderService providerService;
  private OAuth2OwnerTokenDao dao;

  @Inject
  public OAuth2OwnerTokenManagerImpl(@Assisted OAuth2ProviderService providerService,
      OAuth2OwnerTokenDao dao) {
    this.providerService = checkNotNull(providerService, "providerService");
    this.dao = checkNotNull(dao, "dao");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public OAuth2OwnerTokenEntity getToken(long personId) {
    return dao.get(personId, providerService);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public OAuth2OwnerTokenEntity putToken(OAuth2OwnerTokenEntity entity) {
    checkArgument(providerService == entity.getProviderService(), "TokenManager for "
        + providerService + " was used to persist Token of type " + entity.getProviderService());
    return dao.put(entity);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public OAuth2OwnerTokenEntity getTokenByProviderUserId(String providerUserId) {
    if (providerService.isUsedForLogin()) {
      return dao.getTokenByProviderUserId(providerService, providerUserId);
    }

    throw new IllegalArgumentException(
        "this method should not be called for non-login TokenManager"
            + " like " + providerService);
  }
}
