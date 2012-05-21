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
        'light/builders/SearchStateBuilder',
        'light/enums/BrowseContextsEnum'],
        function(declare, AbstractLightController, EventsEnum,
                 PubSubUtils, SearchStateBuilder, BrowseContextsEnum) {
  /**
   * @class
   * @name light.controllers.SearchBarController
   */
  return declare('light.controllers.SearchBarController',
          AbstractLightController, {

    /** @lends light.controllers.SearchBarController# */

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
    _onSearchStateChange: function(searchState, source) {
      if (source != this) {
        this._view.setQuery(searchState.query);
      }
    },

    /**
     * Handler for browse context state change events.
     */
    _onBrowseContextStateChange: function(browseContextState, source) {
      // TODO(waltercacau): Extract gdoc to some kind of constant
      if (browseContextState.context == BrowseContextsEnum.ALL ||
          (browseContextState.context == BrowseContextsEnum.IMPORT &&
           browseContextState.subcontext == 'gdoc')) {
        this._view.enable();
      } else {
        this._view.disable();
      }
    },

    /**
     * Handler for submit events in the search form.
     */
    onSubmit: function() {
      PubSubUtils.publish(EventsEnum.SEARCH_STATE_CHANGED, [
        new SearchStateBuilder().query(this._view.getQuery()).build(), this]);
    }

  });
});
