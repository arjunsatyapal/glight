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
define(['dojo/_base/declare', 'dojo/number',
        'dojo/dom-construct', 'dojo/dom-class'],
        function(declare, numberUtils, domConstruct, domClass) {
  /**
   * Some utilities for dealing with DOM and style.
   *
   * @class
   * @name light.utils.DOMUtils
   */
  return {
    /** @lends light.utils.DOMUtils */

    /**
     * Sets the inner text of a DOM node.
     *
     * TODO(waltercacau): Add test for this. See jQuery manipulation.js
     * test as reference.
     *
     * @param {Node} node DOM Node.
     * @param {string} text Text content.
     */
    setText: function(node, text) {
      domConstruct.empty(node);
      node.appendChild((node.ownerDocument || document).createTextNode(text));
    },

    /**
     * Returns a textual representation of a HTML string or
     * a DOM Node.
     *
     * @param {string|Node} node HTML string or Node.
     * @return {string} Text representation.
     */
    asText: function(node) {
      if (typeof node == 'string')
        node = domConstruct.toDom(node);
      return node.textContent || node.innerText || '';
    },

    /**
     * Hides a DOM node.
     *
     * @param {Node} node Node.
     */
    hide: function(node) {
      domClass.add(node, 'hiddenNode');
    },

    /**
     * Shows a DOM node.
     *
     * @param {Node} node Node.
     */
    show: function(node) {
      domClass.remove(node, 'hiddenNode');
    }
  };
});
