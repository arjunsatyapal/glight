/**
 * This file was modified to make some hook's possibles with
 * Light testconfig.js and the AmdPlugin.
 * @fileoverview Jasmine JsTestDriver Adapter.
 * @author misko@hevery.com (Misko Hevery)
 * @author olmo.maldonado@gmail.com (Olmo Maldonado)
 */
jstestdriver.JasmineAdapter = (function() {
  
  var prettyPrintJSON = function(obj, indent) {
    indent = indent || "";
    var ownIndent = indent+"  ";
    if(typeof obj == "object") {
      var builder = [];
      if(Array.prototype.isPrototypeOf(obj)) {
        if(obj.length == 0)
          return "[]";
        builder.push("[");
        builder.push("\n");
        for(var i = 0, val, last = obj.length-1; val = obj[i]; i++) {
          builder.push(ownIndent);
          builder.push(prettyPrintJSON(val,ownIndent));
          if(i!=last)
            builder.push(",");            
          builder.push("\n");
        }
        builder.push(indent);
        builder.push("]");
      } else {
        var keys = [];
        for(var key in obj) {
          if(obj.hasOwnProperty(key))
            keys.push(key);
        }
        if(keys.length == 0)
          return "{}";
        builder.push("{");
        builder.push("\n");
        keys.sort();
        for(var i = 0, key, last = keys.length-1; key = keys[i]; i++) {
          builder.push(ownIndent);
          builder.push(key);
          builder.push(": ");
          builder.push(prettyPrintJSON(obj[key],ownIndent));
          if(i!=last)
            builder.push(",");            
          builder.push("\n");
        }
        builder.push(indent);
        builder.push("}");
      }
      return builder.join("");
    } else {
      return JSON.stringify(obj);
    }
  }

  var Env = function(onTestDone, onComplete) {
    jasmine.Env.call(this);

    this.specFilter = function(spec) {
      if (!this.exclusive)
        return true;
      var blocks = spec.queue.blocks, l = blocks.length;
      for ( var i = 0; i < l; i++)
        if (blocks[i].func.exclusive >= this.exclusive)
          return true;
      return false;
    };

    this.reporter = new Reporter(onTestDone, onComplete);
  };
  jasmine.util.inherit(Env, jasmine.Env);

  // Here we store:
  // 0: everyone runs
  // 1: run everything under ddescribe
  // 2: run only iits (ignore ddescribe)
  Env.prototype.exclusive = 0;

  Env.prototype.execute = function() {
    collectMode = false;
    playback();
    jasmine.Env.prototype.execute.call(this);
  };

  var Reporter = function(onTestDone, onComplete) {
    this.onTestDone = onTestDone;
    this.onComplete = onComplete;
    this.reset();
  };
  jasmine.util.inherit(Reporter, jasmine.Reporter);

  Reporter.formatStack =
          function(stack) {
            var line, lines = (stack || '').split(/\r?\n/), l =
                    lines.length, frames = [];
            for ( var i = 0; i < l; i++) {
              line = lines[i];
              if (line.match(/\/jasmine[\.-]/))
                continue;
              frames.push(line.replace(/https?:\/\/\w+(:\d+)?\/test\//,
                      '').replace(/^\s*/, '     '));
            }
            return frames.join('\n');
          };

  Reporter.prototype.reset = function() {
    this.specLog = jstestdriver.console.log_ = [];
  };

  Reporter.prototype.log = function(str) {
    this.specLog.push(str);
  };

  Reporter.prototype.reportSpecStarting = function() {
    this.reset();
    this.start = +new Date();
  };

  Reporter.prototype.reportSpecResults =
          function(spec) {
            var elapsed = +new Date() - this.start, results =
                    spec.results();

            if (results.skipped)
              return;

            var item, state = 'passed', items = results.getItems(), l =
                    items.length, messages = [];
            for ( var i = 0; i < l; i++) {
              item = items[i];
              if (item.passed())
                continue;
              state =
                      (item.message.indexOf('AssertionError:') != -1) ? 'error'
                              : 'failed';
              messages.push({
                message: item + '',
                name: item.trace.name,
                stack: Reporter.formatStack(item.trace.stack)
              });
            }
            
            this.onTestDone(new jstestdriver.TestResult(spec.suite
                    .getFullName(), spec.description, state, prettyPrintJSON(messages), this.specLog.join('\n'),
                    elapsed));
          };

  Reporter.prototype.reportRunnerResults = function() {
    this.onComplete();
  };

  var collectMode = true, intercepted = {};

  describe = intercept('describe');
  beforeEach = intercept('beforeEach');
  afterEach = intercept('afterEach');

  var JASMINE_TYPE = 'jasmine test case';
  TestCase('Jasmine Adapter Tests', null, JASMINE_TYPE);

  var defaultPlugin =
          jstestdriver.pluginRegistrar.getPlugin('defaultPlugin');
  jstestdriver.pluginRegistrar.unregister(defaultPlugin);
  jstestdriver.pluginRegistrar
          .register({

            name: 'jasmine',

            getTestRunsConfigurationFor: function(testCaseInfos,
                    expressions, testRunsConfiguration) {
              for ( var i = 0; i < testCaseInfos.length; i++) {
                if (testCaseInfos[i].getType() == JASMINE_TYPE) {
                  testRunsConfiguration
                          .push(new jstestdriver.TestRunConfiguration(
                                  testCaseInfos[i], []));
                }
              }
              return false; // allow other TestCases to be collected.
            },

            runTestConfiguration: function(config, onTestDone,
                    onComplete) {
              if (config.getTestCaseInfo().getType() != JASMINE_TYPE)
                return false;
              (jasmine.currentEnv_ = new Env(onTestDone, onComplete))
                      .execute();
              return true;
            },

            onTestsFinish: function() {
              jasmine.currentEnv_ = null;
              collectMode = true;
            }

          });
  jstestdriver.pluginRegistrar.register(defaultPlugin);

  function intercept(method) {
    var bucket = intercepted[method] = [], method = window[method];
    return function(desc, fn) {
      if (collectMode)
        bucket.push(function() {
          method(desc, fn);
        });
      else
        method(desc, fn);
    };
  }

  function playback() {
    for ( var method in intercepted) {
      var bucket = intercepted[method];
      for ( var i = 0, l = bucket.length; i < l; i++)
        bucket[i]();
    }
  }

  return {
    clearTests: function() {
      collectMode = true;
      for ( var method in intercepted) {
        intercepted[method].length = 0;
      }
    },
    isCollectMode: function() {
      return collectMode;
    }
  }

})();

var ddescribe = function(name, fn) {
  var env = jasmine.getEnv();
  if (!env.exclusive)
    env.exclusive = 1; // run ddescribe only
  describe(name, function() {
    var oldIt = it;
    it = function(name, fn) {
      fn.exclusive = 1; // run anything under ddescribe
      env.it(name, fn);
    };

    try {
      fn.call(this);
    } finally {
      it = oldIt;
    }
    ;
  });
};

var iit = function(name, fn) {
  var env = jasmine.getEnv();
  env.exclusive = fn.exclusive = 2; // run only iits
  env.it(name, fn);
};

// Patch Jasmine for proper stack traces
jasmine.Spec.prototype.fail = function(e) {
  var result = new jasmine.ExpectationResult({
    passed: false,
    message: e ? jasmine.util.formatException(e) : 'Exception'
  });
  if (e)
    result.trace = e;
  this.results_.addResult(result);
};