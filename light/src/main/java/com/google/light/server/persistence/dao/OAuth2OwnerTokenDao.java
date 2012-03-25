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

import com.google.inject.Inject;
import com.google.light.server.dto.oauth2.owner.OAuth2OwnerTokenDto;
import com.google.light.server.persistence.entity.oauth2.owner.OAuth2OwnerTokenEntity;
import com.googlecode.objectify.ObjectifyService;

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
}
