define(function(require) {

  var $ = require('jquery-ui');
  var Backbone = require('backbone');

  /**
   * @exports editwidget as light.learningpath.editwidget
   * @namespace
   */
  var editwidget = {};


  editwidget.Module = Backbone.Model.extend({
    title: null,
    url: null
  });

  /*editwidget.Module.prototype.validate = function(attrs) {
    if (attrs.hasOwnProperty('title')) {
      if (typeof attrs.title != 'string')
        return 'Title should be a string';

      if (attrs.title.length == 0)
        return 'Title should not be empty';
    }

    if (attrs.hasOwnProperty('url')) {
      if (typeof attrs.url != 'string')
        return 'URL should be a string';

      if (attrs.url.length == 0)
        return 'URL should not be empty';
    }
  }*/

  editwidget.ModuleCollection = Backbone.Collection.extend({
    model: editwidget.Module
  });

  /**
   * EditWidget View
   * @constructor
   */
  editwidget.View = Backbone.View.extend();

  editwidget.View.prototype.initialize = function() {
    if (!this.hasOwnProperty('collection')) {
      throw new Error('Expected to have a collection set for this view');
    }
    this.collection.on('add', this.onCollectionAddModel, this);
    this.collection.on('remove', this.onCollectionRemoveModel, this);

    /**
     * Boolean for avoiding our handlers to be called on
     * add and remove event's on the collection caused by our own calls.
     * @type boolean
     * @private
     */
    this.changingCollection = false;
  }

  // =================== UI ===================
  /**
   * Data key associated with Dom elements that represent the model.
   * @const
   */
  editwidget.View.CLIENT_ID_DATA_KEY = 'clientId';

  /**
   * Get the current client ids model as represented by the DOM
   * @return {Array} Array of client id's.
   */
  editwidget.View.prototype.getCurrentClientIdOrder = function() {
    var ids = [];
    this.$el.children().each(function() {
      ids.push($(this).data(editwidget.View.CLIENT_ID_DATA_KEY));
    });
    return ids;
  }

  /**
   * Get's the current dom element for the given model or creates
   * one if necessary.
   * @param {light.learningpath.editwidget.Module} model Model.
   * @return {Element} DOM Element for the given model.
   */
  editwidget.View.prototype.getModelElementFor = function(model) {
    var $modelElement = this.$modelElements[model.cid] = $('<div/>');
    $modelElement.text(model.get('title'));
    $modelElement.data(editwidget.View.CLIENT_ID_DATA_KEY, model.cid);
    $modelElement.appendTo(this.$el);
    return $modelElement;
  }

  /**
   * Adds a model at a given position in this view
   * @param {light.learningpath.editwidget.Module} model Model.
   * @param {number=} at Position.
   */
  editwidget.View.prototype.addModel = function(model, at) {
    var $modelElement = this.getModelElementFor(model);
    $modelElement.detach();

    if (at !== undefined) {
      if (at == 0) {
        this.$el.prepend($modelElement);
      } else {
        var $childs = this.$el.children();
        $modelElement.insertAfter($childs.get(at - 1));
      }
    } else {
      this.$el.append($modelElement);
    }
  }

  /**
   * Removes a model at a given position in this view
   * @param {light.learningpath.editwidget.Module} model Model.
   */
  editwidget.View.prototype.removeModel = function(model) {
    var $modelElement = this.$modelElements[model.cid];
    if ($modelElement !== undefined) {
      delete this.$modelElements[model.cid];
      $modelElement.remove();
    }
  }

  /**
   * Render's this view
   */
  editwidget.View.prototype.render = function() {
    this.$el.empty();

    this.$modelElements = {};

    this.collection.each(this.addModel, this);

    this.$el.sortable({
      stop: _.bind(this.syncCollectionAfterOneMove, this),
      placeholder: 'ui-state-highlight',
      forcePlaceholderSize: true
    });
    this.$el.disableSelection();

  }
  
  //=================== Logic ===================
  /**
   * Discovers who moved in an array considering that at most one move
   * happened. It uses the == operator to determine equality.
   *
   * Note that adjacent moves are ambiguous, so we just return the
   * element that moved up (lower index). Eg. whoMoved([1,2], [2,1]) will
   * return 2.
   *
   * @param {Array} oldArray Old array.
   * @param {Array} newArray New array after at most one move.
   * @return {*} the moved element or null if none moved.
   */
  editwidget.View.prototype.whoMoved = function(oldArray, newArray) {
    var i = 0, len = newArray.length;

    // Finding first different point
    for (; i < len && newArray[i] == oldArray[i]; i++);

    // No move ...
    if (i == len)
      return null;

    if (oldArray[i] == newArray[i + 1]) {
      // Is newArray[i] the moved element? (MoveUp or adjacent move)
      return newArray[i];
    } else {
      // Then it's oldArray[i] that moved (MoveDown)
      return oldArray[i];
    }
  }

  /**
   * Syncs the underlining collection represented by this view to have
   * the same order as the one that's being shown after one move (or no move).
   */
  editwidget.View.prototype.syncCollectionAfterOneMove = function() {
    var newArray = this.getCurrentClientIdOrder();
    var oldArray = this.collection.map(function(model) { return model.cid; });
    var movedCid = this.whoMoved(oldArray, newArray);

    // No move ...
    if (movedCid === null)
      return;

    var moved = this.collection.getByCid(movedCid);
    var newIndex = _.indexOf(newArray, movedCid, false);

    this.changingCollection = true;
    this.collection.remove(moved);
    this.collection.add(moved, {at: newIndex});
    this.changingCollection = false;

  }
  
  editwidget.View.prototype.onCollectionAddModel =
    function(model, collection, options) {
    if (!this.changingCollection)
      this.addModel(model, options.index);
  }

  editwidget.View.prototype.onCollectionRemoveModel = function(model) {
    if (!this.changingCollection) {
      this.removeModel(model);
    }
  }

  return editwidget;
});
