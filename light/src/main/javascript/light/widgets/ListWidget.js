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
        'dojo/dnd/Source',
        'dijit/focus',
        'dojo/_base/event',
        'dojo/on',
        'dojo'],
        function(declare, dndSource, focusUtil, event, on, dojo) {

  return declare('light.widgets.ListWidget', dndSource, {
    /** @lends light.widgets.ListWidget */

    /**
     * An accessible widget for displaying list of
     * results which can be selected and dragged around.
     *
     * <p> It is heavily based on {@link dojo.dnd.Source} . To understand
     * the current implementation you need to be familiar with
     * dojo/dnd, so refer to the link bellow for more details.
     * @link http://dojotoolkit.org/reference-guide/1.7/dojo/dnd.html
     *
     * <p> You construct this widget the same way you would construct
     * a {@link dojo.dnd.Source} , except that the <code>creator</code> param
     * was renamed to <code>rawCreator</code> and it is required.
     *
     * <p>Also, in the rawCreator return value you can specify a focusNode,
     * which will be an node that will receive focus when the item is selected.
     *
     * <p>Code example:
     * <pre>
       var listWidget = new ListWidget(listNode, {
          accept: [],
          type: 'SomeSourceType',
          selfAccept: false,
          copyOnly: true,
          selfCopy: false,
          rawCreator: function(item) {
            var node = TemplateUtils.toDom(
                '<div><a href="${link}">${title}</a>${desc}</div>', item);
            console.log(node);
            return {
              node: node,
              data: item,
              focusNode: dojo.query('a', node)[0],
              type: ['someItemType']
            };
          }
        });

        listWidget.insertNodes(false, [
            {title: 'Foo 1', desc: 'Bar 1', link: 'http://foobar.com/1'},
            {title: 'Foo 2', desc: 'Bar 2', link: 'http://foobar.com/2'},
            {title: 'Foo 3', desc: 'Bar 3', link: 'http://foobar.com/3'}
        ]);
     * </pre>
     *
     * @constructs
     * @extends dojo/dnd/Source
     */
    constructor: function(node, params) {

      // Initial checks
      if (params.creator) {
        throw new Error('You should define rawCreator instead of creator');
      }
      if (!params.rawCreator) {
        throw new Error('You should define rawCreator in params');
      }

      /**
       * Map of node.id to the node which should receive
       * focus when the corresponding item is selected.
       */
      this._focusNodeMap = {};

      this.events.push(
        dojo.connect(this.node, 'onkeypress', this, '_onKeyPress'),
        on(this.node, 'focusin', dojo.hitch(this, this._onFocus))
      );

      // Making the list widget focusable initially
      this.node.setAttribute('tabindex', '0');
    },

    /**
     * Properly destroys this widget making its resources
     * garbage collectible.
     */
    destroy: function() {
      this._focusNodeMap = {};
      this.inherited(arguments);
    },

    /**
     * Startup hook used to introduce our modified creator.
     */
    startup: function() {
      this.inherited(arguments);
      this.defaultCreator = this._modCreator;
    },

    /**
     * Modified creator which takes care of interpreting the
     * focusNode option returned by rawCreator.
     */
    _modCreator: function() {
      var ret = this.rawCreator.apply(this, arguments);
      if (!ret.node.id) { ret.node.id = dojo.dnd.getUniqueId(); }
      if (ret.focusNode)
        this._focusNodeMap[ret.node.id] = ret.focusNode;
      else
        this._focusNodeMap[ret.node.id] = ret.node;
      return ret;
    },

    /**
     * Key press handler to deal with keyboard shortcuts
     */
    _onKeyPress: function(evt) {
      var newAnchor = null;
      if (evt.charOrCode == dojo.keys.DOWN_ARROW) {
        if (this.anchor) {
          newAnchor = this.anchor.nextSibling;
        } else {
          // TODO(waltercacau): see if we can eliminate this
          newAnchor = this.getAllNodes()[0];
        }
      } else if (evt.charOrCode == dojo.keys.UP_ARROW) {
        if (this.anchor) {
          newAnchor = this.anchor.previousSibling;
        } else {
          // TODO(waltercacau): see if we can eliminate this
          newAnchor = this.getAllNodes()[0];
        }
      }
      if (newAnchor) {
        if(evt.shiftKey && this.anchor) {
          if(this.selection[newAnchor.id]) {
            delete this.selection[this.anchor.id];
            this._removeItemClass(newAnchor, 'Selected');
          } else {
            this._addItemClass(this.anchor, 'Selected');
          }
        } else {
          this.selectNone();
        }
        this._setNewAnchor(newAnchor);
      }
    },
    
    /**
     * Focus the first item of this widget.
     */
    focusFirstItem: function() {
      newAnchor = this.getAllNodes()[0];
      if (newAnchor) {
        this.selectNone();
        this._setNewAnchor(newAnchor);
      }
    },

    /**
     * Set's the new anchor
     */
    _setNewAnchor: function(newAnchor) {
      this._removeAnchor();
      this.anchor = newAnchor;
      this._addItemClass(newAnchor, 'Anchor');
      this.selection[newAnchor.id] = 1;
    },

    /**
     * Focus handler
     */
    _onFocus: function(evt) {
      if (!this.anchor) {
        newAnchor = this.getAllNodes()[0];
        if (newAnchor) {
          this.selectNone();
          this._setNewAnchor(newAnchor);
        }
      }
    },

    /*
     * Overriding the css functions so we can properly
     * make elements focusabled depending of the current state
     * of the node.
     * 
     * There is one invariant in this widget that is accomplished
     * in the following methods: There is only one focusabled element
     * all the time (either this.node or this._lastAnchorFocusNode).
     * This makes tab's leave the listWidget and get back in the
     * current anchor.
     * 
     * One implicit assumption: You always remove the class from
     * the previous anchor before adding to the new one.
     */
    _lastAnchorFocusNode: null,
    _addItemClass: function(node, type) {
      if (type == 'Anchor') {
        if (this._lastAnchorFocusNode != null) {
          throw new Error('You must remove the previous ' +
                          'anchor before adding a new one.');
        }
        this.node.setAttribute('tabindex', '-1');
        this._lastAnchorFocusNode = this._focusNodeMap[node.id];
        this._lastAnchorFocusNode.setAttribute('tabindex', '0');
        focusUtil.focus(this._lastAnchorFocusNode);
      }
      this.inherited(arguments);
    },
    _removeItemClass: function(node, type) {
      if (type == 'Anchor') {
        if (this._lastAnchorFocusNode != null) {
          this._lastAnchorFocusNode.setAttribute('tabindex', '-1');
          this._lastAnchorFocusNode = null;
        }
        this.node.setAttribute('tabindex', '0');
      }
      this.inherited(arguments);
    }
  });
});
