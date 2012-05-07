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

import static com.google.light.server.constants.OAuth2ProviderService.GOOGLE_LOGIN;
import static com.google.light.testingutils.TestResourcePaths.GOOGLE_TOKEN_INFO_JSON;
import static com.google.light.testingutils.TestingUtils.compareScopes;
import static com.google.light.testingutils.TestingUtils.getResourceAsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.light.server.constants.LightEnvEnum;
import com.google.light.server.utils.JsonUtils;
import com.google.light.testingutils.GaeTestingUtils;
import org.junit.Test;

/**
 * Test for {@link GoogleLoginTokenInfo}.
 * 
 * @author Arjun Satyapal
 */
public class GoogleLoginTokenInfoTest {
  /**
   * Test to validate JSON Parsing for {@link GoogleLoginTokenInfo}.
   * TODO(arjuns) : Fix this test.
   */
  @Test
  public void test_jsonToGoogleTokenInfo_Parsing() throws Exception {
    GaeTestingUtils.cheapEnvSwitch(LightEnvEnum.UNIT_TEST);

    String jsonString = getResourceAsString(GOOGLE_TOKEN_INFO_JSON.get());
    System.out.println(jsonString);
    GoogleLoginTokenInfo tokenInfo = JsonUtils.getPojo(jsonString, GoogleLoginTokenInfo.class);
    tokenInfo.validate();

    assertEquals("160638920188.apps.googleusercontent.com", tokenInfo.getIssuedTo());
    assertEquals("160638920188.apps.googleusercontent.com", tokenInfo.getAudience());
    assertEquals("115639870677665060321", tokenInfo.getUserId());
    assertTrue(compareScopes(GOOGLE_LOGIN.getScopes(), tokenInfo.getScope()));

    // TODO(arjuns): Fix this.
    // checkPositiveLong(tokenInfo.getExpiresInMillis(), "expiresInMillis");
    assertEquals("unit-test1@myopenedu.com", tokenInfo.getEmail());
    assertTrue(tokenInfo.getVerifiedEmail());
    assertEquals("offline", tokenInfo.getAccessType());
  }

  // TODO(arjuns): Make this test mandatory for all DTOs.
  @Test
  public void test_toJsonAndXml() throws Exception {
    GoogleLoginTokenInfo googleLoginTokenInfo = new GoogleLoginTokenInfo.Builder()
        .userId("115639870677665060321")
        .email("unit-test1@myopenedu.com")
        .verifiedEmail(true)
        .issuedTo("160638920188.apps.googleusercontent.com")
        .audience("160638920188.apps.googleusercontent.com")
        .scope("https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email")
        .expiresIn(3600)
        .expiresInMillis(1332491059527L)
        .accessType("offline")
        .build();

    String json = JsonUtils.toJson(googleLoginTokenInfo);
    System.out.println(json);
    GoogleLoginTokenInfo foo1 = JsonUtils.getPojo(json, GoogleLoginTokenInfo.class);
    assertEquals(googleLoginTokenInfo, foo1);
  }
}
