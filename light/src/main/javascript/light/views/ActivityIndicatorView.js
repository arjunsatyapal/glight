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
        'light/utils/TemplateUtils',
        'light/utils/DOMUtils'],
        function(declare, AbstractLightView, TemplateUtils, DOMUtils) {

  /**
   * @class
   * @name light.views.ActivityIndicatorView
   */
  return declare('light.views.ActivityIndicatorView', [AbstractLightView], {
    /** @lends light.views.ActivityIndicatorView# */

    postCreate: function() {
      this.inherited(arguments);
      this.domNode.innerHTML = '<img src="/images/ajax-loader.gif">';
      this.hide();
    },

    /**
     * Hides all form's of this view.
     */
    hide: function() {
      DOMUtils.hide(this.domNode);
    },

    /**
     * Show a list of imported modules
     */
    show: function(link) {
      DOMUtils.show(this.domNode);
    }

  });

});
