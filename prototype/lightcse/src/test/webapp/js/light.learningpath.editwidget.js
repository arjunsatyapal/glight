define(function(require) {
  var editwidget = require('light.learningpath.editwidget');

  describe('View', function() {
    var view;
    beforeEach(function() {
      spyOn(editwidget.View.prototype, 'initialize');
      view = new editwidget.View();
    });

    describe('syncCollectionAfterOneMove', function() {
      describe('when no move happened', function() {
        it('should do nothing', function() {
          var array = ['A', 'B', 'C'];
          spyOn(view, 'getCurrentClientIdOrder').andReturn(array)
          view.collection = new editwidget.ModuleCollection();
          spyOn(view.collection, 'map').andReturn(array);
          spyOn(view.collection, 'remove');
          spyOn(view.collection, 'add');
          spyOn(view, 'whoMoved').andReturn(null);
          
          view.syncCollectionAfterOneMove();
          
          expect(view.whoMoved).toHaveBeenCalledWith(array, array);
          expect(view.collection.add).not.toHaveBeenCalled();
          expect(view.collection.remove).not.toHaveBeenCalled();
        });
      });
      
      describe('when one move happened', function() {
        it('should move the model', function() {
          var oldArray = ['A', 'B', 'C'];
          var newArray = ['C', 'A', 'B'];
          var model = new editwidget.Module();
          spyOn(view, 'getCurrentClientIdOrder').andReturn(newArray)
          view.collection = new editwidget.ModuleCollection();
          spyOn(view.collection, 'map').andReturn(oldArray);
          spyOn(view.collection, 'remove');
          spyOn(view.collection, 'add');
          spyOn(view.collection, 'getByCid').andReturn(model);
          spyOn(view, 'whoMoved').andReturn('C');
          
          view.syncCollectionAfterOneMove();
          
          expect(view.whoMoved).toHaveBeenCalledWith(oldArray, newArray);
          expect(view.collection.getByCid).toHaveBeenCalledWith('C');
          expect(view.collection.remove).toHaveBeenCalledWith(model);
          expect(view.collection.add).toHaveBeenCalledWith(model, {at: 0});
        });
      });
    });
    
    describe('onCollectionAddModel', function() {
      describe('when we are adding a model to position 3', function() {
        describe('and we are not changing the collection ourselfs', function() {
          it('should add the model to the view in position 3', function() {
            spyOn(view, 'addModel');
            var model = new editwidget.Module();
            view.changingCollection = false;
            
            view.onCollectionAddModel(model, null, {index: 3});
            
            expect(view.addModel).toHaveBeenCalledWith(model, 3);
          });
        });
        describe('and we are changing the collection ourselfs', function() {
          it('should do nothing', function() {
            spyOn(view, 'addModel');
            var model = new editwidget.Module();
            view.changingCollection = true;
            
            view.onCollectionAddModel(model, null, {index: 3});
            
            expect(view.addModel).not.toHaveBeenCalled();
          });
        });
      });
    });
    
    describe('onCollectionRemoveModel', function() {
      describe('when we are removing a model', function() {
        describe('and we are not changing the collection ourselfs', function() {
          it('should add the model to the view in position 3', function() {
            spyOn(view, 'removeModel');
            var model = new editwidget.Module();
            view.changingCollection = false;
            
            view.onCollectionRemoveModel(model);
            
            expect(view.removeModel).toHaveBeenCalledWith(model);
          });
        });
        describe('and we are changing the collection ourselfs', function() {
          it('should do nothing', function() {
            spyOn(view, 'removeModel');
            var model = new editwidget.Module();
            view.changingCollection = true;
            
            view.onCollectionRemoveModel(model);
            
            expect(view.removeModel).not.toHaveBeenCalled();
          });
        });
      });
    });
    
    describe('whoMoved', function() {
      describe('when given a equal lists', function() {
        it('should return null', function() {
          expect(view.whoMoved(
                  ['A', 'B', 'C', 'D'], ['A', 'B', 'C', 'D'])).toBe(null);
        });
      });
      
      describe('when given a list with a adjacent move', function() {
        it('should return the element that moved up', function() {
          expect(view.whoMoved(
                  ['A', 'B', 'C', 'D'], ['A', 'C', 'B', 'D'])).toBe('C');
          expect(view.whoMoved(
                  ['A', 'B', 'C', 'D'], ['B', 'A', 'C', 'D'])).toBe('B');
          expect(view.whoMoved(
                  ['A', 'B', 'C', 'D'], ['A', 'B', 'D', 'C'])).toBe('D');
        });
      });
      
      describe('when given a list with one element moved up', function() {
        it('should return that element', function() {

          expect(view.whoMoved(
                  ['A', 'B', 'C', 'D'], ['D', 'A', 'B', 'C'])).toBe('D');
          
          expect(view.whoMoved(
                  ['A', 'B', 'C', 'D'], ['A', 'D', 'B', 'C'])).toBe('D');
          
          expect(view.whoMoved(
                  ['A', 'B', 'C', 'D'], ['C', 'A', 'B', 'D'])).toBe('C');

        });
      });
      
      describe('when given a list with one element moved down', function() {
        it('should return that element', function() {

          expect(view.whoMoved(
                  ['A', 'B', 'C', 'D'], ['B', 'C', 'D', 'A'])).toBe('A');
          
          expect(view.whoMoved(
                  ['A', 'B', 'C', 'D'], ['B', 'C', 'A', 'D'])).toBe('A');
          
          expect(view.whoMoved(
                  ['A', 'B', 'C', 'D'], ['A', 'C', 'D', 'B'])).toBe('B');

        });
      });
      
      
    });

  });

});
