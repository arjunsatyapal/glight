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

import java.util.List;
import java.util.Locale;

/**
 * List of the supported language interfaces by Light's UI.
 * 
 * Keep this in sync with light.profile.js .
 * 
 * TODO(waltercacau): Add test for this.
 * 
 * @author Walter Cacau
 */
public enum SupportedLanguagesEnum {
  ENGLISH("en-us", new Locale("en", "US")),
  PERSIAN("fa", new Locale("fa")),
  PORTUGUESE_BRAZIL("pt-br", new Locale("pt", "BR"));

  /**
   * Language code conforming to dojo standards
   * 
   * @see http://dojotoolkit.org/reference-guide/1.7/quickstart/internationalization/globalization-
   *      guidelines/locale-and-resource-bundle-guidelines.html
   */
  private String clientLanguageCode;
  private Locale locale;

  SupportedLanguagesEnum(String clientLanguageCode, Locale locale) {
    this.clientLanguageCode = clientLanguageCode;
    this.locale = locale;
  }

  public String getClientLanguageCode() {
    return clientLanguageCode;
  }

  public Locale getLocale() {
    return locale;
  }

  /**
   * Get's the enum value for the closest (most similar) language from a language list ordered
   * by preference or {@link ENGLISH} if it can't figure out a better one.
   */
  public static SupportedLanguagesEnum getClosestPrefferedLanguage(List<Locale> localeList) {

    // first we look for an exact match in the preference order of the list
    for (Locale currLocale : localeList) {
      for (SupportedLanguagesEnum curr : SupportedLanguagesEnum.values()) {
        if (curr.locale.equals(currLocale))
          return curr;
      }
    }

    // Then we look for partial matches (language matches)
    for (Locale currLocale : localeList) {
      for (SupportedLanguagesEnum curr : SupportedLanguagesEnum.values()) {
        if (curr.locale.getLanguage().equals(currLocale.getLanguage()))
          return curr;
      }
    }

    // Otherwise, default to English
    return ENGLISH;
  }
}
