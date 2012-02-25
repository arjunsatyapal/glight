define(function(require) {

  var $ = require('jquery');

  /**
   * @name light
   * @namespace
   */
  var light = {};

  /**
   * Guarantees the existence of a subnamespace inside the light
   * object.
   * @param  {string} name The subnamespace.
   *     Example: If name is "module.something" it is guaranteed that
   *     light.module and light.something would exist without overriding
   *     any other previous declaration.
   * @return {Object} Return's the namespaced created so you can use a local
   *     alias.
   */
  /*
  light.namespace = function(name) {
    var parts = name.split('.');
    var last = light;
    for (var i = 0, len = parts.length; i < len; i++) {
      last = last[parts[i]] = last[parts[i]] || {};
    }
    return last;
  }*/

  /**
   * Log's the given arguments using console.log if available.
   */
  light.log = function() {
    if (window['console']) {
      console.log.apply(console, arguments);
    }
  };

  /**
   * Inherit the prototype methods from one constructor into another.
   *
   * Usage:
   * <pre>
   * function ParentClass(a, b) { }
   * ParentClass.prototype.foo = function(a) { }
   *
   * function ChildClass(a, b, c) {
   *   ParentClass.call(this, a, b);
   * }
   *
   * goog.inherits(ChildClass, ParentClass);
   *
   * var child = new ChildClass('a', 'b', 'see');
   * child.foo(); // works
   * </pre>
   *
   * In addition, a superclass' implementation of a method can be invoked
   * as follows:
   *
   * <pre>
   * ChildClass.prototype.foo = function(a) {
   *   ChildClass.superClass_.foo.call(this, a);
   *   // other code
   * };
   * </pre>
   *
   * Source: Clousure Library
   *
   * @param {Function} childCtor Child class.
   * @param {Function} parentCtor Parent class.
   */
  light.inherits = function(childCtor, parentCtor) {
    function tempCtor() {
    };
    tempCtor.prototype = parentCtor.prototype;
    childCtor.superClass_ = parentCtor.prototype;
    childCtor.prototype = new tempCtor();
    childCtor.prototype.constructor = childCtor;
  };

  /**
   * Return's a binded version of obj.func, which means
   * that you can call it with a different "this" argument and
   * it would work.
   * @param {Function} func Function to be binded.
   * @param {Object} obj Object to be binded to.
   */
  light.bind = function(func, obj) {
    return $.proxy(func, obj);
  }

  return light;
});