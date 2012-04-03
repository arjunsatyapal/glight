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
 * Test for {@link GSSSupportedLanguageEnum}
 * 
 * @author Walter Cacau
 */
public class GSSSupportedLanguageEnumTest {
  @Test
  public void test_getClosestGSSSupportedLanguage() {
    // Asserting exact matches
    for (GSSSupportedLanguageEnum curr : GSSSupportedLanguageEnum.values()) {
      assertEquals(curr,
          GSSSupportedLanguageEnum.getClosestGSSSupportedLanguage(curr.getLanguageCode()));
    }

    // Asserting some non-exact matches
    assertEquals(GSSSupportedLanguageEnum.PORTUGUESE_BRAZIL,
        GSSSupportedLanguageEnum.getClosestGSSSupportedLanguage("pt"));
    assertEquals(GSSSupportedLanguageEnum.ENGLISH,
        GSSSupportedLanguageEnum.getClosestGSSSupportedLanguage("en-us"));
  }
}
