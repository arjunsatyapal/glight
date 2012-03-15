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
define(['dojo/_base/connect', 'dojo/_base/declare', 'light/URLHashUtil',
        'dojo', 'light/SearchRouter'],
        function(connect, declare, hashUtil, dojo, SearchRouter) {
  describe('light.SearchRouter', function() {
    var router;
    beforeEach(function() {
      router = new SearchRouter();
    });
    describe('constructor', function() {
      it('should setup the lastQuery to the defaultQuery', function() {
        expect(router._lastQuery).toBe(router.defaultQuery);
      });
    });

    describe('watch', function() {
      describe('when called', function() {
        it('should subscribe to hashchange event\'s, setup the lastQuery' +
           ' to the default one and do the first sync', function() {
          var subscribeStub = this.stub(connect, 'subscribe');
          var _onHashChangeStub = this.stub(router, '_onHashChange');
          var hashUtilGet = this.stub(hashUtil, 'get');

          hashUtilGet.withArgs().returns('SOMEHASH');

          router.watch();

          var v = expect(subscribeStub);
          expect(subscribeStub).toHaveBeenCalledWith(
            '/dojo/hashchange', router, router._onHashChange
          );
          expect(_onHashChangeStub).toHaveBeenCalledWith('SOMEHASH');

        });
      });
    });

  });
});
