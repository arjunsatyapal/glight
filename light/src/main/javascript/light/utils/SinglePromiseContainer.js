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
define(['dojo/_base/declare'], function(declare) {

  /**
   * Guarantees that only a single promise will be pending.
   *
   * @class
   * @name light.utils.SinglePromiseContainer
   */
  return declare('light.utils.SinglePromiseContainer', null, {
    _lastPromise: null,

    /**
     * Handles the creation of a new promise.
     * If there is a pending promise, it will be cancelled and replaced
     * by the one computed by func.
     *
     * <p> Note that instead of passing a function, one could think this function
     * could be designed as receiving a new promise and canceling the older one.
     * The problem is some promises generators that perform some kind of caching of
     * running promises might not work if we cancel the running promise after
     * creating the new one. For example, a XHR library could return the same promise
     * for two calls of a GET in the same URL. If we received the new promise and then
     * cancelled the old one we would be canceling the new one too.
     *
     * @param {Function} func Function that computes and returns a new promise.
     * @return {dojo.Deferred} The given promise.
     */
    handle: function(func) {
      if (this._lastPromise !== null) {
        this._lastPromise.cancel();
      }
      this._lastPromise = func();
      return this._lastPromise;
    }
  });
});
