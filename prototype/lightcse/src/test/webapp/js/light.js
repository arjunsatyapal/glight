define(function(require) {
  var light = require('light');

  jstestdriver.console.log('I was loaded!x');
  describe("bind", function() {
    describe("when provieded a unrelated function and object", function() {
      jstestdriver.console.log('I was executed!');
      it("should return a function that has that object as context", function() {
        var sampleObj = {
          foo: 'bar'
        };
        function sampleFunction() {
          return this.foo;
        }
        
        var binded = light.bind(sampleFunction, sampleObj);
  
        expect(binded()).toEqual("bar");
      });
      it("bla", function() {
        expect(true).toBeTruthy();
      });
    });
    describe("when provieded a related function(method) and object(instance)", function() {
      it("should return a function that has that object as context", function() {
        var otherObj = {
          foo: 'notbar'
        };
        function sampleClass() {
           this.foo = 'bar';
        };
        sampleClass.prototype.sampleFunction = function() { return this.foo; }
        var sampleObj = new sampleClass();

        var binded = light.bind(sampleObj.sampleFunction, sampleObj);

        expect(binded.apply(otherObj)).toEqual('bar');
        
      });
    });
  });
  /*module('light');

  test('bind: Unrelated Function and Object', function() {
    var sampleObj = {
       foo: 'bar'
    };
    function sampleFunction() { return this.foo; }

    var binded = light.bind(sampleFunction, sampleObj);

    equal(binded(), 'bar');

  });

  test('bind: Related Function/Method and Object/Instance', function() {
    var otherObj = {
      foo: 'notbar'
    };
    function sampleClass() {
       this.foo = 'bar';
    };
    sampleClass.prototype.sampleFunction = function() { return this.foo; }
    var sampleObj = new sampleClass();

    var binded = light.bind(sampleObj.sampleFunction, sampleObj);

    equal(binded.apply(otherObj), 'bar');

  });*/

});
