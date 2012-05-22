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
        'dojo/text!light/templates/ImportedByMeTemplate.html',
        'dojo/i18n!light/nls/ImportedByMeMessages',
        'light/utils/DOMUtils',
        'light/utils/RouterManager',
        'dojo/query',
        'dojo/dom-construct',
        'dijit/_WidgetsInTemplateMixin',
        'light/widgets/PaginatedListWidget',
        'light/utils/TemplateUtils',
        'light/enums/DndConstantsEnum',
        'light/builders/ModuleDndTypeBuilder',
        'dijit/form/Button'],
        function(declare, TemplatedLightView, template,
                messages, DOMUtils,
                RouterManager, $,
                domConstruct, _WidgetsInTemplateMixin, PaginatedListWidget, 
                TemplateUtils, DndConstantsEnum, ModuleDndTypeBuilder,
                Button) {

  /**
   * @class
   * @name light.views.ImportedByMeView
   */
  return declare('light.views.ImportedByMeView', [TemplatedLightView,
      _WidgetsInTemplateMixin], {
    /** @lends light.views.ImportedByMeView# */
    templateString: template,
    messages: messages,
    _earlyTemplatedStartup: true,

    postCreate: function() {
      var self = this;

      this._listWidget = new PaginatedListWidget({
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
              focusNode: $('a', node)[0],
              type: [DndConstantsEnum.MODULE_TYPE]
            };
          }
        },
        onError: function(err) {
          //if(!err.lightCancelled) {
            // TODO(waltercacau): Deal with this error
          //}
        }
      }, this._listWidgetDiv);

      this.inherited(arguments);
      this.hide();
    },

    /**
     * Hides all form's of this view.
     */
    hide: function() {
      DOMUtils.hide(this.domNode);
    },

    /**
     * Show a list of imported modules
     */
    show: function(link) {
      DOMUtils.show(this.domNode);
      this._listWidget.getFirstPage(link);
    },
    
    _importOtherModules: function() {
      this._controller.importOtherModules();
    }

  });

});
