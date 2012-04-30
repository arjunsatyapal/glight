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
package com.google.light.server.persistence.entity.admin;

import static com.google.light.testingutils.TestingUtils.getRandomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import com.google.light.server.constants.OAuth2ProviderEnum;
import com.google.light.server.dto.admin.OAuth2ConsumerCredentialDto;
import com.google.light.server.exception.unchecked.BlankStringException;
import com.google.light.server.persistence.entity.AbstractPersistenceEntityTest;
import com.googlecode.objectify.Key;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link OAuth2ConsumerCredentialEntity}.
 * 
 * @author Arjun Satyapal
 */
public class OAuth2ConsumerCredentialEntityTest extends AbstractPersistenceEntityTest {
  private OAuth2ProviderEnum defaultProvider = OAuth2ProviderEnum.GOOGLE;
  private String clientId;
  private String clientSecret;

  @Before
  public void setUp() {
    clientId = getRandomString();
    clientSecret = getRandomString();
  }

  private OAuth2ConsumerCredentialEntity.Builder getEntityBuilder() {
    return new OAuth2ConsumerCredentialEntity.Builder()
        .providerName(defaultProvider.name())
        .clientId(clientId)
        .clientSecret(clientSecret);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Test
  public void test_builder_with_constructor() {
    // Positive Testing :
    assertNotNull(getEntityBuilder().build());

    // Negative Testing : providerKey = null
    try {
      getEntityBuilder().providerName(null).build();
      fail("should have failed.");
    } catch (NullPointerException e) {
      // Expected
    }

    // Negative Testing : providerKey = " "
    try {
      getEntityBuilder().providerName("  ").build();
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // Expected
    }

    // Negative Testing : providerKey = Invalid string.
    try {
      getEntityBuilder().providerName(getRandomString()).build();
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // Expected
    }

    // Negative Testing : clientId = null
    try {
      getEntityBuilder().clientId(null).build();
      fail("should have failed.");
    } catch (BlankStringException e) {
      // Expected
    }

    // Negative Testing : clientId = "  "
    try {
      getEntityBuilder().clientId("  ").build();
      fail("should have failed.");
    } catch (BlankStringException e) {
      // Expected
    }

    // Negative Testing : clientSecret = null
    try {
      getEntityBuilder().clientSecret(null).build();
      fail("should have failed.");
    } catch (BlankStringException e) {
      // Expected
    }

    // Negative Testing : clientSecret = "  "
    try {
      getEntityBuilder().clientSecret("  ").build();
      fail("should have failed.");
    } catch (BlankStringException e) {
      // Expected
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Test
  public void test_toDto() {
    OAuth2ConsumerCredentialDto expectedDto = new OAuth2ConsumerCredentialDto.Builder()
        .provider(defaultProvider)
        .clientId(clientId)
        .clientSecret(clientSecret)
        .build();
    assertEquals(expectedDto, getEntityBuilder().build().toDto());
  }

  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_getKey() {
    OAuth2ConsumerCredentialEntity entity = getEntityBuilder().build();
    Key<OAuth2ConsumerCredentialEntity> expectedKey = new Key<OAuth2ConsumerCredentialEntity>(
        OAuth2ConsumerCredentialEntity.class, defaultProvider.name());
    assertEquals(expectedKey, entity.getKey());
  }

  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_generateKey() {
    // Positive tests done already as part of test_getKey. Doing negative testing.
    
    // Negative Test : Invalid Provider.
    try {
      OAuth2ConsumerCredentialEntity.generateKey(getRandomString());
      fail("should have failed.");
    } catch (EnumConstantNotPresentException e) {
      // Expected
    }
  }
}
