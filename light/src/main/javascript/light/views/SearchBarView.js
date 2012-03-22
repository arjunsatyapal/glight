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
        'dijit/_WidgetsInTemplateMixin', 'dojo/text!./SearchBarView.html',
        'dojo/_base/event', 
        'dijit/form/TextBox', 'dijit/form/Button', 'dijit/form/Form'],
        function(declare, TemplatedLightView,
                _WidgetsInTemplateMixin, template, eventUtil) {
    return declare('light.views.SearchBarView',
            [TemplatedLightView, _WidgetsInTemplateMixin], {
        templateString: template,
        
        _onSubmit: function(evt) {
          this._controller.onSubmit();
          eventUtil.stop(evt);
        },
        
        getQuery: function() {
          return this._textBox.get('value');
        },
        setQuery: function(value) {
          return this._textBox.set('value', value);
        }
    });

});