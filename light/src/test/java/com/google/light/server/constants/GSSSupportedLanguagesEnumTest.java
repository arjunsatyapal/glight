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
package com.google.light.server.constants;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test for {@link GSSSupportedLanguagesEnum}
 * 
 * @author Walter Cacau
 */
public class GSSSupportedLanguagesEnumTest implements EnumTestInterface {
  /**
   * Test for {@link GSSSupportedLanguagesEnum#getClosestGSSSupportedLanguage(String)}
   */
  @Test
  public void test_getClosestGSSSupportedLanguage() {
    // Asserting exact matches
    for (GSSSupportedLanguagesEnum curr : GSSSupportedLanguagesEnum.values()) {
      assertEquals(curr,
          GSSSupportedLanguagesEnum.getClosestGSSSupportedLanguage(curr.getLanguageCode()));
    }

    // Asserting some non-exact matches
    assertEquals(GSSSupportedLanguagesEnum.PORTUGUESE_BRAZIL,
        GSSSupportedLanguagesEnum.getClosestGSSSupportedLanguage("pt"));
    assertEquals(GSSSupportedLanguagesEnum.ENGLISH,
        GSSSupportedLanguagesEnum.getClosestGSSSupportedLanguage("en-us"));
  }

  @Override
  public void test_count() {
    assertEquals(
        "If you add/remove a new language in SupportedLanguagesEnum," +
            " do the same in GSSSupportedLanguagesEnum",
        3, SupportedLanguagesEnum.values().length);
    assertEquals(4, GSSSupportedLanguagesEnum.values().length);
  }
}
