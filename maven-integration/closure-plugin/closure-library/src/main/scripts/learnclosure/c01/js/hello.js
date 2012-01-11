goog.provide('goog.light');
goog.require('goog.dom');


/**
 * @param {string} message Message to be displayed.
 */
goog.light.sayHello = function(message) {
  goog.dom.getElement('hello').innerHTML = message;
};
