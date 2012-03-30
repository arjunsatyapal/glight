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
package com.google.light.server.manager.implementation.oauth2.consumer;

import static com.google.light.server.utils.GuiceUtils.getInstance;
import static com.google.light.testingutils.TestingUtils.getRandomString;
import static org.junit.Assert.assertEquals;

import com.google.light.server.manager.interfaces.OAuth2ConsumerCredentialManager;

import org.junit.Test;

import com.google.light.server.AbstractLightServerTest;
import com.google.light.server.constants.OAuth2ProviderEnum;
import com.google.light.server.manager.interfaces.AdminOperationManager;
import com.google.light.server.persistence.entity.admin.OAuth2ConsumerCredentialEntity;

/**
 * Test for {@link OAuth2ConsumerCredentialManagerImpl}.
 *
 * @author Arjun Satyapal
 */
public class OAuth2ConsumerCredentialManagerImplTest extends AbstractLightServerTest {
  private String clientId;
  private String clientSecret;
  private OAuth2ProviderEnum defaultProvider = OAuth2ProviderEnum.GOOGLE;
  private AdminOperationManager adminOperationManager;
  private OAuth2ConsumerCredentialManagerFactory factory;
  private OAuth2ConsumerCredentialManager defaultCredentialManager;
  @Override
  public void setUp() {
    super.setUp();
    this.clientId = getRandomString();
    this.clientSecret = getRandomString();
    this.adminOperationManager = getInstance(injector, AdminOperationManager.class);
    adminOperationManager.putOAuth2ConsumerCredential(getConsumerCredentialEntityBuilder().build());
    factory = getInstance(injector,  OAuth2ConsumerCredentialManagerFactory.class);
    defaultCredentialManager = factory.create(defaultProvider);
  }
  
  @Override
  public void tearDown() {
    adminOperationManager = null;
    super.tearDown();
  }
  
  private OAuth2ConsumerCredentialEntity.Builder getConsumerCredentialEntityBuilder() {
    return new OAuth2ConsumerCredentialEntity.Builder()
      .clientId(clientId)
      .clientSecret(clientSecret)
      .oAuth2ProviderKey(defaultProvider.name());
  }
  
  /**
   * Test for {@link OAuth2ConsumerCredentialManagerImpl#getClientId()}.
   */
  @Test
  public void test_getClientId() {
    assertEquals(clientId, defaultCredentialManager.getClientId());
  }
  
  /**
   * Test for {@link OAuth2ConsumerCredentialManagerImpl#getClientSecret())}.
   */
  @Test
  public void test_getClientSecret() {
    assertEquals(clientSecret, defaultCredentialManager.getClientSecret());
  }
}
