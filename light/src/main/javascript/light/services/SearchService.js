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
define(['dojo/_base/declare', 'dojo', 'light/utils/XHRUtils', 'dojo/_base/lang',
        'light/utils/LanguageUtils', 'light/builders/SearchRequestBuilder'],
        function(declare, dojo, XHRUtils, lang, LanguageUtils,
                 SearchRequestBuilder) {
  return declare('light.services.SearchService', null, {

    /**
     * Issues a search request based on a given SearchState
     *
     * @param {Object}
     *          state A search state object. See
     *          {@link light.schemas.SearchStateSchema}.
     * @return {Object} A promise object for when the server answers.
     */
    search: function(state) {
      // Adding the clientLanguageCode to the request
      var request = new SearchRequestBuilder(state, true /* may have extra keys */)
          .clientLanguageCode(LanguageUtils.currentLocale)
          .build();

      return XHRUtils.get({
        url: '/api/search?' + dojo.objectToQuery(request)
      });
    },
    
    searchRecent: function(state) {
      return XHRUtils.get({
        url: '/rest/module/search?' + dojo.objectToQuery({
          filter: state.query
        })
      });
    }
  });
});
