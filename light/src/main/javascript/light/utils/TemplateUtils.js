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
define(['dojo/_base/declare', 'dojox/html/entities', 'dojo/string',
        'dojo/dom-construct', 'dojo/_base/lang'],
        function(declare, htmlEntities, string, domConstruct, lang) {
  /**
   * Some utilities for dealing with templates.
   * @class
   * @name light.utils.TemplateUtils
   */
  var TemplateUtils = {
    /** @lends light.utils.TemplateUtils */

    /**
     * Implementation borrowed from Dojo 1.7.2'
     * {@link dijit._TemplatedMixin._stringRepl}.
     *
     * Injects properties from an context object using
     * {@link dojo.string.substitute} format in a template string automatically
     * escaping HTML Entities in them.
     *
     * <p>Properties inject in the form ${name} will be escaped for
     * HTML Entities. But properties injected in the form of ${!name}
     * will still be unescaped.
     * <p>The original {@link dijit._TemplatedMixin._stringRepl}
     * method only escaped the quote character.
     *
     * @param {String} tmpl The template string.
     * @param {Object} context Object used for property lookup.
     * @return {String} A.
     *
     */
    safelyInjectProperties: function(tmpl, context) {
      // summary:
      //    Does substitution of ${foo} type properties in template string
      // tags:
      //    private
      var className = context.declaredClass;
      // Cache contains a string because we need to do property replacement
      // do the property replacement
      return string.substitute(tmpl, context, function(value, key) {
        if (key.charAt(0) == '!') {
          value = lang.getObject(key.substr(1), false, context);
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
      }, context);
    },

    /**
     * Uses {@link light.utils.TemplateUtils.safelyInjectProperties} with
     * tmpl and context and then transforms it into a dom Node.
     *
     * <p> This function can be seen as an lightweight alternative to
     * creating a widget to show some data using templates.
     *
     * @param {String} tmpl The template string.
     * @param {Object} context Object used for property lookup.
     * @return {*} The rendered template.
     *
     */
    toDom: function(tmpl, context) {
      return domConstruct.toDom(
          TemplateUtils.safelyInjectProperties(tmpl, context));
    },
    
  };

  return TemplateUtils;
});
