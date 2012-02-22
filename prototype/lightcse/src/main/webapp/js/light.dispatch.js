/** @exports dispatch as light.dispatch */
define(function(require) {

  /**
   * @namespace
   */
  var dispatch = {};

  /**
   * Take's care of comunicating with the server.
   * @constructor
   */
  dispatch.Dispacher = function() {}

  /**
   * Dispatch a certain action to our server
   * @param {light.actionType} action The actionType.
   * @param {Object} data The data supplied with the action.
   * @param {Function} callback The callback that will be called with the
   *    result of the action when it ends successfully.
   */
  dispatch.Dispacher.prototype.execute = function(action, data, callback) {
    throw new Error('Not implemented yet');
  };

  /**
   * Enum for actions
   * @enum string
   */
  dispatch.actionType = {
    GET_LEARNINGPATH: '/learningpath/get'
  };

});
