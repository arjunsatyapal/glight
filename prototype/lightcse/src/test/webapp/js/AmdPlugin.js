/** @exports amdPlugin as jstestdriver.AmdPlugin */
/**
 * Asynchronous Module Definition Plugin for JsTestDriver
 *
 * Assumptions for this plugin to work:
 * <ul>
 * <li> You configure amdPlugin.js to run before all files that might use the
 *   define function</li>
 * <li> You configure amdPluginEnd.js to run after all files that might
 *      use the define function</li>
 * <li> There is not a file in your test run that as the same name specified
 *      in dummyTestsFileName variable</li>
 * <li> You are not using RequireJS relative path's</li>
 * </ul>
 * @namespace
 */
jstestdriver.AmdPlugin = (function() {

  var amdPlugin = {};

  /**
   * Utility function for logging in the browser
   */
  amdPlugin.log = function() {
    if (window['console']) {
      window.console.log.apply(window.console, arguments);
    }
    jstestdriver.console.log.apply(jstestdriver.console, arguments);
  }
  
  /**
   * Stores the current module name being evaluated.
   */
  amdPlugin.currentModuleName = null;

  /**
   * A dummy file name that we are going to use to associate tests inside
   * AMD's. This way, every time a reload is necessary we just remove all
   * test's from JsTestDriver associated to that particular dummy file
   * @type string
   */
  var dummyTestsFileName = 'amdPluginStub.js';

  /**
   * Represent's a module definition
   * @param {string} name Module's name.
   * @constructor
   */
  amdPlugin.Module = function(name) {
    this.name = name;

    /**
     * Dependencies' names.
     * @type {Array}
     */
    this.deps = [];

    /**
     * The definition callback.
     * @type Function
     */
    this.callback = function() {};

    /**
     * The content (Result of this.callback([... deps content's ...])).
     * @type *
     */
    this.content = undefined;

    /**
     * The fileObj with data about where it was defined.
     * @type object
     */
    this.fileObj = null;

    this.isDefined = false;
  }

  /**
   * Rewrite's a module's name before it's definition is stored.
   *
   * This method is called whenever an anonymous module definition
   * is done and we have to guess to which name it's mapped to.
   *
   * It's recommended that you override this for your needs.
   * @param {string} name Guessed module name.
   * @return {string} The definitive module name used for that file.
   */
  amdPlugin.rewriteModuleName = function(name) {
    return name;
  }

  /**
   * Get the content's of this module and if necessary create's the
   * content by calling the callback function if the appropriate dependencies.
   */
  amdPlugin.Module.prototype.getContents = function() {
    if (!this.isDefined) {
      this.isDefined = true;
      var depsContent = [];
      for (var i = 0, len = this.deps.length; i < len; i++) {
        depsContent.push(modules[this.deps[i]].getContents());
      }
      amdPlugin.currentModuleName = this.name;
      this.content = this.callback.apply(window, depsContent);
      amdPlugin.currentModuleName = null;
    }
    return this.content;
  }

  /**
   * Reset this module dealocating it's contents.
   */
  amdPlugin.Module.prototype.reset = function() {
    this.isDefined = false;
    this.content = undefined;
  }

  /**
   * Maps modules' names into module's.
   * @type Object.<string, jstestdriver.AmdPlugin.Module>
   */
  var modules = amdPlugin.modules = {};

  /**
   * Return's the content's of a module with the given name.
   * @param  {string} name Module's name.
   * @return {*} Module's content.
   */
  amdPlugin.require = function(name) {
    var module = modules[name];
    if (!module)
      newError('No module named ' + name);

    if (module.isDefined)
      return module.getContents();
    else
      newError('Module ' + name + " wasn't loaded yet!");

  }

  /**
   * The "require" module which wrap's the require function.
   */
  modules['require'] = new amdPlugin.Module('require');
  modules['require'].callback = function() { return amdPlugin.require; }

  /**
   * Map's file's sources into modules' names defined in each file.
   * @type {[type]}
   */
  var fileSrcToModuleNames = amdPlugin.fileSrcToModuleNames = {};
  var errorBuffer = [];

  /**
   * Utility custom error so we can check for it when catching.
   * @param {string} message The error message.
   * @constructor
   */
  function AmdPluginError(message) {
      this.message = message;
  }
  AmdPluginError.prototype.toString = function() {
    if (this.message)
      return 'AmdPluginError: ' + this.message;
    else
      return 'AmdPluginError';
  }

  /**
   * Utility function for raising error's and storing them
   * in a error buffer, whose content's will latter be shown
   * to the user in the form of a failing test.
   * @param  {string} msg Error message.
   * @throws {AmdPluginError}
   */
  function newError(msg) {
    errorBuffer.push(msg);
    throw new AmdPluginError(msg);
  }

  /**
   * This is a dummy test case that we use to comunucate problems
   * to the user. If any error's happen, this TestCase will have one
   * falling testing that show's the error's.
   * @type jsTestDriver.TestCase
   */
  var reportTestCase = TestCase('amdPlugin');
  reportTestCase.prototype.testNoLoadingErrors = function() {
    if (errorBuffer.length > 0) {
      var msg = errorBuffer.join(' / ');
      errorBuffer.length = 0;
      fail(msg);
    }
  }

  /**
   * This function return's a list of module's names in a topological order
   * (any module does not depend on any following module's on the list).
   *
   * @return {Arrau.<string>} Module's name in a topolocial order or an empty
   *                          list in case any problem happens.
   */
  function topologicalSort() {
    var stack = [];
    var onstack = {};
    var visited = {};
    var list = [];
    try {
      for (var name in modules) {
        topologicalSortDfs(name, stack, onstack, visited, list);
      }
    } catch (e) {
      if (!(e instanceof AmdPluginError)) {
        throw e;
      }
    }
    return list;
  }

  /**
   * That's an auxiliary function used by the previous one.
   * It make's a Deep First Search following the dependency graph and
   * adding modules to the list. It also tries to detect problem's like
   * missing dependencies and cycles.
   *
   * @param  {string} name    Module's name.
   * @param  {Object} stack   The current path in the dependency tree.
   * @param  {Object} onstack Map's to true modules' names that
   *     already appered in the current path.
   * @param  {Object} visited Map's to true modules' names that
   *     have already been visited.
   * @param  {Array.<string>} list    [description].
   */
  function topologicalSortDfs(name, stack, onstack, visited, list) {
    var module = modules[name];
    if (!module) {
      newError('Depdency ' + name + ' missing for ' + stack[stack.length - 1]);
    }
    if (onstack[name]) {
      newError('Found dependency cycle! Current stack: ' + stack.join(', ') +
          ' -> ' + name);
    }
    if (visited[name])
      return;

    visited[name] = true;
    stack.push(name);
    onstack[name] = true;


    for (var i = 0, len = module.deps.length; i < len; i++) {
      topologicalSortDfs(module.deps[i], stack, onstack, visited, list);
    }

    stack.pop(name);
    onstack[name] = false;

    list.push(name);
  }
  
  /**
   * Count's how many files where loaded between the first load and first call
   * of jstestdriver.AmdPlugin.finishedLoading or between two consecutive
   * call's of this function.
   * 
   * That allows us to optimze and only process dependencies if a file was actually
   * loaded.
   * @type number
   */
  var lastLoadedFilesCount = 0;

  /**
   * Callback function called when we know that all Javascript File's have been
   * loaded and we can process the dependency graph.
   */
  amdPlugin.finishedLoading = function() {
    
    // No new files? Just skip processing the dependencies
    amdPlugin.log("Finished loading? ",lastLoadedFilesCount)
    if(lastLoadedFilesCount == 0)
      return;
    
    lastLoadedFilesCount = 0;
    
    jstestdriver.testCaseManager.removeTestCaseForFilename(dummyTestsFileName);
    if(jstestdriver.JasmineAdapter)
      jstestdriver.JasmineAdapter.clearTests();

    // Reseting all modules
    for (var name in modules) {
      modules[name].reset();
    }

    var orderdList = topologicalSort();

    // Building the content of modules by requesting them
    for (var i = 0, len = orderdList.length; i < len; i++) {
      modules[orderdList[i]].getContents();
    }

    //amdPlugin.firstEndingTime = function() {}
    //amdPlugin.processQueue();
    /*amdPlugin.log();
    for (var name in modules) {
      var module = modules[name];
      amdPlugin.log('Running', name);
      module.callback();
    }*/
    jstestdriver.testCaseManager.updateLatestTestCase(dummyTestsFileName);

  }

  // Intercept loadSource call's in the pluginRegister so we can know what
  // is the current file being loaded

  var lastFileObj = null;

  amdPlugin.onFileStartLoad = function(fileObj) {
    amdPlugin.log('Loading ...', fileObj.fileSrc);
    lastLoadedFilesCount++;
    lastFileObj = fileObj;

    // Removing old modules
    var oldModules = fileSrcToModuleNames[fileObj.fileSrc];
    if (oldModules) {
      for (var i = oldModules.length - 1; i >= 0; i--) {
        delete modules[oldModules[i]];
      }
    }
    fileSrcToModuleNames[fileObj.fileSrc] = [];
  };

  function getCurrentFileObj() {
    return lastFileObj;
  };

  /**
   * Creates a wrapper of the function obj.name that before
   * calling the original obj.name function call's the given spy.
   * @param {Object} obj Object containing the function.
   * @param {string} name Name of the function.
   * @param {Function} spy Spy.
   */
  amdPlugin.wrapFunction = function(obj, name, spy) {
    var original = obj[name];
    if (original.amdPluginWrappedOriginal)
      original = original.amdPluginWrappedOriginal;
    obj[name] = function() {
      spy.apply(this, arguments);
      return original.apply(this, arguments);
    }
    obj[name].amdPluginWrappedOriginal = original;
  }
  
  /**
   * 
   */
  /*function wrapIntoJasmineDescribeForModule(name, func) {
    return function() {
      var _arguments = arguments;
      var _this = this;
      describe(name, function() {
        return func.apply(_this, _arguments);
      });
    }
  }*/

  /*
    ==========================================================
    Extracted from requireJS 1.0.5
    http://requirejs.org/

    Note: there are some modifications that are highlighted
    by one comment like this.
    ==========================================================
  */
  var commentRegExp = /(\/\*([\s\S]*?)\*\/|([^:]|^)\/\/(.*)$)/mg,
      cjsRequireRegExp = /require\(\s*["']([^'"\s]+)["']\s*\)/g,
      ostring = Object.prototype.toString;
  function isArray(it) {
      return ostring.call(it) === '[object Array]';
  }
  function isFunction(it) {
      return ostring.call(it) === '[object Function]';
  }
  define = function(name, deps, callback) {

      //Allow for anonymous functions
      if (typeof name !== 'string') {
          //Adjust args appropriately
          callback = deps;
          deps = name;
          name = null;
      }

      //This module may not have dependencies
      if (!isArray(deps)) {
          callback = deps;
          deps = [];
      }

      //If no name, and callback is a function, then figure out if it a
      //CommonJS thing with dependencies.
      if (!deps.length && isFunction(callback)) {
          //Remove comments from the callback string,
          //look for require calls, and pull them into the dependencies,
          //but only if there are function args.
          if (callback.length) {
              callback
                  .toString()
                  .replace(commentRegExp, '')
                  .replace(cjsRequireRegExp, function(match, dep) {
                      deps.push(dep);
                  });

              //May be a CommonJS thing even without require calls, but still
              //could use exports, and module. Avoid doing exports and module
              //work though if it just needs require.
              //REQUIRES the function to expect the CommonJS variables in the
              //order listed below.
              deps = (callback.length === 1 ? ['require'] :
                  ['require', 'exports', 'module']).concat(deps);
          }
      }

      /*
        ==========================================================
        This part is modded
        ==========================================================
      */
      
      var fileObj = getCurrentFileObj();

      if (!fileObj)
        newError(
          'Could not associate the current define call with a loaded file!'
        );

      if (!name) {
        name = fileObj.fileSrc;
        if (name.indexOf('/test/') === 0) {
          name = name.substring('/test/'.length);
          if (name.substring(name.length - 3) == '.js') {
            name = name.substring(0, name.length - 3);
          }
          name = amdPlugin.rewriteModuleName(name);
        }
      }
      
      // Making jasmine test's being encapsulated inside the module definitions
      /*if(window.jasmineAdapter) {
        
      }*/

      // Adding the module definition
      var module = modules[name] = new amdPlugin.Module(name);
      module.deps = deps;
      module.callback = callback;
      module.fileObj = fileObj;
      fileSrcToModuleNames[fileObj.fileSrc].push(name);

      /*
        ==========================================================
        End of the modded part
        ==========================================================
      */

      return undefined;
  };
  /*
    ==========================================================
    End of the extracted part from requireJS
    ==========================================================
  */

  // This should allow jquery to associate with it.
  define.amd = {jQuery: {}};

  return amdPlugin;
})();

/**
 * Dirty hack's to make our plugin be able to intercept loading
 * event's.
 */
(function() {

  // Hooking up in the pluginRegister resource loader
  var loadSourceTrackerPlugin = ({
    name: 'loadSourceTracker',
    loadSource: function(fileObj, callback) {
      jstestdriver.AmdPlugin.onFileStartLoad(fileObj);
      return false;
    }
  });

  if (!jstestdriver.pluginRegistrar.getPlugin(loadSourceTrackerPlugin.name)) {
    jstestdriver.AmdPlugin.log('Resgistering: ', loadSourceTrackerPlugin.name);
    var defaultPlugin = jstestdriver.pluginRegistrar.getPlugin('defaultPlugin');
    jstestdriver.pluginRegistrar.unregister(defaultPlugin);
    jstestdriver.pluginRegistrar.register(loadSourceTrackerPlugin);
    jstestdriver.pluginRegistrar.register(defaultPlugin);
  }

  // Hooking up in the manual resource loader
  // Older versions don't seem to use it.
  if (jstestdriver.ManualResourceTracker) {
    jstestdriver.AmdPlugin.log('Hooking up in the manual resource loader');
    jstestdriver.AmdPlugin.wrapFunction(
        jstestdriver.ManualResourceTracker.prototype,
        'startResourceLoad', function(jsonFile) {
      var fileObj = this.parse_(jsonFile);
      jstestdriver.AmdPlugin.onFileStartLoad(fileObj);
    });
  }
  
  // Hooking up in the file loader to know when loading is done so we can
  // process dependencies. This is complemented by the AmdPluginEnd.js
  // because at the first load this wrapping will have no effect.
  jstestdriver.AmdPlugin.wrapFunction(
      jstestdriver.FileLoader.prototype,
      'onFileLoaded_', function() {
        if(this.files_.length == 0) {
          jstestdriver.AmdPlugin.finishedLoading();
        }
      });
      

})();
