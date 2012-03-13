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
        'dijit/form/ValidationTextBox', 'dijit/form/Textarea', 'dijit/form/Button',
        'dijit/form/CheckBox', 'dijit/Dialog', 'dijit/form/Form'],
        function(declare, _Widget, _TemplatedMixin,
                _WidgetsInTemplateMixin, template, Tooltip,
                AbstractLightView, query) {
    return declare('light.RegisterFormView',
            [AbstractLightView, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin], {

        /** @lends light.RegisterFormView# */

        templateString: template,
        getData: function() {
          return {
            firstName: this._firstNameTextBox.get('value'),
            lastName: this._lastNameTextBox.get('value')
          };
        },
        disable: function() {
          // TODO(waltercacau): Make widgets gray out.
          query('input', this.domNode).attr('disabled', 'disabled');
        },
        enable: function() {
          query('input', this.domNode).removeAttr('disabled');
        },
        validate: function() {
          return this._form.validate() && this._tosValidation();
        },
        _tosValidation: function() {
          var ret = this._tosCheckbox.get('checked');
          console.log('asd');
          if (!ret) {
            Tooltip.show('You should agree to the Terms of Service',
                    this._tosCheckbox.domNode.parentNode,
                    [], !this.isLeftToRight());
          }
          return ret;
        },
        _onSubmit: function() {
          this._controller.onSubmit();
        }
    });
});
