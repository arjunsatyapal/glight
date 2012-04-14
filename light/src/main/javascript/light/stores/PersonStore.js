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
define(['dojo/_base/declare', 'light/stores/AbstractLightStore'],
        function(declare, AbstractLightStore) {

  return declare('light.stores.PersonStore', AbstractLightStore, {
    /** @lends light.stores.PersonStore# */

    lightTarget: '/api/person',

    /**
     * Store object for Person resource.
     * 
     * TODO(waltercacau): add test for this class
     * 
     * @extends light.stores.AbstractLightStore
     * @constructs
     */
    constructor: function(options) {
      this.inherited(arguments);
    }

  });
});
