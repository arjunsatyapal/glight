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
        'dojox/html/entities',
        'dojo/dom-construct',
        'dijit/_WidgetsInTemplateMixin',
        'light/widgets/ListWidget',
        'light/utils/TemplateUtils',
        'dijit/form/Button'],
        function(declare, TemplatedLightView, template, GDocListItemTemplate,
                messages, DOMUtils, URLUtils, LanguageUtils, string,
                htmlEntities,
                domConstruct, _WidgetsInTemplateMixin, ListWidget,
                TemplateUtils, Button) {

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
      this.inherited(arguments);
      this.hide();
    },
    
    /**
     * List of possible form's of this view.
     * 
     * <p>Each item of this list has a correspondent div element
     * in the template.
     */
    _forms: ['first', 'gdocList', 'gdocEmptyList', 'gdocAuthRequired'],
    
    /**
     * Hides all form's of this view.
     */
    hide: function() {
      for(var i=0, len=this._forms.length; i<len; i++) {
        DOMUtils.hide(this['_'+this._forms[i]+'FormDiv']);
      }
    },
    
    /**
     * Utility function that hides all form's of this view except
     * the one specified.
     */
    _showForm: function(visibleForm) {
      for(var i=0, len=this._forms.length; i<len; i++) {
        var form = this._forms[i];
        var formDiv = this['_'+form+'FormDiv'];
        if(form == visibleForm) {
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
    
    _listWidget: null,
    _buttons: null,
    _destroyOldListWidget: function() {
      if(this._listWidget != null) {
        var node = this._listWidget.node;
        this._listWidget.destroy();
        domConstruct.empty(node);
        this._listWidget = null;
      }
    },
    
    /**
     * Shows a list of Gdocs items.
     * @param {Object} data Data returned from the server containing the list.
     * @param {number} page Current page index 0-based.
     * @param {boolean} hasNextPage
     */
    showGdocListForm: function(data, page, hasNextPage) {
      this._destroyOldListWidget();
      if(data.list) {
        this._showForm('gdocList');
        var listNode = this._gdocListWidgetDiv;
        this._listWidget = new ListWidget(listNode, {
          accept: [],
          type: 'gdocDndSource',
          selfAccept: false,
          copyOnly: true,
          selfCopy: false,
          rawCreator: function(item) {
            var node = TemplateUtils.toDom(GDocListItemTemplate, item);
            return {
              node: node,
              data: item,
              focusNode: dojo.query('a', node)[0],
              type: ['gdoc']
            };
          }
        });
        this._listWidget.insertNodes(false /* addSelected */, data.list);
        this._listWidget.focusFirstItem();
        
        // Prev page link
        if (page > 0) {
          DOMUtils.show(this._getPreviousPageButton.domNode);
        } else {
          DOMUtils.hide(this._getPreviousPageButton.domNode);
        }

        // Current page number
        DOMUtils.setText(this._gdocPageInfoSpan,
            string.substitute(this.messages.pageInfo, {
              page: LanguageUtils.formatNumber(page+1)
            }));

        // Next page link
        if(hasNextPage) {
          DOMUtils.show(this._getNextPageButton.domNode);
        } else {
          DOMUtils.hide(this._getNextPageButton.domNode);
        }
      } else {
        this._showForm('gdocEmptyList');
      }
      
    },
    
    /**
     * Shows explanation to the user that he needs to grant authorization
     * so we can import his Google Documents.
     */
    showGdocAuthRequiredForm: function() {
      this._showForm('gdocAuthRequired');
    },
    
    _gdocAsSource: function() {
      this._controller.gdocAsSource();
    },
    _gdocAuthorize: function() {
      this._controller.gdocAuthorize();
    },
    _getGdocListPreviousPage: function() {
      this._controller.getGdocListPreviousPage();
    },
    _getGdocListNextPage: function() {
      this._controller.getGdocListNextPage();
    }
  });

});
