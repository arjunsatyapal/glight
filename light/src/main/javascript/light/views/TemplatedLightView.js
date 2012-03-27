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
define(['dojo/_base/declare', 'light/views/AbstractLightView',
        'dijit/_TemplatedMixin', 'dojox/html/entities', 'dojo/string'],
        function(declare, AbstractLightView, _TemplatedMixin,
                htmlEntities, string) {
  /**
   * Base class for all light views that use templates.
   *
   * <p>It includes the following feature(s) for Light's views:
   * <ul>
   * <li>Automatic escaping HTML Entities for injected properties
   *     in templates</li>
   * </ul>
   *
   * @class
   * @name light.views.TemplatedLightView
   * @extends light.views.AbstractLightView
   * @extends dijit._TemplatedMixin
   */
  return declare('light.controllers.TemplatedLightView',
          [AbstractLightView, _TemplatedMixin], {
    /** @lends light.views.TemplatedLightView# */

    /**
     * Implementation borrowed from Dojo 1.7.2's dijit._TemplatedMixin.
     * Adds auto escaping HTML Entities for injected properties in templates.
     *
     * <p>This should make Light's views less vulnerable to XSS attacks.
     * <p>Properties inject in the form ${name} will be escaped for
     * HTML Entities. But properties injected in the form of ${!name}
     * will still be unescaped.
     * <p>Originally, this method only escaped the quote character.
     */
    _stringRepl: function(tmpl) {
      // summary:
      //    Does substitution of ${foo} type properties in template string
      // tags:
      //    private
      var className = this.declaredClass, _this = this;
      // Cache contains a string because we need to do property replacement
      // do the property replacement
      return string.substitute(tmpl, this, function(value, key) {
        if (key.charAt(0) == '!') {
          value = lang.getObject(key.substr(1), false, _this);
        }
        if (typeof value == 'undefined') {
          throw new Error(className + ' template:' + key);
        } // a debugging aide
        if (value == null) { return ''; }

        // Substitution keys beginning with ! will skip the transform step,
        // in case a user wishes to insert unescaped markup, e.g. ${!foo}
        return key.charAt(0) == '!' ? value :
          // Safer substitution, see heading "Attribute values" in
          // http://www.w3.org/TR/REC-html40/appendix/notes.html#h-B.3.2

          // LightMod: Adding escaping to html.
          htmlEntities.encode(value.toString());
      }, this);
    }

  });
});
