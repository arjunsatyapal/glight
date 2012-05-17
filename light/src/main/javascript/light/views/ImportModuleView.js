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
        'light/utils/RouterManager',
        'dojo',
        'dojo/string',
        'dojo/dom-construct',
        'dijit/_WidgetsInTemplateMixin',
        'light/widgets/PaginatedListWidget',
        'light/utils/TemplateUtils',
        'light/utils/DialogUtils',
        'light/enums/DndConstantsEnum',
        'light/builders/ModuleDndTypeBuilder',
        'dijit/form/Button'],
        function(declare, TemplatedLightView, template, GDocListItemTemplate,
                messages, DOMUtils, URLUtils, LanguageUtils,
                RouterManager, dojo, string,
                domConstruct, _WidgetsInTemplateMixin, PaginatedListWidget, 
                TemplateUtils, DialogUtils, DndConstantsEnum, ModuleDndTypeBuilder,
                Button) {
  var SUPPORTED_GDOC_MODULE_TYPES_TO_IMPORT = {
          'GOOGLE_DOCUMENT': true,
          'GOOGLE_COLLECTION': true
  };


  /**
   * @class
   * @name light.views.ImportModuleView
   */
  return declare('light.views.ImportModuleView', [TemplatedLightView,
      _WidgetsInTemplateMixin], {
    /** @lends light.views.ImportModuleView# */
    templateString: template,
    messages: messages,
    _earlyTemplatedStartup: true,

    postCreate: function() {
      var self = this;
      this._gdocPaginatedListWidget = new PaginatedListWidget({
        listOptions: {
          type: DndConstantsEnum.MODULE_SOURCE_TYPE,
          rawCreator: function(item) {
            var additionalClasses = '';
            if (!SUPPORTED_GDOC_MODULE_TYPES_TO_IMPORT[item.moduleType]) {
              additionalClasses = ' gdocListItemNotSupported';
            }
            var node = TemplateUtils.toDom(GDocListItemTemplate, {
              moduleType: item.moduleType,
              link: item.externalId,
              title: item.title,
              additionalClasses: additionalClasses
            });

            return {
              node: node,
              data: new ModuleDndTypeBuilder()
                      .title(item.title)
                      .externalId(item.externalId)
                      .rawData(item)
                      .build(),
              focusNode: dojo.query('a', node)[0],
              type: [DndConstantsEnum.MODULE_TYPE]
            };
          }
        },
        onError: function() {
          // TODO(waltercacau): Check if it was really because of missing auth.
          self.showGdocAuthRequiredForm();
        }
      }, this._gdocPaginatedListWidgetDiv);

      this._importedPaginatedListWidget = new PaginatedListWidget({
        listOptions: {
          type: DndConstantsEnum.MODULE_SOURCE_TYPE,
          rawCreator: function(item) {
            var node = TemplateUtils.toDom('<div class="gdocListItem"><a href="${link}" target="_blank" tabindex="-1">${title}</a></div>', {
              title: item.title,
              link: RouterManager.buildLinkForModuleContent(item.moduleId)
            });
            return {
              node: node,
              data: new ModuleDndTypeBuilder()
                      .title(item.title)
                      .externalId(RouterManager.buildLinkForModuleContent(item.moduleId))
                      .rawData(item)
                      .build(),
              focusNode: dojo.query('a', node)[0],
              type: [DndConstantsEnum.MODULE_TYPE]
            };
          }
        },
        onError: function() {
          // TODO(waltercacau): Check if it was really because of missing auth.
          self.showGdocAuthRequiredForm();
        }
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
     */
    showGdocListForm: function(link) {
      this._showForm('gdocList');
      this._gdocPaginatedListWidget.getFirstPage(link);
    },

    /**
     * Show a list of imported modules
     */
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
        var data = item.data.rawData;
        if (SUPPORTED_GDOC_MODULE_TYPES_TO_IMPORT[data.moduleType] &&
                data.moduleType != 'GOOGLE_COLLECTION') {
          selected.push(data);
        }
      });
      if (selected.length > 0) {
        this._controller.importGdocs(selected);
      }
    },
    _seeImported: function() {
      this._controller.seeImported();
    },
    _importOtherModules: function() {
      this._controller.importModules();
    }
  });

});
