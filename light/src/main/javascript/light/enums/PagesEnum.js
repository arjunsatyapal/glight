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
/**
 * Utilities for handling with the browser's URL
 *
 * @class
 * @name light.enums.PagesEnum
 */
define(['light/enums/EventsEnum',
        'light/builders/SearchStateBuilder',
        'light/builders/BrowseContextStateBuilder',
        'light/builders/RedirectStateBuilder',
        'light/utils/RouterManager'],
        function(EventsEnum, SearchStateBuilder, BrowseContextStateBuilder,
                 RedirectStateBuilder, RouterManager) {
    function toHash(statesOrHash) {
      if(typeof statesOrHash == "undefined") {
        return "";
      } else if(typeof statesOrHash == "string") {
        return statesOrHash;
      } else {
        // TODO(waltercacau): remove this use of a private method
        return RouterManager._hashForStates(statesOrHash);
      }
    }
    
    return {
    /** @lends light.enums.PagesEnum */

    REGISTER: {
      build: 'register',
      main: 'RegisterMain',
      getPath: function(statesOrHash) {
        return '/register#'+toHash(statesOrHash);
      },
      states: [{
        changeEvent: EventsEnum.REDIRECT_STATE_CHANGED,
        Builder: RedirectStateBuilder
      }]
    },
    SEARCH: {
      build: 'search',
      main: 'SearchMain',
      getPath: function(statesOrHash) {
        return '/search#'+toHash(statesOrHash);
      },
      states: [
        {
          changeEvent: EventsEnum.SEARCH_STATE_CHANGED,
          Builder: SearchStateBuilder
        }
      ]
    },
    MYDASH: {
      build: 'mydash',
      main: 'MyDashMain',
      getPath: function(statesOrHash) {
        return '/mydash#'+toHash(statesOrHash);
      },
      states: [
        {
          changeEvent: EventsEnum.BROWSE_CONTEXT_STATE_CHANGED,
          Builder: BrowseContextStateBuilder
        },
        {
          changeEvent: EventsEnum.SEARCH_STATE_CHANGED,
          Builder: SearchStateBuilder
        }
      ]
    },

    /**
     * Get's the page for a given path.
     *
     * @param {string} path Path.
     * @return {light.enums.PagesEnum|null} The enum value for
     *    the page or null if not found.
     */
    getByPath: function(path) {
      // For now relying on the pattern applied
      var pageMatch = path.match(/\/([a-zA-Z0-9-_]+)$/);
      if (pageMatch) {
        var page = this[pageMatch[1].toUpperCase()];
        if (page)
          return page;
      }
      return null;
    }
  };
});
