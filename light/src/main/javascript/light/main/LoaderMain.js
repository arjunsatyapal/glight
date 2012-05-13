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
define(['dojo/has', 'dojo/query', 'dojo/dom-construct', 'dojo',
        'light/utils/URLUtils', 'light/utils/PersonUtils',
        'light/enums/PagesEnum',
        'light/utils/RouterManager'],
        function(has, $, domConstruct, dojo, URLUtils, PersonUtils,
                 PagesEnum, RouterManager) {

  function loadMainFor(page) {
    require(['light/main/CoreMain'], function() {
      require(['light/main/' + page.main], function() {
        RouterManager.watch(page);
      });
    });
  }

  var page = PagesEnum.getByPath(URLUtils.getPath());
  if (page) {

    // Checking if the Person has accepted the Terms of Service.
    if (!PersonUtils.tosCheck(page)) {
      return;
    }

    /*
     * If we are in development, we should not load code through layers. this
     * way, we don't need to rebuild to test every time a file changes.
     */
    if (has('light-dev')) {
      loadMainFor(page);
    } else {
      require(['light/build/core', 'light/build/' + page.build], function() {
        loadMainFor(page);
      });
    }

  } else {
    // TODO(waltercacau): Find a friendly way to show that to the user
    alert('Page not found');
  }

  // CSS Stuff
  /**
   * Loads a CSS file
   *
   * <p>Note that it works by prepend a link node to the
   * head, so the page's defined CSS will have precedence.
   * @see http://dojo.codegreene.com/2009/03/dynamically-inserting-javascript-and-css-with-javascript/
   */
  function load_css(url) {
    var e = domConstruct.create('link', {
      'href': url,
      'type': 'text/css',
      'rel': 'stylesheet',
      'media': 'screen'
    });
    domConstruct.place(e, $('head')[0], 'first');
  }
  require(['dojo/domReady!'], function() {

    // Loading CSS
    load_css('/css/base.css');
    load_css(require.toUrl('dijit/themes/claro/claro.css'));
    load_css(require.toUrl('dojo/resources/dojo.css'));

    // Adding the dojo theme class to the body element
    var body = $('body')[0];
    var currentClassAttr = body.getAttribute('class');
    if (currentClassAttr) {
      body.setAttribute('class', 'claro ' + currentClassAttr);
    } else {
      body.setAttribute('class', 'claro');
    }

  });
});
