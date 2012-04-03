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
 * This file should be bundled with our custom dojo loader so it can
 * load the correct Main.js file depending on the host html page.
 */
define(['dojo/has', 'dojo/query', 'dojo/dom-construct', 'dojo'],
        function(has, $, domConstruct, dojo) {

  /**
   * Loads a CSS file
   *
   * <p>Note that it works by prepend a link node to the
   * head, so the page's defined CSS will have precedence.
   * @see http://dojo.codegreene.com/2009/03/dynamically-inserting-javascript-and-css-with-javascript/
   */
  function load_css(url) {
    var e = document.createElement('link');
    e.href = url;
    e.type = 'text/css';
    e.rel = 'stylesheet';
    e.media = 'screen';
    domConstruct.place(e, $('head')[0], 'first');
  }
  
  function loadMain(main) {
    require(['light/main/CoreMain'], function() {
      require(['light/main/' + pageMain], function() {});
    });
  }

  // CSS Stuff
  require(['dojo/domReady!'], function() {

    // Loading CSS
    load_css(require.toUrl('dijit/themes/claro/claro.css'));
    load_css(require.toUrl('dojo/resources/dojo.css'));

    // Adding the dojo theme class to the body element
    $('body')[0].setAttribute('class', 'claro');

  });

  var path = window.location.pathname;
  var pageMatch = path.match(/\/([a-zA-Z0-9-_]+)(.html|.htm|)$/);
  if (pageMatch) {
    var page = pageMatch[1];
    var pageMain = page[0].toUpperCase() + page.substr(1) + 'Main';

    /*
     * If we are in development, we should not load code through layers. this
     * way, we don't need to rebuild to test every time a file changes.
     */
    if (has('light-dev')) {
      loadMain(pageMain);
    } else {
      require(['light/build/core', 'light/build/' + page], function() {
        loadMain(pageMain);
      });
    }

  } else {
    // TODO(waltercacau): Find a friendly way to show that to the user
    alert('Page not found');
  }
});
