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
package com.google.light.server.servlets.oauth2.google;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.light.server.dto.DtoInterface;
import java.io.IOException;

/**
 * Interface to encapsulate methods required by a generic OAuth2 Helper.
 * 
 * @author Arjun Satyapal
 */
public interface OAuth2Helper {
  /**
   * Method to get {@link AuthorizationCodeFlow}.
   * 
   * @return
   */
  public AuthorizationCodeFlow getAuthorizationCodeFlow();

  /**
   * Method to fetch AccessToken.
   * 
   * @param callbackUrl URL that was used to redirect the user back to Callback Servlet.
   * @param authorizationCode code returned by OAuth2 Provider.
   * @return
   * @throws IOException
   */
  public TokenResponse getAccessToken(String callbackUrl, String authorizationCode)
      throws IOException;

  /**
   * Mehtod to refresh AccessToken.
   * 
   * @param refreshToken
   * @param clazz
   * @return
   * @throws IOException
   */
  public <D extends DtoInterface<D>> D refreshAccessToken(String refreshToken, Class<D> clazz)
      throws IOException;

  /**
   * Method to fetch TokenInformation from OAuth2 Provider.
   * 
   * @param accessToken
   * @param clazz
   * @return
   * @throws IOException
   */
  public <D extends DtoInterface<D>> D getTokenInfo(String accessToken, Class<D> clazz)
      throws IOException;

  /**
   * Get a Google URI where Light should redirect User's browser to initiate the OAuth Procedure.
   * 
   * For more details, see {@link https://developers.google.com/accounts/docs/OAuth2Login}. For
   * browser based Login Flow, a sample URL might look like :
   * 
   * <pre>
   *   https://accounts.google.com/o/oauth2/auth?
   *   scope=https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo
   *   .email+https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.profile& state=%2Fprofile&
   *   redirect_uri=https%3A%2F%2Foauth2-login-demo.appspot.com%2Foauthcallback& response_type=token&
   *   client_id=812741506391.apps.googleusercontent.com
   * </pre>
   * 
   * @param cbUrl : Url where OAuth2 Provider should redirect the user.
   * @return
   */
  public String getOAuth2RedirectUri(String cbUrl);

  /**
   * Method to get complete OAuth2 Redirect URI for the OAuth2 Provider, ensuring that user
   * is forced to give access.
   * 
   * <pre>
   * e.g. 
   * Google returns refreshToken only when User manually gives access. Now there are two flows :<br>
   * 1. Force user to give access at every instance. Then Google will return refreshToken every
   * time.<br>
   * 2. Ask user for access for first time, and Google will give refreshToken. And then save
   * refreshToken on DataStore.
   * <p>
   * With Approach 1, there is issue with UserExperience and therefore Approach2 is selected.
   * <p>
   * With Approach 2, there is issue when Light fails to save the refreshToken. In that case, Google
   * will not return refreshToken unless :
   * <p>
   * 1. User revokes access. 2. Light forces user to give access again with approval_prompt=true.
   * see {@link https://developers.google.com/accounts/docs/OAuth2WebServer#formingtheurl}.
   * <p>
   * Note that, every time user gives an approval, value of refreshToken changes. And if caller of
   * this method has persisted refreshToken earlier, then client is responsible for updating it.
   * </pre>
   * 
   * @param cbUrl
   * @return
   */
  public String getOAuth2RedirectUriWithPrompt(String cbUrl);

  /**
   * Method to verify if the available accessToken is a valid AccessToken. It can become invalid in
   * following scenarios : <br>
   * 1. when Person revokes the access. <br>
   * 2. If an invalid token value is used. <br>
   * 3. TODO(arjuns): See what happens for expired token. <br>
   * TODO(arjuns): See what is the proper exception to catch for invalid Token.
   * 
   * This method depends on {{@link #getTokenInfoFromGoogle(String, String, String)}
   * 
   * @param tokenInfo
   * @return
   * 
   * @param accessToken
   * @param clazz
   * @return
   */
  public <D extends DtoInterface<D>> boolean isValidAccessToken(String accessToken, Class<D> clazz);
}
