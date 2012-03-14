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
define(['dojo/query', 'light/views/AbstractLightView'],
        function($, AbstractLightView) {
  /**
   * Utility Methods for testing UI.
   * @class
   * @name lightTest.robot
   */
  var robot = {
    /** @lends lightTest.robot */

    /**
     * Array to hold the current stuff added to the DOM for tests.
     * <p>After each test, this array will be used for cleanup.
     */
    _attached: [],

    /**
     * A collection of functions that translate dojo utility classes into
     * the corresponding DOM element to which events should be fired.
     *
     * <p>Eg. dijit.form.Button listens to events on a span with
     * class dijitButtonNode
     */
    _toDomElementForClass: {
      'dijit.form.Button': function(obj) {
        return $('.dijitButtonNode', obj.domNode)[0];
      },
      'dijit.form.ValidationTextBox': function(obj) {
        return $('input', obj.domNode)[0];
      },
      'dijit.form.TextBox': function(obj) {
        return $('input', obj.domNode)[0];
      },
      'dijit.form.CheckBox': function(obj) {
        return $('input', obj.domNode)[0];
      },
      'dijit.form.Textarea': function(obj) {
        return $('textarea', obj.domNode)[0];
      }
    },

    /**
     * Attaches the corresponding DOM element for an object
     * to the DOM for testing.
     *
     * <p>Currently it accepts only DOM elements or light's views
     * as first parameter. For light's views, it uses the .domNode
     * property.
     * <p>Attached elements will be cleaned up after each test.
     *
     * @param {*} obj Object that corresponds to a DOM element.
     */
    attach: function(obj) {
      if (typeof obj != 'object')
        throw new Error('Unsupported type for attach: ' + obj);

      if (obj instanceof HTMLElement) {
        document.body.appendChild(obj);
        this._attached.push(obj);
      } else if (obj.isInstanceOf && obj.isInstanceOf(AbstractLightView)) {
        document.body.appendChild(obj.domNode);
        this._attached.push(obj);
      } else {
        throw new Error('Unsupported object: ' + obj);
      }
    },

    /**
     * Detaches current attached elements.
     *
     * <p>It should be called after each test.
     */
    detachAll: function() {
      for (var i = 0, len = this._attached.length; i < len; i++) {
        var obj = this._attached[i];
        if (obj instanceof HTMLElement) {
          document.body.removeChild(obj);
        }
        if (obj.isInstanceOf && obj.isInstanceOf(AbstractLightView)) {
          obj.destroyRecursive(false);
        }
      }
      this._attached.length = 0;
    },

    /**
     * Convert's an object to the the corresponding DOM element
     * that listens for events on behalf of that object.
     *
     * @param {Object} obj Object that corresponds to a DOM element.
     * @return {HTMLElement} The corresponding DOM element.
     * @see lightTest.robot._toDomElementForClass
     */
    _toDomElement: function(obj) {
      if (obj) {
        if (obj instanceof HTMLElement)
          return obj;
        AbstractLightView;
        if (obj.declaredClass && this._toDomElementForClass[obj.declaredClass])
          return this._toDomElementForClass[obj.declaredClass](obj);
      }
      throw new Error('Unsupported object: ' + obj);
    },

    /**
     * Creates a promise object according to the
     *
     * @see http://wiki.commonjs.org/wiki/Promises/A
     * @return {Object} Return's a promise object with an extra
     *     property called fulfill that will fulfill this promise.
     */
    _createPromise: function() {
      var isDone = false;
      var callback = null;

      return {
        then: function(cb) {
          if (isDone)
            cb();
          else
            callback = cb;
        },
        fulfill: function() {
          isDone = true;
          if (typeof callback == 'function') {
            callback();
            callback = null;
          }
        }
      };
    },


    /**
     * Simulates a click action for testing.
     *
     * @param {Object} obj Object that corresponds to a DOM element.
     * @return {Object} A promise object that will be fulfilled when the
     *          corresponding event has been fired and it's callbacks
     *          processed. See {@link lightTest.robot._createPromise}.
     */
    click: function(obj) {
      var promise = this._createPromise();
      Syn.click(this._toDomElement(obj), promise.fulfill);
      return promise;
    },

    /**
     * Simulates typing events for testing.
     *
     * @param {Object} obj Object that corresponds to a DOM element.
     * @param {string} text Text to be typed.
     * @return {Object} A promise object that will be fulfilled when the
     *          corresponding event has been fired and it's callbacks
     *          processed. See {@link lightTest.robot._createPromise}.
     */
    type: function(obj, text) {
      var promise = this._createPromise();
      Syn.type(this._toDomElement(obj), promise.fulfill);
      return promise;
    }

  };

  // Registering with jasmine to cleanUp after each test.
  afterEach(function() {
    robot.detachAll();
  });
  return robot;
});
