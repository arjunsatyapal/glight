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

import com.google.light.server.constants.OAuth2ProviderEnum;
import com.google.light.server.manager.interfaces.OAuth2ConsumerCredentialManager;

/**
 * Guice Factory for {@link OAuth2ConsumerCredentialManager} which is binded to
 * {@link OAuth2ConsumerCredentialManagerImpl}
 *
 * @author Arjun Satyapal
 */
public interface OAuth2ConsumerCredentialManagerFactory {
  public OAuth2ConsumerCredentialManager create(OAuth2ProviderEnum provider);
}
