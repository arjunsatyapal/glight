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
        'light/enums/BrowseContextsEnum', 'light/utils/URLUtils', 'dojo',
        'light/builders/BrowseContextStateBuilder',
        'light/utils/XHRUtils',
        'light/utils/promises/SinglePromiseContainer',
        'light/builders/SearchStateBuilder'],
        function(declare, AbstractLightController, EventsEnum,
                 PubSubUtils, BrowseContextsEnum, URLUtils, dojo,
                 BrowseContextStateBuilder, XHRUtils, SinglePromiseContainer,
                 SearchStateBuilder) {
  var NUMBER_OF_ITEMS_PER_PAGE = 10;
  var FIRST_FORM = 'first';
  var GDOC_FORM = 'gdoc';

  return declare('light.controller.ImportModuleController',
          AbstractLightController, {
    /** @lends light.controller.ImportModuleController# */

    /**
     * @extends light.controllers.AbstractLightController
     * @constructs
     */
    constructor: function(collectionService, importService) {
      this._importService = importService;
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
      PubSubUtils.subscribe(EventsEnum.SEARCH_STATE_CHANGED, this,
              this._onSearchStateChange);
    },

    _currentForm: null,
    /**
     * Handler for browse context state change events.
     */
    _onBrowseContextStateChange: function(browseContextState, source) {
      var lastForm = this._currentForm;
      this._singlePromiseContainer.cancel();
      if (browseContextState.context == BrowseContextsEnum.IMPORT) {
        if (browseContextState.subcontext == GDOC_FORM) {
          this._showGdocForm();
          this._currentForm = GDOC_FORM;
        } else {
          this._view.hide();
          this._currentForm = FIRST_FORM;
          var self = this;
          this._singlePromiseContainer.handle(function() {
            return self._collectionService.getMyCollections();
          }).then(function(collections) {
            self._view.showFirstForm(collections);
          });
        }
      } else {
        this._view.hide();
        this._currentForm = null;
      }
      // Cleaning search query
      if (lastForm == GDOC_FORM && this._currentForm != GDOC_FORM) {
        PubSubUtils.publish(EventsEnum.SEARCH_STATE_CHANGED, [
            new SearchStateBuilder().build(), this]);
      }
    },

    /**
     * Handler for search state change events.
     */
    _lastQuery: '',
    _onSearchStateChange: function(searchState, source) {
      if (source != this) {
        this._lastQuery = searchState.query;
        if (this._currentForm == GDOC_FORM) {
          this._showGdocForm();
        }
      }
    },
    _showGdocForm: function() {
      var link = '/rest/thirdparty/google/gdoc/list';
      if (this._lastQuery !== '') {
        link += '?' + dojo.objectToQuery({
          'filter': this._lastQuery
        });
      }
      this._view.showGdocListForm(link);
    },

    /**
     * Indicates the user has selected Google Docs as the desired source of
     * modules to import.
     */
    gdocAsSource: function() {
      PubSubUtils.publish(EventsEnum.BROWSE_CONTEXT_STATE_CHANGED, [
           new BrowseContextStateBuilder()
               .context(BrowseContextsEnum.IMPORT)
               .subcontext(GDOC_FORM).build(), this]);
    },

    /**
     * Indicates the user wants to see what he have imported on light.
     */
    seeImported: function() {
      PubSubUtils.publish(EventsEnum.BROWSE_CONTEXT_STATE_CHANGED, [
           new BrowseContextStateBuilder()
               .context(BrowseContextsEnum.IMPORTED_BY_ME)
               .build(), this]);
    },

    /**
     * Indicates the user wants to import modules on light
     * and should see a list of sources from where he can do it.
     */
    importModules: function() {
      PubSubUtils.publish(EventsEnum.BROWSE_CONTEXT_STATE_CHANGED, [
           new BrowseContextStateBuilder()
               .context(BrowseContextsEnum.IMPORT).build(), this]);
    },

    /**
     * Redirects the user to the OAuth2 authorizaton page to grant
     * Lights access to his Google Documents.
     */
    gdocAuthorize: function() {
      URLUtils.redirect('/oauth2/google_doc?' + dojo.objectToQuery({
        'redirectPath': URLUtils.getPathWithHash()
      }));
    },

    importGdocs: function(items) {
      var list = [];
      for (var i = 0, len = items.length; i < len; i++) {
        list.push({ 'externalId' : items[i].externalId });
      }
      var self = this;
      this._importService.import_(list).then(function() {
        self._view.alertSuccessfullyStartedGdocsImport();
      });
    },
    importURL: function(url, collectionId, collectionTitle) {
      var self = this;
      this._importService.addToCollection([{ 'externalId' : url }], collectionId, collectionTitle).then(function() {
        self._view.alertSuccessfullyStartedGdocsImport();
      });
    }
  });
});
