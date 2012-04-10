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

import static com.google.light.server.utils.LightPreconditions.*;

/**
 * List of the supported language interfaces by GSS (Google Site Search).
 * 
 * This enum only show languages that are supported by Light's UI, although
 * GSS supports more. See {@link SupportedLanguagesEnum}.
 * 
 * @author Walter Cacau
 * @see https://developers.google.com/custom-search/docs/xml_results?hl=en#interfaceLanguages
 */
public enum GSSSupportedLanguagesEnum {
  ENGLISH("en"),
  PERSIAN("fa"),
  PORTUGUESE_BRAZIL("pt-BR"),
  PORTUGUESE_PORTUGAL("pt-PT");

  /**
   * Language code conforming GSS standards
   */
  private String languageCode;
  private String languageCodeLowerCase;

  GSSSupportedLanguagesEnum(String languageCode) {
    this.languageCode = languageCode;
    this.languageCodeLowerCase = languageCode.toLowerCase();
  }

  /**
   * Get's the enum value for the closest (most similar) language
   * to the one given or {@link ENGLISH} if it can't figure out a better one.
   * 
   * @param clientLanguageCode
   * @return
   */
  public static GSSSupportedLanguagesEnum getClosestGSSSupportedLanguage(String clientLanguageCode) {
    checkNotBlank(clientLanguageCode, "clientLanguageCode");
    clientLanguageCode = clientLanguageCode.toLowerCase();

    // first we look for an exact match
    for (GSSSupportedLanguagesEnum curr : GSSSupportedLanguagesEnum.values()) {
      if (curr.languageCodeLowerCase.equals(clientLanguageCode))
        return curr;
    }

    // Then we look for partial matches
    String prefix = clientLanguageCode.split("-")[0];
    String prefixWithDash = prefix + "-";
    for (GSSSupportedLanguagesEnum curr : GSSSupportedLanguagesEnum.values()) {
      if (curr.languageCodeLowerCase.equals(prefix)
          || curr.languageCodeLowerCase.startsWith(prefixWithDash))
        return curr;
    }

    // Otherwise, default to English (as GSS already does)
    return ENGLISH;
  }

  public String getLanguageCode() {
    return languageCode;
  }

}
