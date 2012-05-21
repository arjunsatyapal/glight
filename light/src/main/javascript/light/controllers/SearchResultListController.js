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
        'light/utils/SinglePromiseContainer',
        'light/enums/BrowseContextsEnum'],
        function(declare, AbstractLightController, EventsEnum,
                 connect, RegexCommon, SinglePromiseContainer,
                 BrowseContextsEnum) {

  return declare('light.controller.SearchResultListController',
          AbstractLightController, {
    /** @lends light.controller.SearchResultListController# */

    _lastSearchState: null,
    _enabled: true,

    /**
     * @extends light.controllers.AbstractLightController
     * @constructs
     */
    constructor: function(searchService) {
      this._searchService = searchService;
      this._searchPromiseContainer = new SinglePromiseContainer();
      this._searchRecentPromiseContainer = new SinglePromiseContainer();
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
      this._lastSearchState = searchState;

      if (!this._enabled) {
        return;
      }

      if (searchState.query.match(RegexCommon.SPACES_ONLY)) {
        this._view.clear();
        this._cancelCurrentSearches();
      } else {
        var self = this;
        this._searchPromiseContainer.handle(function() {
          return self._searchService.search(searchState);
        }).then(function(data) {
          self._view.show(searchState, data);
        });
        this._searchRecentPromiseContainer.handle(function() {
          return self._searchService.searchRecent(searchState);
        }).then(function(data) {
          self._view.showRecent(data);
        });
      }

    },
    
    _cancelCurrentSearches: function() {
      this._searchPromiseContainer.cancel();
      this._searchRecentPromiseContainer.cancel();
    },

    /**
     * Handler for browse context state change events.
     */
    _onBrowseContextStateChange: function(browseContextState, source) {
      if (browseContextState.context == BrowseContextsEnum.ALL) {
        this._enabled = true;
        if (this._lastSearchState) {
          this._onSearchStateChange(this._lastSearchState);
        }
      } else {
        this._enabled = false;
        this._cancelCurrentSearches();
        this._view.clear();
      }
    }
  });
});
