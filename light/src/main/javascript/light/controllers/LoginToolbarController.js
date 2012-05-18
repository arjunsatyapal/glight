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
define(['dojo/_base/declare', 'light/controllers/AbstractLightController',
        'light/utils/URLUtils', 'light/utils/PersonUtils',
        'light/enums/PagesEnum', 'dojo'],
        function(declare, AbstractLightController, URLUtils, PersonUtils,
                 PagesEnum, dojo) {
  /**
   * Controller for the LoginToolbar
   *
   * @class
   * @name light.controllers.LoginToolbarController
   */
  return declare('light.controllers.LoginToolbarController',
          AbstractLightController, {

    /** @lends light.controllers.LoginToolbarController# */

    /**
     * Sets up the toolbar on the page
     */
    setup: function() {
      var person = PersonUtils.getCurrent();
      if (person) {
        this._view.showLoggedForm(person.firstName + ' ' + person.lastName);
      } else {
        this._view.showUnloggedForm(['google']);
      }
    },

    /**
     * Redirects to the login servlet for the given provider.
     * TODO(waltercacau): update test
     */
    login: function(provider) {
      var redirectPath = URLUtils.getPathWithHash();
      if (PagesEnum.getCurrentPage() == PagesEnum.SEARCH) {
        redirectPath = PagesEnum.MYDASH.getPathWithHash(URLUtils.getHash());
      }
      URLUtils.redirect('/login/' + provider + '?' + dojo.objectToQuery({
        'redirectPath': redirectPath
      }));
    },

    /**
     * Redirects to the logout servlet.
     */
    logout: function() {
      URLUtils.redirect('/logout');
    }


  });
});
