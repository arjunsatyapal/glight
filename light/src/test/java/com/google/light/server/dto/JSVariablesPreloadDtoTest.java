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
package com.google.light.server.dto;

import static com.google.light.server.constants.SupportedLanguagesEnum.ENGLISH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Ignore;

import org.junit.Test;

import com.google.light.server.constants.SupportedLanguagesEnum;
import com.google.light.server.exception.unchecked.BlankStringException;
import com.google.light.testingutils.TestResourcePaths;
import com.google.light.testingutils.TestingUtils;

/**
 * Test for {@link JSVariablesPreloadDto}
 * 
 * @author Walter Cacau
 */
public class JSVariablesPreloadDtoTest extends AbstractDtoTest {

  private static final SupportedLanguagesEnum SAMPLE_LANGUAGE = ENGLISH;

  @Test
  @Override
  public void test_builder() throws Exception {
    // Already tested in test_validate
  }

  @Test
  @Override
  public void test_toJson() throws Exception {
    assertEquals(
        TestingUtils.getResourceAsString(TestResourcePaths.JS_VARIABLES_PRELOAD_JSON.get()),
        getDefaultBuilder().build().toJson());
  }

  @Test
  @Override
  public void test_validate() throws Exception {
    // Positive test
    JSVariablesPreloadDto dto = getDefaultBuilder().build();
    assertNotNull(dto);
    assertEquals(dto.getLocale(), SAMPLE_LANGUAGE.getClientLanguageCode());

    // Negative test: locale = null
    try {
      getDefaultBuilder().locale(null).build();
      fail("should have failed");
    } catch (BlankStringException e) {
      // expected
    }

    // Negative test: locale = " "
    try {
      getDefaultBuilder().locale(" ").build();
      fail("should have failed");
    } catch (BlankStringException e) {
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

  private JSVariablesPreloadDto.Builder getDefaultBuilder() {
    return new JSVariablesPreloadDto.Builder().locale(SAMPLE_LANGUAGE.getClientLanguageCode());
  }

}
