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
        'dojo/_base/event',
        'light/utils/TemplateUtils',
        'dojo/text!light/templates/PromptDialogTemplate.html',
        'dojo/text!light/templates/AlertDialogTemplate.html',
        'light/views/TemplatedLightView',
        'dijit/_WidgetsInTemplateMixin',
        'dijit/Dialog',
        'dijit/form/Button',
        'dijit/form/TextBox'],
        function(declare, lang, eventUtil, TemplateUtils, PromptDialogTemplate,
                 AlertDialogTemplate, TemplatedLightView,
                 _WidgetsInTemplateMixin) {
  var BaseDialog = declare([TemplatedLightView, _WidgetsInTemplateMixin], {
    cleanup: function() {
      var self = this;
      self._dialog.hide();
      setTimeout(function() {
        self._dialog.destroyRecursive();
        self.destroyRecursive();
      }, 3000);
    },
    _earlyTemplatedStartup: true,
    constructor: function(options) {
      declare.safeMixin(options);
    },
    show: function() {
      this._dialog.show();
    }
  });

  var PromptDialog = declare([BaseDialog], {
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
    _onClickOk: function(evt) {
      // This avoids submitting the form
      eventUtil.stop(evt);
      this.onOk(this._value.get('value'));
      this.cleanup();
    },
    _onClickCancel: function() {
      this.onCancel();
      this.cleanup();
    },
    postCreate: function() {
      this.inherited(arguments);
      this.connect(this._dialog, 'onCancel', this._onClickCancel);
    }
  });


  var AlertDialog = declare([BaseDialog], {
    // Options
    // TODO(waltercacau): Add support for translations
    okLabel: 'OK',
    title: 'Alert',
    onOk: function() {},
    content: '',

    templateString: AlertDialogTemplate,
    _onClickOk: function() {
      this.onOk();
      this.cleanup();
    },
    postCreate: function() {
      this.inherited(arguments);
      this.connect(this._dialog, 'onCancel', this._onClickOk);
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
      var dialog = new PromptDialog(options);
      document.body.appendChild(dialog.domNode);
      dialog.show();
    },

    alert: function(options) {
      var dialog = new AlertDialog(options);
      document.body.appendChild(dialog.domNode);
      dialog.show();
    },

    _AlertDialog: AlertDialog,
    _PromptDialog: PromptDialog

  };
});
