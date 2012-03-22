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

import com.google.inject.Inject;
import com.google.light.server.constants.OAuth2Provider;
import com.google.light.server.manager.interfaces.OAuth2ConsumerCredentialManager;
import com.google.light.server.persistence.dao.OAuth2ConsumerCredentialDao;

/**
 * Test implementation for {@link OAuth2ConsumerCredentialManager}.
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class GoogleOAuth2ConsumerManagerImpl extends AbstractOAuth2ConsumerManagerImpl {
  @Inject
  public GoogleOAuth2ConsumerManagerImpl(OAuth2ConsumerCredentialDao dao) {
    super(OAuth2Provider.GOOGLE_LOGIN, dao);
  }
}
