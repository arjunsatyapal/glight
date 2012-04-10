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

import java.util.List;
import java.util.Locale;

import org.junit.Test;

import com.google.common.collect.Lists;

/**
 * Test for {@link SupportedLanguagesEnum}
 * 
 * @author Walter Cacau
 */
public class SupportedLanguagesEnumTest implements EnumTestInterface {

  private static final Locale SAMPLE_NON_SUPPORTED_LOCALE = new Locale("xs", "XS");

  @Test
  @Override
  public void test_count() {
    assertEquals(3, SupportedLanguagesEnum.values().length);
  }

  /**
   * Test for {@link SupportedLanguagesEnum#getClosestPrefferedLanguage(java.util.List)}
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Test
  public void test_getClosestPrefferedLanguage() {

    // Asserting exact matches
    for (SupportedLanguagesEnum curr : SupportedLanguagesEnum.values()) {
      assertEquals(curr,
          SupportedLanguagesEnum.getClosestPrefferedLanguage(Lists.newArrayList(curr.getLocale())));
    }

    // Asserting exact matches when the list has more then one element and the first is the answer
    SupportedLanguagesEnum[] values = SupportedLanguagesEnum.values();
    Locale last = values[values.length - 1].getLocale();
    for (SupportedLanguagesEnum curr : values) {
      assertEquals(curr,
          SupportedLanguagesEnum.getClosestPrefferedLanguage(Lists.newArrayList(curr.getLocale(),
              last)));
      last = curr.getLocale();
    }

    // Asserting exact matches when the list has more then one element and the second is the answer
    for (SupportedLanguagesEnum curr : values) {
      assertEquals(curr,
          SupportedLanguagesEnum.getClosestPrefferedLanguage(Lists.newArrayList(
              SAMPLE_NON_SUPPORTED_LOCALE, curr.getLocale())));
    }

    // Asserting some non-exact matches
    assertEquals(SupportedLanguagesEnum.PORTUGUESE_BRAZIL,
        SupportedLanguagesEnum.getClosestPrefferedLanguage(Lists.newArrayList(new Locale("pt"))));

    assertEquals(SupportedLanguagesEnum.PERSIAN,
        SupportedLanguagesEnum.getClosestPrefferedLanguage(Lists
            .newArrayList(new Locale("fa", "IR"))));

    // Asserting over the corner cases
    assertEquals(SupportedLanguagesEnum.ENGLISH,
        SupportedLanguagesEnum.getClosestPrefferedLanguage(Lists
            .newArrayList(SAMPLE_NON_SUPPORTED_LOCALE)));
    assertEquals(SupportedLanguagesEnum.ENGLISH,
        SupportedLanguagesEnum.getClosestPrefferedLanguage((List) Lists.newArrayList()));
  }
}
