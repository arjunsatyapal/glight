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
package com.google.light.server.manager.implementation.oauth2.owner;

import com.google.light.server.manager.interfaces.OAuth2OwnerTokenManager;

import com.google.light.server.constants.OAuth2ProviderService;

/**
 * Guice Factory for {@link OAuth2OwnerTokenManager} which is implemented by 
 * {@link OAuth2OwnerTokenManagerImpl}.
 * 
 * @author Arjun Satyapal
 */
public interface OAuth2OwnerTokenManagerFactory {
  public OAuth2OwnerTokenManager create(OAuth2ProviderService providerService);
}