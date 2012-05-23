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
define(['dojo/_base/declare',
        'dijit/tree/dndSource',
        'dojo/_base/array',
        'dojo/dnd/Manager',
        'dojo'],
        function(declare, dndSource, array, dndManager, dojo) {

  // TODO(waltercacau): Move this function to a utility class and test it.
  function compareArrayTuples(a, b) {
    for (var len = Math.min(a.length, b.length), i = 0; i < len; i++) {
      if (a[i] < b[i]) {
        return -1;
      }
      if (a[i] > b[i]) {
        return 1;
      }
    }
    if (a.length < b.length) {
      return -1;
    }
    if (a.length > b.length) {
      return 1;
    }
    return 0;
  }

  return declare('light.widgets.TreeDndSource', dndSource, {
    /** @lends light.widgets.TreeDndSource */

    _isDragEnabled: true,
    disableDrag: function() {
      this._isDragEnabled = false;
      if (this.isDragging) {
        dojo.publish('/dnd/cancel');
        dndManager.manager().stopDrag();
        this.onDndCancel();
      }
    },
    enableDrag: function() {
      this._isDragEnabled = true;
    },
    onMouseMove: function() {
      if (this._isDragEnabled) {
        return this.inherited(arguments);
      }
    },

    /**
     * Overriding getSelectedTreeNodes to guarantee that the order
     * of the nodes returned is the same as the one the user is seeing.
     */
    getSelectedTreeNodes: function() {
      var selectedNodes = this.inherited(arguments);
      if (selectedNodes.length <= 1) {
        return selectedNodes;
      }
      var indexPaths = {};
      array.forEach(selectedNodes, function(node) {
        var indexReversePath = [];
        var iterNode = node;
        var rootNode = node.tree.rootNode;
        while (iterNode != rootNode) {
          var parent = iterNode.getParent();
          if(parent === null) {
            indexPaths[node.id] = [];
            return;
          }
          var index = parent.getChildren().indexOf(iterNode);
          indexReversePath.push(index);
          iterNode = parent;
        }
        indexReversePath.reverse();
        indexPaths[node.id] = indexReversePath;
      });

      selectedNodes.sort(function(a,b) {
        return compareArrayTuples(indexPaths[a.id], indexPaths[b.id]);
      });
      return selectedNodes;
    },

    /**
     * Overriding onDndDrop to fix issue of multiple selection
     * drag&drop inversion.
     *
     * The implementation of this function was borrowed from the file
     * dijit/tree/dndSource of dojo 1.7.2 and was slightly modified to
     * correct the mentioned bug.
     *
     * TODO(waltercacau): File a bug for dojo.
     */
    onDndDrop: function(source, nodes, copy) {
      /*jshint funcscope:true */
      // summary:
      //    Topic event processor for /dnd/drop, called to finish the DnD operation.
      // description:
      //    Updates data store items according to where node was dragged from and dropped
      //    to.   The tree will then respond to those data store updates and redraw itself.
      // source: Object
      //    The dijit.tree.dndSource / dojo.dnd.Source which is providing the items
      // nodes: DomNode[]
      //    The list of transferred items, dndTreeNode nodes if dragging from a Tree
      // copy: Boolean
      //    Copy items, if true, move items otherwise
      // tags:
      //    protected
      if (this.containerState == 'Over') {
        var tree = this.tree,
          model = tree.model,
          target = this.targetAnchor;

        this.isDragging = false;

        // Compute the new parent item
        var newParentItem;
        var insertIndex;
        newParentItem = (target && target.item) || tree.item;
        if (this.dropPosition == 'Before' || this.dropPosition == 'After') {
          // TODO: if there is no parent item then disallow the drop.
          // Actually this should be checked during onMouseMove too, to make the drag icon red.
          newParentItem = (target.getParent() && target.getParent().item) || tree.item;
          // Compute the insert index for reordering
          insertIndex = target.getIndexInParent();
          if (this.dropPosition == 'After') {
            insertIndex = target.getIndexInParent() + 1;
          }
        }else {
          newParentItem = (target && target.item) || tree.item;
        }

        // If necessary, use this variable to hold array of hashes to pass to model.newItem()
        // (one entry in the array for each dragged node).
        var newItemsParams;

        array.forEach(nodes, function(node, idx) {
          // dojo.dnd.Item representing the thing being dropped.
          // Don't confuse the use of item here (meaning a DnD item) with the
          // uses below where item means dojo.data item.
          var sourceItem = source.getItem(node.id);

          // Information that's available if the source is another Tree
          // (possibly but not necessarily this tree, possibly but not
          // necessarily the same model as this Tree)
          if (array.indexOf(sourceItem.type, 'treeNode') != -1) {
            var childTreeNode = sourceItem.data,
              childItem = childTreeNode.item,
              oldParentItem = childTreeNode.getParent().item;
          }

          if (source == this) {
            // This is a node from my own tree, and we are moving it, not copying.
            // Remove item from old parent's children attribute.
            // TODO: dijit.tree.dndSelector should implement deleteSelectedNodes()
            // and this code should go there.

            if (typeof insertIndex == 'number') {
              if (newParentItem == oldParentItem && childTreeNode.getIndexInParent() < insertIndex) {
                insertIndex -= 1;
              }
            }
            model.pasteItem(childItem, oldParentItem, newParentItem, copy, insertIndex);
            // LightMod Start: Incrementing insertIndex
            if (typeof insertIndex == 'number') {
              insertIndex++;
            }
            // LightMod End
          }else if (model.isItem(childItem)) {
            // Item from same model
            // (maybe we should only do this branch if the source is a tree?)
            model.pasteItem(childItem, oldParentItem, newParentItem, copy, insertIndex);
          }else {
            // Get the hash to pass to model.newItem().  A single call to
            // itemCreator() returns an array of hashes, one for each drag source node.
            if (!newItemsParams) {
              newItemsParams = this.itemCreator(nodes, target.rowNode, source);
            }

            // Create new item in the tree, based on the drag source.
            model.newItem(newItemsParams[idx], newParentItem, insertIndex);
          }
        }, this);

        // Expand the target node (if it's currently collapsed) so the user can see
        // where their node was dropped.   In particular since that node is still selected.
        this.tree._expandNode(target);
      }
      this.onDndCancel();
    }
  });
});
