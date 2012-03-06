define(['dojo/_base/declare', 'dojo/hash'], function(declare, dojoHash) {
  /**
   * Small wrapper around dojo's hash functionality to enable better testing
   * @class
   * @name light.URLHashUtil
   */
  return {
    /** @lends light.URLHashUtil */

    get: function() {
      return dojoHash();
    },
    set: function(hash) {
      return dojoHash(hash);
    }
  };
});
