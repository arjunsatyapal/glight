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
define(['exports',
        'dojo/_base/lang',
        'light/enums/EventsEnum',
        'light/builders/SearchStateBuilder',
        'light/builders/BrowseContextStateBuilder',
        'light/builders/RedirectStateBuilder',
        'light/utils/RouterManager',
        'light/utils/PersonUtils',
        'light/utils/URLUtils'],
        function(exports, lang, EventsEnum, SearchStateBuilder,
                 BrowseContextStateBuilder, RedirectStateBuilder,
                 RouterManager, PersonUtils, URLUtils) {
    function toHash(statesOrHash) {
      if (typeof statesOrHash == 'undefined') {
        return '';
      } else if (typeof statesOrHash == 'string') {
        return statesOrHash;
      } else {
        // TODO(waltercacau): remove this use of a private method
        return RouterManager._hashForStates(statesOrHash);
      }
    }
    
    var REGISTER = {
      build: 'register',
      main: 'RegisterMain',
      getPathWithHash: function(statesOrHash) {
        return '/register#' + toHash(statesOrHash);
      },
      pathRegex: /^\/register(\/|$)/,
      states: [{
        changeEvent: EventsEnum.REDIRECT_STATE_CHANGED,
        Builder: RedirectStateBuilder
      }]
    };
    var SEARCH = {
      build: 'search',
      main: 'SearchMain',
      getPathWithHash: function(statesOrHash) {
        return '/#' + toHash(statesOrHash);
      },
      pathRegex: /^\/(\/|$)/,
      onlyForUnloggedUsers: true,
      states: [
        {
          changeEvent: EventsEnum.SEARCH_STATE_CHANGED,
          Builder: SearchStateBuilder
        }
      ]
    };
    var MYDASH = {
      build: 'mydash',
      main: 'MyDashMain',
      getPathWithHash: function(statesOrHash) {
        return '/#' + toHash(statesOrHash);
      },
      pathRegex: /^\/(\/|$)/,
      onlyForLoggedUsers: true,
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
    };

    var CONTENT_VIEWER = {
      build: 'content_viewer',
      main: 'ContentViewerMain',
      getPathWithHash: function(statesOrHash) {
        return '/rest/content/general/collection/SAMPLE#' + toHash(statesOrHash);
      },
      pathRegex: /^\/rest\/content\/general\/(collection|module)(\/|$)/,
      states: []
    };

    return lang.mixin(exports, {
      /** @lends light.enums.PagesEnum */
  
      SEARCH: SEARCH,
      MYDASH: MYDASH,
      REGISTER: REGISTER,
      COLLECTION_VIEWER: CONTENT_VIEWER,
      values: [REGISTER, SEARCH, MYDASH, CONTENT_VIEWER],
  
      getCurrentPage: function() {
        var path = URLUtils.getPath();
        var loggedIn = PersonUtils.isLogged();
        for(var i=0, len=this.values.length; i < len; i++) {
          var value = this.values[i];
          if(!(value.onlyForUnloggedUsers && loggedIn) &&
             !(value.onlyForLoggedUsers && !loggedIn) &&
             value.pathRegex.test(path)) {
            return value;
          }
        }
        throw new Error('Could not figure out the current page.');
      }
  
    });
});
