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
define(['dojo/_base/declare', 'light/utils/URLUtils', 'light/enums/PagesEnum',
    'dojo'], function(declare, URLUtils, PagesEnum, dojo) {
  /**
   * Utilities for handling with the logged person. TODO(waltercacau): add test
   * for this class
   * 
   * @class
   * @name light.utils.PersonUtils
   */
  return {
    /** @lends light.utils.PersonUtils */

    /**
     * Return's the current logged person or null if there is no person logged
     * in.
     * 
     * @returns Current hash
     */
    getCurrent: function() {
      return lightPreload.person ? lightPreload.person : null;
    },

    /**
     * Check's whetever the person has accepted the term's and conditions. If he
     * didn't, he will be redirected to the register page if he isn't already
     * there.
     * <p>
     * This function is supposed to be called in the beginning of
     * {@link light.main.LoaderMain}.
     * 
     * @param {light.enums.PagesEnum}
     *          currentPage The page we are currently in.
     * @returns
     */
    tosCheck: function(currentPage) {
      if (currentPage == PagesEnum.REGISTER) {
        return;
      }

      var person = this.getCurrent();
      if (person && !person.acceptedTos) {
        URLUtils.replace(PagesEnum.REGISTER.path + "#" + dojo.objectToQuery({
          "redirectPath": URLUtils.getPathWithHash()
        }));
      }
    },

    /**
     * Return's true if there is a person logged in.
     * 
     * TODO(waltercacau): add test for this.
     * @returns
     */
    isLogged: function() {
      return this.getCurrent() ? true : false;
    }

  };
});