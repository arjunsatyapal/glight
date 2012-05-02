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
        'light/RegexCommon',
        'light/enums/BrowseContextsEnum'],
        function(declare, AbstractLightController, EventsEnum,
                 connect, RegexCommon, BrowseContextsEnum) {

  return declare('light.controller.SearchResultListController',
          AbstractLightController, {
    /** @lends light.controller.SearchResultListController# */

    lastSearchState: null,
    enabled: true,
    
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
      connect.subscribe(EventsEnum.SEARCH_STATE_CHANGED, this,
              this._onSearchStateChange);
      connect.subscribe(EventsEnum.BROWSE_CONTEXT_STATE_CHANGED, this,
              this._onBrowseContextStateChange);
    },

    /**
     * Handler for search state change events.
     */
    _onSearchStateChange: function(searchState) {

      // Canceling last search
      this.searchService.cancelLastSearch();
      this.lastSearchState = searchState;
      
      if(!this.enabled)
        return;

      var view = this._view;
      if (searchState.query.match(RegexCommon.SPACES_ONLY)) {
        view.clear();
      } else {
        this.searchService.search(searchState)
            .then(function(data) {
              view.show(searchState, data);
            });
      }

    },
    
    /**
     * Handler for browse context state change events.
     */
    _onBrowseContextStateChange: function(browseContextState, source) {
      if(browseContextState.context == BrowseContextsEnum.ALL) {
        this.enabled = true;
        if(this.lastSearchState)
          this._onSearchStateChange(this.lastSearchState);
      } else {
        this.enabled = false;
        this.searchService.cancelLastSearch();
        this._view.clear();
      }
    }
  });
});
