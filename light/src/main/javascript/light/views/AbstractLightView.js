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
define(['dojo/_base/declare', 'dijit/_Widget'], function(declare, _Widget) {
  /**
   * Base abstract class for all light views.
   *
   * @class
   * @name light.views.AbstractLightView
   */
  return declare('light.views.AbstractLightView', _Widget, {
    /** @lends light.views.AbstractLightView# */

    /**
     * Defines the controller for this view.
     *
     * @param {light.controllers.AbstractLightController}
     *          controller Controller.
     */
    setController: function(controller) {
      this._controller = controller;
    }

  });
});
