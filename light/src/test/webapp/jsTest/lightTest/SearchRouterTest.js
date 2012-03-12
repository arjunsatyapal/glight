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
