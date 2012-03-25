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
import static com.google.light.server.utils.ObjectifyUtils.getKey;
import static com.google.light.server.utils.ObjectifyUtils.initiateTransaction;
import static com.google.light.testingutils.TestingConstants.DEFAULT_CLIENT_ID;
import static com.google.light.testingutils.TestingConstants.DEFAULT_CLIENT_SECRET;
import static com.google.light.testingutils.TestingUtils.getRandomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.light.server.dto.admin.OAuth2ConsumerCredentialDto;
import com.google.light.server.persistence.entity.admin.OAuth2ConsumerCredentialEntity;
import com.google.light.server.utils.ObjectifyUtils;
import com.google.light.testingutils.TestingUtils;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link OAuth2ConsumerCredentialDao}
 * 
 * @author Arjun Satyapal
 */
public class OAuth2ConsumerCredentialDaoTest extends
    AbstractBasicDaoTest<OAuth2ConsumerCredentialDto, OAuth2ConsumerCredentialEntity, String> {

  public OAuth2ConsumerCredentialDaoTest() {
    super(OAuth2ConsumerCredentialEntity.class, String.class);
  }

  private OAuth2ConsumerCredentialDao dao;
  private OAuth2ConsumerCredentialEntity.Builder defaultEntityBuilder;

  @Override
  @Before
  public void setUp() {
    super.setUp();
    dao = getInstance(injector, OAuth2ConsumerCredentialDao.class);
    defaultEntityBuilder = new OAuth2ConsumerCredentialEntity.Builder()
        .clientId(DEFAULT_CLIENT_ID)
        .clientSecret(DEFAULT_CLIENT_SECRET)
        .oAuth2ProviderKey(defaultLoginProvider.name());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void test_get_ofyKey() {
    OAuth2ConsumerCredentialEntity testEntity = defaultEntityBuilder.build();
    OAuth2ConsumerCredentialEntity savedEntity = dao.put(testEntity);

    Objectify ofy = ObjectifyUtils.initiateTransaction();
    try {
      Key<OAuth2ConsumerCredentialEntity> key = dao.getKey(savedEntity.getOAuth2ProviderKey());
      OAuth2ConsumerCredentialEntity getEntity = dao.get(ofy, key);
      assertEquals(savedEntity, getEntity);
    } catch (Exception e) {
      ObjectifyUtils.rollbackTransaction(ofy);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_get_ofyId() {
    OAuth2ConsumerCredentialEntity testEntity = defaultEntityBuilder.build();

    OAuth2ConsumerCredentialEntity savedEntity = dao.put(testEntity);
    OAuth2ConsumerCredentialEntity getEntity = dao.get(savedEntity.getOAuth2ProviderKey());
    assertEquals(savedEntity, getEntity);

    // Negative test.
    assertNull(dao.get(getRandomString()));
  }

  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_get_key() {
    String id = TestingUtils.getRandomString();
    assertEquals(getKey(OAuth2ConsumerCredentialEntity.class, id), dao.getKey(id));
  }

  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_put_ofyEntity() {
    OAuth2ConsumerCredentialEntity testEntity = defaultEntityBuilder.build();
    OAuth2ConsumerCredentialEntity savedEntity = dao.put(testEntity);

    OAuth2ConsumerCredentialEntity getEntity = dao.get(testEntity.getOAuth2ProviderKey());
    assertEquals(savedEntity, getEntity);

    // Same entity can be persisted twice. Change clientId & clientSecret and persist.
    String newClientId = getRandomString();
    String newClientSecret = getRandomString();

    testEntity = defaultEntityBuilder.clientId(newClientId).clientSecret(newClientSecret).build();
    savedEntity = dao.put(testEntity);
    assertEquals(savedEntity, dao.put(testEntity));
    assertEquals(savedEntity, dao.put(testEntity));

    // Now original email can be set back again.
    testEntity = defaultEntityBuilder.build();
    assertEquals(savedEntity, dao.put(testEntity));

    /*
     * Negative : No negative test, as the key is based on OAuth2Provider and therefore any attempt
     * to put will create/update entry.
     */
  }

  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_put() {
    OAuth2ConsumerCredentialEntity testEntity = defaultEntityBuilder.build();

    // Positive Test :
    Objectify txn = initiateTransaction();
    assertTrue(txn.getTxn().isActive());

    dao.put(txn, testEntity);
    commitTransaction(txn);
    OAuth2ConsumerCredentialEntity getEntity = dao.get(testEntity.getOAuth2ProviderKey());
    assertEquals(testEntity, getEntity);

    /*
     * Negative : No negative test, as the key is based on OAuth2Provider and therefore any attempt
     * to put will create/update entry.
     */
  }
}
