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
package com.google.light.server.dto.login;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Ignore;

import org.junit.Test;

import com.google.common.collect.ImmutableMapBuilder;
import com.google.light.server.dto.AbstractDtoTest;
import com.google.light.server.dto.LoginStateDto;
import com.google.light.server.dto.LoginStateDto.Builder;
import com.google.light.server.utils.QueryUtils;
import com.google.light.testingutils.TestResourcePaths;
import com.google.light.testingutils.TestingUtils;

/**
 * Test for {@link LoginStateDto}
 * 
 * @author Walter Cacau
 */
public class LoginStateDtoTest extends AbstractDtoTest {

  private static final String SAMPLE_REDIRECT_PATH = "/page.html#bla";

  /**
   * Test constructing this Dto through QueryUtils.
   */
  @Test
  public void test_ConstructThroughQueryUtils() {
    // Just positive tests. We can count with enough negative tests in QueryUtilsTest

    LoginStateDto dto =
        QueryUtils.getValidDto(TestingUtils
            .getMockedRequestWithParameterMap(new ImmutableMapBuilder<String, String[]>()
                .put("redirectPath", new String[] { SAMPLE_REDIRECT_PATH })
                .getMap()), LoginStateDto.class);

    assertNotNull(dto);
    assertEquals(SAMPLE_REDIRECT_PATH, dto.getRedirectPath());
  }

  @Test
  @Override
  public void test_builder() throws Exception {
    // already tested in test_validate
  }

  private Builder getDefaultBuilder() {
    return new LoginStateDto.Builder();
  }

  @Test
  @Override
  public void test_toJson() throws Exception {
    assertEquals(
        TestingUtils.getResourceAsString(TestResourcePaths.LOGIN_STATE_JSON.get()),
        getDefaultBuilder().redirectPath(SAMPLE_REDIRECT_PATH).build().toJson());
  }

  @Test
  @Override
  public void test_validate() throws Exception {
    // Positive case: redirectPath not defined
    LoginStateDto dto = getDefaultBuilder().build();
    assertNotNull(dto);
    assertEquals(null, dto.getRedirectPath());

    // Positive case: redirectPath defined and well formated
    dto = getDefaultBuilder().redirectPath(SAMPLE_REDIRECT_PATH).build();
    assertNotNull(dto);
    assertEquals(SAMPLE_REDIRECT_PATH, dto.getRedirectPath());

    // Negative case:
    try {
      getDefaultBuilder().redirectPath("www.evil.com").build();
      fail("should have failed");
    } catch (IllegalStateException e) {
      // expected
    }
  }

  @Ignore(value="TODO(arjuns): Fix me.")
  @Test
  @Override
  public void test_toXml() throws Exception {
    try {
      getDefaultBuilder().build().toXml();
      fail("should have failed");
    } catch (UnsupportedOperationException e) {
      // expected
    }
  }
}
