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
define(['dojo/_base/declare', 'dojo/_base/connect',
        'light/controllers/AbstractLightController',
        'light/enums/EventsEnum', 'light/utils/PubSubUtils',
        'light/enums/BrowseContextsEnum',
        'light/utils/promises/SinglePromiseContainer',
        'light/utils/promises/CompositePromise',
        'light/utils/promises/WaitPromise',
        'light/enums/LightConstantsEnum'],
        function(declare, connect, AbstractLightController, EventsEnum,
                PubSubUtils, BrowseContextsEnum, SinglePromiseContainer,
                CompositePromise, WaitPromise,
                LightConstantsEnum) {

  var PUBLISH_VALIDATOR_IDENTIFIER =
    'light.controller.CollectionEditorController';

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
      PubSubUtils.subscribe(EventsEnum.BROWSE_CONTEXT_STATE_CHANGED, this,
              this._onBrowseContextStateChange);
    },

    /**
     * Handler for browse context state change events.
     */
    _onBrowseContextStateChange: function(browseContextState, source) {
      // TODO(waltercacau): Create better utilities for this and rewrite this code
      this._view.hide();
      this.everythingIsSaved();
      this._collectionId = null;
      if (browseContextState.context == BrowseContextsEnum.COLLECTION) {
        this._collectionId = browseContextState.subcontext;
        var self = this;
        this._singlePromiseContainer.handle(function() {
          return CompositePromise.of(
                  self._collectionService.get(
                          browseContextState.subcontext
                  ),
                  self._collectionService.getVersion(
                          browseContextState.subcontext,
                          LightConstantsEnum.LATEST_VERSION_STR
                  )
          );
        }).then(function(results) {
          if (results[0] instanceof Error || results[1] instanceof Error) {
            self._view.showCouldNotLoad();
          } else {
            self._collection = results[0];
            self._collectionVersion = results[1];
            var isPartiallyPublished =
                self._collectionVersion.collectionState == 'PARTIALLY_PUBLISHED';
            self._view.showEditor(self._collectionId,
                    self._collectionVersion.collectionTree, isPartiallyPublished);
            if(isPartiallyPublished) {
              self._poolForPublishedVersion();
            }
          }
        });
      } else {
        this._singlePromiseContainer.cancel();
      }
    },
    _poolForPublishedVersion: function() {
      var self = this;
      self._singlePromiseContainer.handle(function() {
        return WaitPromise.of(3000);
      }).then(function() {
        self._singlePromiseContainer.handle(function() {
          return self._collectionService.getVersion(
                  self._collectionId,
                  LightConstantsEnum.LATEST_VERSION_STR,
                  false /* ioPublish = false, so we don't trigger the ActivityIndicator */
          );
        }).then(function(result) {
          if(result.collectionState == 'PUBLISHED') {
            self._collectionVersion = result;
            self._view.showEditor(self._collectionId,
                    self._collectionVersion.collectionTree, false);
          } else {
            self._poolForPublishedVersion();
          }
        }, function(err) {
          if(!err.lightCancelled) {
            self._poolForPublishedVersion();
          }
        });
      });
    },
    saveCollection: function(collectionTree) {
      var self = this;
      this._singlePromiseContainer.handle(function() {
        return self._collectionService.updateLatest(
                self._collectionId, collectionTree);
      }).then(function() {
        self._view.savedSuccessfully();
      },function(error) {
        // Avoiding cancelling errors if the user presses more then once
        if (!error.lightCancelled) {
          self._view.failedToSave();
        }
      });
    },
    _lastBeforeUnloadHandle: null,
    everythingIsSaved: function() {
      PubSubUtils.removePublishValidator(
              EventsEnum.BROWSE_CONTEXT_STATE_CHANGED,
              PUBLISH_VALIDATOR_IDENTIFIER);
      if (this._lastBeforeUnloadHandle !== null) {
        connect.disconnect(this._lastBeforeUnloadHandle);
        this._lastBeforeUnloadHandle = null;
      }
    },
    thereAreUnsavedChanges: function() {
      // Cleaning up before doing anything, because there is no guarantee
      // from the view that this method will be called only once
      // before the next cleanup
      this.everythingIsSaved();
      // TODO(waltercacau): move to somewhere else,
      // other components might need this too.
      var self = this;
      this._lastBeforeUnloadHandle = connect.connect(window, 'beforeunload',
              this, function() {
        return self._view.messages.thereAreUnsavedChangesBeforeUnload;
      });
      PubSubUtils.addPublishValidator(EventsEnum.BROWSE_CONTEXT_STATE_CHANGED,
              PUBLISH_VALIDATOR_IDENTIFIER, function() {
        return confirm(
                self._view.messages.thereAreUnsavedChangesConfirmLeaving);
      });
    }
  });

});
