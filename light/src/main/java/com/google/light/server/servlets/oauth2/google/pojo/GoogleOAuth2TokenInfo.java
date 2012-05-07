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
package com.google.light.server.servlets.oauth2.google.pojo;

import org.codehaus.jackson.annotate.JsonCreator;

/**
 * Java representation for the JSON object returned by Google as a response to
 * {@link OldGoogleOAuth2Helper#getTokenInfo(String)}
 * 
 * TODO(arjuns): Update tests. and package for test.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class GoogleOAuth2TokenInfo extends AbstractOAuth2TokenInfo<GoogleOAuth2TokenInfo> {
  /**
   * {@inheritDoc}
   */
  @Override
  public GoogleOAuth2TokenInfo validate() {
    return super.validate();
  }
  
  // For Jaxb.
  @JsonCreator
  public GoogleOAuth2TokenInfo() {
    super(null);
  }
}
