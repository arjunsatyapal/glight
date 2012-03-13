define(['dojo/query', 'light/AbstractLightView'], 
        function($, AbstractLightView) {
  /**
   * Utility Methods for testing UI.
   */
  var robot = {
    _attached: [],
    _toDomElementForClass: {
      'dijit.form.Button': function(obj) {
        return $('.dijitButtonNode', obj.domNode)[0];
      }
    },
    attach: function(obj) {
      if(obj) {
        if(obj instanceof HTMLElement) {
          document.body.appendChild(obj);
          this._attached.push(obj);
          return;
        }
        if(obj.isInstanceOf && obj.isInstanceOf(AbstractLightView)) {
          document.body.appendChild(obj.domNode);
          this._attached.push(obj);
          return;
        }
      }
      throw new Error("Unsupported object: " + obj);
    },
    detachAll: function() {
      for(var i = 0, len = this._attached.length; i < len; i++) {
        var obj = this._attached[i];
        if(obj instanceof HTMLElement) {
          document.body.removeChild(obj);
        }
        if(obj.isInstanceOf && obj.isInstanceOf(AbstractLightView)) {
          obj.destroyRecursive(false);
        }
      }
      this._attached.length = 0;
    },
    _toDomElement: function(obj) {
      if(obj) {
        if(obj instanceof HTMLElement)
          return obj;
        AbstractLightView
        if(obj.declaredClass && this._toDomElementForClass[obj.declaredClass])
          return this._toDomElementForClass[obj.declaredClass](obj);
      }
      throw new Error("Unsupported object: " + obj);
    },
    _createPromise: function() {
      var isDone = false;
      var callback = null;
      
      return {
        then: function(cb) {
          if(isDone)
            cb();
          else
            callback = cb;
        },
        fulfil: function() {
          isDone = true;
          if(typeof callback == 'function') {
            callback();
            callback = null;
          }
        }
      }
    },
    click: function(element) {
      var promise = this._createPromise();
      Syn.click(this._toDomElement(element), promise.fulfil);
      return promise;
    }
  };
  afterEach(function() {
    robot.detachAll();
  });
  return robot;
});