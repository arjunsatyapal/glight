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
package com.google.light.server.servlets.thirdparty.google.gdoc;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.constants.LightConstants.HTTP_CONNECTION_TIMEOUT_IN_MILLIS;
import static com.google.light.server.constants.LightStringConstants.LIGHT_APPLICATION_NAME;
import static com.google.light.server.constants.OAuth2ProviderService.GOOGLE_DOC;

import com.google.gdata.client.sites.SitesService;
import com.google.inject.Inject;
import com.google.light.server.manager.implementation.oauth2.owner.OAuth2OwnerTokenManagerFactory;
import com.google.light.server.manager.interfaces.OAuth2OwnerTokenManager;
import com.google.light.server.persistence.entity.oauth2.owner.OAuth2OwnerTokenEntity;
import com.google.light.server.servlets.GSitesTestServlet;

/**
 * Simple wrapper class for the {@link SitesService} analogous
 * to the {@link DocsServiceWrapper}.
 * 
 * This is being used by some proof of concept codes for
 * integration with Google Sites.
 * 
 * @author Walter Cacau
 * @see GSitesTestServlet
 *
 */
public class SitesServiceWrapper extends SitesService {


  @Inject
  public SitesServiceWrapper(OAuth2OwnerTokenManagerFactory ownerTokenManagerFactory) {
    super(LIGHT_APPLICATION_NAME);
    OAuth2OwnerTokenManagerFactory tokenManagerFactory = checkNotNull(
        ownerTokenManagerFactory, "ownerTokenManagerFactory");
    OAuth2OwnerTokenManager googDocTokenManager = tokenManagerFactory.create(GOOGLE_DOC);
    OAuth2OwnerTokenEntity token = googDocTokenManager.get();

    String authorizationToken = token.getAuthorizationToken();
    setUserToken(token.getAuthorizationToken());
    getRequestFactory().setHeader("Authorization", authorizationToken);
    getRequestFactory().setHeader("GData-Version", "2.0");
    setConnectTimeout(HTTP_CONNECTION_TIMEOUT_IN_MILLIS);
  }

}
