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
package com.google.light.server.persistence.dao;

import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.light.server.constants.OAuth2ProviderEnum;
import com.google.light.server.dto.admin.OAuth2ConsumerCredentialDto;
import com.google.light.server.persistence.entity.admin.OAuth2ConsumerCredentialEntity;
import com.google.light.server.utils.ObjectifyUtils;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import java.util.List;

/**
 * DAO for {@link OAuth2ConsumerCredentialEntity}.
 * 
 * @author Arjun Satyapal
 */
public class OAuth2ConsumerCredentialDao extends
    AbstractBasicDao<OAuth2ConsumerCredentialDto, OAuth2ConsumerCredentialEntity> {
  static {
    ObjectifyService.register(OAuth2ConsumerCredentialEntity.class);
  }

  @Inject
  public OAuth2ConsumerCredentialDao() {
    super(OAuth2ConsumerCredentialEntity.class);
  }
  
  /** 
   * Method to get all OAuth2Consumer credentials for all {@link OAuth2ProviderService} for Light.
   */
  public List<OAuth2ConsumerCredentialEntity> getAllOAuth2ConsumerCredentials() {
    Objectify ofy = ObjectifyUtils.nonTransaction();
    QueryResultIterable<OAuth2ConsumerCredentialEntity> resultsIterable = 
        ofy.query(OAuth2ConsumerCredentialEntity.class).fetch();
    
    return Lists.newArrayList(resultsIterable);
  }
  
  /**
   * TODO(arjuns): Add test for this.
   * Fetch Consumer Credential Dao via ProviderName.
   */
  public OAuth2ConsumerCredentialEntity get(OAuth2ProviderEnum provider) {
    return super.get(OAuth2ConsumerCredentialEntity.generateKey(provider.name()));
  }
}
