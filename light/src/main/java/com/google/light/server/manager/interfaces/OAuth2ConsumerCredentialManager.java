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

import com.google.api.client.auth.oauth2.ClientParametersAuthentication;


/**
 * All OAuth Consumer Implementations should implement this.
 * 
 * TODO(arjuns): Add test for this class.
 * TODO(arjuns): Add methods for listAll, and updateCredentails.
 * @author Arjun Satyapal
 */
public interface OAuth2ConsumerCredentialManager {
  /**
   * Returns OAuth2 client_id 
   * @return
   */
  public String getClientId();
  
  /**
   * Returns OAuth2 client_secret.
   * @return
   */
  public String getClientSecret();
  
  /**
   * Returns {@link ClientParametersAuthentication}.
   * TODO(arjuns): Add test for this.
   * 
   * @return
   */
  public ClientParametersAuthentication getClientAuthentication();
}
