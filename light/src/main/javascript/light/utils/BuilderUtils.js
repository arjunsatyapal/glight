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
       * @param {Object=} params
       *    Optional params like <code>schema</code> for
       *    validation during development and map of <code>defaults</code> values.
       * @param {string|Object=} schema A schema or a JSON string of the schema.
       * @return {*} A Builder Class.
       */
      createBuilderClass: function(className, fields, params) {
        if (has('light-dev') && typeof params.schema == 'string') {
          params.schema = dojo.fromJson(params.schema);
        }
        if (!params.defaults) {
          params.defaults = {};
        }
        if (!params.validate) {
          params.validate = {};
        }

        var proto = {
          constructor: function(rawData, mightHaveExtraKeys) {
            this._data = lang.clone(params.defaults);
            if (rawData) {

              // During development, being more strict
              if (has('light-dev') && !mightHaveExtraKeys) {
                for (var key in rawData) {
                  if (rawData.hasOwnProperty(key) &&
                          array.indexOf(fields, key) == -1) {
                    throw new Error(params.className + ': Invalid key ' + key);
                  }
                }
              }

              var data = this._data;
              array.forEach(fields, function(field) {
                if (rawData.hasOwnProperty(field)) {
                  data[field] = rawData[field];
                }
              });
            }
          },

          build: function() {
            if (params.normalize) {
              params.normalize.apply(this._data, []);
            }
            if (has('light-dev') && params.schema) {
              var validationData = jsonSchema.validate(this._data, params.schema);
              if (!validationData.valid) {
                console.log(validationData);
                throw new Error(className +
                    ': Built object does not conform to schema' +
                    (params.schema.description ? ' ' + params.schema.description : '') +
                    ': ' + dojo.toJson(this._data));
              }
            }
            return this._data;
          }
        };
        if (params.normalize) {
          proto.normalize = function() {
            params.normalize.apply(this._data, []);
            return this;
          };
        } else {
          proto.normalize = function() {}
        }

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
