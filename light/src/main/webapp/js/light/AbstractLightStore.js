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
define(['dojo/_base/declare', 'dojo', 'dojox', 'dojox/data/JsonRestStore'],
        function(declare, dojo, dojox, JsonRestStore) {
  /**
   * Creates custom getRequest that accepts/adds custom headers
   *
   * <p>This part is basically a copy of dojox.rpc.Rest from Dojo 1.7.2 inner
   * function getRequest slightly modified.
   * {@link http://svn.dojotoolkit.org/src/tags/release-1.7.2/dojox/rpc/Rest.js}
   *
   * <p>Since Dojo does not have custom headers
   * implemented on its Rest and gives us no access to extend
   * this inner getRequest function, we have to completely replace it.
   *
   * @param {string} path Resource path (eg. /api/person).
   * @param {string} lightAPIVersion API version to communicate with our
   *                 REST backend.
   * @return {Function} The custom getRequest function.
   */
  function createCustomGetRequest(path, lightAPIVersion) {
    var isJson = true;
    return function getRequest(id, args) {
      if (dojo.isObject(id)) {
        id = dojo.objectToQuery(id);
        id = id ? '?' + id : '';
      }
      if (args && args.sort && !args.queryStr) {
        id += (id ? '&' : '?') + 'sort(';
        for (var i = 0; i < args.sort.length; i++) {
          var sort = args.sort[i];
          id += (i > 0 ? ',' : '') + (sort.descending ? '-' : '+') +
              encodeURIComponent(sort.attribute);
        }
        id += ')';
      }
      var request = {
        url: path + (id == null ? '' : id),
        handleAs: isJson ? 'json' : 'text',
        contentType: isJson ? 'application/json' : 'text/plain',
        sync: dojox.rpc._sync,
        headers: {
          Accept: isJson ? 'application/json,application/javascript' : '*/*',

          // LightMod: The next header was not set in the original version.
          // Light API Version
          'X-Light-API-Version': lightAPIVersion

          // TODO(waltercacau): Add X-CSRF-Token
        }
      };
      if (args && (args.start >= 0 || args.count >= 0)) {
        request.headers.Range = 'items=' + (args.start || '0') + '-' +
          (('count' in args && args.count != Infinity) ?
            (args.count + (args.start || 0) - 1) : '');
      }
      dojox.rpc._sync = false;
      return request;
    };
  }

  return declare('light.AbstractLightStore', JsonRestStore, {
    /** @lends light.AbstractLightStore# */

    /**
     * This is the base abstract class for all Light stores.
     *
     * <p>It takes care of setting the right headers for communicating
     * with the Light backend.
     *
     * <p>Make sure when you subclass this class that you call
     * the parent constructor using this.inherited(arguments),
     * otherwise the store will not be properly setup. We are
     * using manual chaining for the constructors, so the
     * superclass constructor is not called automatically.
     *
     * @extends dojox.data.JsonRestStore
     * @constructs
     */
    constructor: function(options) {
      options = options || {};

      if (!this.lightTarget)
        throw new Error('You should specify a target!');

      options.target = this.lightTarget;
      if (!options.target.match(/\/$/))
        options.target += '/';

      this.service = dojox.rpc.JsonRest.services[options.target] ||
          dojox.rpc.Rest(options.target, true, null,
                  createCustomGetRequest(options.target,
                          true, this.lightAPIVersion));

      this.inherited(arguments, [options]);
    },

    lightAPIVersion: '1.0',

    // Chain configuration should be at the end for JSDoc to work.
    '-chains-': {
      constructor: 'manual'
    }

  });
});
