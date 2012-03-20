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

import static com.google.light.testingutils.TestingUtils.getResourceAsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.inject.internal.Sets;
import com.google.light.server.servlets.oauth2.google.GoogleOAuth2Helper;
import com.google.light.server.utils.JsonUtils;
import java.util.Set;
import org.junit.Test;

/**
 * Test for {@link GoogleTokenInfo}.
 * 
 * @author Arjun Satyapal
 */
public class GoogleTokenInfoTest {
  private static final String GOOGLE_TOKEN_INFO_JSON_FILE_PATH = 
      "/login/oauth2/google/google_token_info.json";
  
  /**
   * Test to validate JSON Parsing for {@link GoogleTokenInfo}.
   */
  @Test
  public void test_jsonToGoogleTokenInfo_Parsing() throws Exception {
    String jsonString = getResourceAsString(GOOGLE_TOKEN_INFO_JSON_FILE_PATH);
    GoogleTokenInfo tokenInfo = JsonUtils.getDto(jsonString, GoogleTokenInfo.class);
    
    assertEquals("1234.apps.googleusercontent.com", tokenInfo.getIssuedTo());
    assertEquals("1234.apps.googleusercontent.com", tokenInfo.getAudience());
    assertEquals("123456789", tokenInfo.getUserId());
    
    Set<String> expectedScope = Sets.newLinkedHashSet();
    for (String currScope : GoogleOAuth2Helper.LOGIN_SCOPE) {
      expectedScope.add(currScope);
    }
    
    Set<String> actualScope = Sets.newLinkedHashSet();
    for (String currScope : tokenInfo.getScope().split(" ")) {
      actualScope.add(currScope);
    }
    
    assertEquals(expectedScope, actualScope);
    
    assertEquals(3600, tokenInfo.getExpiresInSeconds());
    assertEquals("unit-test1@myopenedu.com", tokenInfo.getEmail());
    assertTrue(tokenInfo.getVerifiedEmail());
    assertEquals("online", tokenInfo.getAccessType());
  }
}
