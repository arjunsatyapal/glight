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
'use strict';
define(['dojo/_base/declare', 'dijit/_Widget', 'dijit/_TemplatedMixin',
        'dijit/_WidgetsInTemplateMixin', 'dojo/text!./RegisterFormView.html',
        'dijit/Tooltip', 'light/AbstractLightView', 'dojo/query',
        'dijit/form/ValidationTextBox', 'dijit/form/Textarea',
        'dijit/form/Button', 'dijit/form/CheckBox', 'dijit/Dialog',
        'dijit/form/Form'],
        function(declare, _Widget, _TemplatedMixin,
                _WidgetsInTemplateMixin, template, Tooltip,
                AbstractLightView, query) {
  /**
   * @class
   * @name light.RegisterFormView
   */
  return declare('light.RegisterFormView',
          [AbstractLightView, _Widget,
           _TemplatedMixin, _WidgetsInTemplateMixin], {

      /** @lends light.RegisterFormView# */

      templateString: template,

      /**
       * Return's the current data in the form.
       * @return {Object} Current data.
       */
      getData: function() {
        return {
          firstName: this._firstNameTextBox.get('value'),
          lastName: this._lastNameTextBox.get('value')
        };
      },

      /**
       * Disables all the form's inputs
       */
      disable: function() {
        // TODO(waltercacau): Make widgets gray out.
        query('input', this.domNode).attr('disabled', 'disabled');
      },

      /**
       * Enables all the form's inputs
       */
      enable: function() {
        query('input', this.domNode).removeAttr('disabled');
      },

      /**
       * Validates the form and visually gives feedback
       * to the user if some validation has failed.
       * @return {boolean} True if there are not validation errors.
       */
      validate: function() {
        return this._form.validate() && this._tosValidation();
      },

      /**
       * Validates that the user has agreed to the Terms of Service.
       * @return {boolean} True if the user has agreed.
       */
      _tosValidation: function() {
        var ret = this._tosCheckbox.get('checked');
        if (!ret) {
          Tooltip.show('You should agree to the Terms of Service',
                  this._tosCheckbox.domNode.parentNode,
                  [], !this.isLeftToRight());
        }
        return ret;
      },

      /**
       * Callback to be called when the submit button is pressed.
       */
      _onSubmit: function() {
        this._controller.onSubmit();
      }
  });
});
