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
'use strict';
define(['dojo/_base/declare', 'light/AbstractLightController'],
        function(declare, AbstractLightController) {
  return declare('light.RegisterFormController', AbstractLightController, {
    
    /** @lends light.RegisterFormController# */
    
    constructor: function(personStore) {
      this._personStore = personStore;
    },
    
    /**
     * Callback for when the form is submitted.
     */
    onSubmit: function() {
      if(!this._view.validate())
        return;
      this._view.disable();
      this._personStore.newItem(this._view.getData());
      var self = this;
      this._personStore.save({
        onComplete: function() {
          alert("Ok");
        },
        onError: function() {
          alert("Not ok");
          console.log('Error:',arguments);
          self._view.enable();
        }
      });
    }
  });
});