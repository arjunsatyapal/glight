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
package com.google.light.server.dto.oauth2.owner;

import static com.google.light.testingutils.TestingUtils.getRandomLongNumber;
import static com.google.light.testingutils.TestingUtils.getRandomPersonId;
import static com.google.light.testingutils.TestingUtils.getRandomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import com.google.light.server.constants.OAuth2ProviderService;
import com.google.light.server.dto.AbstractDtoToPersistenceTest;
import com.google.light.server.exception.unchecked.BlankStringException;
import com.google.light.server.persistence.entity.oauth2.owner.OAuth2OwnerTokenEntity;
import com.google.light.testingutils.TestingConstants;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link OAuth2OwnerTokenDto}.
 *
 * @author Arjun Satyapal
 */
public class OAuth2OwnerTokenDtoTest extends AbstractDtoToPersistenceTest {
  private long personId;
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
  
  private OAuth2OwnerTokenDto.Builder getDtoBuilder() {
    return new OAuth2OwnerTokenDto.Builder()
      .personId(personId)
      .provider(defaultProviderService)
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
  public void test_builder() throws Exception {
    assertNotNull(getDtoBuilder().build());
  }

  /** 
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_toJson() throws Exception {
    try {
      getDtoBuilder().build().toJson();
      fail("should have failed.");
    } catch (UnsupportedOperationException e) {
      // If you implement, then fix this test.
    }
  }
  
  /** 
   * {@inheritDoc}
   */
  @Override
  @Test
  public void test_toPersistenceEntity() throws Exception {
    try {
      getDtoBuilder().build().toPersistenceEntity(getRandomString());
      fail("should have failed.");
    } catch (UnsupportedOperationException e) {
      // If you implement, then fix this test.
    } 
  }

  /** 
   * {@inheritDoc}
   */
  @Test
  public void test_toPersistenceEntity_noParam() throws Exception {
    OAuth2OwnerTokenEntity entity = new OAuth2OwnerTokenEntity.Builder()
      .personId(personId)
      .providerService(defaultProviderService)
      .providerUserId(providerUserId)
      .accessToken(accessToken)
      .refreshToken(refreshToken)
      .expiresInMillis(expiresInMillis)
      .tokenType(defaultTokenType)
      .tokenInfo(tokenInfo)
      .build();
    
    assertEquals(entity, getDtoBuilder().build().toPersistenceEntity());
  }

  /** 
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_validate() throws Exception {
    // Negative Test : Zero personId
    try {
      getDtoBuilder().personId(0).build();
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // Expected
    }
    
    // Negative Test : Negative personId
    try {
      getDtoBuilder().personId(-3).build();
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // Expected
    }
    
    // Negative Test : null provider
    try {
      getDtoBuilder().provider(null).build();
      fail("should have failed.");
    } catch (NullPointerException e) {
      // Expected
    }
    
    // Negative Test : null providerUserId
    try {
      getDtoBuilder().providerUserId(null).build();
      fail("should have failed.");
    } catch (BlankStringException e) {
      // Expected
    }
    
    // Negative Test : blank providerUserId
    try {
      getDtoBuilder().providerUserId(" ").build();
      fail("should have failed.");
    } catch (BlankStringException e) {
      // Expected
    }
    
    // Negative Test : null accessToken
    try {
      getDtoBuilder().accessToken(null).build();
      fail("should have failed.");
    } catch (BlankStringException e) {
      // Expected
    }
    
    // Negative Test : blank accesstoken
    try {
      getDtoBuilder().accessToken(" ").build();
      fail("should have failed.");
    } catch (BlankStringException e) {
      // Expected
    }
    
    // Negative Test : null refreshToken
    try {
      getDtoBuilder().refreshToken(null).build();
      fail("should have failed.");
    } catch (BlankStringException e) {
      // Expected
    }
    
    // Negative Test : blank refreshToken
    try {
      getDtoBuilder().refreshToken(" ").build();
      fail("should have failed.");
    } catch (BlankStringException e) {
      // Expected
    }
    
    // Negative Test : Zero expiresInMillis
    try {
      getDtoBuilder().expiresInMillis(0).build();
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // Expected
    }
    
    // Negative Test : Negative expiresInMillis
    try {
      getDtoBuilder().expiresInMillis(-3).build();
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // Expected
    }
    
    // Negative Test : null tokenType
    try {
      getDtoBuilder().tokenType(null).build();
      fail("should have failed.");
    } catch (BlankStringException e) {
      // Expected
    }
    
    // Negative Test : blank tokenType
    try {
      getDtoBuilder().tokenType(" ").build();
      fail("should have failed.");
    } catch (BlankStringException e) {
      // Expected
    }
    
    // Negative Test : null tokenInfo
    try {
      getDtoBuilder().tokenInfo(null).build();
      fail("should have failed.");
    } catch (BlankStringException e) {
      // Expected
    }
    
    // Negative Test : blank tokenInfo
    try {
      getDtoBuilder().tokenInfo(" ").build();
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
  public void test_toXml() throws Exception {
    try {
      getDtoBuilder().build().toXml();
      fail("should have failed.");
    } catch (UnsupportedOperationException e) {
      // If you implement, then fix this test.
    }
  }

}
