define(function(require) {
  
  var $ = require('jquery-ui');

  /**
   * @exports editwidget as light.learningpath.editwidget
   * @namespace
   */
  var editwidget = {};

  /**
   * EditWidget for LearningPath's Presenter
   *
   * @param {light.learningpath.editwidget.View}
   *          view Corresponding View.
   * @param {light.dispatch.Dispatcher}
   *          dispatcher Dispatcher to be used to communicate with the server.
   * @constructor
   */
  editwidget.Presenter = function(view, dispatcher) {
    this.view = view;
    this.dispatcher = dispatcher;
  }

  editwidget.View = function() {}
  
  editwidget.View.prototype.decorate = function(el) {
    this.element = el;
    var el = $(el);
    el.sortable({ placeholder: 'ui-state-highlight', forcePlaceholderSize: true });
    el.disableSelection();
  }

  return editwidget;
});
