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
        'light/enums/EventsEnum', 'dojo/_base/connect',
        'light/enums/BrowseContextsEnum',
        'light/utils/SinglePromiseContainer',
        'light/enums/LightConstantsEnum',
        'dojo/DeferredList'],
        function(declare, AbstractLightController, EventsEnum,
                connect, BrowseContextsEnum, SinglePromiseContainer,
                LightConstantsEnum, DeferredList) {
  return declare('light.controller.CollectionEditorController',
          AbstractLightController, {

    _collectionId: null,
    _collection: null,
    _collectionVersion: null,

    constructor: function(collectionService) {
      this._collectionService = collectionService;
      this._singlePromiseContainer = new SinglePromiseContainer();

    },

    /**
     * Wire's this object to the dojo event system so it can
     * watch for browser context state changes.
     */
    watch: function() {
      connect.subscribe(EventsEnum.BROWSE_CONTEXT_STATE_CHANGED, this,
              this._onBrowseContextStateChange);
    },

    /**
     * Handler for browse context state change events.
     */
    _onBrowseContextStateChange: function(browseContextState, source) {
      this._view.hide();
      this._collectionId = null;
      if (browseContextState.context == BrowseContextsEnum.COLLECTION) {
        this._collectionId = browseContextState.subcontext;
        var self = this;
        this._singlePromiseContainer.handle(function() {
          /* TODO(waltercacau): Maybe create a better utility then
             DeferredList to deal with parallel promises. Currently
             DeferredList does not provide an automatic canceler to
             cancel the given promises.
             Also it does not propagate the errors to the errback.*/
            var getCollection = self._collectionService.get(
                    browseContextState.subcontext
            );
            var getCollectionVersion = self._collectionService.getVersion(
                    browseContextState.subcontext,
                    LightConstantsEnum.LATEST_VERSION_STR
            );
            var dfd = new DeferredList([getCollection, getCollectionVersion]);
            dfd.cancel = function() {
              // This is quite a hack. If you simple pass a canceler to
              // the deferred and then cancel the individual promises
              // you will get nasty messages in the console about
              // the deferred being already resolved. 
              getCollection.cancel();
              getCollectionVersion.cancel();
            };
            return dfd;
        }).then(function(results) {
          if (!results[0][0] || !results[1][0]) {
            self._view.showCouldNotLoad();
          } else {
            self._collection = results[0][1];
            self._collectionVersion = results[1][1];
            self._view.showEditor(self._collectionVersion.collectionTree);
          }
        });
      }
    }
  });
});
