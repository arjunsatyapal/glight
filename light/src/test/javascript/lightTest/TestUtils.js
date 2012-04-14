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
define(['light/views/AbstractLightView', 'dojox/json/schema', 'dojo'],
        function(AbstractLightView, jsonSchema, dojo) {
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

    _currentSpec: null,

    /**
     * Validates the given object agains an json schema.
     * @param {*} obj
     * @param {Object} schema
     * @see http://json-schema.org/
     */
    validateSchema: function(obj, schema) {
      if (!jsonSchema.validate(obj, schema).valid) {
        throw new Error('Given object does not conform to schema' +
            (schema.description ? ' ' + schema.description : '') +
            ': ' + dojo.toJson(obj));
      }
    },

    getEnumNames: function(_enum) {
      var names = [];
      for (var name in _enum) {
        if (_enum.hasOwnProperty(name) && name.toUpperCase() == name)
          names.push(name);
      }
      return names;
    }

  };

  // Registering with jasmine to cleanUp after each test.
  beforeEach(function() {
    TestUtils._currentSpec = this;
  });
  return TestUtils;
});
