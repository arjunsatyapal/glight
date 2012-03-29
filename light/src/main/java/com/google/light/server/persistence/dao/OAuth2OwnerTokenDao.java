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
package com.google.light.server.persistence.dao;

import com.google.inject.Inject;
import com.google.light.server.constants.OAuth2ProviderService;
import com.google.light.server.dto.oauth2.owner.OAuth2OwnerTokenDto;
import com.google.light.server.persistence.entity.oauth2.owner.OAuth2OwnerTokenEntity;
import com.google.light.server.utils.ObjectifyUtils;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Query;

/**
 * DAO for {@link OAuth2OwnerTokenEntity}.
 * 
 * @author Arjun Satyapal
 */
public class OAuth2OwnerTokenDao extends
    AbstractBasicDao<OAuth2OwnerTokenDto, OAuth2OwnerTokenEntity, String> {
  static {
    ObjectifyService.register(OAuth2OwnerTokenEntity.class);
  }

  @Inject
  public OAuth2OwnerTokenDao() {
    super(OAuth2OwnerTokenEntity.class, String.class);
  }

  /**
   * Fetch OAuth2OwnerTokenEntity by ProviderUserId. The underlying assumption is that only one
   * entry exists with combination of {@link OAuth2ProviderService} and providerUserId.
   * <p>
   * It does not matter if providerUserId is reused by different Provider.
   * 
   * TODO(arjuns) : Add test for this.
   * 
   * @param email
   * @return
   */
  public OAuth2OwnerTokenEntity getTokenByProviderUserId(
      OAuth2ProviderService providerService, String providerUserId) {
    Objectify ofy = ObjectifyUtils.nonTransaction();

    Query<OAuth2OwnerTokenEntity> query = ofy.query(OAuth2OwnerTokenEntity.class)
        .filter(OAuth2OwnerTokenEntity.OFY_PROVIDER, providerService)
        .filter(OAuth2OwnerTokenEntity.OFY_PROVIDER_USER_ID, providerUserId);

    String errMessage = "Found more then one entry for providerService[" + providerService +
        "], providerUserId[" + providerUserId + "].";
    return ObjectifyUtils.assertAndReturnUniqueEntity(query, errMessage);
  }

  /**
   * OAuth2 Owner Tokens dont use a DataStore generatedId. Instead they have a derived Id.
   * Therefore, clients should use {{@link #get(String, OAuth2ProviderService)}.
   */
  @Override
  public OAuth2OwnerTokenEntity get(String id) {
    throw new UnsupportedOperationException(
        "this should not be called. Instead use get(String, OAuth2Provider)");
  }

  /**
   * Fetch OAuth2 Owner token from Datastore using PersonId and OAuth2Provider.
   * 
   * @param personId
   * @param providerService
   * @return
   */
  public OAuth2OwnerTokenEntity get(long personId, OAuth2ProviderService providerService) {
    String fetchId = personId + "." + providerService.name();
    return super.get(fetchId);
  }
}
