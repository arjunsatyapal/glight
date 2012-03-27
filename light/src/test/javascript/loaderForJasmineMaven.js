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
/**
 * This file should expose an AMD Loader that has a require.config function
 * like RequireJS 1.0.7
 *
 * For simplicity, we configured Jasmine Maven Plugin to think it is working
 * with requireJS, because by the time this file was created (2012-03-07) there
 * is no support for other AMD loaders in such plugin.
 *
 * We also use this file to load other test framework dependencies
 * (eg. sinon.JS).
 *
 * Ideally this file should be analogous to jsTestDriver.conf
 */

var setupDojoLoaderForJasmine = function() {

  /**
   * Convert's a Javascript object Friendly JSON representation.
   *
   * @param {*} obj Data to be transformed into JSON.
   * @param {String=} indent Indentation level.
   * @return {String} Friendly JSON representation.
   */
  var prettyPrintJSON = function(obj, indent) {
    indent = indent || '';
    var ownIndent = indent + '  ';
    if (typeof obj == 'object') {
      var builder = [];
      if (Array.prototype.isPrototypeOf(obj)) {
        if (obj.length == 0)
          return '[]';
        builder.push('[');
        builder.push('\n');
        for (var i = 0, val, last = obj.length - 1; val = obj[i]; i++) {
          builder.push(ownIndent);
          builder.push(prettyPrintJSON(val, ownIndent));
          if (i != last)
            builder.push(',');
          builder.push('\n');
        }
        builder.push(indent);
        builder.push(']');
      } else {
        var keys = [];
        for (var key in obj) {
          if (obj.hasOwnProperty(key))
            keys.push(key);
        }
        if (keys.length == 0)
          return '{}';
        builder.push('{');
        builder.push('\n');
        keys.sort();
        for (var i = 0, key, last = keys.length - 1; key = keys[i]; i++) {
          builder.push(ownIndent);
          builder.push(key);
          builder.push(': ');
          builder.push(prettyPrintJSON(obj[key], ownIndent));
          if (i != last)
            builder.push(',');
          builder.push('\n');
        }
        builder.push(indent);
        builder.push('}');
      }
      return builder.join('');
    } else {
      return JSON.stringify(obj);
    }
  }

  var failed = false;
  var debug = [];
  describe('loader', function() {
    it('should succeed', function() {
      if (failed)
        throw new Error('Error loading resource: ' + prettyPrintJSON(debug));
    });
  });
  require.config = function() {};
  require.on('error', function() {
    failed = true;
    debug = Array.prototype.slice.call(arguments, 0);
  });
};

document.write(
    '<script src="src/test/javascript/external/syn.js"></script>');
document.write(
    '<script src="src/test/javascript/external/sinon-1.3.1.js"></script>');
document.write(
    '<script src="src/test/javascript/external/jasmine-sinon.js"></script>');
document.write(
    '<script src="src/test/javascript/lightTestExtensions.js"></script>');
document.write('<script src="src/test/javascript/testConfig.js"></script>');
document.write(
    '<script src="src/main/javascript/external/djk/dojo/dojo.js"></script>');
document.write('<script>setupDojoLoaderForJasmine()</script>');
