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
package com.google.light.server.manager.implementation.oauth2.consumer;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.light.server.constants.OAuth2Provider;
import com.google.light.server.manager.interfaces.OAuth2ConsumerManager;
import com.google.light.server.persistence.dao.OAuth2ConsumerCredentialDao;

/**
 * Test implementation for {@link OAuth2ConsumerManager}.
 * 
 * TODO(arjuns): Add test for this class.
 * TODO(arjuns): Add caching for this.
 * 
 * @author Arjun Satyapal
 */
public class AbstractOAuth2ConsumerManagerImpl implements OAuth2ConsumerManager {
  private OAuth2Provider oAuth2Provider;
  private OAuth2ConsumerCredentialDao dao;
  
  protected AbstractOAuth2ConsumerManagerImpl(OAuth2Provider oauth2Provider, 
      OAuth2ConsumerCredentialDao dao) {
    this.oAuth2Provider = checkNotNull(oauth2Provider);
    this.dao = checkNotNull(dao);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getClientId() {
    return dao.get(oAuth2Provider.name()).getClientId();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getClientSecret() {
    return dao.get(oAuth2Provider.name()).getClientSecret();
  }
}
