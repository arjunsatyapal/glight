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
        var subscribeStub, _onHashChangeStub, hashUtilGet;
        beforeEach(function() {
          subscribeStub = this.stub(connect, 'subscribe');
          _onHashChangeStub = this.stub(router, '_onHashChange');
          hashUtilGet = this.stub(hashUtil, 'get');
        });
        it('should subscribe to hashchange event\'s, setup the lastQuery' +
           ' to the default one and do the first sync', function() {
          hashUtilGet.withArgs().returns('SOMEHASH');

          router.watch();

          var v = expect(subscribeStub);
          /*for(q in v)
            console.log(q,v[q]);*/
          expect(subscribeStub).toHaveBeenCalledWith(
            '/dojo/hashchange', router, router._onHashChange
          );
          expect(_onHashChangeStub).toHaveBeenCalledWith('SOMEHASH');

        });
      });
    });

  });
});
