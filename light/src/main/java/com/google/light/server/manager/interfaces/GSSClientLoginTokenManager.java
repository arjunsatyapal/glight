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
package com.google.light.server.manager.interfaces;

import java.io.IOException;

import com.google.api.client.googleapis.auth.clientlogin.ClientLoginResponseException;
import com.google.light.server.constants.OAuth2ProviderEnum;
import com.google.light.server.exception.checked.InvalidGSSClientLoginToken;
import com.google.light.server.persistence.entity.admin.OAuth2ConsumerCredentialEntity;

/**
 * Interface for the manager that takes care of the whole authentication operations
 * related with the ClientLogin flow to enable CSE Control API operations.
 * 
 * @author Walter Cacau
 */
public interface GSSClientLoginTokenManager {
  /**
   * Attempts to authenticate using Google ClientLogin.
   * 
   * <p>
   * If successfully authenticates with Google, it will persist to the datastore the clientlogin
   * token and the given credentials (username and password) of the Search Admin.
   * 
   * <p>
   * Throws if it was not possible to authenticate for any reason.
   * 
   * <p>
   * If the reason for not authenticating was google asking for a captcha challenge, then the
   * exception will contain a captchaToken and a captcha URL, which can be used for another attempt
   * to login.
   * 
   * @param username Cannot be blank
   * @param password Cannot be blank
   * @param captchaToken Can be null
   * @param captchaAnswer Can be null
   * @throws IOException
   * @returns The authentication token.
   */
  String authenticate(String username, String password, String captchaToken, String captchaAnswer)
      throws ClientLoginResponseException;

  /**
   * Attempts to authenticate using Google ClientLogin automatically using the stored
   * {@link OAuth2ConsumerCredentialEntity} for {@link OAuth2ProviderEnum#GSS} assuming there
   * will be no captcha challenge.
   * 
   * <p>
   * It is equivalent to calling
   * {@link GSSClientLoginTokenManager#authenticate(String, String, String, String)} with the stored
   * credentials.
   */
  String authenticate() throws ClientLoginResponseException;

  /**
   * Get's the current known non-expired token or null if none is found
   * 
   * @return
   */
  String getCurrentToken();

  /**
   * Throws if the current token is not valid.
   * 
   * It issues a request to a CSE URL to check if the authentication
   * token is properly working.
   * 
   * @param token
   * @throws InvalidGSSClientLoginToken
   */
  void validateCurrentToken() throws InvalidGSSClientLoginToken;

}
