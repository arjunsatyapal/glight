define(['dojo/_base/declare', 'dojo/_base/array',
        'light/views/TemplatedLightView',
        'dijit/_WidgetsInTemplateMixin',
        'light/utils/DOMUtils',
        'dijit/Tree', 'dijit/tree/dndSource', 'dijit/tree/_dndSelector',
        'dojo/data/ItemFileWriteStore',
        'dojo/data/ItemFileReadStore',
        'dijit/tree/TreeStoreModel',
        'dijit/tree/ForestStoreModel', 'dojo/_base/lang',
        'dojo', 'dijit', 'light/enums/BrowseContextsEnum',
        'light/builders/BrowseContextStateBuilder',
        'light/utils/TemplateUtils',
        'dojo/_base/event',
        'light/utils/DialogUtils',
        ],
        function(declare, array, TemplatedLightView, _WidgetsInTemplateMixin,
                DOMUtils, Tree, TreeDndSource, TreeDndSelector,
                ItemFileWriteStore, ItemFileReadStore, TreeStoreModel,
                ForestStoreModel, lang, dojo, dijit, BrowseContextsEnum,
                BrowseContextStateBuilder, TemplateUtils, eventUtil,
                DialogUtils) {
  var TREE_NODE_TYPE = 'treeNode';
  var SEARCH_RESULT_TYPE = 'searchResult';

  /**
   * @class
   * @name light.views.MyDashSidebarView
   */
  return declare('light.views.MyDashSidebarView',
          [TemplatedLightView, _WidgetsInTemplateMixin], {
    /** @lends light.views.MyDashSidebarView# */
    templateString: '<div></div>',

    constructor: function() {
      /**
       * A list of the trees that are composing the sidebar.
       * Trees listed on it and that override the setSelection method on their
       * dndControllers with <code>this._synchronizedSetSelection</code> will
       * have their selections synchronized.
       */
      this._synchronizedSelectionTrees = [];
    },

    setContext: function(browseContextState) {
      if (browseContextState.context == BrowseContextsEnum.COLLECTION) {
        var collectionNodes = this._collectionTree
            .getNodesByItem(browseContextState.id);
        if (collectionNodes[0]) {
          this._collectionTree.dndController
              .setSelection([collectionNodes[0]], true);
          return;
        }
        // TODO(waltercacau): maybe send another event correcting
        // this if we don't find any
      }
      var menuNodes = this._dummyMenuTree
          .getNodesByItem(browseContextState.context);
      if (menuNodes[0]) {
        this._dummyMenuTree.dndController.setSelection([menuNodes[0]], true);
        return;
      }

      // If we didn't find any node to be selected, forcing empty selection
      var trees = this._synchronizedSelectionTrees;
      for (var i = 0, len = trees.length; i < len; i++) {
        TreeDndSelector.prototype.setSelection
            .apply(trees[i].dndController, [[]]);
      }
    },

    postCreate: function() {
      // TODO(waltercacau): add cleanup functions (eg. destroy method) or
      // transfer some of these stuff to templates and rely on its cleanup
      // TODO(waltercacau): Integrate keyboard navigation
      var self = this;

      /**
       * A setSelection method for tree's dndControllers with single selection
       * that will try to keep always one item selected and at most one item
       * selected between all trees.
       */
      var synchronizedSetSelection = function(newSelection,
              shouldNotChangeContext) {
        // Avoiding empty selection
        if (newSelection.length == 0)
          return;

        if (!shouldNotChangeContext) {
          // Changing the page's context
          if (this == self._dummyMenuTree.dndController) {
            self._controller.changeContextTo(
                    new BrowseContextStateBuilder()
                        .context(newSelection[0].item.context)
                        .build());
          } else if (this == self._collectionTree.dndController) {
            if (!newSelection[0].item.root) {
              self._controller.changeContextTo(
                      new BrowseContextStateBuilder()
                          .context(BrowseContextsEnum.COLLECTION)
                          .subcontext(newSelection[0].item.id)
                          .build());
            }
          }
        }

        if (this == self._collectionTree.dndController &&
                newSelection[0].item.root) {
          // Avoiding the context MY_COLLECTIONS for now
          // TODO(waltercacau): If you stop ignoring, remember to
          // solve the problem that My Collections will be selected when
          // you are actually clicking in the add collections button.
          return;
        }

        var trees = self._synchronizedSelectionTrees;
        for (var i = 0, len = trees.length; i < len; i++) {
          if (trees[i].dndController != this) {
            TreeDndSelector.prototype.setSelection
                .apply(trees[i].dndController, [[]]);
          } else {
            TreeDndSelector.prototype.setSelection.apply(this, [newSelection]);
          }
        }
      };

      // dummy menu tree
      this._dummyMenuTree = new dijit.Tree({
        model: new ForestStoreModel({
          store: new ItemFileReadStore({
            data: {
              identifier: 'context',
              label: 'name',
              items: [
                       { context: BrowseContextsEnum.ALL,
                         name: 'All modules' },
                       { context: BrowseContextsEnum.IMPORT,
                         name: 'Import' }
                      ]
            }
          }),
          query: {} // TODO(waltercacau): remove this if possible
        }),
        persist: false,
        showRoot: false,

        // Enabling single selection mode
        dndParams: ['singular', 'setSelection'],
        singular: true,
        setSelection: synchronizedSetSelection
      });

      this.domNode.appendChild(this._dummyMenuTree.domNode);
      this._synchronizedSelectionTrees.push(this._dummyMenuTree);

      // Collection's tree stuff
      this._collectionStoreNextId = 4;
      this._collectionStore = new ItemFileWriteStore({
        data: {
          identifier: 'id',
          label: 'name',
          items: [
                  { id: '1', name: 'Collection 1' },
                  { id: '2', name: 'Collection 2' },
                  { id: '3', name: 'Collection 3' }
                ]
        }
      });

      this._collectionTreeModel = new ForestStoreModel({
          store: this._collectionStore,
          childrenAttrs: ['children'],
          rootLabel: 'My Collections',
          query: {}, // TODO(waltercacau): remove this if possible
          newItem: function(item, parent, insertIndex) {
            console.log(item);
            // TODO(waltercacau): Trigger the insertion of a module in a
            // collection when dropped (or move maybe).
            //ForestStoreModel.prototype.newItem.apply(this, arguments);
          }
      });

      this._collectionTree = new dijit.Tree({
          model: this._collectionTreeModel,
          persist: false,

          // We are going to create our own later on
          // This may break with newer versions of Dojo because we are
          // relying that initiating the actual dndController later
          // causes no problem.
          dndController: null
      });

      this._collectionTree.dndController = new TreeDndSource(
              this._collectionTree, {
        singular: true,
        isSource: false,

        setSelection: synchronizedSetSelection,
        checkItemAcceptance: function(target, source, position) {
          // TODO(waltercacau): Enforce right drop behaviour
          var treeNode = dijit.getEnclosingWidget(target);

          // Assuming there are no folders, only the root can't
          // receive drop events.
          return !treeNode.item.root;
        },
        checkAcceptance: function(source, nodes) {
          return source.type == 'searchResultDndSource';
        },
        itemCreator: function(nodes, target, source) {
          // TODO(waltercacau): Create the proper item to be consumed
          // by our custom ForestStoreModel.newItem method
          return array.map(nodes, function(node) {
            var item = source.getItem(node.id);
            if (array.indexOf(item.type, TREE_NODE_TYPE) != -1) {
              return item.data;
            }
            if (array.indexOf(item.type, SEARCH_RESULT_TYPE) != -1) {
              return {
                name: DOMUtils.asText(item.data.title),
                link: item.data.link
              };
            }
            throw new Error('Non accepted types ' + item.type);
          });
        }
      });


      //this.tree.domNode.style.overflow = 'hidden';

      this.domNode.appendChild(this._collectionTree.domNode);
      this._synchronizedSelectionTrees.push(this._collectionTree);


      // Adding custom button to My Collections
      this._addCollectionButton = TemplateUtils.toDom(
              '<img src="${_blankGif}" class="addCollectionButton" ' +
              'role="button" tabindex="0" aria-label="Add a collection" />',
              this);

      this.connect(this._addCollectionButton, 'onclick',
              this._onAddCollection);
      this.connect(this._addCollectionButton, 'onkeypress',
              this._onAddCollectionByKey);

      var myCollectionsTreeNode = this._collectionTree
          .getNodesByItem(this._collectionTreeModel.root)[0];
      myCollectionsTreeNode.domNode.children[0]
          .appendChild(this._addCollectionButton);

      // Initial selection
      // this.setContext(BrowseContextsEnum.COLLECTION);
    },
    _onAddCollectionByKey: function(evt) {
      if(evt.charOrCode == dojo.keys.ENTER) {
        this._onAddCollection();
      }
    },
    _onAddCollection: function() {
      // TODO(waltercacau): Implement this
      // Just a mock implementation for now
      var self = this;
      DialogUtils.prompt({
        title: 'Create a collection',
        okLabel: 'Create',
        label: 'Name',
        onOk: function(collectionName) {
          self._collectionStore.newItem({
            id: '' + (self._collectionStoreNextId++),
            name: collectionName
          });
        }
      });
    }
  });
});
