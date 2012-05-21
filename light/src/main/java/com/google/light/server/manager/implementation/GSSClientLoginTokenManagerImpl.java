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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.constants.OAuth2ProviderEnum.GSS;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import com.google.api.client.googleapis.auth.clientlogin.ClientLogin;
import com.google.api.client.googleapis.auth.clientlogin.ClientLogin.Response;
import com.google.api.client.googleapis.auth.clientlogin.ClientLoginResponseException;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpTransport;
import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.light.server.constants.LightConstants;
import com.google.light.server.dto.search.GSSClientLoginTokenInfoDto;
import com.google.light.server.exception.checked.InvalidGSSClientLoginToken;
import com.google.light.server.manager.interfaces.GSSClientLoginTokenManager;
import com.google.light.server.persistence.dao.OAuth2ConsumerCredentialDao;
import com.google.light.server.persistence.entity.admin.OAuth2ConsumerCredentialEntity;
import com.google.light.server.utils.GaeUtils;
import com.google.light.server.utils.JsonUtils;
import com.google.light.server.utils.LightUtils;

/**
 * Implementation for {@link GSSClientLoginTokenManager}
 * 
 * @author Walter Cacau
 */
public class GSSClientLoginTokenManagerImpl implements GSSClientLoginTokenManager {
  private static final Logger logger = Logger.getLogger(GSSClientLoginTokenManagerImpl.class.getName());
  private OAuth2ConsumerCredentialDao consumerCredentialDao;
  private HttpTransport httpTransport;
  private Provider<ClientLogin> clientLoginProvider;
  public static final String GSS_CLIENTLOGIN_SERVICE = "cprose";
  public static final String GSS_CLIENTLOGIN_ACCEPTED_ACCOUNT_TYPE = "HOSTED_OR_GOOGLE";

  @Inject
  public GSSClientLoginTokenManagerImpl(OAuth2ConsumerCredentialDao consumerCredentialDao,
      HttpTransport httpTransport, Provider<ClientLogin> clientLoginProvider) {
    this.consumerCredentialDao = checkNotNull(consumerCredentialDao);
    this.httpTransport = checkNotNull(httpTransport);
    this.clientLoginProvider = checkNotNull(clientLoginProvider);
  }

  @Override
  public String getCurrentToken() {
    OAuth2ConsumerCredentialEntity consumerCredentialEntity = consumerCredentialDao.get(GSS);
    if(consumerCredentialEntity == null)
      return null;
    
    GSSClientLoginTokenInfoDto tokenInfo;
    try {
      tokenInfo =
          JsonUtils.getDto(consumerCredentialEntity.getExtraData(),
              GSSClientLoginTokenInfoDto.class).validate();
    } catch (Exception e) {
      // TODO(arjuns): Remove this validation when JsonUtils incorporates it.
      logger.log(Level.SEVERE, "Failed due to : " + Throwables.getStackTraceAsString(e));
      return null;
    }
    
    if (tokenInfo.getExpiresInMillis() >= LightUtils.getCurrentTimeInMillis())
      return tokenInfo.getToken();
    return null;
  }

  @Override
  public void validateCurrentToken() throws InvalidGSSClientLoginToken {
    String token = getCurrentToken();
    checkNotNull(token);

    try {
      HttpRequest request =
          httpTransport.createRequestFactory().buildGetRequest(
              new GenericUrl("http://www.google.com/cse/api/default/cse/"));
      request.getHeaders().setAuthorization(token);
      request.execute();
    } catch (Exception e) {
      throw new InvalidGSSClientLoginToken(e);
    }
  }

  @Override
  public String authenticate(String username, String password, String captchaToken,
      String captchaAnswer) throws ClientLoginResponseException {
    
    // Building request
    ClientLogin clientLogin = clientLoginProvider.get();
    clientLogin.transport = httpTransport;
    clientLogin.accountType = GSSClientLoginTokenManagerImpl.GSS_CLIENTLOGIN_ACCEPTED_ACCOUNT_TYPE;
    clientLogin.authTokenType = GSSClientLoginTokenManagerImpl.GSS_CLIENTLOGIN_SERVICE;
    clientLogin.username = checkNotBlank(username, "username");
    clientLogin.password = checkNotBlank(password, "password");
    if (!StringUtils.isEmpty(captchaToken) && !StringUtils.isEmpty(captchaAnswer)) {
      clientLogin.captchaToken = captchaToken;
      clientLogin.captchaAnswer = captchaAnswer;
    }
    clientLogin.applicationName = GaeUtils.getAppId()+LightConstants.GSS_CLIENTLOGIN_APPNAME_SUFFIX;

    Response response;
    try {
      response = clientLogin.authenticate();
    } catch (Exception e) {
      if (!(e instanceof ClientLoginResponseException)) {
        LightUtils.wrapIntoRuntimeExceptionAndThrow(e);
      }
      throw (ClientLoginResponseException) e;
    }
    String token = response.getAuthorizationHeaderValue();

    // Persisting
    GSSClientLoginTokenInfoDto tokenInfo = new GSSClientLoginTokenInfoDto.Builder()
        .token(token).expiresInMillis(LightUtils.getCurrentTimeInMillis() + LightConstants.GSS_CLIENTLOGIN_TOKEN_TIMEOUT_IN_MILLIS).build();
    OAuth2ConsumerCredentialEntity consumerCredentialEntity = new OAuth2ConsumerCredentialEntity.Builder()
        .providerName(GSS.name())
        .clientId(username)
        .clientSecret(password)
        .extraData(tokenInfo.toJson())
        .build();

    consumerCredentialDao.put(null, consumerCredentialEntity);
    
    return token;
  }

  @Override
  public String authenticate() throws ClientLoginResponseException {
    OAuth2ConsumerCredentialEntity consumerCredentialEntity =
        checkNotNull(consumerCredentialDao.get(GSS));
    return authenticate(consumerCredentialEntity.getClientId(),
        consumerCredentialEntity.getClientSecret(), null, null);
  }

}
