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
define(['dojo/_base/connect'],
        function(connect) {
    /**
     * Wraps dojo pubsub system to provide extra hooks/functionality.
     *
     * TODO(waltercacau): add test for this class
     *
     * @class
     * @name light.utils.PubSubUtils
     */
    var PubSubUtils = {
      /** @lends light.utils.PubSubUtils */
      _validators: {},
      subscribe: function(topic, context, method) {
        return connect.subscribe.apply(connect, arguments);
      },
      
      /**
       * Tries to publish in the given topic/event. 
       * @return {boolean} False if some validator has forbidden
       *    this operation.
       */
      publish: function(topic, args) {
        if(typeof this._validators[topic] != "undefined") {
          for(var identifier in this._validators[topic]) {
            if(!this._validators[topic][identifier]()) {
              return false;
            }
          }
        }
        connect.publish.apply(connect, arguments);
        return true;
      },
      addPublishValidator: function (topic, identifier, validator) {
        if(typeof this._validators[topic] == "undefined") {
          this._validators[topic] = {};
        }
        this._validators[topic][identifier] = validator;
      },
      removePublishValidator: function(topic, identifier) {
        if(typeof this._validators[topic] != "undefined") {
          delete this._validators[topic][identifier];
        }
      }
    };
    return PubSubUtils;
});
