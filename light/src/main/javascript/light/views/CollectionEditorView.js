/*
 * Copyright (C) Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
define(['dojo/_base/declare', 'dojo/_base/lang',
        'light/views/TemplatedLightView',
        'dijit/_WidgetsInTemplateMixin',
        'light/utils/DOMUtils',
        'light/utils/DialogUtils',
        'light/utils/BuilderUtils',
        'dojo/text!light/templates/CollectionEditorTemplate.html',
        'dojo/i18n!light/nls/CollectionEditorMessages',
        'dijit/Tree',
        'dijit/tree/dndSource',
        'dojo/data/ItemFileWriteStore',
        'dojo/store/DataStore',
        'dijit/tree/TreeStoreModel',
        'dijit',
        'dijit/form/Button'],
        function(declare, lang, TemplatedLightView, _WidgetsInTemplateMixin,
                DOMUtils, DialogUtils, BuilderUtils, CollectionEditorTemplate,
                CollectionEditorMessages,
                Tree, TreeDndSource, ItemFileWriteStore, DataStore,
                TreeStoreModel, dijit) {

  /**
   * Builder for items in the internal nodeStore of this view.
   */
  var NodeBuilder = BuilderUtils.createBuilderClass(
          'light.views.CollectionEditorView._NodeBuilder',
          ['externalId', 'moduleId', 'moduleType', 'title', 'nodeType',
           'id', 'children', 'parent'], {});

  /**
   * @class
   * @name light.views.CollectionEditorView
   */
  return declare('light.views.CollectionEditorView', [TemplatedLightView,
      _WidgetsInTemplateMixin], {
    /** @lends light.views.CollectionEditorView# */
    templateString: CollectionEditorTemplate,
    messages: CollectionEditorMessages,
    _NodeBuilder: NodeBuilder,

    hide: function() {
      DOMUtils.hide(this._editorFormDiv);
      DOMUtils.hide(this._couldNotLoadFormDiv);
      this._destroyEditor();
    },

    _collectionTree: null,
    _idGenerator: 0,
    _destroyEditor: function() {
      this._nodeStore = null;
      this._rootItem = null;
      this._rootId = null;
      this._nodeTreeModel = null;
      if (this._collectionTree !== null) {
        this._collectionTree.destroy();
        this._collectionTree = null;
      }
    },

    /**
     * Adds a new node in the node store and generates an id for it.
     */
    _putInNodeStore: function(node) {
      node.id = this._idGenerator++;
      return this._nodeStore.newItem(node);
    },

    /**
     * Converts the CollectionTreeNode received from the server into
     * nodes to be stored in the nodeStore .
     *
     * @param {Object} root A CollectionTreeNode which represents its subtree.
     * @return {Object} The corresponding item in the nodeStore.
     */
    _putCollectionTreeInNodeStore: function(root) {
      var children = [];
      var i, len;
      if (lang.isArray(root.list)) {
        for (i = 0, len = root.list.length; i < len; i++) {
          children.push(this._putCollectionTreeInNodeStore(root.list[i]));
        }
      }
      var item = this._putInNodeStore(
              new NodeBuilder(root, true)
                  .children(children).parent(null).build());

      for (i = 0, len = children.length; i < len; i++) {
        this._nodeStore.setValue(children[i], 'parent', item);
      }
      return item;
    },

    /**
     * Shows the editor for the given collectionTree
     * @param {Object} root A CollectionTreeNode.
     */
    showEditor: function(collectionTree) {
      // TODO(waltercacau): Show link to be shared with students
      // (GMaps has a good solution for it with tooltip and a textbox).
      DOMUtils.show(this._editorFormDiv);
      DOMUtils.hide(this._couldNotLoadFormDiv);
      this._destroyEditor();
      var self = this;


      this._nodeStore = new ItemFileWriteStore({
          data: {
            identifier: 'id',
            label: 'title',
            items: []
          }
      });
      this._rootItem = this._putCollectionTreeInNodeStore(collectionTree);
      this._rootId = this._nodeStore.getValue(
              this._rootItem,
              'id');

      // Disabling save until we have any tree change
      this._saveButton.set('disabled', true);
      var onModelChange = lang.hitch(this, '_onModelChange');

      this._nodeTreeModel = new TreeStoreModel({
          store: this._nodeStore,
          childrenAttrs: ['children'],
          query: {id: this._rootId},
          mayHaveChildren: function(item) {
            return self._nodeStore.getValue(item, 'nodeType') != 'LEAF_NODE';
          },
          onChange: onModelChange,
          onChildrenChange: onModelChange,
          onDelete: onModelChange

      });

      this._collectionTree = new Tree({
        model: this._nodeTreeModel,
        persist: false,
        betweenThreshold: 5,
        checkItemAcceptance: function(target, source, position) {
          // For now, only accepting items from this dnd controller
          // (equivalent to only accepting item from this tree).
          if (source !== this) {
            return false;
          }
          if (position != 'over') {
            return true;
          }
          var treeNode = dijit.byNode(target.parentNode);
          return !self._isLeafNode(treeNode.item, 'nodeType');
        },

        dndController: TreeDndSource
      });
      this._editorFormDiv.appendChild(this._collectionTree.domNode);
    },

    /**
     * Called when any change happens in the tree.
     */
    _onModelChange: function() {
      // A change happened, let's enable the save button.
      this._saveButton.set('disabled', false);
    },

    /**
     * Shows a message which informs the user we could not load
     * the requested collection to edit.
     */
    showCouldNotLoad: function() {
      DOMUtils.hide(this._editorFormDiv);
      DOMUtils.show(this._couldNotLoadFormDiv);
    },

    /**
     * Starts the saving process.
     */
    _save: function() {
      // TODO(waltercacau): implement
    },

    /**
     * Takes care of the new subcollection flow.
     */
    _newSubcollection: function() {
      // TODO(waltercacau): implement
      var self = this;
      DialogUtils.prompt({
        title: this.messages.createSubcollectionDialogTitle,
        okLabel: this.messages.createSubcollectionOkButtonLabel,
        label: this.messages.createSubcollectionTitleTextboxLabel,
        onOk: function(subcollectionTitle) {
          var parentItem = self._getFirstSelectedOrRoot();
          if (self._isLeafNode(parentItem)) {
            parentItem = self._nodeStore.getValue(parentItem, 'parent');
          }
          var item = self._putInNodeStore(
                  new NodeBuilder()
                      .moduleType('LIGHT_SUB_COLLECTION')
                      .nodeType('INTERMEDIATE_NODE')
                      .title(subcollectionTitle)
                      .parent(parentItem)
                      .children([])
                      .build()
              );
          var children = self._nodeStore.getValues(parentItem, 'children');
          children.push(item);
          self._nodeStore.setValues(parentItem, 'children', children);
        }
      });
    },

    /**
     * @return {boolean} true if item is a leaf node.
     */
    _isLeafNode: function(item) {
      return this._nodeStore.getValue(item, 'nodeType') ==
                'LEAF_NODE';
    },

    /**
     * Returns the first selected element or the root
     * if there is no selection.
     */
    _getFirstSelectedOrRoot: function() {
      var firstSelected = null;
      this._collectionTree.dndController.forInSelectedItems(function(dndItem) {
        if (firstSelected === null) {
          firstSelected = dndItem.data.item;
        }
      });
      if (firstSelected !== null) {
        return firstSelected;
      } else {
        return this._rootItem;
      }
    },

    /**
     * Traverse utility for a subtree stored in nodeStore.
     * It goes through the tree nodes in postorder and executes func(node).
     */
    _traversePostorder: function(root, func) {
      var children = this._nodeStore.getValues(root, 'children');
      for (var i = 0, len = children.length; i < len; i++) {
        this._traversePostorder(children[i], func);
      }
      func(root);
    },

    /**
     * Delete selected items in the tree.
     */
    _deleteSelected: function() {
      var self = this;
      var found = {};
      var itemsToDelete = [];
      this._collectionTree.dndController.forInSelectedItems(function(dndItem) {
        self._traversePostorder(dndItem.data.item, function(item) {
          var id = self._nodeStore.getValue(item, 'id');
          if (!found[id]) {
            found[id] = true;
            itemsToDelete.push(item);
            self._nodeStore.setValue(item, 'children', []);
          }
        });
      });
      for (var i = 0, len = itemsToDelete.length; i < len; i++) {
        self._nodeStore.deleteItem(itemsToDelete[i]);
      }
    }
  });
});
