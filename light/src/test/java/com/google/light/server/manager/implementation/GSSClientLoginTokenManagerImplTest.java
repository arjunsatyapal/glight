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
import static com.google.light.testingutils.TestResourcePaths.GSS_CLIENTLOGIN_SAMPLE_FORBIDDEN_RESPONSE_TXT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.api.client.googleapis.auth.clientlogin.ClientLogin;
import com.google.api.client.googleapis.auth.clientlogin.ClientLoginResponseException;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.google.inject.Provider;
import com.google.light.server.AbstractLightServerTest;
import com.google.light.server.constants.LightConstants;
import com.google.light.server.constants.http.ContentTypeEnum;
import com.google.light.server.constants.http.HttpStatusCodesEnum;
import com.google.light.server.dto.search.GSSClientLoginTokenInfoDto;
import com.google.light.server.exception.checked.InvalidGSSClientLoginToken;
import com.google.light.server.persistence.dao.OAuth2ConsumerCredentialDao;
import com.google.light.server.persistence.dao.TestDao;
import com.google.light.server.persistence.entity.admin.OAuth2ConsumerCredentialEntity;
import com.google.light.server.utils.LightUtils;
import com.google.light.testingutils.TestingUtils;

/**
 * Test for {@link GSSClientLoginTokenManagerImpl}
 * 
 * @author Walter Cacau
 */
public class GSSClientLoginTokenManagerImplTest extends AbstractLightServerTest {
  private static final String SAMPLE_CAPTCHA_ANSWER = "captchaAnswer";
  private static final String SAMPLE_CAPTCHA_TOKEN = "captchaToken";
  private static final String SAMPLE_PASSWORD = "password";
  private static final String SAMPLE_USERNAME = "username";
  private static final String SAMPLE_TOKEN = "SAMPLE_TOKEN";
  ClientLogin clientLoginInstance;
  OAuth2ConsumerCredentialEntity consumerCredentialEntityInstance;
  private OAuth2ConsumerCredentialDao consumerCredentialDao;
  private GSSClientLoginTokenManagerImpl tokenManager;
  private MockHttpTransport httpTransport;
  private TestDao testDao;

  @Before
  @Override
  public void setUp() {
    super.setUp();
    httpTransport = mock(MockHttpTransport.class, Mockito.CALLS_REAL_METHODS);

    consumerCredentialDao = injector.getInstance(OAuth2ConsumerCredentialDao.class);
    testDao = injector.getInstance(TestDao.class);

    clientLoginInstance = new ClientLogin();

    Provider<ClientLogin> clientLoginProvider =
        TestingUtils.getMockProviderFor(clientLoginInstance);

    tokenManager = new GSSClientLoginTokenManagerImpl(consumerCredentialDao, httpTransport,
        clientLoginProvider);
  }

  /**
   * Test for {@link GSSClientLoginTokenManagerImpl#getCurrentToken()}
   */
  @Test
  public void test_getCurrentToken() {

    // TODO(waltercacau): Inject a clock in GSSClientLoginTokenManagerImpl instead of
    // using time ranges

    // Negative test: no credential
    testDao.delete(consumerCredentialDao.get(GSS));
    assertEquals(null, tokenManager.getCurrentToken());

    // Positive test:
    putOAuth2ConsumerCredentialWithExtraData(buildTokenInfoJson(SAMPLE_TOKEN,
        LightUtils.getCurrentTimeInMillis() + LightConstants.ONE_DAY_IN_MILLIS));

    assertEquals(SAMPLE_TOKEN, tokenManager.getCurrentToken());

    // Negative test: wrong json
    putOAuth2ConsumerCredentialWithExtraData("-");

    assertEquals(null, tokenManager.getCurrentToken());

    // Negative test: Expired token
    putOAuth2ConsumerCredentialWithExtraData(buildTokenInfoJson(SAMPLE_TOKEN,
        LightUtils.getCurrentTimeInMillis() - LightConstants.ONE_DAY_IN_MILLIS));

    assertEquals(null, tokenManager.getCurrentToken());

  }

  /**
   * Test for {@link GSSClientLoginTokenManagerImpl#authenticate(String, String, String, String)}
   * 
   * Negative test only here (and assertions about captcha setting), positive test can be found in
   * {@link GSSClientLoginTokenManagerImplITCase#test_all()}
   * 
   * @throws IOException
   */
  @Test
  public void test_authenticate_withargs() throws IOException {
    feedHttpTransportWithForbiddenClientLoginResponse();
    try {
      tokenManager.authenticate(SAMPLE_USERNAME, SAMPLE_PASSWORD, SAMPLE_CAPTCHA_TOKEN,
          SAMPLE_CAPTCHA_ANSWER);
      fail("should have failed");
    } catch (ClientLoginResponseException e) {
      // expected
    }

    assertEquals(clientLoginInstance.captchaAnswer, SAMPLE_CAPTCHA_ANSWER);
    assertEquals(clientLoginInstance.captchaToken, SAMPLE_CAPTCHA_TOKEN);
  }

  /**
   * Test for {@link GSSClientLoginTokenManagerImpl#authenticate()}
   * 
   * Negative test only here (and assertions about captcha setting), positive test can be found in
   * {@link GSSClientLoginTokenManagerImplITCase#test_all()}
   * 
   * @throws IOException
   */
  @Test
  public void test_authenticate_noargs() throws IOException {
    putOAuth2ConsumerCredentialWithExtraData(null);

    feedHttpTransportWithForbiddenClientLoginResponse();
    try {
      tokenManager.authenticate();
      fail("should have failed");
    } catch (ClientLoginResponseException e) {
      // expected
    }

    assertEquals(clientLoginInstance.username, SAMPLE_USERNAME);
    assertEquals(clientLoginInstance.password, SAMPLE_PASSWORD);
    assertEquals(clientLoginInstance.captchaAnswer, null);
    assertEquals(clientLoginInstance.captchaToken, null);
  }

  /**
   * Test for {@link GSSClientLoginTokenManagerImpl#validateCurrentToken()}
   * 
   * Negative test only here, positive test can be found in
   * {@link GSSClientLoginTokenManagerImplITCase#test_all()}
   * 
   * @throws IOException
   * @throws InvalidGSSClientLoginToken
   */
  @Test(expected = InvalidGSSClientLoginToken.class)
  public void test_validateCurrentToken() throws IOException, InvalidGSSClientLoginToken {
    when(httpTransport.buildGetRequest((String) any())).thenReturn(
        new MockLowLevelHttpRequest() {
          @Override
          public LowLevelHttpResponse execute() throws IOException {
            throw new IOException();
          }
        }
        );
    putOAuth2ConsumerCredentialWithExtraData(buildTokenInfoJson(SAMPLE_TOKEN,
        LightUtils.getCurrentTimeInMillis() + LightConstants.ONE_DAY_IN_MILLIS));

    tokenManager.validateCurrentToken();
  }

  private void feedHttpTransportWithForbiddenClientLoginResponse() throws IOException {
    when(httpTransport.buildPostRequest((String) any())).thenReturn(
        new MockLowLevelHttpRequest() {
          @Override
          public LowLevelHttpResponse execute() throws IOException {
            MockLowLevelHttpResponse result = new MockLowLevelHttpResponse();
            result.setStatusCode(HttpStatusCodesEnum.FORBIDDEN.getStatusCode());
            result.setContentType(ContentTypeEnum.TEXT_PLAIN.get());
            result.setContent(TestingUtils
                .getResourceAsStream(GSS_CLIENTLOGIN_SAMPLE_FORBIDDEN_RESPONSE_TXT.get()));
            return result;
          }
        }
        );
  }

  private void putOAuth2ConsumerCredentialWithExtraData(String extraData) {
    consumerCredentialDao.put(null,
        new OAuth2ConsumerCredentialEntity.Builder()
            .providerName(GSS.name())
            .clientId(SAMPLE_USERNAME)
            .clientSecret(SAMPLE_PASSWORD)
            .extraData(extraData).build());
  }

  private String buildTokenInfoJson(String token, Long expiresInMillis) {
    return new GSSClientLoginTokenInfoDto.Builder().token(token).expiresInMillis(expiresInMillis)
        .build().toJson();
  }

}
