goog.provide('goog.light');
goog.require('goog.dom');

goog.light.sayHello = function(message) {
  goog.dom.getElement('hello').innerHTML = message;
};
