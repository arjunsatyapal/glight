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


import com.google.light.server.utils.JsonUtils;
import org.junit.Test;

/**
 * Test for {@link GoogleUserInfo}.
 * 
 * @author Arjun Satyapal
 */
public class GoogleUserInfoTest {
  private static final String GOOGLE_USER_INFO_JSON_FILE_PATH = 
      "/login/oauth2/google/google_user_info.json";
  
  /**
   * Test to validate JSON Parsing for {@link GoogleUserInfo}.
   */
  @Test
  public void test_jsonParsing() throws Exception {
    String jsonString = getResourceAsString(GOOGLE_USER_INFO_JSON_FILE_PATH);
    GoogleUserInfo userTokenInfo = JsonUtils.getDto(jsonString, GoogleUserInfo.class);
    
    assertEquals("115639870677665060321", userTokenInfo.getId());
    assertEquals("unit-test1@myopenedu.com", userTokenInfo.getEmail());
    assertTrue(userTokenInfo.getVerifiedEmail());
    assertEquals("Unit Test1", userTokenInfo.getName());
    assertEquals("Unit", userTokenInfo.getGivenName());
    assertEquals("Test1", userTokenInfo.getFamilyName());
    assertEquals("en", userTokenInfo.getLocale());
  }
}
