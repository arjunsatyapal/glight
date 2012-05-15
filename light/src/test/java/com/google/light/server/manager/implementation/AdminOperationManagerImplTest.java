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
package com.google.light.server.manager.implementation;

import static com.google.light.server.constants.OAuth2ProviderEnum.GOOGLE;
import static com.google.light.server.constants.OAuth2ProviderEnum.GSS;
import static com.google.light.server.utils.GuiceUtils.getInstance;
import static com.google.light.testingutils.TestingUtils.getRandomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.google.light.server.AbstractLightServerTest;
import com.google.light.server.constants.OAuth2ProviderEnum;
import com.google.light.server.manager.interfaces.AdminOperationManager;
import com.google.light.server.persistence.entity.admin.OAuth2ConsumerCredentialEntity;

/**
 * Test for {@link AdminOperationManagerImpl}
 * 
 * @author Arjun Satyapal
 */
public class AdminOperationManagerImplTest extends AbstractLightServerTest {
  private AdminOperationManager adminOperationManager;
  private String clientId;
  private String clientSecret;
  private OAuth2ProviderEnum defaultProvider = OAuth2ProviderEnum.GOOGLE;

  @Override
  public void setUp() {
    super.setUp();
    this.adminOperationManager = getInstance(AdminOperationManager.class);
    this.clientId = getRandomString();
    this.clientSecret = getRandomString();
  }

  @Override
  public void tearDown() {
    adminOperationManager = null;
    super.tearDown();
  }

  private OAuth2ConsumerCredentialEntity.Builder getEntityBuilder() {
    return new OAuth2ConsumerCredentialEntity.Builder()
      .clientId(clientId)
      .clientSecret(clientSecret)
      .providerName(defaultProvider.name());
  }
  
  /**
   * Test for
   * {@link AdminOperationManagerImpl#putOAuth2OAuth2ConsumerCredential(OAuth2ConsumerCredentialEntity)}
   */
  @Test
  public void test_putOAuth2OAuth2ConsumerCredential() {
    do_test_put_get_OAuth2ConsumerCrednetial();
  }
  
  /**
   * Test for
   * {@link AdminOperationManagerImpl#getOAuth2OAuth2ConsumerCredential(OAuth2ConsumerCredentialEntity)}
   */
  @Test
  public void test_getOAuth2OAuth2ConsumerCredential() {
    do_test_put_get_OAuth2ConsumerCrednetial();
  }
  
  private void do_test_put_get_OAuth2ConsumerCrednetial() {
    OAuth2ConsumerCredentialEntity entity = getEntityBuilder().build();
    OAuth2ConsumerCredentialEntity savedEntity = 
        adminOperationManager.putOAuth2ConsumerCredential(entity);
    assertEquals(entity, savedEntity);
    
    // Saving twice should still work.
    OAuth2ConsumerCredentialEntity savedEntity1 = 
        adminOperationManager.putOAuth2ConsumerCredential(entity);
    assertEquals(savedEntity, savedEntity1);
    
    OAuth2ConsumerCredentialEntity fetchedEntity = 
        adminOperationManager.getOAuth2ConsumerCredential(defaultProvider);
    assertEquals(entity, fetchedEntity);
  }
  
  /**
   * Test for {@link AdminOperationManagerImpl#getAllOAuth2ConsumerCredentials()}
   */
  @Test
  public void test_getAllOAuth2ConsumerCredentials() {
    assertEquals("Add newly added provider here.", 2, OAuth2ProviderEnum.values().length);
    OAuth2ConsumerCredentialEntity googleTestEntity =
        getEntityBuilder().providerName(GOOGLE.name()).build();
    OAuth2ConsumerCredentialEntity gssTestEntity =
        getEntityBuilder().providerName(GSS.name()).build();
    adminOperationManager.putOAuth2ConsumerCredential(googleTestEntity);
    adminOperationManager.putOAuth2ConsumerCredential(gssTestEntity);
    
    List<OAuth2ConsumerCredentialEntity> entities = 
        adminOperationManager.getAllOAuth2ConsumerCredentials();
    assertEquals(2, entities.size());
    assertTrue(entities.contains(googleTestEntity));
    assertTrue(entities.contains(gssTestEntity));
  }
}
