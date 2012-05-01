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
        'light/utils/TemplateUtils',
        'dojo/text!light/templates/PromptDialogTemplate.html',
        'light/views/TemplatedLightView',
        'dijit/_WidgetsInTemplateMixin',
        'dijit/Dialog',
        'dijit/form/Button',
        'dijit/form/TextBox'],
        function(declare, lang, TemplateUtils, PromptDialogTemplate, TemplatedLightView, _WidgetsInTemplateMixin) {
  var PromptDialog = declare([TemplatedLightView, _WidgetsInTemplateMixin], {
    // Options
    // TODO(waltercacau): Add support for translations
    okLabel: 'OK',
    cancelLabel: 'Cancel',
    title: 'Prompt',
    onCancel: function() {},
    onOk: function() {},
    label: '',
    value: '',
    
    templateString: PromptDialogTemplate,
    _onClickOk: function() {
      this.onOk(this._value.get('value'));
      this.cleanup();
    },
    _onClickCancel: function() {
      this.onCancel();
      this.cleanup();
    },
    cleanup: function() {
      var self = this;
      setTimeout(function () {
        self._dialog.hide();
        self._dialog.destroyRecursive();
        self.destroyRecursive();
      }, 0);
    },
    _earlyTemplatedStartup: true,
    constructor: function(options) {
      declare.safeMixin(options);
    },
    postCreate: function() {
      this.inherited(arguments);
      this.connect(this._dialog, "onCancel", this._onClickCancel);
    },
    show: function() {
      this._dialog.show();
    }
  });
  
  
  /**
   * Some utilities for dealing with DOM and style.
   *
   * @class
   * @name light.utils.DialogUtils
   */
  return {
    /** @lends light.utils.DialogUtils */

    /**
     * Shows a prompt dialog.
     * 
     * Usage example:
     * DialogUtils.prompt({
     *  okLabel: 'Create',
     *  cancelLabel: 'Cancel',
     *  title: 'Title',
     *  label: 'Fill with a luck number',
     *  value: 10,
     *  onOk: function(value) { ... },
     *  onCancel: function() { ... }
     * })
     */
    prompt: function(options) {
      var dialog = new PromptDialog(options)
      console.log(dialog);
      document.body.appendChild(dialog.domNode);
      dialog.show();
    },
    
    _PromptDialog: PromptDialog
  
  };
});
