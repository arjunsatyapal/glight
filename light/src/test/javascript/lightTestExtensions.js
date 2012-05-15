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
 * This file contains some patches/extensions to some testing frameworks
 * to make Light tests easier.
 */
(function() {
  /*
   * Overriding jasmine dummy specFilter so we can run only
   * a subset of the tests using the queryString ?spec=.
   * This method was actually borrowed from the following file:
   * https://github.com/searls/jasmine-maven-plugin/blob/cdd2091be640f323a2f7e56c932a1e07de9d8830/src/main/resources/vendor/js/jasmine-html.js
   *
   * <p>This implementation for a specFilter is actually used by the
   * jasmine-maven-plugin, but for some reason it is limited to filter
   * only the Jasmine Reporter (viewing of results), not the actual
   * execution of tests.
   * 
   * <p>Example of use: If you spec fullname is ABC and you put a querystring
   * ?spec=A, your spec will be executed. Same happens if you use ?spec=AB.
   * But if you use ?spec=X or ?spec=AX the ABC spec will not be executed.
   */
  jasmine.getEnv().specFilter = function(spec) {
    var paramMap = {};
    var params = window.location.search.substring(1).split('&');
    for (var i = 0; i < params.length; i++) {
      var p = params[i].split('=');
      paramMap[decodeURIComponent(p[0])] = decodeURIComponent(p[1]);
    }

    if (!paramMap.spec) {
      return true;
    }
    return spec.getFullName().indexOf(paramMap.spec) === 0;
  };

  /*
   * Patching the methods jasmine.Spec.prototype.waitsFor
   * and jasmine.WaitsForBlock.prototype.execute
   * to accept javascript promises.
   */
  jasmine.Spec.prototype.waitsFor = function(latchFunction, optional_timeoutMessage, optional_timeout) {
    var latchFunction_ = null;
    var optional_timeoutMessage_ = null;
    var optional_timeout_ = null;

    for (var i = 0; i < arguments.length; i++) {
      var arg = arguments[i];
      switch (typeof arg) {
        // LightMod: Adding support for promises
        case 'object':
          if (typeof arg.then != 'function') {
            throw new Error('Object to wait for is not a promise!');
          }
          latchFunction_ = arg;
          break;
        case 'function':
          latchFunction_ = arg;
          break;
        case 'string':
          optional_timeoutMessage_ = arg;
          break;
        case 'number':
          optional_timeout_ = arg;
          break;
      }
    }

    var waitsForFunc = new jasmine.WaitsForBlock(this.env, optional_timeout_, latchFunction_, optional_timeoutMessage_, this);
    this.addToQueue(waitsForFunc);
    return this;
  };

  jasmine.WaitsForBlock.prototype.execute = function(onComplete) {
    if (jasmine.VERBOSE) {
      this.env.reporter.log('>> Jasmine waiting for ' + (this.message || 'something to happen'));
    }

    // LightMod: Adding support for promises
    if (typeof this.latchFunction == 'object') {
      var failFunc = function() {
        this.spec.fail(new Error('Promise failed'));
        onComplete();
      };
      try {
        this.latchFunction.then(onComplete, failFunc);
      } catch (e) {
        this.spec.fail(e);
        onComplete();
      }
      return;
    }

    var latchFunctionResult;
    try {
      latchFunctionResult = this.latchFunction.apply(this.spec);
    } catch (e) {
      this.spec.fail(e);
      onComplete();
      return;
    }

    if (latchFunctionResult) {
      onComplete();
    } else if (this.totalTimeSpentWaitingForLatch >= this.timeout) {
      var message = 'timed out after ' + this.timeout + ' msec waiting for ' + (this.message || 'something to happen');
      this.spec.fail({
        name: 'timeout',
        message: message
      });

      this.abort = true;
      onComplete();
    } else {
      this.totalTimeSpentWaitingForLatch += jasmine.WaitsForBlock.TIMEOUT_INCREMENT;
      var self = this;
      this.env.setTimeout(function() {
        self.execute(onComplete);
      }, jasmine.WaitsForBlock.TIMEOUT_INCREMENT);
    }
  };

})();
