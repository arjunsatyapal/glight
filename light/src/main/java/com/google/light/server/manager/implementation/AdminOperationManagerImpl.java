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
package com.google.light.server.manager.implementation;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.light.server.constants.OAuth2ProviderEnum;

import com.google.inject.Inject;
import com.google.light.server.manager.interfaces.AdminOperationManager;
import com.google.light.server.persistence.dao.OAuth2ConsumerCredentialDao;
import com.google.light.server.persistence.entity.admin.OAuth2ConsumerCredentialEntity;
import java.util.List;

/**
 * Implementation for {@link AdminOperationManager}.
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class AdminOperationManagerImpl implements AdminOperationManager {
  private OAuth2ConsumerCredentialDao consumerCredentialDao;

  @Inject
  public AdminOperationManagerImpl(OAuth2ConsumerCredentialDao consumerCredentialDao) {
    this.consumerCredentialDao = checkNotNull(consumerCredentialDao);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public OAuth2ConsumerCredentialEntity putOAuth2ConsumerCredential(
      OAuth2ConsumerCredentialEntity entity) {
    return consumerCredentialDao.put(entity);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public OAuth2ConsumerCredentialEntity getOAuth2ConsumerCredential(
      OAuth2ProviderEnum provider) {
    return consumerCredentialDao.get(provider.name());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<OAuth2ConsumerCredentialEntity> getAllOAuth2ConsumerCredentials() {
    return consumerCredentialDao.getAllOAuth2ConsumerCredentials();
  }
}
