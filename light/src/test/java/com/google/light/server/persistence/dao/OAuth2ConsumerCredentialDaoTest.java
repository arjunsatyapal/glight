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
package com.google.light.server.persistence.dao;

import static com.google.light.server.utils.GuiceUtils.getInstance;
import static com.google.light.server.utils.ObjectifyUtils.commitTransaction;
import static com.google.light.server.utils.ObjectifyUtils.initiateTransaction;
import static com.google.light.testingutils.TestingConstants.DEFAULT_CLIENT_ID;
import static com.google.light.testingutils.TestingConstants.DEFAULT_CLIENT_SECRET;
import static com.google.light.testingutils.TestingUtils.getRandomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.light.server.constants.OAuth2ProviderEnum;
import com.google.light.server.dto.admin.OAuth2ConsumerCredentialDto;
import com.google.light.server.persistence.entity.admin.OAuth2ConsumerCredentialEntity;
import com.google.light.server.utils.ObjectifyUtils;
import com.googlecode.objectify.Objectify;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link OAuth2ConsumerCredentialDao}
 * 
 * @author Arjun Satyapal
 */
public class OAuth2ConsumerCredentialDaoTest extends
    AbstractBasicDaoTest<OAuth2ConsumerCredentialDto, OAuth2ConsumerCredentialEntity> {

  private OAuth2ProviderEnum defaultProvider = OAuth2ProviderEnum.GOOGLE;
  public OAuth2ConsumerCredentialDaoTest() {
    super(OAuth2ConsumerCredentialEntity.class);
  }

  private OAuth2ConsumerCredentialDao dao;

  @Override
  @Before
  public void setUp() {
    super.setUp();
    dao = getInstance(OAuth2ConsumerCredentialDao.class);
  }

  private OAuth2ConsumerCredentialEntity.Builder getDefaultEntityBuilder() {
    return new OAuth2ConsumerCredentialEntity.Builder()
        .clientId(DEFAULT_CLIENT_ID)
        .clientSecret(DEFAULT_CLIENT_SECRET)
        .providerName(defaultProvider.name());
  }

  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_get_by_key_in_txn() {
    OAuth2ConsumerCredentialEntity testEntity = getDefaultEntityBuilder().build();
    OAuth2ConsumerCredentialEntity savedEntity = dao.put(testEntity);

    Objectify ofy = ObjectifyUtils.initiateTransaction();
    try {
      OAuth2ConsumerCredentialEntity getEntity = dao.get(ofy, savedEntity.getKey());
      assertEquals(savedEntity, getEntity);
    } catch (Exception e) {
      ObjectifyUtils.rollbackTransactionIfStillActive(ofy);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_get_by_key() {
    OAuth2ConsumerCredentialEntity testEntity = getDefaultEntityBuilder().build();

    OAuth2ConsumerCredentialEntity savedEntity = dao.put(testEntity);
    OAuth2ConsumerCredentialEntity getEntity = dao.get(savedEntity.getKey());
    assertEquals(savedEntity, getEntity);

    /*
     * Negative test : Since we are using Enum name as a key, and we dont allow custom enum
     * construction, so negative test is not possible at the moment.
     */
    
  }

  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_put_txn() {
    OAuth2ConsumerCredentialEntity testEntity = getDefaultEntityBuilder().build();

    // Positive Test :
    Objectify txn = initiateTransaction();
    assertTrue(txn.getTxn().isActive());

    dao.put(txn, testEntity);
    commitTransaction(txn);
    OAuth2ConsumerCredentialEntity getEntity = dao.get(testEntity.getOAuth2Provider());
    assertEquals(testEntity, getEntity);

    /*
     * Negative : No negative test, as the key is based on OAuth2Provider and therefore any attempt
     * to put will create/update entry. And there is no-uniqueness constraint across two different
     * entities.
     */
  }

  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_put() {
    OAuth2ConsumerCredentialEntity testEntity = getDefaultEntityBuilder().build();
    OAuth2ConsumerCredentialEntity savedEntity = dao.put(testEntity);

    OAuth2ConsumerCredentialEntity getEntity = dao.get(testEntity.getOAuth2Provider());
    assertEquals(savedEntity, getEntity);

    // Same entity can be persisted twice. Change clientId & clientSecret and persist.
    String newClientId = getRandomString();
    String newClientSecret = getRandomString();

    testEntity = getDefaultEntityBuilder()
        .clientId(newClientId)
        .clientSecret(newClientSecret)
        .build();

    OAuth2ConsumerCredentialEntity newSavedEntity = dao.put(testEntity);
    assertEquals(newSavedEntity, dao.put(testEntity));
    assertEquals(newSavedEntity, dao.put(testEntity));

    // Ensures that savedEntity and newSavedEntity refer to same Entity.
    assertEquals(savedEntity.getOAuth2Provider(), newSavedEntity.getOAuth2Provider());
    // Ensures that savedEntity and newSavedEntity are not equal post modification.
    assertTrue(!savedEntity.equals(newSavedEntity));

    // Now original clientId and clientSecret are set back.
    testEntity = getDefaultEntityBuilder().build();
    newSavedEntity = dao.put(testEntity);
    assertEquals(savedEntity, newSavedEntity);

    /*
     * Negative : No negative test, as the key is based on OAuth2Provider and therefore any attempt
     * to put will create/update entry. And there is no-uniqueness constraint across two different
     * entities.
     */
  }
  
  /**
   * Test for {@link OAuth2ConsumerCredentialDao#getAllOAuth2ConsumerCredentials()}.
   */
  @Test
  public void test_getAllOAuth2ConsumerCredentials() {
    assertEquals("When more providers are added, update this test.", 
        1, OAuth2ProviderEnum.values().length);
    
    OAuth2ConsumerCredentialEntity testEntity = getDefaultEntityBuilder().build();
    dao.put(testEntity);
    
    List<OAuth2ConsumerCredentialEntity> entities = dao.getAllOAuth2ConsumerCredentials();
    assertEquals(1, entities.size());
    assertTrue(entities.contains(testEntity));
    
  }
}
