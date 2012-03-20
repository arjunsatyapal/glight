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
package com.google.light.server.dto.admin;

import static com.google.light.testingutils.TestingUtils.getRandomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import com.google.light.server.exception.unchecked.BlankStringException;

import com.google.light.server.constants.OAuth2Provider;
import com.google.light.server.dto.AbstractDtoToPersistenceTest;
import com.google.light.server.persistence.entity.admin.OAuth2ConsumerCredentialEntity;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link OAuth2ConsumerCredentialDto}.
 * 
 * @author Arjun Satyapal
 */
public class OAuth2ConsumerCredentialDtoTest extends AbstractDtoToPersistenceTest {
  private String clientId = getRandomString();
  private String clientSecret = getRandomString();
  private OAuth2Provider defaultProvider = OAuth2Provider.GOOGLE_LOGIN;

  @Before
  public void setUp() {
    clientId = getRandomString();
    clientSecret = getRandomString();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Test
  public void test_builder() throws Exception {
    // Nothing to test here.
  }

  private OAuth2ConsumerCredentialDto.Builder getDtoBuilder() {
    return new OAuth2ConsumerCredentialDto.Builder()
        .provider(defaultProvider)
        .clientId(clientId)
        .clientSecret(clientSecret);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Test
  public void test_toJson() throws Exception {
    try {
      getDtoBuilder().build().toJson();
      fail("should have failed.");
    } catch (UnsupportedOperationException e) {
      // If you are implementing something, then fix this test.
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Test
  public void test_toPersistenceEntity() throws Exception {
    OAuth2ConsumerCredentialEntity entity = new OAuth2ConsumerCredentialEntity.Builder()
      .provider(defaultProvider.name())
      .clientId(clientId)
      .clientSecret(clientSecret)
      .build();

    assertEquals(entity, getDtoBuilder().build().toPersistenceEntity(null));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Test
  public void test_validate() throws Exception {
    // Positive Test : Should build fine.
    assertNotNull(getDtoBuilder().build().validate());

    // Negative Test : null provider.
    try {
      getDtoBuilder().provider(null).build();
      fail("should have failed.");
    } catch (NullPointerException e) {
      // Expected;
    }

    // Negative Test : clientId=null
    try {
      getDtoBuilder().clientId(null).build();
      fail("should have failed.");
    } catch (BlankStringException e) {
      // Expected
    }
    
    // Negative Test : clientId=" "
    try {
      getDtoBuilder().clientId(" ").build();
      fail("should have failed.");
    } catch (BlankStringException e) {
      // Expected
    }

    // Negative Test : clientSecret=null
    try {
      getDtoBuilder().clientSecret(null).build();
      fail("should have failed.");
    } catch (BlankStringException e) {
      // Expected
    }
    
    // Negative Test : clientSecret=" "
    try {
      getDtoBuilder().clientSecret(" ").build();
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
  public void test_toXml() throws Exception {
    try {
      getDtoBuilder().build().toXml();
      fail("should have failed.");
    } catch (UnsupportedOperationException e) {
      // If you are implementing, then fix this test.
    }
  }
}
