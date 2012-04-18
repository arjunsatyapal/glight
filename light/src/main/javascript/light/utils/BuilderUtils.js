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
define(['dojo/_base/declare', 'dojo/_base/lang',
        'dojo/_base/array', 'dojo/has', 'dojo',
        'dojo/has!light-dev?dojox/json/schema'],
        function(declare, lang, array, has, dojo, jsonSchema) {
    /**
     * Some utilities for creating builders.
     *
     * TODO(waltercacau): add test for this class
     *
     * @class
     * @name light.utils.BuilderUtils
     */
    var BuilderUtils = {
      /** @lends light.utils.BuilderUtils */
      /**
       * Create a builder based on the given fields
       *
       * @param {string} className Fully qualified class name.
       * @param {Array.<string>} fields List of fields.
       * @param {Object=} defaults Map of default values for the fields.
       * @param {string|Object=} schema A schema or a JSON string of the schema.
       * @return {*} A Builder Class.
       */
      createBuilderClass: function(className, fields, defaults, schema) {
        if (has('light-dev') && typeof schema == "string") {
          schema = dojo.fromJson(schema);
        }
        if (!defaults) {
          defaults = {};
        }

        var proto = {
          constructor: function(rawData) {
            this._data = lang.clone(defaults);
            if (rawData) {

              // During development, being more strict
              if (has('light-dev')) {
                for (var key in rawData) {
                  if (rawData.hasOwnProperty(key) &&
                          array.indexOf(fields, key) == -1) {
                    throw new Error(className + ': Invalid key ' + key);
                  }
                }
              }

              var data = this._data;
              array.forEach(fields, function(field) {
                data[field] = rawData[field];
              });
            }
          },
          build: function() {
            if (has('light-dev') && schema) {
              if (!jsonSchema.validate(this._data, schema).valid) {
                throw new Error(className +
                    ': Built object does not conform to schema' +
                    (schema.description ? ' ' + schema.description : '') +
                    ': ' + dojo.toJson(this._data));
              }
            }
            return this._data;
          }
        };

        // Creating getter/setter
        array.forEach(fields, function(field) {
          if (proto[field])
            throw new Error('Reserved field ' + field);
          proto[field] = BuilderUtils._createGetterSetterMethod(field);
        });

        return declare(className, null, proto);
      },
      _createGetterSetterMethod: function(field) {
        return function(value) {
          if (arguments.length == 0) {
            return this._data[field];
          }
          this._data[field] = value;
          return this;
        }
      }
    };
    return BuilderUtils;
});
