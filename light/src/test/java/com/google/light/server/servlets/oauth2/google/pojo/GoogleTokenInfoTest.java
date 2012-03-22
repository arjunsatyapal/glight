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

import static com.google.light.server.constants.OAuth2Provider.GOOGLE_LOGIN;
import static com.google.light.server.utils.LightPreconditions.checkPositiveLong;
import static com.google.light.testingutils.TestResourcePaths.GOOGLE_TOKEN_INFO_JSON;
import static com.google.light.testingutils.TestingUtils.getResourceAsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.inject.internal.Sets;
import com.google.light.server.utils.JsonUtils;
import java.util.Set;
import org.junit.Test;

/**
 * Test for {@link GoogleTokenInfo}.
 * 
 * @author Arjun Satyapal
 */
public class GoogleTokenInfoTest {
  /**
   * Test to validate JSON Parsing for {@link GoogleTokenInfo}.
   */
  @Test
  public void test_jsonToGoogleTokenInfo_Parsing() throws Exception {
    String jsonString = getResourceAsString(GOOGLE_TOKEN_INFO_JSON.get());
    GoogleTokenInfo tokenInfo = JsonUtils.getDto(jsonString, GoogleTokenInfo.class);
    
    assertEquals("1234.apps.googleusercontent.com", tokenInfo.getIssuedTo());
    assertEquals("1234.apps.googleusercontent.com", tokenInfo.getAudience());
    assertEquals("123456789", tokenInfo.getUserId());
    
    Set<String> expectedScope = Sets.newLinkedHashSet();
    for (String currScope : GOOGLE_LOGIN.getScopes()) {
      expectedScope.add(currScope);
    }
    
    Set<String> actualScope = Sets.newLinkedHashSet();
    for (String currScope : tokenInfo.getScope().split(" ")) {
      actualScope.add(currScope);
    }
    
    assertEquals(expectedScope, actualScope);
    
    checkPositiveLong(tokenInfo.getExpiresInMillis());
    assertEquals("unit-test1@myopenedu.com", tokenInfo.getEmail());
    assertTrue(tokenInfo.getVerifiedEmail());
    assertEquals("online", tokenInfo.getAccessType());
  }
}
