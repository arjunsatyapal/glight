/*
a * Copyright (C) Google Inc.
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
define(['dojo/_base/Deferred'], function(Deferred) {
  // TODO(waltercacau): Add Test
  /**
   * This promise is resolved when the given promises
   * are done (with error or without).
   * 
   * This promise only fails when it is cancelled.
   */
  return {
    _getResult: function(promise, results, resultIdx, dfd) {
      promise.then(function(result) {
        results[resultIdx] = result;
        dfd._promisesLeft--;
        if(dfd._promisesLeft === 0) {
          dfd.resolve(results);
        }
      }, function(err) {
        if(!err instanceof Error) {
          throw new Error('Assertion failed: Error callback called without some instance of Error');
        }
        results[resultIdx] = err;
        dfd._promisesLeft--;
        if(dfd._promisesLeft === 0) {
          dfd.resolve(results);
        }
      });
    },
    of: function() {
      var promises = Array.prototype.slice.apply(arguments, [0]);
      var promisesLen = promises.length;
      
      var dfd = new Deferred(function() {
        for(var i=0; i<promisesLen; i++) {
          promises.cancel();
        }
        var err = new Error('Cancelled');
        err.lightCancelled = true;
        err.results = results;
        return err;
      });
      
      var results = [];
      dfd._promisesLeft = promisesLen;
      for(var idx=0; idx < promisesLen; idx++) {
        results.push(null);
        this._getResult(promises[idx], results, idx, dfd);
      }
      return dfd;
    }
  };
});