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
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.light.server.constants.OAuth2ProviderEnum;
import com.google.light.server.manager.interfaces.OAuth2ConsumerCredentialManager;
import com.google.light.server.persistence.dao.OAuth2ConsumerCredentialDao;

/**
 * Test implementation for {@link OAuth2ConsumerCredentialManager}.
 * 
 * TODO(arjuns): Add mem-cache for this.
 * 
 * @author Arjun Satyapal
 */
public class OAuth2ConsumerCredentialManagerImpl implements OAuth2ConsumerCredentialManager {
  private String clientId;
  private String clientSecret;
  
  @Inject
  public OAuth2ConsumerCredentialManagerImpl(@Assisted OAuth2ProviderEnum provider, 
      OAuth2ConsumerCredentialDao dao) {
    checkNotNull(provider);
    checkNotNull(dao);
    
    this.clientId = checkNotBlank(dao.get(provider).getClientId(),
        "clientId for provider[" + provider + "].");
    
    this.clientSecret = checkNotBlank(dao.get(provider).getClientSecret(),
        "clientSecret for provider[" + provider + "].");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getClientId() {
    return clientId;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getClientSecret() {
    return clientSecret;
  }
}
