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
define(['light/views/AbstractLightView'],
        function(AbstractLightView) {
  /**
   * Utility Methods for testing.
   * @class
   * @name lightTest.TestUtils
   */
  var TestUtils = {
    /** @lends lightTest.TestUtils */

    /**
     * Return's a stubbed version of the given view
     * class without needing to mess with dojo internals.
     *
     * <p>If you just stub the view after calling the new operator,
     * dojo _Widget class which is inherited by Light Views will
     * do some stuff that would require some latter cleanup.
     * <p>We avoid that by avoiding dojo stuff being called
     * by overriding the class constructor.
     *
     * @param {Function} viewClass A LightView class.
     * @return {Object} Sinon stub of the viewClass.
     */
    stubView: function(viewClass) {
      function dummyConstructor() {}
      dummyConstructor.prototype = viewClass.prototype;
      var stubbed = new dummyConstructor();
      this._currentSpec.stub(stubbed);
      return stubbed;
    },

    _currentSpec: null

  };

  // Registering with jasmine to cleanUp after each test.
  beforeEach(function() {
    TestUtils._currentSpec = this;
  });
  return TestUtils;
});
