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
package com.google.light.server.manager.implementation.oauth2.owner;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.GuiceUtils.getInstance;

import com.google.api.client.auth.oauth2.RefreshTokenRequest;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.io.CharStreams;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.light.server.constants.LightEnvEnum;
import com.google.light.server.constants.OAuth2ProviderService;
import com.google.light.server.dto.DtoInterface;
import com.google.light.server.dto.oauth2.owner.OAuth2OwnerTokenDto;
import com.google.light.server.exception.unchecked.httpexception.UnauthorizedException;
import com.google.light.server.manager.implementation.oauth2.consumer.OAuth2ConsumerCredentialManagerFactory;
import com.google.light.server.manager.interfaces.OAuth2ConsumerCredentialManager;
import com.google.light.server.manager.interfaces.OAuth2OwnerTokenManager;
import com.google.light.server.persistence.dao.OAuth2OwnerTokenDao;
import com.google.light.server.persistence.entity.oauth2.owner.OAuth2OwnerTokenEntity;
import com.google.light.server.servlets.oauth2.google.pojo.AbstractOAuth2TokenInfo;
import com.google.light.server.utils.JsonUtils;
import com.google.light.server.utils.LightPreconditions;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation for {@link OAuth2OwnerTokenManager}.
 * 
 * @author Arjun Satyapal
 */
public class OAuth2OwnerTokenManagerImpl implements OAuth2OwnerTokenManager {
  private static final Logger logger = Logger
      .getLogger(OAuth2OwnerTokenManagerImpl.class.getName());

  private OAuth2ProviderService providerService;
  private OAuth2OwnerTokenDao dao;
  private HttpTransport httpTransport;
  private JsonFactory jsonFactory;
  private OAuth2OwnerTokenEntity entity;

  @Inject
  public <D extends AbstractOAuth2TokenInfo<D>> OAuth2OwnerTokenManagerImpl(
      OAuth2OwnerTokenDao ownerTokenDao, HttpTransport httpTransport, JsonFactory jsonFactory,
      @Assisted OAuth2ProviderService providerService) {
    this.providerService = checkNotNull(providerService, "providerService");

//    // TODO(arjuns): Add test for this.
//    if (!providerService.isUsedForLogin()) {
//      SessionManager sessionManager = getInstance(injector, SessionManager.class);
//      LightPreconditions.checkValidSession(sessionManager);
//    }
    
    // TODO(arjuns): Figure out if any validation is required here.
    

    this.dao = checkNotNull(ownerTokenDao, "ownerTokenDao");
    this.httpTransport = checkNotNull(httpTransport, "httpTransport");
    this.jsonFactory = checkNotNull(jsonFactory, "jsonFactory");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public OAuth2OwnerTokenEntity get() {
    if (entity == null) {
      // Try to fetch.
      this.entity = dao.getByProviderService(providerService);

      // If it is still null, then return.
      if (this.entity == null) {
        return null;
      }
    }

    /*
     * Unfortunately without doing lot of duplication of code, we cannot avoid testing this.
     * For unit-test values are fetched from a file. So they will eventually expire. And we dont
     * want to talk to Google in UNIT_TEST environment. So for UNIT_TEST environemtn, we will
     * skip the refresh procedure.
     */
    if (LightEnvEnum.getLightEnv() != LightEnvEnum.UNIT_TEST) {
      // TODO(arjuns): Add test for this to ensure this is called.
      // If entity hasExpired, then refresh it.

      if (entity.hasExpired()) {
        entity = refresh();
        checkNotNull(entity, "After refresh, entity should not be null.");
        Preconditions.checkArgument(
            !entity.hasExpired(),
            "After refresh, entity should not be in expired state. This happened for PersonId : "
                + entity.getPersonId() + " and ProviderService = "
                + entity.getProviderService());

      }
    }

    return this.entity;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public OAuth2OwnerTokenEntity put(OAuth2OwnerTokenEntity entity) {
    checkArgument(providerService == entity.getProviderService(), "TokenManager for "
        + providerService + " was used to persist Token of type " + entity.getProviderService());
    this.entity = dao.put(entity);
    return this.entity;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public OAuth2OwnerTokenEntity getByProviderUserId(String providerUserId) {
    if (providerService.isUsedForLogin()) {
      return dao.getByProviderServiceAndProviderUserId(providerService, providerUserId);
    }

    throw new IllegalArgumentException(
        "this method should not be called for non-login TokenManager"
            + " like " + providerService);
  }

  /**
   * {@inheritDoc} TODO(arjuns): When OAuth2 Provider other then Google is added, this code may have
   * to change.
   */
  @SuppressWarnings("rawtypes")
  @Override
  public OAuth2OwnerTokenEntity refresh() {
    try {
      logger.info("Refreshing Token[" + providerService + "] for Person["
          + entity.getPersonId() + "].");
      checkNotNull(entity, "entity should not be null.");

      OAuth2ConsumerCredentialManagerFactory consumerCredFactory = getInstance(
          OAuth2ConsumerCredentialManagerFactory.class);
      OAuth2ConsumerCredentialManager cosumerCredManager =
          consumerCredFactory.create(providerService.getProvider());

      RefreshTokenRequest refreshRequest = new RefreshTokenRequest(
          httpTransport,
          jsonFactory,
          new GenericUrl(providerService.getTokenServerUrl()),
          entity.getRefreshToken())
          .setClientAuthentication(cosumerCredManager.getClientAuthentication());

      TokenResponse tokenResponse = null;
      
      try {
        tokenResponse = refreshRequest.execute();
      } catch (TokenResponseException e) {
        logger.log(Level.INFO, "Failed to refresh token due to : ", e);
        
        // Failed to refresh token. So lets delete the token.
        // TODO(arjuns): Once revokeToken code is there, first revoke and then delete.
        this.delete();
        throw new UnauthorizedException("OAuth2 token for " + providerService 
            + " could not be refreshed. So deleting it and forcing person to authorize again.");
      }

      @SuppressWarnings("unchecked")
      AbstractOAuth2TokenInfo newTokenInfo = getInfoByAccessToken(tokenResponse.getAccessToken());

      OAuth2OwnerTokenDto newDto = OAuth2OwnerTokenDto.getOAuth2OwnerTokenDto(
          entity.getPersonId(), entity.getRefreshToken(), tokenResponse,
          providerService, entity.getProviderUserId(), newTokenInfo.toJson());

      return put(newDto.toPersistenceEntity());
    } catch (Exception e) {
      // TODO(arjuns): Add exception handling.
      throw new RuntimeException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <D extends AbstractOAuth2TokenInfo<D>> D getInfo() {
    checkNotNull(entity, "entity should not be null");
    return getInfoByAccessToken(entity.getAccessToken());
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  public <D extends AbstractOAuth2TokenInfo<D>> D getInfoByAccessToken(String accessToken) {
    try {
      LightPreconditions.checkNotBlank(accessToken, "accessToken");
      GenericUrl tokenUrl = new GenericUrl(providerService.getTokenInfoUrl() + "?access_token=" +
          accessToken);

      HttpRequest tokenInfoRequest = httpTransport.createRequestFactory().buildGetRequest(tokenUrl);
      HttpResponse tokenInfoResponse = tokenInfoRequest.execute();

      String tokenInfoStr = CharStreams.toString(new InputStreamReader(
          tokenInfoResponse.getContent(), Charsets.UTF_8));

      DtoInterface<? extends AbstractOAuth2TokenInfo> tokenInfo =
          JsonUtils.getDto(tokenInfoStr, providerService.getTokenInfoClazz());

      return ((D) tokenInfo.validate());
    } catch (Exception e) {
      // TODO(arjuns): Add exception handling.
      throw new RuntimeException(e);
    }
  }

  /** 
   * {@inheritDoc}
   */
  @Override
  public void delete() {
    dao.deleteByProviderService(providerService);
  }
}
