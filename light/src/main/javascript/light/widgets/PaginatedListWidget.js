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
define(['dojo/_base/declare',
        'dojo/_base/lang',
        'dojo',
        'dojo/string',
        'dojo/dom-construct',
        'light/widgets/ListWidget',
        'light/views/TemplatedLightView',
        'dijit/_WidgetsInTemplateMixin',
        'dojo/i18n!light/nls/PaginatedListWidgetMessages',
        'dojo/text!light/templates/PaginatedListWidgetTemplate.html',
        'light/utils/LanguageUtils',
        'light/utils/DOMUtils',
        'light/utils/XHRUtils',
        'dijit/form/Button'],
        function(declare, lang, dojo, string, domConstruct, ListWidget, 
                 TemplatedLightView,
                 _WidgetsInTemplateMixin,
                 PaginatedListWidgetMessages,
                 PaginatedListWidgetTemplate,
                 LanguageUtils, DOMUtils, XHRUtils) {
  // TODO(waltercacau): make this a parameter
  var NUMBER_OF_ITEMS_PER_PAGE = 10;

  return declare('light.widgets.PaginatedListWidget', [TemplatedLightView,
       _WidgetsInTemplateMixin], {
    /** @lends light.widgets.PaginatedListWidget */

    DEFAULT_LIST_OPTIONS: {
      accept: [],
      selfAccept: false,
      copyOnly: true,
      selfCopy: false
    },
    DEFAULT_MESSAGES: PaginatedListWidgetMessages,

    templateString: PaginatedListWidgetTemplate,

    /**
     * Wrapper of ListWidget that knows how to talk to a Rest Service that
     * uses the PageDto.
     *
     * Because we are using PageDto and we can't simply jump to a particular
     * page, this widget also does not reflect the current page in the URL,
     * because it would not make that much sense if we can't simply jump to
     * a particular page.
     */
    constructor: function(options) {
      options = options || {};

      this._onError = options.onError || function() {};
      if (!lang.isFunction(this._onError)) {
        throw new Error(
                'You should define the onError callback' +
                ' as a function');
      }

      options.listOptions = options.listOptions || {};
      if (!lang.isFunction(options.listOptions.rawCreator)) {
        throw new Error(
                'You should define the listOptions.rawCreator' +
                ' as a function');
      }
      this._listOptions = lang.clone(this.DEFAULT_LIST_OPTIONS);
      lang.mixin(this._listOptions, options.listOptions);

      // TODO(waltercacau): this.messages should be renamed to
      // this._messages. It makes sense to be private. We should change
      // in the whole Javascript Light Code all such kind of
      // messages variables to _messages.
      this.messages = lang.clone(this.DEFAULT_MESSAGES);
      lang.mixin(this.messages, options.messages || {});

      this._pageLinks = [];
    },

    forInSelectedItems: function(func) {
      if (this._listWidget !== null) {
        this._listWidget.forInSelectedItems(func);
      }
    },

    _currentPage: 0,

    /**
     * Get's the first page and shows it.
     * @param {string} link The URL/Path to the get the first page.
     */
    getFirstPage: function(link) {
      this._currentPage = 0;
      this._pageLinks[0] = link;
      this._loadCurrentPage();
    },
    _getNextPage: function() {
      this._currentPage++;
      this._loadCurrentPage();
    },
    _getPreviousPage: function() {
      if (this._currentPage > 0) {
        this._currentPage--;
      }
      this._loadCurrentPage();
    },

    _listWidget: null,
    _destroyOldListWidget: function() {
      if (this._listWidget !== null) {
        var node = this._listWidget.node;
        this._listWidget.destroy();
        domConstruct.empty(node);
        this._listWidget = null;
      }
    },

    destroy: function() {
      this._destroyOldListWidget();
      this.inherited(arguments);
    },

    _loadCurrentPage: function() {
      this._destroyOldListWidget();
      DOMUtils.hide(this._resultsForm);
      DOMUtils.hide(this._noResultsForm);
      var self = this;
      // TODO(waltercacau): Encapsulate this kind of XHR in some utility
      // so everything that needs to talk to a service that returns a PageDto
      // can simply use it.
      XHRUtils.get({
        url: this._pageLinks[this._currentPage]
      }).then(function(result) {
        if (result['handlerUri'] && result['startIndex']) {
          self._pageLinks[self._currentPage + 1] =
              result['handlerUri'] + '?startIndex=' +
              result['startIndex'] + '&' + dojo.objectToQuery({
                'maxResults': NUMBER_OF_ITEMS_PER_PAGE
              });
          self._showCurrentPage(result, true);
        } else {
          self._showCurrentPage(result, false);
        }
      }, this._onError);
    },

    _showCurrentPage: function(result, hasNextPage) {
      if (lang.isArray(result.list) && result.list.length > 0) {
        DOMUtils.show(this._resultsForm);
        DOMUtils.hide(this._noResultsForm);

        this._listWidget = new ListWidget(this._listWidgetDiv,
                this._listOptions);

        this._listWidget.insertNodes(false /* addSelected */, result.list);
        this._listWidget.focusFirstItem();


        // Prev page button
        if (this._currentPage > 0) {
          DOMUtils.show(this._getPreviousPageButton.domNode);
        } else {
          DOMUtils.hide(this._getPreviousPageButton.domNode);
        }

        // Current page number
        DOMUtils.setText(this._pageInfoSpan,
            string.substitute(this.messages.pageInfo, {
              page: LanguageUtils.formatNumber(this._currentPage + 1)
            }));

        // Next page button
        if (hasNextPage) {
          DOMUtils.show(this._getNextPageButton.domNode);
        } else {
          DOMUtils.hide(this._getNextPageButton.domNode);
        }

      } else {
        DOMUtils.show(this._noResultsForm);
        DOMUtils.hide(this._resultsForm);
      }

    }

  });
});
