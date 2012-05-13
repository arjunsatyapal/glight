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
/**
 * Configuration for dojo loader.
 *
 * <p>Note that theses configurations are here to support three use cases:
 *
 * <ul>
 * <li> Run tests on JSTestDriver with Light dojoAmdPlugin</li>
 * <li> Run tests using the jasmine:bdd goal with jasmine-maven-plugin</li>
 * <li> Run tests using the jasmine:test goal with jasmine-maven-plugin</li>
 * </ul>
 *
 * In the first two cases, our project base directory (the one with the pom.xml)
 * is being served on the root of the server. In the second case, actually we
 * will be running the tests against the filesystem and not a webserver.
 */
 var dojoAmdAdapterConfig, dojoConfig;
(function() {
  /*
    jasmine:test goal don't start a web server. They are just run
    on the filesystem. So we can't rely on the serving structure and
    we need to discover our current location.
  */
  var base = (window.jstestdriver ? '' :
    location.pathname.replace(/\/[^\/]+$/, ''));
  if (base.substr(base.length - 1) != '/') {
    base += '/';
  }
  
  // If running inside jasmine:test, skipping target/jasmine folder
  // and going direct to the root of the project.
  base = base.replace(/\/target\/jasmine\/$/, "/");
    

  dojoConfig = {
    //'dojo-undef-api': true, 'dojo-publish-privates': true,
    has: {
      'config-dojo-loader-catches': true,
      'light-dev': 1,
      'light-unit-test': 1
    },
    // Needed to avoid some browsers from caching the code
    cacheBust: new Date(),
    isDebug: true,
    waitSeconds: 2,
    deferredOnError: function(e) { console.log(e.message, e.stack); },
      packages: [{
          name: 'light',
          location: base + 'src/main/javascript/light'
      }, {
          name: 'lightTest',
          location: base + 'src/test/javascript/lightTest'
      }, {
          name: 'testResources',
          location: base + 'src/test/resources'
      }, {
        name: 'dojo',
        location: base + 'src/main/javascript/external/djk/dojo'
      }, {
        name: 'dijit',
        location: base + 'src/main/javascript/external/djk/dijit'
      }, {
        name: 'dojox',
        location: base + 'src/main/javascript/external/djk/dojox'
      }]
  };

  /**
   * Configuration for the dojoAmdAdapter
   * @see jstestdriver.dojoAmdAdapter
   */
  dojoAmdAdapterConfig = {
    /*
      Only the JSTestDriver adapter uses this. Jasmine Maven plugin
      automatically adds the files. This file is supposed to be
      generated by maven.
    */
    testListFile: '/target/testList.js',
    testMidFormatter: function(path) {
      /*
        Maven produces a list of filenames, but dojo.require expect a list
        of module ids (eg. lightTest/SomeTest.js -> lightTest/SomeTest)
       */
      if (path.substr(path.length - 3) == '.js') {
        return path.substr(0, path.length - 3);
      }
    }
  };
})();
