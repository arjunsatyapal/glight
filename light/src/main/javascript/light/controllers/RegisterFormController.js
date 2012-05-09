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
        'light/utils/PersonUtils', 'dojo/_base/lang',
        'light/utils/URLUtils', 'dojo', 'light/enums/PagesEnum'],
        function(declare, AbstractLightController, PersonUtils, lang,
                 URLUtils, dojo, PagesEnum) {
  return declare('light.controllers.RegisterFormController',
          AbstractLightController, {

    /** @lends light.controllers.RegisterFormController# */

    /**
     * @extends light.controllers.AbstractLightController
     * @constructs
     */
    constructor: function(personStore) {
      this._personStore = personStore;
    },
    _redirectPath: PagesEnum.SEARCH.getPath(),

    setup: function() {
      this._person = PersonUtils.getCurrent();
      this._view.setData(this._person);
      var hashData = null;
      try {
        hashData = dojo.queryToObject(URLUtils.getHash());
      } catch (e) {
        // TODO(waltercacau): Maybe inform the user about invalid URL.
      }
      if (hashData && hashData.redirectPath)
        this._redirectPath = hashData.redirectPath;
    },

    /**
     * Callback for when the form is submitted.
     */
    onSubmit: function() {
      if (!this._view.validate())
        return;
      var updatedPerson = {};
      lang.mixin(updatedPerson, this._person);
      lang.mixin(updatedPerson, this._view.getData());
      this._view.disable();
      var self = this;
      this._personStore.put(updatedPerson, {id: 'me'}).then(
          function() {
            URLUtils.redirect(self._redirectPath);
          },
          function() {
            // TODO(waltercacau): Show friendly error.
            self._view.warnError();
            self._view.enable();
          }
      )
    }
  });
});
