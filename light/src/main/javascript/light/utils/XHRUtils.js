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
define(['dojo/_base/declare', 'dojo/_base/xhr',
        'dojo/_base/lang', 'dojo/_base/json'],
        function(declare, xhr, lang, json) {
  /**
   * Utilities for issuing XML HTTP Requests.
   *
   * <p>This utility is a light-weight wrapper on top of dojo.xhr that
   * adds some error handling behavior and some default values for headers
   * to talk with Light's server.
   *
   * @class
   * @name light.utils.XHRUtils
   */
  return {
    /** @lends light.utils.XHRUtils */

    /**
     * Return's the current path + '#' + current hash.
     */
    send: function(method, params) {
      // TODO(waltercacau): Add CSFR protection here
      var headers = {
        'X-Light-API-Version': '1.0',
        'Accept': 'application/json',
        'Content-Type': 'application/json'
      };
      if (params.headers) {
        lang.mixin(headers, params.headers);
      }
      params.headers = headers;
      params.handleAs = params.handleAs || 'json';
      return xhr(method, params).then(null, function(err) {
        if (err.xhr &&
                err.xhr.getResponseHeader('Content-Type').split(';')[0] ==
                  'application/json') {
          err.response = json.fromJson(err.xhr.responseText || null);
        }
        throw err;
      });
    },

    /**
     * Alias for send('GET', ...)
     */
    get: function(params) {
      return this.send('GET', params);
    },


    /**
     * Alias for send('POST', ...)
     */
    post: function(params) {
      return this.send('POST', params);
    }

  };
});
