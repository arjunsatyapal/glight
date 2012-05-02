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
        'light/enums/EventsEnum', 'dojo/_base/connect',
        'light/enums/BrowseContextsEnum'],
        function(declare, AbstractLightController, EventsEnum,
                 connect, BrowseContextsEnum) {

  return declare('light.controller.ImportModuleController',
          AbstractLightController, {
    /** @lends light.controller.ImportModuleController# */

    /**
     * @extends light.controllers.AbstractLightController
     * @constructs
     */
    constructor: function() {
    },

    /**
     * Wire's this object to the dojo event system so it can
     * watch for browser context state changes.
     */
    watch: function() {
      connect.subscribe(EventsEnum.BROWSE_CONTEXT_STATE_CHANGED, this,
              this._onBrowseContextStateChange);
    },

    /**
     * Handler for browse context state change events.
     */
    _onBrowseContextStateChange: function(browseContextState, source) {
      if(browseContextState.context == BrowseContextsEnum.IMPORT) {
        this.view.showFirstForm();
      } else {
        this.view.hide();
      }
    }
  });
});
