var dojoConfig = {
  //'dojo-undef-api': true, 'dojo-publish-privates': true, 
  has: {'config-dojo-loader-catches': true},
  isDebug: true,
  deferredOnError: function(e) { console.log(e.message, e.stack) },
      packages: [ {
          name: 'light',
          location: '/src/main/webapp/js/light'
      }, {
          name: 'lightTest',
          location: '/src/test/webapp/jsTest/lightTest'
      },
      { name: "dojo", location: "/src/main/webapp/js/external/djk/dojo" },
      { name: "dijit", location: "/src/main/webapp/js/external/djk/dijit" },
      { name: "dojox", location: "/src/main/webapp/js/external/djk/dojox" }
      ]
};
var dojoAmdPluginConfig = {
  tests: [
    'lightTest/SearchRouterTest'
  ],
  beforeRunning: function() {
    // We need to register sinon (mock framework) matchers every time before running, because
    // each run will clear old jasmine specs
    beforeEach(function() {
      this.addMatchers(sinonJasmine.getMatchers());
    });
  }
}
