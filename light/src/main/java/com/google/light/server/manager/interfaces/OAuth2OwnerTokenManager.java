/*
 * Copyright 2012 Google Inc.
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

import com.google.light.server.persistence.entity.oauth2.owner.OAuth2OwnerTokenEntity;
import com.google.light.server.servlets.oauth2.google.pojo.AbstractOAuth2TokenInfo;

/**
 * Manager for OAuth2 Tokens for a Person.
 * 
 * TODO(arjuns): Add test for this class.
 * TODO(arjuns) : Add code for refreshToken for user unsubscription.
 * @author Arjun Satyapal
 */
public interface OAuth2OwnerTokenManager {
  /**
   * Fetch OAuth2 Token for a Person for given {@link OAuth2ProviderService}.
   * 
   * @param PersonId
   * @return
   */
  public OAuth2OwnerTokenEntity get();
  
  /**
   * Put OAuth2 Token on DataStore.
   * 
   * @param entity
   * @return
   */
  public OAuth2OwnerTokenEntity put(OAuth2OwnerTokenEntity entity);
  
  /**
   * Fetch an OAuth2 Token by ProviderUserId.
   * 
   * @param provider
   * @param providerUserId
   * @return
   */
  public OAuth2OwnerTokenEntity getByProviderUserId(String providerUserId);


  /**
   * Returns Tokeninfo for the current Token for given ProviderService.
   * 
   * @return
   */
  public <D extends AbstractOAuth2TokenInfo<D>> D getInfo();
  
  /**
   * Fetches TokenInfo using AccessToken.
   * 
   * @return
   */
  public <D extends AbstractOAuth2TokenInfo<D>> D getInfoByAccessToken(String accessToken);
  
  
  /**
   * Refresh OAuth2 Token.
   * 
   * TODO(arjuns): Add test for this.
   * 
   * @return
   */
  public OAuth2OwnerTokenEntity refresh();
}
