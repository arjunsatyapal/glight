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
package com.google.light.server.persistence.entity.oauth2.owner;

import static com.google.light.server.constants.OAuth2ProviderService.GOOGLE_DOC;
import static com.google.light.server.constants.OAuth2ProviderService.GOOGLE_LOGIN;
import static com.google.light.testingutils.TestingUtils.getRandomLongNumber;
import static com.google.light.testingutils.TestingUtils.getRandomPersonId;
import static com.google.light.testingutils.TestingUtils.getRandomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.google.light.server.dto.pojo.typewrapper.longwrapper.PersonId;





import com.google.light.server.constants.OAuth2ProviderService;
import com.google.light.server.dto.oauth2.owner.OAuth2OwnerTokenDto;
import com.google.light.server.exception.unchecked.BlankStringException;
import com.google.light.server.exception.unchecked.InvalidPersonIdException;
import com.google.light.server.persistence.entity.AbstractPersistenceEntityTest;
import com.google.light.server.persistence.entity.person.PersonEntity;
import com.google.light.testingutils.TestingConstants;
import com.googlecode.objectify.Key;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link OAuth2OwnerTokenEntity}
 * 
 * @author Arjun Satyapal
 */
public class OAuth2OwnerTokenEntityTest extends AbstractPersistenceEntityTest {
  private PersonId personId;
  private OAuth2ProviderService defaultProviderService = OAuth2ProviderService.GOOGLE_LOGIN;
  private String providerUserId;
  private String accessToken;
  private String refreshToken;
  private long expiresInMillis;
  private String defaultTokenType = TestingConstants.DEFAULT_ACCESS_TYPE;

  // At present it is some random string. Eventually may need fix.
  private String tokenInfo;

  @Before
  public void setUp() {
    personId = getRandomPersonId();
    providerUserId = getRandomString();
    accessToken = getRandomString();
    refreshToken = getRandomString();
    expiresInMillis = getRandomLongNumber();
    tokenInfo = getRandomString();
  }

  private OAuth2OwnerTokenEntity.Builder getEntityBuilder() {
    return new OAuth2OwnerTokenEntity.Builder()
        .personKey(PersonEntity.generateKey(personId))
        .providerService(defaultProviderService)
        .providerUserId(providerUserId)
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .expiresInMillis(expiresInMillis)
        .tokenType(defaultTokenType)
        .tokenInfo(tokenInfo);
  }

  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_builder_with_constructor() {
    // Negative Test : Zero personId
    try {
      getEntityBuilder().personKey(PersonEntity.generateKey(new PersonId(0L))).build();
      fail("should have failed.");
    } catch (InvalidPersonIdException e) {
      // Expected
    }

    // Negative Test : Negative personId
    try {
      getEntityBuilder().personKey(PersonEntity.generateKey(new PersonId(-3L))).build();
      fail("should have failed.");
    } catch (InvalidPersonIdException e) {
      // Expected
    }

    // Negative Test : null provider
    try {
      getEntityBuilder().providerService(null).build();
      fail("should have failed.");
    } catch (NullPointerException e) {
      // Expected
    }

    // Negative Test : null providerUserId with GOOGLE_LOGIN.
    try {
      getEntityBuilder().providerService(GOOGLE_LOGIN).providerUserId(null).build();
      fail("should have failed.");
    } catch (BlankStringException e) {
      // Expected
    }

    // Positive Test : blank providerUserId with GOOGLE_DOC
    getEntityBuilder().providerService(GOOGLE_DOC).providerUserId(null).build();

    // Negative Test : non-null providerUserId with GOOGLE_DOC.
    try {
      getEntityBuilder().providerService(GOOGLE_DOC).providerUserId(getRandomString()).build();
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // Expected
    }

    // Negative Test : null accessToken
    try {
      getEntityBuilder().accessToken(null).build();
      fail("should have failed.");
    } catch (BlankStringException e) {
      // Expected
    }

    // Negative Test : blank accesstoken
    try {
      getEntityBuilder().accessToken(" ").build();
      fail("should have failed.");
    } catch (BlankStringException e) {
      // Expected
    }

    // Negative Test : null refreshToken
    try {
      getEntityBuilder().refreshToken(null).build();
      fail("should have failed.");
    } catch (BlankStringException e) {
      // Expected
    }

    // Negative Test : blank refreshToken
    try {
      getEntityBuilder().refreshToken(" ").build();
      fail("should have failed.");
    } catch (BlankStringException e) {
      // Expected
    }

    // Negative Test : Zero expiresInMillis
    try {
      getEntityBuilder().expiresInMillis(0).build();
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // Expected
    }

    // Negative Test : Negative expiresInMillis
    try {
      getEntityBuilder().expiresInMillis(-3).build();
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // Expected
    }

    // Negative Test : null tokenType
    try {
      getEntityBuilder().tokenType(null).build();
      fail("should have failed.");
    } catch (BlankStringException e) {
      // Expected
    }

    // Negative Test : blank tokenType
    try {
      getEntityBuilder().tokenType(" ").build();
      fail("should have failed.");
    } catch (BlankStringException e) {
      // Expected
    }

    // Negative Test : null tokenInfo
    try {
      getEntityBuilder().tokenInfo(null).build();
      fail("should have failed.");
    } catch (BlankStringException e) {
      // Expected
    }

    // Negative Test : blank tokenType
    try {
      getEntityBuilder().tokenInfo(" ").build();
      fail("should have failed.");
    } catch (BlankStringException e) {
      // Expected
    }
  }

  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_toDto() {
    OAuth2OwnerTokenDto dto = new OAuth2OwnerTokenDto.Builder()
        .personId(personId)
        .provider(defaultProviderService)
        .providerUserId(providerUserId)
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .expiresInMillis(expiresInMillis)
        .tokenType(defaultTokenType)
        .tokenInfo(tokenInfo)
        .build();

    assertEquals(dto, getEntityBuilder().build().toDto());
  }

  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_getKey() {
    OAuth2OwnerTokenEntity entity = getEntityBuilder().build();
    Key<PersonEntity> personKey = PersonEntity.generateKey(personId);
    Key<OAuth2OwnerTokenEntity> expectedKey = new Key<OAuth2OwnerTokenEntity>(personKey,
        OAuth2OwnerTokenEntity.class, defaultProviderService.name());
    assertEquals(expectedKey, entity.getKey());
  }

  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_generateKey() {
    // Positive tests already done as part of test_getKey.
    PersonId randomPersonId = getRandomPersonId();
    Key<PersonEntity> personKey = PersonEntity.generateKey(randomPersonId);

    // Negative Test : Invalid ProviderService.
    try {
      OAuth2OwnerTokenEntity.generateKey(personKey, getRandomString());
      fail("should have failed.");
    } catch (EnumConstantNotPresentException e) {
      // Expected
    }
  }
}
