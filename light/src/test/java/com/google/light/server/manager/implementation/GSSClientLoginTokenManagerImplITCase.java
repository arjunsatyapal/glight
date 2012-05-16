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
package com.google.light.server.manager.implementation;

import static com.google.light.server.constants.OAuth2ProviderEnum.GSS;
import static com.google.light.server.constants.RequestParamKeyEnum.CLIENT_ID;
import static com.google.light.server.constants.RequestParamKeyEnum.CLIENT_SECRET;
import static com.google.light.server.servlets.test.CredentialStandardEnum.CLIENT_LOGIN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.util.Properties;
import java.util.zip.ZipException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.api.client.googleapis.auth.clientlogin.ClientLoginResponseException;
import com.google.common.collect.Lists;
import com.google.light.server.AbstractLightServerTest;
import com.google.light.server.constants.OAuth2ProviderEnum;
import com.google.light.server.exception.checked.InvalidGSSClientLoginToken;
import com.google.light.server.persistence.dao.OAuth2ConsumerCredentialDao;
import com.google.light.server.persistence.entity.admin.OAuth2ConsumerCredentialEntity;
import com.google.light.server.servlets.test.CredentialUtils;
import com.google.light.testingutils.TestingUtils;

/**
 * ITCase for {@link GSSClientLoginTokenManagerImpl}
 * 
 * @author Walter Cacau
 */
public class GSSClientLoginTokenManagerImplITCase extends AbstractLightServerTest {
  private static final int WAIT_BETWEEN_AUTHENTICATIONS = 3000;
  private GSSClientLoginTokenManagerImpl tokenManager;
  private OAuth2ConsumerCredentialEntity consumerCredentialEntity;
  private OAuth2ConsumerCredentialDao consumerCredentialDao;
  private static Properties consumerCredentials;

  @BeforeClass
  public static void loadConsumerJsonString() throws ZipException, IOException {
    consumerCredentials =
        TestingUtils.loadProperties(CredentialUtils.getConsumerCredentialFileAbsPath(CLIENT_LOGIN,
            GSS));
    TestingUtils.validatePropertiesFile(consumerCredentials, Lists.newArrayList(
        CLIENT_ID.get(), CLIENT_SECRET.get()));

  }

  @Before
  @Override
  public void setUp() {
    super.setUp();
    consumerCredentialEntity = new OAuth2ConsumerCredentialEntity.Builder()
        .providerName(OAuth2ProviderEnum.GSS.name())
        .clientId(consumerCredentials.getProperty(CLIENT_ID.get()))
        .clientSecret(consumerCredentials.getProperty(CLIENT_SECRET.get()))
        .build();

    tokenManager = injector.getInstance(GSSClientLoginTokenManagerImpl.class);
    consumerCredentialDao = injector.getInstance(OAuth2ConsumerCredentialDao.class);
  }

  @Test
  /**
   * A integration test that assert over all functions of {@link GSSClientLoginTokenManagerImplITCase}
   * by composing a story.
   * 
   * @throws ClientLoginResponseException
   * @throws InvalidGSSClientLoginToken
   */
  public void test_all() throws ClientLoginResponseException, InvalidGSSClientLoginToken,
      InterruptedException {

    // Initially the admin will help in the logining in
    String firstToken = tokenManager.authenticate(consumerCredentialEntity.getClientId(),
        consumerCredentialEntity.getClientSecret(), null, null);

    assertOAuth2ConsumerCredentialAndValidateToken(firstToken);

    // Finally, let's try to authenticate without any user help, only with the data saved
    synchronized (this) {
      wait(WAIT_BETWEEN_AUTHENTICATIONS); // wait so we don't trigger google servers to suspect we
                                          // are a bot.
    }
    String secondToken = tokenManager.authenticate();

    // Again to be sure:
    assertOAuth2ConsumerCredentialAndValidateToken(secondToken);

    assertFalse("First token given by google should be different from second one",
        firstToken.equals(secondToken));
  }

  private void assertOAuth2ConsumerCredentialAndValidateToken(String firstToken)
      throws InvalidGSSClientLoginToken {
    // We need to check if the data saved in the datastore is correct
    OAuth2ConsumerCredentialEntity savedOAuth2ConsumerCredential = consumerCredentialDao.get(GSS);
    assertEquals(consumerCredentialEntity.getClientId(),
        savedOAuth2ConsumerCredential.getClientId());
    assertEquals(consumerCredentialEntity.getClientSecret(),
        savedOAuth2ConsumerCredential.getClientSecret());
    assertEquals(firstToken, tokenManager.getCurrentToken());

    // To be sure the token is valid, let's validate it by issue a call to CSE servers.
    tokenManager.validateCurrentToken();
  }

}
