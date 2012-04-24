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
define(['dojo/_base/declare', 'dojo/hash'], function(declare, dojoHash) {
  /**
   * Utilities for handling with the browser's URL
   *
   * @class
   * @name light.utils.URLUtils
   */
  return {
    /** @lends light.utils.URLUtils */

    /**
     * Small wrapper around dojoHash.get functionality
     * to enable better testing
     * @return {string} Current hash.
     */
    getHash: function() {
      return dojoHash();
    },

    /**
     * Small wrapper around dojoHash.set functionality to enable better testing.
     *
     * @param {string} hash
     *          Desired hash.
     * @param {boolean=} replaceCurrentInHistory
     *          If we should not create another entry for this hash in the
     *          browser's history.
     * @see http://dojotoolkit.org/reference-guide/1.7/dojo/hash.html#setter
     */
    setHash: function(hash, replaceCurrentInHistory) {
      dojoHash(hash, replaceCurrentInHistory);
    },

    /**
     * Redirects the user to the given URL/path
     * @param {string} url URL/Path.
     */
    redirect: function(urlOrPath) {
      window.location.href = urlOrPath;
    },

    /**
     * Redirects the user to the given URL/path replacing
     * the current URL in the browsers history.
     * @param {string} url URL/Path.
     */
    replace: function(urlOrPath) {
      window.location.replace(urlOrPath);
    },

    /**
     * Return's the current path.
     *
     * Example: http://www.example.com/folder/page.html would return page.html
     */
    getPath: function() {
      return window.location.pathname;
    },

    /**
     * Return's the current path + '#' + current hash.
     */
    getPathWithHash: function() {
      return this.getPath() + '#' + this.getHash();
    }

  };
});
