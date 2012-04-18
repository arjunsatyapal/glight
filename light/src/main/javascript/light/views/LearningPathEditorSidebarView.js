define(['dojo/_base/declare', 'dojo/_base/array',
        'light/views/TemplatedLightView',
        'dijit/_WidgetsInTemplateMixin',
        'dojo/data/ItemFileWriteStore',
        'dijit/tree/TreeStoreModel',
        'light/utils/DOMUtils',
        'dijit/Tree', 'dijit/tree/dndSource'],
        function(declare, array, TemplatedLightView, _WidgetsInTemplateMixin,
                ItemFileWriteStore, ForestStoreModel,
                DOMUtils, Tree, TreeDndSource) {
  var TREE_NODE_TYPE = 'treeNode';
  var SEARCH_RESULT_TYPE = 'searchResult';
  
  /**
   * @class
   * @name light.views.LearningPathEditorSidebarView
   */
  return declare('light.views.LearningPathEditorSidebarView',
          [TemplatedLightView, _WidgetsInTemplateMixin], {
    /** @lends light.views.LearningPathEditorSidebarView# */
    templateString: '<div></div>',
    postCreate: function() {
      this.store = new dojo.data.ItemFileWriteStore({
        data: {
          label: 'name',
          items: [
                  { id: 0, name:'Search OERs', numberOfItems:0 },
                  { id: 1, name:'Published by me', numberOfItems:0 },
                  { id: 2, name:'My Collections', numberOfItems:0 }
                ]
        }
      });
      this.store._saveEverything = function(successCb,failureCb,content) {
        console.log('New content: ', content);
        successCb();
      }

      var treeModel = new dijit.tree.ForestStoreModel({
          store: this.store,
          // query: {id: 0},
          childrenAttrs: ["children"]
      });

      this.tree = new dijit.Tree({
          model: treeModel,
          persist: false,
          showRoot: false
      });

      var dndController = new TreeDndSource(this.tree, {
        accept: [TREE_NODE_TYPE, SEARCH_RESULT_TYPE],
        checkItemAcceptance: function(rowNode, source, sourceNode) {
          /*console.log('Heyyyy Item!!!', source.getItem(sourceNode.id));
          console.log('Heyyyy Item debug: ', arguments);*/
          return true;
        },
        checkAcceptance: function(source, nodes) {
          /*for (var i = 0, len = nodes.length; i < len; i++) {
            console.log('Heyyyy!!!', JSON.stringify(source.getItem(nodes[i].id)));
          }
          console.log('Heyyyy debug: ', arguments);*/
          return true;
        },
        itemCreator: function(nodes, target, source) {
          return array.map(nodes, function(node) {
            var item = source.getItem(node.id);
            if(array.indexOf(item.type, TREE_NODE_TYPE) != -1) {
              return item.data;
            }
            if(array.indexOf(item.type, SEARCH_RESULT_TYPE) != -1) {
              return {
                name: DOMUtils.asText(item.data.title),
                numberOfItems: 0
              };
            }
            throw new Error('Non accepted types ' + item.type);
          });
        }
      });

      //this.tree.domNode.style.overflow = 'hidden';

      this.domNode.appendChild(this.tree.domNode);
    }
  });
});
