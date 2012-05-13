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
        'light/enums/BrowseContextsEnum', 'light/utils/URLUtils', 'dojo',
        'light/builders/BrowseContextStateBuilder',
        'light/utils/XHRUtils'],
        function(declare, AbstractLightController, EventsEnum,
                 connect, BrowseContextsEnum, URLUtils, dojo,
                 BrowseContextStateBuilder, XHRUtils) {
  var NUMBER_OF_ITEMS_PER_PAGE = 10;

  return declare('light.controller.ImportModuleController',
          AbstractLightController, {
    /** @lends light.controller.ImportModuleController# */

    /**
     * @extends light.controllers.AbstractLightController
     * @constructs
     */
    constructor: function() {
      this._gdocPageLinks = [
        '/rest/thirdparty/google/gdoc/list?' + dojo.objectToQuery({
          'max-results': NUMBER_OF_ITEMS_PER_PAGE
        })
      ];
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
      if (browseContextState.context == BrowseContextsEnum.IMPORT) {
        if (browseContextState.subcontext == 'gdoc') {
          this._view.showGdocListForm('/rest/thirdparty/google/gdoc/list');
        } else if (browseContextState.subcontext == 'me') {
          this._view.showImportedListForm('/rest/module/me');
        } else {
          this._view.showFirstForm();
        }
      } else {
        this._view.hide();
      }
    },

    /**
     * Indicates the user has selected Google Docs as the desired source of
     * modules to import.
     */
    gdocAsSource: function() {
      connect.publish(EventsEnum.BROWSE_CONTEXT_STATE_CHANGED, [
           new BrowseContextStateBuilder()
               .context(BrowseContextsEnum.IMPORT)
               .subcontext('gdoc').build(), this]);
    },

    /**
     * Indicates the user wants to see what he have imported on light.
     */
    seeImported: function() {
      connect.publish(EventsEnum.BROWSE_CONTEXT_STATE_CHANGED, [
           new BrowseContextStateBuilder()
               .context(BrowseContextsEnum.IMPORT)
               .subcontext('me').build(), this]);
    },

    /**
     * Indicates the user wants to import modules on light
     * and should see a list of sources from where he can do it.
     */
    importModules: function() {
      connect.publish(EventsEnum.BROWSE_CONTEXT_STATE_CHANGED, [
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

    /**
     *
     */
    importGdocs: function(items) {
      var list = [];
      for (var i = 0, len = items.length; i < len; i++) {
        list.push(items[i].externalId);
      }
      var self = this;
      XHRUtils.post({
        url: '/rest/thirdparty/google/gdoc/import',
        postData: JSON.stringify({'list': list})
      }).then(function() {
        self._view.alertSuccessfullyStartedGdocsImport();
      });
    }
  });
});
