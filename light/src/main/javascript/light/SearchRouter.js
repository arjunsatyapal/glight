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
define(['dojo/_base/connect', 'dojo/_base/declare', 'light/URLHashUtil',
        'dojo', 'light/enums/SearchEventsEnum'],
        function(connect, declare, hashUtil, dojo, SearchEventsEnum) {

  return declare('light.SearchRouter', null, {
    /** @lends light.SearchRouter# */

    /**
     * Responsible for syncing the URL hash with the current search state.
     *
     * @constructs
     */
    constructor: function() {

      /**
       * Last sucessful query state.
       *
       * It is initialized with the default "empty" one
       */
      this._lastSearchState = this.DEFAULT_SEARCH_STATE;
      
      /**
       * Auxiliary variable to help skip handling hash
       * changes caused by this router.
       */
      this._skipHash = null;
    },

    /**
     * Default query state (empty, no query)
     *
     * @const
     */
    DEFAULT_SEARCH_STATE: {
      page: 1,
      query: ''
    },

    /**
     * Wire's this object to the dojo event system so it can
     * watch for search state changes and or changes in
     * the hash (http://www.../somepage#SOMEHASH) to
     * change the search state based on it.
     */
    watch: function() {
      connect.subscribe('/dojo/hashchange', this, this._onHashChange);
      connect.subscribe(SearchEventsEnum.SEARCH_STATE_CHANGED, this,
              this._onSearchStateChange);
      this._onHashChange(hashUtil.get());
    },
    
    /**
     * Set's the hash taking care of avoiding this router
     * to handle this hashChange.
     */
    _setHash: function(hash) {
      this._skipHash = hash;
      hashUtil.set(hash);
    },
    
    /**
     * Checks if we should skip handling this hash change.
     * 
     * <p>This is particularly useful to avoid this router
     * to handle hashChanges caused by itself.
     */
    _shouldSkipThisHashChange: function(hash) {
      var result = this._skipHash == hash; 
      this._skipHash = null;
      return result;
    },
    
    /**
     * Handles searchStateChange events.
     */
    _onSearchStateChange: function(searchState, source) {
      if(source != this) {
        this._lastSearchState = searchState;
        this._setHash(this._searchStateToHash(searchState));
      }
    },

    /**
     * Callback that should be called every time the hash changes to
     * update the search state based on the new hash.
     *
     * If the hash is not well formated, it will use the last
     * valid search state or the default one.
     * @param {String} hash The current page hash.
     * @private
     */
    _onHashChange: function(hash) {
      if(this._shouldSkipThisHashChange(hash))
        return;
      var searchState = null;
      try {
        searchState = this._hashToSearchState(hash);
      } catch (e) {
        // TODO(waltercacau): Warn the user that his query was wrong
      }
      if (searchState) {
        this._lastSearchState = searchState;
        connect.publish(SearchEventsEnum.SEARCH_STATE_CHANGED, [searchState, this]);
      } else {
        this._setHash(this._searchStateToHash(this._lastSearchState));
      }
    },

    _searchStateToHash: function(data) {
      return dojo.objectToQuery(data);
    },

    _hashToSearchState: function(hash) {
      if (hash == '')
        return this.defaultSearchState;
      var data = dojo.queryToObject(hash);
      if (typeof data.page != 'string' || !data.page.match(/[0-9]+/)) {
        throw new Error('Page is not a number');
      }
      data.page = parseInt(data.page);
      if (data.page <= 0) {
        throw new Error('Page is less then 1');
      }
      if (data.query === undefined) {
        throw new Error('Query Undefined');
      }

      return data;
    }
  });
});
