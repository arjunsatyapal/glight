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

import static com.google.light.server.constants.OAuth2ProviderService.GOOGLE_LOGIN;
import static com.google.light.server.utils.GuiceUtils.getInstance;
import static com.google.light.server.utils.ObjectifyUtils.commitTransaction;
import static com.google.light.server.utils.ObjectifyUtils.getKey;
import static com.google.light.server.utils.ObjectifyUtils.initiateTransaction;
import static com.google.light.testingutils.TestingConstants.DEFAULT_ACCESS_TOKEN;
import static com.google.light.testingutils.TestingConstants.DEFAULT_REFRESH_TOKEN;
import static com.google.light.testingutils.TestingConstants.DEFAULT_TOKEN_EXPIRES_IN_MILLIS;
import static com.google.light.testingutils.TestingConstants.DEFAULT_TOKEN_INFO;
import static com.google.light.testingutils.TestingConstants.DEFAULT_TOKEN_TYPE;
import static com.google.light.testingutils.TestingUtils.getRandomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.light.server.constants.OAuth2ProviderService;
import com.google.light.server.dto.oauth2.owner.OAuth2OwnerTokenDto;
import com.google.light.server.persistence.entity.oauth2.owner.OAuth2OwnerTokenEntity;
import com.google.light.server.persistence.entity.oauth2.owner.OAuth2OwnerTokenEntity.Builder;
import com.google.light.server.persistence.entity.person.PersonEntity;
import com.google.light.server.utils.ObjectifyUtils;
import com.google.light.testingutils.TestingUtils;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link OAuth2OwnerTokenDao}.
 * 
 * @author Arjun Satyapal
 */
public class OAuth2OwnerTokenDaoTest extends
    AbstractBasicDaoTest<OAuth2OwnerTokenDto, OAuth2OwnerTokenEntity, String> {

  public OAuth2OwnerTokenDaoTest() {
    super(OAuth2OwnerTokenEntity.class, String.class);
  }

  private String providerUserId;
  private PersonEntity personEntity;
  private OAuth2OwnerTokenDao dao;
  private Long personId;

  @Override
  @Before
  public void setUp() {
    super.setUp();
    personEntity = TestingUtils.createRandomPerson(injector);
    personId = personEntity.getId();
    providerUserId = getRandomString();
    dao = getInstance(injector, OAuth2OwnerTokenDao.class);
  }

  private Builder getDefaultEntityBuilder(OAuth2ProviderService providerService,
      String providerUserId) {
    return new OAuth2OwnerTokenEntity.Builder()
        .personId(personId)
        .providerService(providerService)
        .providerUserId(providerUserId)
        .accessToken(DEFAULT_ACCESS_TOKEN)
        .refreshToken(DEFAULT_REFRESH_TOKEN)
        .expiresInMillis(DEFAULT_TOKEN_EXPIRES_IN_MILLIS)
        .tokenType(DEFAULT_TOKEN_TYPE)
        .tokenInfo(DEFAULT_TOKEN_INFO);
  }

  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_get_ofyKey() {
    OAuth2OwnerTokenEntity testEntity =
        getDefaultEntityBuilder(GOOGLE_LOGIN, providerUserId).build();
    OAuth2OwnerTokenEntity savedEntity = dao.put(testEntity);

    Objectify ofy = ObjectifyUtils.initiateTransaction();
    try {
      Key<OAuth2OwnerTokenEntity> key = dao.getKey(savedEntity.getId());
      OAuth2OwnerTokenEntity getEntiy = dao.get(ofy, key);
      assertEquals(savedEntity, getEntiy);
    } catch (Exception e) {
      ObjectifyUtils.rollbackTransaction(ofy);
    }
  }

  /**
   * {@inheritDoc}
   * In the parent class, it is deprecated to ensure callers use proper method.
   */
  @SuppressWarnings("deprecation")
  @Test
  @Override
  public void test_get() {
    try {
      dao.get(TestingUtils.getRandomString());
      fail("should have failed.");
    } catch (UnsupportedOperationException e) {
      // Expected. If you implement method, then fix this.
    }
  }

  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_get_key() {
    String id = getRandomString();
    assertEquals(getKey(OAuth2OwnerTokenEntity.class, id), dao.getKey(id));
  }

  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_put_ofyEntity() {
    OAuth2OwnerTokenEntity testEntity =
        getDefaultEntityBuilder(GOOGLE_LOGIN, providerUserId).build();

    // Positive Test :
    Objectify txn = initiateTransaction();
    assertTrue(txn.getTxn().isActive());

    dao.put(txn, testEntity);
    commitTransaction(txn);
    OAuth2OwnerTokenEntity getEntity =
        dao.getByPersonIdAndOAuth2ProviderService(personId, defaultProviderService);
    assertEquals(testEntity, getEntity);

    /*
     * Negative : No negative test, as the key is based on PersonId + OAuth2Provider and therefore
     * any attempt to put will create/update entry. And there is no-uniqueness constraint across two
     * entities.
     */
  }

  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_put() {

    OAuth2OwnerTokenEntity testEntity =
        getDefaultEntityBuilder(GOOGLE_LOGIN, providerUserId).build();
    OAuth2OwnerTokenEntity savedEntity = dao.put(testEntity);

    OAuth2OwnerTokenEntity getEntity =
        dao.getByPersonIdAndOAuth2ProviderService(personId, defaultProviderService);
    assertEquals(savedEntity, getEntity);

    // Same entity can be persisted twice. Change accessToken & refreshToken.
    String newAccessToken = getRandomString();
    String newRefreshToken = getRandomString();

    testEntity = getDefaultEntityBuilder(GOOGLE_LOGIN, providerUserId)
        .accessToken(newAccessToken)
        .refreshToken(newRefreshToken)
        .build();

    OAuth2OwnerTokenEntity newSavedEntity = dao.put(testEntity);
    assertEquals(newSavedEntity, dao.put(testEntity));
    assertEquals(newSavedEntity, dao.put(testEntity));

    // Ensures that savedEntity and newSavedEntity refer to same Entity.
    assertEquals(savedEntity.getId(), newSavedEntity.getId());
    // Ensures that savedEntity and newSavedEntity are not equal post modification.
    assertTrue(!savedEntity.equals(newSavedEntity));

    // Now original accessToken and refreshToken is set back.
    testEntity = getDefaultEntityBuilder(GOOGLE_LOGIN, providerUserId).build();
    newSavedEntity = dao.put(testEntity);
    assertEquals(savedEntity, newSavedEntity);

    /*
     * Negative : No negative test, as the key is based on PersonId + OAuth2Provider and therefore
     * any attempt to put will create/update entry. And there is no-uniqueness constraint across two
     * entities.
     */
  }

  /**
   * Test for
   * {@link OAuth2OwnerTokenDao#getByPersonIdAndOAuth2ProviderService(long, OAuth2ProviderService)}.
   */
  @Test
  public void test_getByPersonIdAndOAuth2ProviderService() {
    OAuth2OwnerTokenEntity testEntity =
        getDefaultEntityBuilder(GOOGLE_LOGIN, providerUserId).build();

    OAuth2OwnerTokenEntity savedEntity = dao.put(testEntity);
    OAuth2OwnerTokenEntity getEntity =
        dao.getByPersonIdAndOAuth2ProviderService(personId, defaultProviderService);
    assertEquals(savedEntity, getEntity);

    // Negative test : Try to fetch with randomId.
    assertNull(dao.getByPersonIdAndOAuth2ProviderService(TestingUtils.getRandomPersonId(),
        defaultProviderService));
  }

  /**
   * Test for {@link OAuth2OwnerTokenDao#getTokenByProviderAndUserId(OAuth2ProviderService, String)}.
   */
  @Test
  public void test_getTokenByProviderUserId() {
    OAuth2OwnerTokenEntity token1 = getDefaultEntityBuilder(GOOGLE_LOGIN, providerUserId).build();
    dao.put(token1);

    OAuth2OwnerTokenEntity fetchedEntity =
        dao.getTokenByProviderAndUserId(GOOGLE_LOGIN, providerUserId);
    assertEquals(token1, fetchedEntity);

    // Negative Testing : Create two different people with same providerUserId.
    PersonEntity randomPerson2 = TestingUtils.createRandomPerson(injector);
    OAuth2OwnerTokenEntity token2 = getDefaultEntityBuilder(GOOGLE_LOGIN, providerUserId)
        .personId(randomPerson2.getId())
        .build();
    dao.put(token2);

    try {
      dao.getTokenByProviderAndUserId(GOOGLE_LOGIN, providerUserId);
      fail("should have failed.");
    } catch (IllegalStateException e) {
      // Expected
    }
  }
}
