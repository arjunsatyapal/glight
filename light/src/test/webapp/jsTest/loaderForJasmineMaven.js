/**
 * This file should expose an AMD Loader that has a require.config function like RequireJS 1.0.7
 * 
 * For simplicity, we configured Jasmine Maven Plugin to think he is working with requireJS,
 * because by the time this file was created there is no support for other AMD loaders in such
 * plugin.
 * 
 * We also use this file to load other test framework dependencies (eg. sinon.JS).
 * Ideally this file should be analogous to jsTestDriver.conf
 */
document.write('<script src="src/test/webapp/jsTest/external/sinon-1.3.1.js"></script>');
document.write('<script src="src/test/webapp/jsTest/external/jasmine-sinon.js"></script>');
document.write('<script src="src/test/webapp/jsTest/testConfig.js"></script>');
document.write('<script src="src/main/webapp/js/external/djk/dojo/dojo.js"></script>');
document.write('<script>require.config = function() {}</script>');
