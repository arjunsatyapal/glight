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
define(['light/utils/DOMUtils',
        'dojo/dom-construct', 'dojo/dom-class'],
        function(DOMUtils, domConstruct, domClass) {
  describe('light.utils.DOMUtils', function() {
    var node;
    beforeEach(function() {
      node = domConstruct.toDom('<div></div>');
    });

    describe('hide when called with a dom node', function() {
      it('should set display css property to none', function() {

        DOMUtils.hide(node);

        expect(domClass.contains(node, 'hiddenNode')).toBeTruthy();

      });
    });

    describe('show when called with a dom node', function() {
      it('should set display css property to block', function() {

        DOMUtils.show(node);

        expect(domClass.contains(node, 'hiddenNode')).toBeFalsy();

      });
    });
  });
});
