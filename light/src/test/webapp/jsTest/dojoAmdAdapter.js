/** @exports dojoAmdAdapter as jstestdriver.dojoAmdAdapter */
/**
 * <p>Dojo Asynchronous Module Definition Adapter for JsTestDriver
 *
 * <p>It aims to allow loading tests and modules to test using the
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
 *      // JSON File with the tests to be loaded using the Dojo Loader
 *      testListFile: "JSTestDriver/Path/For/A/JSON/File/Listing/Test/Files",
 *      // Function to customize the module id's found in the testListFile
 *      // If it returns false we will ignore the given module
 *      testMidFormatter: function(mid) { return mid; }
 *  };
 * </pre></li>
 * <li> You add the dojo/dojo.js to be loaded with your JsTestDriver</li>
 * <li> You add this file (dojoAmdAdapter.js) to be loaded after dojo with your
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

  // Checking some probably mistakes of new users of this plugin
  if (jstestdriver.dojoAmdAdapter)
    throw new Error('dojoAmdAdapter was already defined. AMD Modules ' +
            "aren't currently reloaded, so you need to use --reset.");
  if (!window.dojoAmdAdapterConfig)
    throw new Error('dojoAmdAdapterConfig not defined!');
  if (!(dojoAmdAdapterConfig.testMidFormatter instanceof Function))
    throw new Error('dojoAmdAdapterConfig.testMidFormatter is not a function!');


  /**
   * Convert's a Javascript object Friendly JSON representation.
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
   * @const
   */
  var DUMMY_TEST_FILE = '/dojoAmdAdapterWait.js';

  var dojoAmdAdapter = {};

  dojoAmdAdapter.log = function() {
    /*var console = jstestdriver.console;
    console.log.apply(console, arguments);*/
  }

  // Variables to store data from the last loading of DUMMY_TEST_FILE
  var lastCallback = null;
  var lastFile = null;

  /**
   * Error callback
   */
  function fail() {
    if (lastFile != null) {
      lastCallback(new jstestdriver.FileResult(lastFile, false,
              'Error loading resource: ' + prettyPrintJSON(arguments)));
      lastFile = null;
    }
  }

  /**
   * Success callback
   */
  function success() {
    if (lastFile != null) {
      lastCallback(new jstestdriver.FileResult(lastFile, true, '', 0));
      lastFile = null;
    }
  }

  // Wiring an error callback for loading problems
  require.on('error', fail);

  // JSTestDriverPlugin that knows how to load the "dummy" resource,
  // so we can pause the execution until everything is loaded.
  jstestdriver.pluginRegistrar.register({
    name: 'waitForDojoAndTestsToBeReady',
    loadSource: function(fileObj, callback) {
      if (fileObj.fileSrc.substr(fileObj.fileSrc.length -
              DUMMY_TEST_FILE.length) === DUMMY_TEST_FILE) {
        lastCallback = callback;
        lastFile = fileObj;
        require(['dojo/text!' + dojoAmdAdapterConfig.testListFile],
                function(fileData) {
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
          var mids = [];
          for (var i = 0, len = data.length; i < len; i++) {
            var mid = dojoAmdAdapterConfig.testMidFormatter(data[i]);
            if (mid)
              mids.push(mid);
          }
          require(mids, success);
        });
        return true;
      }
      return false;
    }
  });

  return dojoAmdAdapter;

})();
