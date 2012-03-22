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
        'dojo', 'light/SearchRouter', 'light/schemas/SearchStateSchema',
        'lightTest/TestUtils', 'light/enums/SearchEventsEnum'],
        function(connect, declare, hashUtil, dojo, SearchRouter,
                SearchStateSchema, TestUtils, SearchEventsEnum) {

  // Test inputs
  var SAMPLE_SEARCH_STATE = {
    query: 'someQuery',
    page: 2
  };
  var SAMPLE_HASH = 'SAMPLE_HASH';
  var SAMPLE_HASH_ALTERNATIVE = 'SAMPLE_HASH_ALTERNATIVE';

  describe('lightTest.SearchRouterTest', function() {
    it('should be good', function() {
      TestUtils.validateSchema(SAMPLE_SEARCH_STATE, SearchStateSchema);
      expect(SAMPLE_HASH_ALTERNATIVE).not.toEqual(SAMPLE_HASH);
      expect(SAMPLE_SEARCH_STATE).not
          .toEqual(SearchRouter.prototype.DEFAULT_SEARCH_STATE);
    });
  });

  describe('light.SearchRouter', function() {
    var router;

    beforeEach(function() {
      router = new SearchRouter();
    });

    describe('DEFAULT_SEARCH_STATE', function() {
      it('should conform to SearchStateSchema', function() {
        TestUtils.validateSchema(router.DEFAULT_SEARCH_STATE,
                SearchStateSchema);
      });
    });

    describe('constructor', function() {
      it('should setup the lastQuery to the defaultQuery and initialize' +
         ' _skipHash', function() {
        expect(router._lastSearchState).toBe(router.DEFAULT_SEARCH_STATE);
        expect(router._skipHash).toBe(null);
      });
    });

    describe('watch when called', function() {
      it('should wire the object to the dojo event system to watch' +
         'hash and search state changes', function() {
        var subscribeStub = this.stub(connect, 'subscribe');
        var _onHashChangeStub = this.stub(router, '_onHashChange');
        var hashUtilGet = this.stub(hashUtil, 'get');

        hashUtilGet.withArgs().returns(SAMPLE_HASH);

        router.watch();

        expect(subscribeStub).toHaveBeenCalledWith(
          '/dojo/hashchange', router, router._onHashChange
        );
        expect(subscribeStub).toHaveBeenCalledWith(
          SearchEventsEnum.SEARCH_STATE_CHANGED,
          router, router._onSearchStateChange
        );
        expect(_onHashChangeStub).toHaveBeenCalledWith(SAMPLE_HASH);

      });
    });

    describe('_setHash when called with a hash', function() {
      it('change the pages hash and mark that we shouldn\'t ' +
         'handle this hashchange event', function() {
        var hashUtilSet = this.stub(hashUtil, 'set');

        router._setHash(SAMPLE_HASH);

        expect(router._skipHash).toBe(SAMPLE_HASH);
        expect(hashUtilSet).toHaveBeenCalledWith(SAMPLE_HASH);
      });
    });

    describe('_shouldSkipThisHashChange', function() {
      describe('when hash was marked as skipped', function() {
        it('should return true and cleanup the mark', function() {
          router._skipHash = SAMPLE_HASH;

          expect(router._shouldSkipThisHashChange(SAMPLE_HASH)).toBeTruthy();

          expect(router._skipHash).toBe(null);
        });
      });
      describe('when hash was not marked as skipped', function() {
        it('should return false and cleanup the mark', function() {
          router._skipHash = null;

          expect(router._shouldSkipThisHashChange(SAMPLE_HASH)).toBeFalsy();

          expect(router._skipHash).toBe(null);
        });
      });
      // Exceptional test case:
      describe('when hash was not marked as skipped ' +
               'but another hash was', function() {
        it('should return false and cleanup the mark', function() {
          router._skipHash = SAMPLE_HASH_ALTERNATIVE;

          expect(router._shouldSkipThisHashChange(SAMPLE_HASH)).toBeFalsy();

          expect(router._skipHash).toBe(null);
        });
      });
    });

    describe('_onSearchStateChange', function() {
      var hashUtilSet;
      beforeEach(function() {
        hashUtilSet = this.stub(hashUtil, 'set');
      });
      describe('when the router has published the event', function() {
        it('should not change the hash', function() {

          router._onSearchStateChange(SAMPLE_SEARCH_STATE, router);

          expect(hashUtilSet).not.toHaveBeenCalled();
        });
      });
      describe('when the router has not published the event', function() {
        it('should change the hash', function() {

          var _searchStateToHashStub = this.stub(router, '_searchStateToHash');
          _searchStateToHashStub.withArgs(SAMPLE_SEARCH_STATE)
              .returns(SAMPLE_HASH);
          var otherSource = {};

          router._onSearchStateChange(SAMPLE_SEARCH_STATE, otherSource);

          expect(router._lastSearchState).toBe(SAMPLE_SEARCH_STATE);
          expect(hashUtilSet).toHaveBeenCalledWith(SAMPLE_HASH);
        });
      });
    });

    describe('_onHashChange', function() {
      var _setHashStub, _hashToSearchStateStub, publishStub,
          _searchStateToHashStub, _shouldSkipThisHashChangeStub;
      beforeEach(function() {
        _setHashStub = this.stub(router, '_setHash');
        _hashToSearchStateStub = this.stub(router, '_hashToSearchState');
        publishStub = this.stub(connect, 'publish');
        _searchStateToHashStub = this.stub(router, '_searchStateToHash');
        _shouldSkipThisHashChangeStub = this.stub(router,
                '_shouldSkipThisHashChange');
      });
      describe('when we should skip the given hash', function() {
        it('should do nothing', function() {
          _shouldSkipThisHashChangeStub.withArgs(SAMPLE_HASH).returns(true);

          router._onHashChange(SAMPLE_HASH);

          expect(publishStub).not.toHaveBeenCalled();
          expect(_hashToSearchStateStub).not.toHaveBeenCalled();
          expect(_setHashStub).not.toHaveBeenCalled();
        });
      });
      describe('when we shouldn\'t skip the given hash and it is' +
              ' not a valid hash',
              function() {
        it('should set the hash to the last valid hash', function() {
          _shouldSkipThisHashChangeStub.withArgs(SAMPLE_HASH).returns(false);
          _hashToSearchStateStub.withArgs(SAMPLE_HASH).throws();
          router._lastSearchState = SAMPLE_SEARCH_STATE;
          _searchStateToHashStub.withArgs(SAMPLE_SEARCH_STATE)
              .returns(SAMPLE_HASH_ALTERNATIVE);

          router._onHashChange(SAMPLE_HASH);

          expect(_setHashStub).toHaveBeenCalledWith(SAMPLE_HASH_ALTERNATIVE);
          expect(publishStub).not.toHaveBeenCalled();
        });
      });
      describe('when we shouldn\'t skip the given hash and it is a valid hash',
              function() {
        it('should change the search state and publish the corresponding event',
                function() {
          _shouldSkipThisHashChangeStub.withArgs(SAMPLE_HASH).returns(false);
          _hashToSearchStateStub.withArgs(SAMPLE_HASH)
              .returns(SAMPLE_SEARCH_STATE);

          router._onHashChange(SAMPLE_HASH);

          expect(_setHashStub).not.toHaveBeenCalled();
          expect(publishStub).toHaveBeenCalledWith(
            SearchEventsEnum.SEARCH_STATE_CHANGED,
            [SAMPLE_SEARCH_STATE, router]
          );
          expect(router._lastSearchState).toBe(SAMPLE_SEARCH_STATE);
        });
      });
    });

    describe('_hashToSearchState when feeded with ' +
             '_searchStateToHash(searchState)', function() {
      it('should return the initial searchState', function() {
        expect(router._hashToSearchState(
                router._searchStateToHash(SAMPLE_SEARCH_STATE)
                )).toEqual(SAMPLE_SEARCH_STATE);
      });
    });

  });
});