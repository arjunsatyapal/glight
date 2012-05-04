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
package com.google.light.server.exception.unchecked;

import com.google.light.server.constants.OAuth2ProviderEnum;


/**
 * This indicates that an attempt was done to fetch Consumer Credentials for the specified provider.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class MissingConsumerCredentialExecption extends RuntimeException {
  /**
   * {@inheritDoc}
   * @param message
   */
  public MissingConsumerCredentialExecption(OAuth2ProviderEnum provider) {
      super("Missing Consumer Credentials for : " + provider);
  }
}
