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
        'dojo'], function(connect, declare, hashUtil, dojo) {

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
      this._lastQuery = this.defaultQuery;

    },

    /**
     * Default query state (empty, no query)
     *
     * @const
     */
    defaultQuery: {
      page: 1,
      query: ''
    },

    /**
     * Wire's this object to the page so it can watch for
     * changes in the hash (http://www.../somepage#SOMEHASH) and
     * change the search state based on it.
     */
    watch: function() {
      connect.subscribe('/dojo/hashchange', this, this._onHashChange);
      this._onHashChange(hashUtil.get());
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
      var query = null;
      try {
        query = this._hashToQuery(hash);
      } catch (e) {
        // TODO(waltercacau): Warn the user that his query was wrong
      }
      if (query) {
        // TODO(waltercacau): Publish an event saying that the query has changed
        this.lastQuery = query;
      } else {
        hashUtil.set(this._queryToHash(this._lastQuery));
      }
    },

    _queryToHash: function(data) {
      return dojo.objectToQuery(data);
    },

    _hashToQuery: function(hash) {
      if (hash == '')
        return this.defaultQuery;
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
