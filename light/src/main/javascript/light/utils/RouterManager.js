/*
a * Copyright (C) Google Inc.
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
define(['light/utils/PubSubUtils', 'dojo/_base/declare', 'dojo/_base/lang',
        'light/utils/URLUtils', 'dojo', 'light/RegexCommon',
        'light/enums/LightConstantsEnum'],
        function(PubSubUtils, declare, lang, URLUtils, dojo, RegexCommon,
                 LightConstantsEnum) {

  /**
   * Responsible for reflecting the page states in the URL.
   *
   * TODO(waltercacau): Test this class
   *
   * @class
   * @name light.utils.RouterManager
   */
  var RouterManager = {
    /** @lends light.utils.RouterManager */

    /**
     * Auxiliary variable to help skip handling hash
     * changes caused by this router.
     * @type {string}
     */
    _skipHash: null,

    /**
     * Stores the current page we are wired upon.
     * @type {light.enums.PagesEnum}
     */
    _currentPage: null,

    /**
     * Returns true if two states are equal.
     *
     * @param {Object} a A state.
     * @param {Object} b Another state.
     * @return {Boolean} true if two states are equal.
     */
    _statesAreEqual: function(a, b) {
      // TODO(waltercacau): see if this can be a problem
      // depending on the browser
      return dojo.toJson(a) == dojo.toJson(b);
    },

    _hashForStates: function(states) {
      if (!states || !states.length) {
        return '';
      }
      var hashData = {};
      for (var i = 0, len = states.length; i < len; i++) {
        lang.mixin(hashData, states[i]);
      }
      return dojo.objectToQuery(hashData);
    },

    _getCurrentStates: function(except) {
      if (!this._currentPage) {
        return [];
      }
      var states = [];
      for (var i = 0, len = this._currentPage.states.length; i < len; i++) {
        var stateDesc = this._currentPage.states[i];
        if (except != stateDesc.changeEvent) {
          states.push(this._lastRecordedStateForEvent[stateDesc.changeEvent]);
        }
      }
      return states;
    },

    /**
     * Tries to publish the event if it has changed.
     *
     * Note this will not guarantee the event to be fired, because
     * it is possible some publish validator will block it.
     */
    _tryToFireIfChanged: function(changeEvent, state) {
      if (!this._statesAreEqual(state,
              this._lastRecordedStateForEvent[changeEvent])) {
        if (PubSubUtils.publish(changeEvent, [state, this])) {
          this._lastRecordedStateForEvent[changeEvent] = state;
        }
      }
    },

    buildLocalLink: function(changeEvent, state) {
      var states = this._getCurrentStates(changeEvent /* except this one */);
      states.push(state);
      return '#' + this._hashForStates(states);
    },

    _lastRecordedStateForEvent: {},

    /**
     * Wire's this object to the dojo event system so it can
     * watch for changes in the hash (http://www.../somepage#SOMEHASH).
     *
     * After wiring, it also fire all the registered events in the current
     * page.
     * @param {light.enums.PagesEnum} currentPage Current page.
     */
    watch: function(currentPage) {
      this._currentPage = currentPage;
      PubSubUtils.subscribe('/dojo/hashchange', this, this._onHashChange);

      this._lastWorkingHashData = {};
      for (var i = 0, len = this._currentPage.states.length; i < len; i++) {
        var state = this._currentPage.states[i];

        // Observing change state events
        PubSubUtils.subscribe(state.changeEvent, this,
                lang.partial(this._onStateChange, state.changeEvent));
      }

      this._onHashChange(URLUtils.getHash());
    },

    /**
     * Set's the hash taking care of avoiding this router
     * to handle the hashChange caused by it.
     */
    _setHash: function(hash, replaceCurrentInHistory) {
      this._skipHash = hash;
      URLUtils.setHash(hash, replaceCurrentInHistory);
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

    _onStateChange: function(changeEvent, state, source) {
      if (source == this) {
        return;
      }
      this._lastRecordedStateForEvent[changeEvent] = state;
      this._triggerHashNormalization(false  /* does not replace the current one in browser history */);
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
      if (this._shouldSkipThisHashChange(hash)) {
        return;
      }

      var hashData = {};
      try {
        hashData = dojo.queryToObject(hash);
      } catch (e) {

      }

      for (var i = 0, len = this._currentPage.states.length; i < len; i++) {
        var stateDesc = this._currentPage.states[i];
        var stateBuilder = new stateDesc.Builder(hashData, true);
        var fail = false;
        try {
          stateBuilder.normalize();
        } catch (e) {
          // TODO(waltercacau): Maybe warn the user here?
          fail = true;
        }
        if (fail) {
          // If failed, we might still need to send an event in case no
          // event was fired/recorded earlier, because components on the
          // page might be expecting it to initialize.
          if (!this._lastRecordedStateForEvent[stateDesc.changeEvent]) {
            this._tryToFireIfChanged(stateDesc.changeEvent,
                    new stateDesc.Builder().build());
          }
        } else {
          this._tryToFireIfChanged(stateDesc.changeEvent, stateBuilder.build());
        }

      }

      this._triggerHashNormalization(true  /* replaces the current one in browser history */); 
    },
    
    _hashNormalizationTimeout: null,
    _shouldHashNormalizationReplaceHistory: false,
    /**
     * Triggers a deferred hash normalization/change, so the states may change
     * several times, but we will only informe the browser and update the hash when
     * they stabilize. 
     * 
     * Doing this in a deferred way gives time for the components
     * to talk and change the page states until they agree on a particular
     * set of states.
     * 
     * During this process, if this function is called with replaceHistory
     * set to true at least once, it will update the history.
     * 
     * TODO(waltercacau): Analyze this better. It may cause some
     * really odd behaviors.
     * 
     * @param replaceHistory
     */
    _triggerHashNormalization: function(replaceHistory) {
      if(this._hashNormalizationTimeout !== null) {
        clearTimeout(this._hashNormalizationTimeout);
      }
      var self = this;
      self._shouldHashNormalizationReplaceHistory = self._shouldHashNormalizationReplaceHistory || replaceHistory;
      setTimeout(function() {
        self._hashNormalizationTimeout = null;
        var shouldReplaceHistory = self._shouldHashNormalizationReplaceHistory;
        self._shouldHashNormalizationReplaceHistory = false;
        self._setHash(self._hashForStates(self._getCurrentStates()),
                shouldReplaceHistory);
      }, 0);
    },

    // Utility linking build
    buildLinkForModuleContent: function(moduleId, version) {
      if (!version) {
        version = LightConstantsEnum.LATEST_VERSION_STR;
      }
      return URLUtils.getOrigin() + '/rest/content/general/module/' +
          moduleId + '/' + version + '/';
    },


    buildLinkForModuleInCollectionContent:
      function(collectionId, version, moduleId) {
      if (!version) {
        version = LightConstantsEnum.LATEST_VERSION_STR;
      }
      return URLUtils.getOrigin() + '/rest/content/general/collection/' +
          collectionId + '/' + version + '/' + moduleId + '/';
    },

    buildLinkForCollectionContent: function(collectionId, version) {
      if (!version) {
        version = LightConstantsEnum.LATEST_VERSION_STR;
      }
      return URLUtils.getOrigin() + '/rest/content/general/collection/' +
          collectionId + '/' + version;
    }

  };

  return RouterManager;

});
