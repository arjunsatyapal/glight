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
define(['dojo/_base/connect', 'dojo/_base/declare', 'light/utils/URLUtils',
        'dojo', 'light/routers/SearchRouter', 'light/schemas/SearchStateSchema',
        'lightTest/TestUtils', 'light/enums/EventsEnum'],
        function(connect, declare, URLUtils, dojo, SearchRouter,
                SearchStateSchema, TestUtils, EventsEnum) {

  // Test inputs
  var SAMPLE_SEARCH_STATE = {
    query: 'someQuery',
    page: 2
  };
  var SAMPLE_HASH = 'SAMPLE_HASH';
  var SAMPLE_HASH_ALTERNATIVE = 'SAMPLE_HASH_ALTERNATIVE';

  describe('lightTest.SearchRouterTest inputs', function() {
    it('should be good', function() {
      TestUtils.validateSchema(SAMPLE_SEARCH_STATE, SearchStateSchema);
      expect(SAMPLE_HASH_ALTERNATIVE).not.toEqual(SAMPLE_HASH);
      expect(SAMPLE_SEARCH_STATE).not
          .toEqual(SearchRouter.prototype.DEFAULT_SEARCH_STATE);
    });
  });

  describe('light.routers.SearchRouter', function() {
    var router;

    beforeEach(function() {
      router = new SearchRouter();
    });

    describe('DEFAULT_SEARCH_STATE', function() {
      it('should conform to SearchStateSchema', function() {
        TestUtils.validateSchema(router.DEFAULT_SEARCH_STATE,
                SearchStateSchema);
      });
      it('should not cause searchStateToHash to throw', function() {
        expect(function() {
          SearchRouter.searchStateToHash(router.DEFAULT_SEARCH_STATE);
        }).not.toThrow();
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
        var URLUtilsGetHashStub = this.stub(URLUtils, 'getHash');

        URLUtilsGetHashStub.withArgs().returns(SAMPLE_HASH);

        router.watch();

        expect(subscribeStub).toHaveBeenCalledWith(
          '/dojo/hashchange', router, router._onHashChange
        );
        expect(subscribeStub).toHaveBeenCalledWith(
          EventsEnum.SEARCH_STATE_CHANGED,
          router, router._onSearchStateChange
        );
        expect(_onHashChangeStub).toHaveBeenCalledWith(SAMPLE_HASH);

      });
    });

    describe('_setHash when called with a hash', function() {
      describe('and with second argument true', function() {
        it('change the page hash and mark that we shouldn\'t ' +
           'handle this hashchange event', function() {
          var URLUtilsSetHashStub = this.stub(URLUtils, 'setHash');

          router._setHash(SAMPLE_HASH, true);

          expect(router._skipHash).toBe(SAMPLE_HASH);
          expect(URLUtilsSetHashStub).toHaveBeenCalledWith(SAMPLE_HASH, true);
        });
      });
      describe('without second argument', function() {
        it('change the page hash and mark that we shouldn\'t ' +
           'handle this hashchange event', function() {
          var URLUtilsSetHashStub = this.stub(URLUtils, 'setHash');

          router._setHash(SAMPLE_HASH);

          expect(router._skipHash).toBe(SAMPLE_HASH);
          expect(URLUtilsSetHashStub)
              .toHaveBeenCalledWith(SAMPLE_HASH, undefined);
        });
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
      var URLUtilsSetHashStub;
      beforeEach(function() {
        URLUtilsSetHashStub = this.stub(URLUtils, 'setHash');
      });
      describe('when the router has published the event', function() {
        it('should not change the hash', function() {

          router._onSearchStateChange(SAMPLE_SEARCH_STATE, router);

          expect(URLUtilsSetHashStub).not.toHaveBeenCalled();
        });
      });
      describe('when the router has not published the event', function() {
        it('should change the hash', function() {

          var searchStateToHashStub =
              this.stub(SearchRouter, 'searchStateToHash');
          searchStateToHashStub.withArgs(SAMPLE_SEARCH_STATE)
              .returns(SAMPLE_HASH);
          var otherSource = {};

          router._onSearchStateChange(SAMPLE_SEARCH_STATE, otherSource);

          expect(router._lastSearchState).toBe(SAMPLE_SEARCH_STATE);
          expect(URLUtilsSetHashStub).toHaveBeenCalledWith(SAMPLE_HASH);
        });
      });
    });

    describe('_onHashChange', function() {
      var _setHashStub, hashToSearchStateStub, publishStub,
          searchStateToHashStub, _shouldSkipThisHashChangeStub;
      beforeEach(function() {
        _setHashStub = this.stub(router, '_setHash');
        hashToSearchStateStub = this.stub(SearchRouter, 'hashToSearchState');
        publishStub = this.stub(connect, 'publish');
        searchStateToHashStub = this.stub(SearchRouter, 'searchStateToHash');
        _shouldSkipThisHashChangeStub = this.stub(router,
                '_shouldSkipThisHashChange');
      });
      describe('when we should skip the given hash', function() {
        it('should do nothing', function() {
          _shouldSkipThisHashChangeStub.withArgs(SAMPLE_HASH).returns(true);

          router._onHashChange(SAMPLE_HASH);

          expect(publishStub).not.toHaveBeenCalled();
          expect(hashToSearchStateStub).not.toHaveBeenCalled();
          expect(_setHashStub).not.toHaveBeenCalled();
        });
      });
      describe('when we shouldn\'t skip the given hash and it is' +
              ' not a valid hash',
              function() {
        it('should set the hash to the last valid hash', function() {
          _shouldSkipThisHashChangeStub.withArgs(SAMPLE_HASH).returns(false);
          hashToSearchStateStub.withArgs(SAMPLE_HASH).throws();
          router._lastSearchState = SAMPLE_SEARCH_STATE;
          searchStateToHashStub.withArgs(SAMPLE_SEARCH_STATE)
              .returns(SAMPLE_HASH_ALTERNATIVE);

          router._onHashChange(SAMPLE_HASH);

          expect(_setHashStub)
              .toHaveBeenCalledWith(SAMPLE_HASH_ALTERNATIVE, true);
          expect(publishStub).not.toHaveBeenCalled();
        });
      });
      describe('when we shouldn\'t skip the given hash and it is a valid hash',
              function() {
        it('should change the search state and publish the corresponding event',
                function() {
          _shouldSkipThisHashChangeStub.withArgs(SAMPLE_HASH).returns(false);
          hashToSearchStateStub.withArgs(SAMPLE_HASH)
              .returns(SAMPLE_SEARCH_STATE);

          router._onHashChange(SAMPLE_HASH);

          expect(_setHashStub).not.toHaveBeenCalled();
          expect(publishStub).toHaveBeenCalledWith(
            EventsEnum.SEARCH_STATE_CHANGED,
            [SAMPLE_SEARCH_STATE, router]
          );
          expect(router._lastSearchState).toBe(SAMPLE_SEARCH_STATE);
        });
      });
    });

    describe('hashToSearchState', function() {
      describe('when feeded with ' +
               'searchStateToHash(searchState)', function() {
        it('should return the initial searchState', function() {
          expect(SearchRouter.hashToSearchState(
                  SearchRouter.searchStateToHash(SAMPLE_SEARCH_STATE)
                  )).toEqual(SAMPLE_SEARCH_STATE);
        });
      });
      describe('when feeded with a hash that represents a wrong ' +
              'search state', function() {
        it('should throw', function() {
          expect(function() {
            SearchRouter.hashToSearchState('query=A&page=-1');
          }).toThrow();
          expect(function() {
            SearchRouter.hashToSearchState('query=A');
          }).toThrow();
          expect(function() {
            SearchRouter.hashToSearchState('page=1');
          }).toThrow();
        });
      });
    });

  });
});
