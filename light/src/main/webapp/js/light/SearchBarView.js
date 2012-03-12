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
"use strict";
define(["dojo/_base/declare", "dijit/_Widget", "dijit/_TemplatedMixin", "dijit/_WidgetsInTemplateMixin", "dojo/text!./SearchBarView.html", "dijit/form/TextBox", "dijit/form/Button"], 
        function(declare, _Widget, _TemplatedMixin, _WidgetsInTemplateMixin, template) {
    return declare("light.SearchBarView", [_Widget, _TemplatedMixin, _WidgetsInTemplateMixin], {
        templateString: template
    });
     
});