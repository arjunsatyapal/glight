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
        'dojo/i18n!light/nls/ImportModuleMessages',
        'light/utils/DOMUtils',
        'dijit/_WidgetsInTemplateMixin',
        'dijit/form/Button'],
        function(declare, TemplatedLightView, template, messages, DOMUtils,
                _WidgetsInTemplateMixin) {

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
    
    hide: function() {
      DOMUtils.hide(this.domNode);
    },
    
    showFirstForm: function() {
      DOMUtils.show(this.domNode);
    },
    
    _gdocImport: function() {
      
    }
  });

});
