/*
 * Copyright 2012 Google Inc.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.light.server.manager.implementation.oauth2.owner;

import static com.google.light.server.constants.OAuth2ProviderService.GOOGLE_DOC;
import static com.google.light.server.constants.OAuth2ProviderService.GOOGLE_LOGIN;
import static com.google.light.server.utils.GuiceUtils.getInstance;
import static com.google.light.server.utils.LightUtils.getCurrentTimeInMillis;
import static com.google.light.testingutils.TestingUtils.createRandomPerson;
import static com.google.light.testingutils.TestingUtils.getRandomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.google.light.server.AbstractLightServerTest;
import com.google.light.server.constants.OAuth2ProviderService;
import com.google.light.server.exception.unchecked.BlankStringException;
import com.google.light.server.manager.interfaces.OAuth2OwnerTokenManager;
import com.google.light.server.persistence.entity.oauth2.owner.OAuth2OwnerTokenEntity;
import com.google.light.server.persistence.entity.person.PersonEntity;
import org.junit.Test;

/**
 * Test for {@link OAuth2OwnerTokenManagerImpl}
 * 
 * @author Arjun Satyapal
 */
public class OAuth2OwnerTokenManagerImplTest extends AbstractLightServerTest {
  private PersonEntity randomPerson;
  private String providerUserId;
  private String accessToken;
  private String refreshToken;
  private long expiresInMillis;
  private String tokenType;
  private String tokenInfo;

  private OAuth2OwnerTokenManager googLogintokenManager;
  private OAuth2OwnerTokenManager googDocTokenManager;

  @Override
  public void setUp() {
    super.setUp();
    randomPerson = createRandomPerson(defaultEnv, testSession);

    providerUserId = getRandomString();
    accessToken = getRandomString();
    refreshToken = getRandomString();
    expiresInMillis = getCurrentTimeInMillis();
    tokenType = getRandomString();
    tokenInfo = getRandomString();

    OAuth2OwnerTokenManagerFactory factory = getInstance(
        injector, OAuth2OwnerTokenManagerFactory.class);
    googLogintokenManager = factory.create(GOOGLE_LOGIN);
    googDocTokenManager = factory.create(GOOGLE_DOC);
  }

  @Override
  public void tearDown() {
    super.tearDown();
  }

  private OAuth2OwnerTokenEntity.Builder getOwnerTokenEntiy(OAuth2ProviderService providerService,
      String providerUserId) {
    return new OAuth2OwnerTokenEntity.Builder()
        .personId(randomPerson.getId())
        .providerService(providerService)
        .providerUserId(providerUserId)
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .expiresInMillis(expiresInMillis)
        .tokenType(tokenType)
        .tokenInfo(tokenInfo);
  }

  /**
   * Test for {@link OAuth2OwnerTokenManagerImpl#putToken(OAuth2OwnerTokenEntity)}.
   */
  @Test
  public void test_putToken() {
    do_test_put_get(randomPerson.getId());
  }

  /**
   * Test for {@link OAuth2OwnerTokenManagerImpl#getToken(long)}.
   */
  @Test
  public void test_getToken() {
    do_test_put_get(randomPerson.getId());
  }
  
  /**
   * Test for {@link OAuth2OwnerTokenManagerImpl#getTokenByProviderUserId(String)}.
   */
  @Test
  public void test_getTokenByProviderUserId() {
    OAuth2OwnerTokenEntity expectedToken = getOwnerTokenEntiy(GOOGLE_LOGIN, providerUserId).build();
    // Now persisting token.
    do_test_put_get(randomPerson.getId());
    
    // Now fetch using providerUserId;
    OAuth2OwnerTokenEntity fetchedToken = 
        googLogintokenManager.getTokenByProviderUserId(providerUserId);
    assertEquals(expectedToken, fetchedToken);
    
    // Negative Testing : Try to fetch by providerUserId for GOOGLE_DOC token.
    try {
      googDocTokenManager.getTokenByProviderUserId(providerUserId);
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // Expected
    }
  }

  private void do_test_put_get(long personId) {
    // First put Google Login Token.
    OAuth2OwnerTokenEntity googLoginToken =
        getOwnerTokenEntiy(GOOGLE_LOGIN, providerUserId).build();
    doTestPutGet(googLoginToken, googLogintokenManager, personId);

    // Now put Google Doc Token
    OAuth2OwnerTokenEntity googDocToken =
        getOwnerTokenEntiy(GOOGLE_DOC, null /* non-login token */).build();
    doTestPutGet(googDocToken, googDocTokenManager, personId);

    // Now some negative Testing.

    // Negative Test : Try to persiste Login Token with Null ProviderUserId.
    try {
      doTestPutGet(getOwnerTokenEntiy(GOOGLE_LOGIN, null).build(), googLogintokenManager, personId);
      fail("should have failed.");
    } catch (BlankStringException e) {
      // Expected
    }

    // Negative Test : Try to persiste Non-Login Token with non-null ProviderUserId.
    try {
      doTestPutGet(getOwnerTokenEntiy(GOOGLE_LOGIN, getRandomString()).build(),
          googDocTokenManager, personId);
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // Expected
    }

    // Negative Test : Try to persist one type of token with another type of tokenManager.
    try {
      doTestPutGet(googLoginToken, googDocTokenManager, personId);
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // Expected
    }
  }

  private void doTestPutGet(OAuth2OwnerTokenEntity token, OAuth2OwnerTokenManager tokenManager, long personId) {
    OAuth2OwnerTokenEntity savedToken = tokenManager.putToken(token);
    assertEquals(token, savedToken);
    
    OAuth2OwnerTokenEntity fetchedToken = tokenManager.getToken(personId);
    assertEquals(token, fetchedToken);
  }
}
