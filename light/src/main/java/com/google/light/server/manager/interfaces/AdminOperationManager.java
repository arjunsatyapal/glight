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
package com.google.light.server.manager.interfaces;

import java.util.List;

import com.google.light.server.constants.OAuth2Provider;
import com.google.light.server.persistence.entity.admin.OAuth2ConsumerCredentialEntity;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public interface AdminOperationManager {
  /**
   * Method to Create/Updated OAuth2Consumer Credential for Light for {@link OAuth2Provider}.
   * 
   * @param dto
   * @return
   */
  public OAuth2ConsumerCredentialEntity putOAuth2ConsumerCredential(
      OAuth2ConsumerCredentialEntity entity);

  /**
   * Method to fetch OAuth2ConsumerCredentials from Persistence.
   * 
   * @param provider
   * @return
   */
  public OAuth2ConsumerCredentialEntity getOAuth2ConsumerCredential(OAuth2Provider provider);
  
  /**
   * Method to fetch all OAuth2ConsumerCredentials.
   * @return
   */
  public List<OAuth2ConsumerCredentialEntity> getAllOAuth2ConsumerCredentials();
}
