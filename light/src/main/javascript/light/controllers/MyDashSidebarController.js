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
        'light/enums/EventsEnum', 'dojo/_base/connect', 'dojo/_base/array',
        'light/builders/BrowseContextStateBuilder',
        'light/utils/XHRUtils'],
        function(declare, AbstractLightController, EventsEnum,
                 connect, array, BrowseContextStateBuilder, XHRUtils) {
  var MAX_NUMBER_OF_COLLECTIONS_PER_BULK = 50;

  /**
   * @class
   * @name light.controllers.MyDashSidebarController
   */
  return declare('light.controllers.MyDashSidebarController',
          AbstractLightController, {

    /** @lends light.controllers.MyDashSidebarController# */
    constructor: function(collectionService, importService) {
      this._collectionService = collectionService;
      this._importService = importService;
    },

    /**
     * Wire's this object to the dojo event system so it can
     * watch for state changes.
     */
    watch: function() {
      connect.subscribe(EventsEnum.BROWSE_CONTEXT_STATE_CHANGED, this,
              this._onBrowseContextStateChange);
      // TODO(waltercacau): rename all watch methods to setup
      // so it can contain other kinds of logic like this
      this._fetchCollectionsList();
    },

    /**
     * Handler for browse context state change events.
     */
    _onBrowseContextStateChange: function(browseContextState, source) {
      if (source != this) {
        this._view.setContext(browseContextState);
      }
    },

    /**
     * Fire's the proper event to change the context.
     */
    changeContextTo: function(browseContextState) {
      connect.publish(EventsEnum.BROWSE_CONTEXT_STATE_CHANGED,
          [browseContextState, this]);
    },

    _lastXhr: null,
    _fetchCollectionsList: function() {
      // TODO(waltercacau): Extract this to a pagination utility and move the
      // XHRUtils calls to the services layer.
      var self = this;
      XHRUtils.get({
        url: '/rest/collection/me?maxResults=' +
             MAX_NUMBER_OF_COLLECTIONS_PER_BULK
      }).then(function(result) {
        self._view.setCollectionList(result.list);
        if (result['handlerUri'] && result['startIndex']) {
          self._fetchRemainingCollectionList(result);
        }
      });
    },
    _fetchRemainingCollectionList: function(prevResult) {
      var self = this;
      XHRUtils.get({
        url: prevResult['handlerUri'] + '?startIndex=' + prevResult['startIndex'] +
             '&maxResults=' + MAX_NUMBER_OF_COLLECTIONS_PER_BULK
      }).then(function(result) {
        array.forEach(result.list, function(item) {
          self._view.addToCollectionList(item);
        });
        if (result['handlerUri'] && result['startIndex']) {
          self._fetchRemainingCollectionList(result);
        }
      });

    },

    createCollection: function(collectionTitle) {
      var self = this;
      this._collectionService.create({
        title: collectionTitle
      }).then(function(collection) {
        self._view.addToCollectionList(collection);
      });
    },
    addToCollection: function(list, collectionId, collectionTitle) {
      // TODO(waltercacau): Remove the collectionTitle requirement
      this._importService.addToCollection(list, collectionId, collectionTitle);
    }

  });
});
