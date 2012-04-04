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
        'dijit/_TemplatedMixin', 'light/utils/TemplateUtils'],
        function(declare, AbstractLightView, _TemplatedMixin,
                 TemplateUtils) {
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
     * Adds auto escaping HTML Entities for injected properties in templates.
     *
     * <p>This should make Light's views less vulnerable to XSS attacks.
     */
    _stringRepl: function(tmpl) {
      return TemplateUtils.safelyInjectProperties(tmpl, this);
    }

  });
});
