/** @exports dojoAmdPlugin as jstestdriver.dojoAmdPlugin */
/**
 * <p>Dojo Asynchronous Module Definition Plugin for JsTestDriver
 *
 * <p>It aims to allow loading tests and modules to test using the
 * dojo AMD loader. Currently it is not pretty general and it was specially
 * tweaked to the needs of Light.
 *
 * <p>This plugin is known to work with dojo 1.7.2
 *
 * <p>Current assumptions for this plugin to work:
 * <ul>
 * <li> You create a file containing a variable dojoAmdPluginConfig with the
 *      options shown bellow and configure JsTestDriver to load it before
 *      loading this plugin.
 * <pre>
 * var dojoAmdPluginConfig = {
 *  tests: [
 *    '/pathToTestFile1',
 *    '/pathToTestFile2',
 *    // ...
 *  ]
 * };
 * </pre></li>
 * <li> You add the dojo/dojo.js to be loaded with your JsTestDriver</li>
 * <li> You add this file (dojoAmdPlugin.js) to be loaded after dojo with your
 *      JsTestDriver.conf</li>
 * <li> You create a empty file called "dojoAmdPluginWait.js" and make it be loaded
 *      by JSTestDriver after this file (dojoAmdPlugin.js).</li>
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
jstestdriver.dojoAmdPlugin = (function() {

  // Checking some probably mistakes of new users of this plugin
  if (jstestdriver.dojoAmdPlugin)
    throw new Error('dojoAmdPlugin was already defined. AMD Modules ' +
            "aren't currently reloaded, so you need to use --reset.");
  if (!window.dojoAmdPluginConfig)
    throw new Error('dojoAmdPluginConfig not defined!');
  if (!(dojoAmdPluginConfig.tests instanceof Array))
    throw new Error('dojoAmdPluginConfig.tests is not an array!');

  
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
  var DUMMY_TEST_FILE = '/dojoAmdPluginWait.js';

  var dojoAmdPlugin = {};

  dojoAmdPlugin.log = function() {
    var console = jstestdriver.console;
    console.log.apply(console, arguments);
  }

  // Variables to store data from the last loading of /dojoAmdPluginEnd.js
  var lastCallback = null;
  var lastFile = null;

  // Wiring an error callback for loading problems
  require.on('error', function() {
    console.log("Hollyshit!!!!!");
    if (lastFile != null) {
      lastCallback(new jstestdriver.FileResult(lastFile, false, 'require.error: ' + prettyPrintJSON(arguments)));
      lastFile = null;
    }
  });

  jstestdriver.pluginRegistrar.register({
    name: 'waitDojoToBeReady',
    loadSource: function(fileObj, callback) {
      if (fileObj.fileSrc.substr(fileObj.fileSrc.length - DUMMY_TEST_FILE.length) === DUMMY_TEST_FILE) {
        lastCallback = callback;
        lastFile = fileObj;
        require(dojoAmdPluginConfig.tests, function() {
          callback(new jstestdriver.FileResult(fileObj, true, '', 0));
          lastFile = null;
        });
        return true;
      }
      return false;
    }
  });

  return dojoAmdPlugin;

})();
