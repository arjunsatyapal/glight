goog.provide('goog.light');
goog.require('goog.dom');
goog.require('goog.light.templates');


/**
 * @param {string} message message to be displayed to the user.
 */
goog.light.sayHello = function(message) {
    var data = {
        greeting: message,
        year: new Date().getFullYear()
    };
    var html = goog.light.templates.welcome(data);
    goog.dom.getElement('hello').innerHTML = html;
};
