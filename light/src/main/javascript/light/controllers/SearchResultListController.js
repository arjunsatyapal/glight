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
        'light/enums/SearchEventsEnum', 'dojo/_base/connect',
        'light/RegexCommon'],
        function(declare, AbstractLightController, SearchEventsEnum,
                 connect, RegexCommon) {

  return declare('light.controller.SearchResultListController',
          AbstractLightController, {
    /** @lends light.controller.SearchResultListController# */

    /**
     * @extends light.controllers.AbstractLightController
     * @constructs
     */
    constructor: function(searchService) {
      this.searchService = searchService;
    },

    /**
     * Wire's this object to the dojo event system so it can
     * watch for search state changes.
     */
    watch: function() {
      connect.subscribe(SearchEventsEnum.SEARCH_STATE_CHANGED, this,
              this._onSearchStateChange);
    },

    /**
     * Handler for search state change events.
     */
    _onSearchStateChange: function(searchState) {

      // Canceling last search
      this.searchService.cancelLastSearch();

      var view = this._view;
      if (searchState.query.match(RegexCommon.SPACES_ONLY)) {
        view.clear();
      } else {
        this.searchService.search(searchState)
            .then(function(data) {
              view.show(searchState, data);
            });
      }

    }
  });
});
