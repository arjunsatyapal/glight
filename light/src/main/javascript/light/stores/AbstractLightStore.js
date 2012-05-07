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
define(['light/utils/XHRUtils', 'dojo/json', 'dojo/_base/declare', 'dojo/store/util/QueryResults'],
        function(XHRUtils, JSON, declare, QueryResults) {
  return declare('light.stores.AbstractLightStore', null, {
    /** @lends light.stores.AbstractLightStore# */

    /**
     * This is the base abstract class for all Light JSONRest stores.
     *
     * <p>It takes care of setting the right headers for communicating
     * with the Light backend.
     *
     * @constructs
     */
    constructor: function(options) {
      options = options || {};

      if (!this.lightTarget)
        throw new Error('You should specify a target!');

      options.target = this.lightTarget;
      if (!options.target.match(/\/$/))
        options.target += '/';

      declare.safeMixin(this, options);
    },

    /*
     * The rest of this class was borrowed from dojo.store.JsonRest with a few
     * modifications to use {@link light.utils.XHRUtils} so we could
     * add custom headers and some error handling behavior.
     */
    // target: String
    //    The target base URL to use for all requests to the server. This string will be
    //  prepended to the id to generate the URL (relative or absolute) for requests
    //  sent to the server
    target: '',
    // idProperty: String
    //    Indicates the property to use as the identity property. The values of this
    //    property should be unique.
    idProperty: 'id',
    // sortParam: String
    //    The query parameter to used for holding sort information. If this is omitted, than
    //    the sort information is included in a functional query token to avoid colliding
    //    with the set of name/value pairs.

    get: function(id, options) {
      //  summary:
      //    Retrieves an object by its identity. This will trigger a GET request to the server using
      //    the url `this.target + id`.
      //  id: Number
      //    The identity to use to lookup the object
      //  returns: Object
      //    The object in the store that matches the given id.
      var headers = options || {};
      headers.Accept = this.accepts;
      return XHRUtils.send('GET', {
        url: this.target + id,
        handleAs: 'json',
        headers: headers
      });
    },
    // accepts: String
    //    Defines the Accept header to use on HTTP requests
    accepts: 'application/javascript, application/json',
    getIdentity: function(object) {
      // summary:
      //    Returns an object's identity
      // object: Object
      //    The object to get the identity from
      //  returns: Number
      return object[this.idProperty];
    },
    put: function(object, options) {
      // summary:
      //    Stores an object. This will trigger a PUT request to the server
      //    if the object has an id, otherwise it will trigger a POST request.
      // object: Object
      //    The object to store.
      // options: dojo.store.api.Store.PutDirectives?
      //    Additional metadata for storing the data.  Includes an "id"
      //    property if a specific id is to be used.
      //  returns: Number
      options = options || {};
      var id = ('id' in options) ? options.id : this.getIdentity(object);
      var hasId = typeof id != 'undefined';
      return XHRUtils.send(hasId && !options.incremental ? 'PUT' : 'POST', {
          url: hasId ? this.target + id : this.target,
          postData: JSON.stringify(object),
          handleAs: 'json',
          headers: {
            'Content-Type': 'application/json',
            Accept: this.accepts,
            'If-Match': options.overwrite === true ? '*' : null,
            'If-None-Match': options.overwrite === false ? '*' : null
          }
        });
    },
    add: function(object, options) {
      // summary:
      //    Adds an object. This will trigger a PUT request to the server
      //    if the object has an id, otherwise it will trigger a POST request.
      // object: Object
      //    The object to store.
      // options: dojo.store.api.Store.PutDirectives?
      //    Additional metadata for storing the data.  Includes an "id"
      //    property if a specific id is to be used.
      options = options || {};
      options.overwrite = false;
      return this.put(object, options);
    },
    remove: function(id) {
      // summary:
      //    Deletes an object by its identity. This will trigger a DELETE request to the server.
      // id: Number
      //    The identity to use to delete the object
      return XHRUtils.send('DELETE', {
        url: this.target + id
      });
    },
    query: function(query, options) {
      // summary:
      //    Queries the store for objects. This will trigger a GET request to the server, with the
      //    query added as a query string.
      // query: Object
      //    The query to use for retrieving objects from the store.
      //  options: dojo.store.api.Store.QueryOptions?
      //    The optional arguments to apply to the resultset.
      //  returns: dojo.store.api.Store.QueryResults
      //    The results of the query, extended with iterative methods.
      var headers = {Accept: this.accepts};
      options = options || {};

      if (options.start >= 0 || options.count >= 0) {
        headers.Range = 'items=' + (options.start || '0') + '-' +
          (('count' in options && options.count != Infinity) ?
            (options.count + (options.start || 0) - 1) : '');
      }
      if (query && typeof query == 'object') {
        query = xhr.objectToQuery(query);
        query = query ? '?' + query : '';
      }
      if (options && options.sort) {
        var sortParam = this.sortParam;
        query += (query ? '&' : '?') + (sortParam ? sortParam + '=' : 'sort(');
        for (var i = 0; i < options.sort.length; i++) {
          var sort = options.sort[i];
          query += (i > 0 ? ',' : '') + (sort.descending ? '-' : '+') + encodeURIComponent(sort.attribute);
        }
        if (!sortParam) {
          query += ')';
        }
      }
      var results = XHRUtils.send('GET', {
        url: this.target + (query || ''),
        handleAs: 'json',
        headers: headers
      });
      results.total = results.then(function() {
        var range = results.ioArgs.xhr.getResponseHeader('Content-Range');
        return range && (range = range.match(/\/(.*)/)) && +range[1];
      });
      return QueryResults(results);
    }

  });
});
