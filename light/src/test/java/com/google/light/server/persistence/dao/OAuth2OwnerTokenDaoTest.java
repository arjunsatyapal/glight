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
import static com.google.light.server.utils.ObjectifyUtils.initiateTransaction;
import static com.google.light.testingutils.TestingConstants.DEFAULT_ACCESS_TOKEN;
import static com.google.light.testingutils.TestingConstants.DEFAULT_REFRESH_TOKEN;
import static com.google.light.testingutils.TestingConstants.DEFAULT_TOKEN_EXPIRES_IN_MILLIS;
import static com.google.light.testingutils.TestingConstants.DEFAULT_TOKEN_INFO;
import static com.google.light.testingutils.TestingConstants.DEFAULT_TOKEN_TYPE;
import static com.google.light.testingutils.TestingUtils.createRandomPerson;
import static com.google.light.testingutils.TestingUtils.getMockSessionForTesting;
import static com.google.light.testingutils.TestingUtils.getRequestScopedValueProvider;
import static com.google.light.testingutils.TestingUtils.getRandomEmail;
import static com.google.light.testingutils.TestingUtils.getRandomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.light.server.guice.provider.TestRequestScopedValuesProvider;

import com.google.light.server.constants.OAuth2ProviderService;
import com.google.light.server.dto.oauth2.owner.OAuth2OwnerTokenDto;
import com.google.light.server.persistence.entity.oauth2.owner.OAuth2OwnerTokenEntity;
import com.google.light.server.persistence.entity.oauth2.owner.OAuth2OwnerTokenEntity.Builder;
import com.google.light.server.persistence.entity.person.PersonEntity;
import com.google.light.server.utils.ObjectifyUtils;
import com.googlecode.objectify.Objectify;
import javax.servlet.http.HttpSession;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link OAuth2OwnerTokenDao}.
 * 
 * @author Arjun Satyapal
 */
public class OAuth2OwnerTokenDaoTest extends
    AbstractBasicDaoTest<OAuth2OwnerTokenDto, OAuth2OwnerTokenEntity> {

  public OAuth2OwnerTokenDaoTest() {
    super(OAuth2OwnerTokenEntity.class);
  }

  private OAuth2OwnerTokenDao dao;

  @Override
  @Before
  public void setUp() {
    super.setUp();
    dao = getInstance(OAuth2OwnerTokenDao.class);
  }

  private Builder getDefaultEntityBuilder(OAuth2ProviderService providerService,
      String providerUserId) {
    return new OAuth2OwnerTokenEntity.Builder()
        .personKey(testPerson.getKey())
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
  public void test_get_by_key() {
    OAuth2OwnerTokenEntity testEntity =
        getDefaultEntityBuilder(GOOGLE_LOGIN, testProviderUserId).build();
    dao.put(testEntity);

    OAuth2OwnerTokenEntity getEntiy = dao.get(testEntity.getKey());
    assertEquals(testEntity, getEntiy);
  }

  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_get_by_key_in_txn() {
    OAuth2OwnerTokenEntity testEntity =
        getDefaultEntityBuilder(GOOGLE_LOGIN, testProviderUserId).build();
    dao.put(testEntity);

    Objectify ofy = ObjectifyUtils.initiateTransaction();
    try {
      OAuth2OwnerTokenEntity getEntiy = dao.get(ofy, testEntity.getKey());
      assertEquals(testEntity, getEntiy);
    } catch (Exception e) {
      ObjectifyUtils.rollbackTransactionIfStillActive(ofy);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_put_txn() {
    OAuth2OwnerTokenEntity testEntity = getDefaultEntityBuilder(
        GOOGLE_LOGIN, testProviderUserId).build();

    // Positive Test :
    Objectify txn = initiateTransaction();
    assertTrue(txn.getTxn().isActive());

    dao.put(txn, testEntity);
    commitTransaction(txn);
    OAuth2OwnerTokenEntity getEntity =
        dao.getByProviderService(GOOGLE_LOGIN);
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
    OAuth2OwnerTokenEntity originalEntity = getDefaultEntityBuilder(
        GOOGLE_LOGIN, testProviderUserId).build();
    OAuth2OwnerTokenEntity savedEntity = dao.put(originalEntity);
    assertEquals(originalEntity, savedEntity);

    OAuth2OwnerTokenEntity getEntity = dao.getByProviderService(defaultProviderService);
    assertEquals(originalEntity, getEntity);

    // Same entity can be persisted twice. Change accessToken & refreshToken.
    String newAccessToken = getRandomString();
    String newRefreshToken = getRandomString();

    originalEntity = getDefaultEntityBuilder(GOOGLE_LOGIN, testProviderUserId)
        .accessToken(newAccessToken)
        .refreshToken(newRefreshToken)
        .build();

    OAuth2OwnerTokenEntity newSavedEntity = dao.put(originalEntity);
    assertEquals(newSavedEntity, dao.put(originalEntity));
    assertEquals(newSavedEntity, dao.put(originalEntity));

    // Ensures that savedEntity and newSavedEntity refer to same Entity.
    assertEquals(savedEntity.getKey().getId(), newSavedEntity.getKey().getId());

    // Ensures that savedEntity and newSavedEntity are not equal post modification.
    assertTrue(!savedEntity.equals(newSavedEntity));

    // Now original accessToken and refreshToken is set back.
    originalEntity = getDefaultEntityBuilder(GOOGLE_LOGIN, testProviderUserId).build();
    newSavedEntity = dao.put(originalEntity);
    assertEquals(originalEntity, newSavedEntity);

    /*
     * Negative : No negative test, as the key is based on PersonId + OAuth2Provider and therefore
     * any attempt to put will create/update entry. And there is no-uniqueness constraint across two
     * entities.
     */
  }

  /**
   * Test for {@link OAuth2OwnerTokenDao#getByOAuth2ProviderService(long, OAuth2ProviderService)}.
   */
  @Test
  public void test_getOAuth2ProviderService() {
    OAuth2OwnerTokenEntity testEntity =
        getDefaultEntityBuilder(GOOGLE_LOGIN, testProviderUserId).build();

    OAuth2OwnerTokenEntity savedEntity = dao.put(testEntity);
    OAuth2OwnerTokenEntity getEntity =
        dao.getByProviderService(defaultProviderService);
    assertEquals(savedEntity, getEntity);

    // Negative test : Since we don't allow custom enums, no negative test possible at the moment.
  }

  /**
   * Test for {@link OAuth2OwnerTokenDao#getTokenByProviderAndUserId(OAuth2ProviderService, String)}
   * .
   */
  @Test
  public void test_getTokenByProviderUserId() {
    OAuth2OwnerTokenEntity token1 =
        getDefaultEntityBuilder(GOOGLE_LOGIN, testProviderUserId).build();
    dao.put(token1);

    OAuth2OwnerTokenEntity fetchedToken1 = dao.getByProviderServiceAndProviderUserId(
        GOOGLE_LOGIN, testProviderUserId);
    assertEquals(token1, fetchedToken1);

    // Negative Testing : Create two different people with same providerUserId.

    String email2 = getRandomEmail();
    assertTrue(!email2.equals(testEmail));

    TestRequestScopedValuesProvider testRequestScopedValueProvider =
        getRequestScopedValueProvider(null, null);
    HttpSession session2 = getMockSessionForTesting(defaultEnv, defaultProviderService,
        testProviderUserId, null/* personId */, email2);
    PersonEntity randomPerson2 =
        createRandomPerson(defaultEnv, testRequestScopedValueProvider, session2);
    testRequestScopedValueProvider.getRequestScopedValues().updateBoth(randomPerson2.getPersonId());

    OAuth2OwnerTokenEntity token2 = getDefaultEntityBuilder(GOOGLE_LOGIN, testProviderUserId)
        .personKey(randomPerson2.getKey())
        .build();

    // Ensure that token1 and token2 are two different entities for persistence.
    assertTrue(!token1.getKey().equals(token2.getKey()));

    dao.put(token2);

    try {
      dao.getByProviderServiceAndProviderUserId(GOOGLE_LOGIN, testProviderUserId);
      fail("should have failed as two Persons have same providerUserId.");
    } catch (IllegalStateException e) {
      // Expected
    }
  }
}
