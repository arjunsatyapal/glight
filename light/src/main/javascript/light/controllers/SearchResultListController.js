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
        'light/enums/EventsEnum', 'light/utils/PubSubUtils',
        'light/RegexCommon',
        'light/utils/promises/SinglePromiseContainer',
        'light/enums/BrowseContextsEnum',
        'light/builders/SearchStateBuilder'],
        function(declare, AbstractLightController, EventsEnum,
                 PubSubUtils, RegexCommon, SinglePromiseContainer,
                 BrowseContextsEnum, SearchStateBuilder) {

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
      PubSubUtils.subscribe(EventsEnum.SEARCH_STATE_CHANGED, this,
              this._onSearchStateChange);
      PubSubUtils.subscribe(EventsEnum.BROWSE_CONTEXT_STATE_CHANGED, this,
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

      this._view.clear();
      this._cancelCurrentSearches();
      if (!searchState.query.match(RegexCommon.SPACES_ONLY)) {
        var self = this;
        this._searchPromiseContainer.handle(function() {
          return self._searchService.search(searchState);
        }).then(function(data) {
          self._view.show(searchState, data);
        });
        // Only in the first page we should use the appengine stuff
        if(searchState.page == 1) {
          this._searchRecentPromiseContainer.handle(function() {
            return self._searchService.searchRecent(searchState);
          }).then(function(data) {
            self._view.showRecent(data);
          });
        }
      }

    },
    
    _cancelCurrentSearches: function() {
      this._searchPromiseContainer.cancel();
      this._searchRecentPromiseContainer.cancel();
    },

    _lastBrowseContext: null,
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
        // Cleaning search query
        if(this._lastBrowseContext == BrowseContextsEnum.ALL) {
          PubSubUtils.publish(EventsEnum.SEARCH_STATE_CHANGED, [
              new SearchStateBuilder().build(), this]);
        }
        this._enabled = false;
        this._cancelCurrentSearches();
        this._view.clear();
      }
      this._lastBrowseContext = browseContextState.context;
    }
  });
});
