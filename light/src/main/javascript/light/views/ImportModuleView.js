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
define(['dojo/_base/declare', 'light/views/TemplatedLightView',
        'dojo/text!light/templates/ImportModuleTemplate.html',
        'dojo/text!light/templates/GDocListItemTemplate.html',
        'dojo/i18n!light/nls/ImportModuleMessages',
        'light/utils/DOMUtils',
        'light/utils/URLUtils',
        'light/utils/LanguageUtils',
        'dojo/string',
        'dojo/dom-construct',
        'dijit/_WidgetsInTemplateMixin',
        'light/widgets/PaginatedListWidget',
        'light/utils/TemplateUtils',
        'light/utils/DialogUtils',
        'dijit/form/Button'],
        function(declare, TemplatedLightView, template, GDocListItemTemplate,
                messages, DOMUtils, URLUtils, LanguageUtils, string,
                domConstruct, _WidgetsInTemplateMixin, PaginatedListWidget, 
                TemplateUtils, DialogUtils, Button) {

  /**
   * @class
   * @name light.views.LoginToolbarView
   */
  return declare('light.views.LoginToolbarView', [TemplatedLightView,
      _WidgetsInTemplateMixin], {
    /** @lends light.views.LoginToolbarView# */
    templateString: template,
    messages: messages,
    _earlyTemplatedStartup: true,

    postCreate: function() {
      var self = this;
      this._gdocPaginatedListWidget = new PaginatedListWidget({
        listOptions: {
          type: 'gdocDndSource',
          rawCreator: function(item) {
            var node = TemplateUtils.toDom(GDocListItemTemplate, item);
            return {
              node: node,
              data: item,
              focusNode: dojo.query('a', node)[0],
              type: ['gdoc']
            };
          }
        },
        onError: function() {
          // TODO(waltercacau): Check if it was really because of missing auth.
          self.showGdocAuthRequiredForm();
        },
      }, this._gdocPaginatedListWidgetDiv);
      
      this._importedPaginatedListWidget = new PaginatedListWidget({
        listOptions: {
          //type: 'gdocDndSource',
          rawCreator: function(item) {
            var node = TemplateUtils.toDom('<div class="gdocListItem"><a href="#">${title}</a></div>', item);
            return {
              node: node,
              data: item,
              focusNode: dojo.query('a', node)[0],
              type: ['gdoc']
            };
          }
        },
        onError: function() {
          // TODO(waltercacau): Check if it was really because of missing auth.
          self.showGdocAuthRequiredForm();
        },
      }, this._importedPaginatedListWidgetDiv);
      
      
      this.inherited(arguments);
      this.hide();
    },

    /**
     * List of possible form's of this view.
     *
     * <p>Each item of this list has a correspondent div element
     * in the template.
     */
    _forms: ['first', 'gdocList', 'gdocAuthRequired', 'importedList'],

    /**
     * Hides all form's of this view.
     */
    hide: function() {
      for (var i = 0, len = this._forms.length; i < len; i++) {
        DOMUtils.hide(this['_' + this._forms[i] + 'FormDiv']);
      }
    },

    /**
     * Utility function that hides all form's of this view except
     * the one specified.
     */
    _showForm: function(visibleForm) {
      for (var i = 0, len = this._forms.length; i < len; i++) {
        var form = this._forms[i];
        var formDiv = this['_' + form + 'FormDiv'];
        if (form == visibleForm) {
          DOMUtils.show(formDiv);
        } else {
          DOMUtils.hide(formDiv);
        }
      }
    },

    /**
     * Show first form where the user should select from which source
     * he wants to import modules into Light.
     */
    showFirstForm: function() {
      this._showForm('first');
    },

    /**
     * Shows a list of Gdocs items.
     * @param {Object} data Data returned from the server containing the list.
     * @param {number} page Current page index 0-based.
     * @param {boolean} hasNextPage
     */
    showGdocListForm: function(link) {
      this._showForm('gdocList');
      this._gdocPaginatedListWidget.getFirstPage(link);
    },
    
    showImportedListForm: function(link) {
      this._showForm('importedList');
      this._importedPaginatedListWidget.getFirstPage(link);
    },
    

    /**
     * Shows explanation to the user that he needs to grant authorization
     * so we can import his Google Documents.
     */
    showGdocAuthRequiredForm: function() {
      this._showForm('gdocAuthRequired');
    },

    alertSuccessfullyStartedGdocsImport: function() {
      DialogUtils.alert({
        title: this.messages.importInProgress,
        content: this.messages.successfullyStartedGdocsImport
      });
    },

    _gdocAsSource: function() {
      this._controller.gdocAsSource();
    },
    _gdocAuthorize: function() {
      this._controller.gdocAuthorize();
    },
    _importSelectedGdocs: function() {
      var selected = [];
      this._gdocPaginatedListWidget.forInSelectedItems(function(item) {
        selected.push(item.data);
      });
      if(selected.length > 0) {
        this._controller.importGdocs(selected);
      }
    }
  });

});
