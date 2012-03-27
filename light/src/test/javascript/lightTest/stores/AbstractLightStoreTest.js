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
define(['dojo/_base/declare', 'light/stores/AbstractLightStore'],
        function(declare, AbstractLightStore) {
  describe('light.stores.AbstractLightStore subclass constructor', function() {
    describe('without a target', function() {
      it('should throw', function() {
        var ResourceStore = declare(AbstractLightStore, {
          constructor: function() {
            this.inherited(arguments);
          }
        });
        expect(function() {
          new ResourceStore();
        }).toThrow(new Error('You should specify a target!'));
      });
    });

    describe('with a target', function() {
      it('should not throw', function() {
        var ResourceStore = declare(AbstractLightStore, {
          lightTarget: '/api/resource',
          constructor: function() {
            // This is needed because AbstractLightStore
            // uses manual chaining for the constructor/
            this.inherited(arguments);
          }
        });
        expect(function() {
          new ResourceStore();
        }).not.toThrow();
      });
    });

  });
});
