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
define(['dojo/_base/declare', 'dojo/number'], function(declare, numberUtils) {
  /**
   * Some utilities for dealing with i18n.
   *
   * @class
   * @name light.utils.LanguageUtils
   */
  var LanguageUtils = {
    /** @lends light.utils.LanguageUtils */
    /**
     * Determines if the language referred by the given languageCode is written
     * left to right.
     *
     * @param {string=}
     *          languageCode Language code (optional, defaults to
     *          {@link light.utils.LanguageUtils.currentLocale}).
     * @return {Boolean} True if the language is left to right.
     */
    isLeftToRight: function(languageCode) {
      if (languageCode == undefined)
        languageCode = this.currentLocale;
      // TODO(waltercacau): find a list of RTL languageCodes or a better way to
      // detect it.
      languageCode = languageCode.split('-')[0];
      if (languageCode == 'fa') {
        return false;
      }
      return true;
    },

    /**
     * Alias for {@link dojo.number.format}
     *
     * @see http://dojotoolkit.org/api/dojo/number/format
     */
    formatNumber: function() {
      return numberUtils.format.apply(numberUtils, arguments);
    },

    /**
     * The current locale for the UI according to dojo. Alias for
     * {@link dojo.locale}
     *
     * @const
     */
    currentLocale: dojo.locale
  };
  return LanguageUtils;
});
