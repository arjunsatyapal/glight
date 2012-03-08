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
"use strict";
/** @exports dojoAmdAdapter as jstestdriver.dojoAmdAdapter */
/**
 * <p>Dojo Asynchronous Module Definition Adapter for JsTestDriver
 *
 * <p>It aims to allow loading tests and code modules to test using the
 * dojo AMD loader. Currently it is not pretty general and it was specially
 * tweaked to the needs of Light.
 *
 * <p>This plugin is known to work with dojo 1.7.2
 *
 * <p>Current assumptions for this plugin to work:
 * <ul>
 * <li> You create a file containing a variable dojoAmdAdapterConfig with the
 *      options shown bellow and configure JsTestDriver to load it before
 *      loading this plugin.
 * <pre>
 *  var dojoAmdAdapterConfig = {
 *
 *      // JSON File with the tests to be loaded using the Dojo Loader
 *      testListFile: "JSTestDriver/Path/For/A/JSON/File/Listing/Test/Files",
 *
 *      // Function to customize the module id's found in the testListFile
 *      // If it returns false we will ignore the given module
 *      testMidFormatter: function(mid) { return mid; }
 *
 *  };
 * </pre></li>
 * <li> You add the dojo/dojo.js to be loaded with your JsTestDriver</li>
 * <li> You add this file (dojoAmdAdapter.js) to be loaded after dojo.js in your
 *      JsTestDriver.conf</li>
 * <li> You create a empty file called "dojoAmdAdapterWait.js" and make it be
 *      loaded by JSTestDriver after this file (dojoAmdAdapter.js).</li>
 * <li> Your test files and their dependencies are not on load section! Use
 *      serve or proxy (recommended) to make them available to the dojo loader
 *      when running jstestdriver. Even dojo files besides dojo.js should not
 *      be on the load section. </li>
 * <li> You have fresh browsers between runs (use the --reset flag when running
 *      JSTestDriver).</li>
 * <li> You add to dojoConfig the option
 *      <code>has: {'config-dojo-loader-catches': true}</code></li>
 * </ul>
 *
 * @namespace
 */
jstestdriver.dojoAmdAdapter = (function() {

  // Checking some probable mistakes done by new users of this plugin
  if (jstestdriver.dojoAmdAdapter)
    throw new Error('dojoAmdAdapter was already defined. AMD Modules ' +
            'aren\'t currently reloaded, so you need to use --reset.');
  if (!window.dojoAmdAdapterConfig)
    throw new Error('dojoAmdAdapterConfig not defined!');
  if (!(dojoAmdAdapterConfig.testMidFormatter instanceof Function))
    throw new Error('dojoAmdAdapterConfig.testMidFormatter is not a function!');


  /**
   * Convert's a Javascript object Friendly JSON representation.
   *
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

  /**
   * Dummy test file used to make JSTestDriver wait for Dojo to load files.
   *
   * @const
   */
  var DUMMY_TEST_FILE = '/dojoAmdAdapterWait.js';


  /**
   * Loading timeout in milliseconds
   *
   * Theoretically dojo has a builtin system for that,
   * but some kind of syntax errors in a module will not fire the
   * timeout in IE with Dojo 1.7.2 loader and the JSTestDriver just
   * hangs.
   *
   * @const
   */
  var LOAD_TIMEOUT_IN_MILLIS = 5000;

  /**
   * Boolean for tracking if the last loading attempt failed
   */
  var previous_load_failed = false;

  /**
   * A test case to make JSTestDriver fail in case a loading error
   * occurs.
   *
   * For some reason, just telling JSTestDriver that the dummy
   * resource failed to load does not make it globally fail. That means,
   * for example, if there is a loading error (path problem, syntax
   * error on a file, etc) it would be possible that the maven build
   * will not fail. This is why this fake testcase exists.
   */
  var reporterTestCase = TestCase('LoadTest');
  reporterTestCase.prototype.testLoadingSucceeded = function() {
    if (previous_load_failed) {
      previous_load_failed = false;
      throw new Error('Loading errors happened!');
    }
  }

  var dojoAmdAdapter = {};

  dojoAmdAdapter.log = function() {
    var console = jstestdriver.console;
    console.log.apply(console, arguments);
  }

  // Variables to store data from the last loading of DUMMY_TEST_FILE
  var lastCallback = null;
  var lastFile = null;
  var lastTimer = 0;
  var lastAction = null;

  /**
   * Error callback
   */
  function fail() {
    if (lastFile != null) {
      previous_load_failed = true;
      lastCallback(new jstestdriver.FileResult(lastFile, false,
              'Error loading resource: ' +
              prettyPrintJSON(Array.prototype.slice.call(arguments, 0)) +
              '\n  Last action was: ' + lastAction));
      lastFile = null;
      lastAction = null;
      clearTimeout(lastTimer);
    }
  }

  /**
   * Success callback
   */
  function success() {
    if (lastFile != null) {
      lastCallback(new jstestdriver.FileResult(lastFile, true, '', 0));
      lastFile = null;
      lastAction = null;
      clearTimeout(lastTimer);
    }
  }

  // Wiring an error callback for loading problems
  require.on('error', fail);

  /*
     JSTestDriverPlugin that knows how to load the "dummy" resource,
     so we can pause the execution until everything is loaded.
  */
  jstestdriver.pluginRegistrar.register({
    name: 'waitForDojoAndTestsToBeReady',
    loadSource: function(fileObj, callback) {

      // If this is the dummy resource
      if (fileObj.fileSrc.substr(fileObj.fileSrc.length -
              DUMMY_TEST_FILE.length) === DUMMY_TEST_FILE) {

        // Data about the dummy resource that we will need
        lastCallback = callback;
        lastFile = fileObj;

        // Starting timer for failing if we timeout
        lastTimer = setTimeout(function() {
          fail('Loading timeout (dojoAmdAdapter). ' +
               'Please, check if your file path\'s are ok and ' +
               'if there are no syntax errors in your files.');
        }, LOAD_TIMEOUT_IN_MILLIS);

        // Loading the list
        lastAction = 'Loading test list: ' + dojoAmdAdapterConfig.testListFile;

        require(['dojo/text!' + dojoAmdAdapterConfig.testListFile],
                function(fileData) {

          // Parsing the list
          lastAction = 'Parsing JSON data from ' + dojoAmdAdapterConfig.testListFile;
          var data;
          try {
            data = jstestdriver.JSON.parse(fileData);
          } catch (e) {
            fail({
              message: 'Failed to parse JSON file',
              file: dojoAmdAdapterConfig.testListFile,
              data: fileData
            });
            return;
          }

          // Reformatting files to proper module ids
          lastAction = 'Iterating over JSON data from ' + dojoAmdAdapterConfig.testListFile;
          var mids = [];
          for (var i = 0, len = data.length; i < len; i++) {
            var mid = dojoAmdAdapterConfig.testMidFormatter(data[i]);
            if (mid)
              mids.push(mid);
          }

          // Finally loading them
          lastAction = 'Loading: ' + mids;
          require(mids, success);

        });
        return true;
      }

      return false;
    }
  });

  return dojoAmdAdapter;

})();
