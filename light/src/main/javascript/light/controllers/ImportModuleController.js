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
          this.getGdocListFirstPage();
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

    _gdocPage: 0,

    /**
     * Gets first page of GdocList and shows it when the data arrive.
     */
    getGdocListFirstPage: function() {
      this._gdocPage = 0;
      this._getGdocList();
    },

    /**
     * Gets next page of GdocList and shows it when the data arrive.
     */
    getGdocListNextPage: function() {
      this._gdocPage++;
      this._getGdocList();
    },

    /**
     * Gets previous page of GdocList and shows it when the data arrive.
     */
    getGdocListPreviousPage: function() {
      if (this._gdocPage > 0)
        this._gdocPage--;
      this._getGdocList();
    },

    /**
     * Utility function that loads the gdoc list for the current page
     * and shows it.
     */
    _getGdocList: function() {
      this._view.hide();
      var self = this;
      XHRUtils.get({
        url: this._gdocPageLinks[this._gdocPage]
      }).then(function(result) {
        if (result['start-index']) {
          self._gdocPageLinks[self._gdocPage + 1] =
              '/rest/thirdparty/google/gdoc/list?start-index=' +
              result['start-index'] + '&' + dojo.objectToQuery({
                'max-results': NUMBER_OF_ITEMS_PER_PAGE
              });
          self._view.showGdocListForm(result, self._gdocPage, true);
        } else {
          self._view.showGdocListForm(result, self._gdocPage, false);
        }
      }, function() {
        // TODO(waltercacau): Check if it was really because of missing auth.
        self._view.showGdocAuthRequiredForm();
      });
    },

    /**
     * Redirects the user to the OAuth2 authorizaton page to grant
     * Lights access to his Google Documents.
     */
    gdocAuthorize: function() {
      URLUtils.redirect('/oauth2/google_doc?' + dojo.objectToQuery({
        'redirectPath': URLUtils.getPathWithHash()
      }));
    }
  });
});
