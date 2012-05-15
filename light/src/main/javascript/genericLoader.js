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
var dojoConfig = {
  async: true,
  isDebug: true,
  /*
   * waitSeconds is the timeout for loading a certain resource. By default it is
   * 15 seconds, but as this file is only for local testing, we can set
   * a smaller value to get a faster feedback when loading errors occur.
   */
  waitSeconds: 2,
  // Commented part used for testing i18n
  // locale: 'pt-br',
  deferredOnError: function(e) { console.log(e.message, e.stack); },
    packages: [{
        name: 'light',
        location: '/js/light'
    },
    { name: 'dojo', location: '/js/external/djk/dojo' },
    { name: 'dijit', location: '/js/external/djk/dijit' },
    { name: 'dojox', location: '/js/external/djk/dojox' }
    ]
};
document.write('<script src="/js/external/djk/dojo/dojo.js"></script>');
document.write('<link rel="stylesheet" type="text/css" ' +
        'href="/js/external/djk/dojo/resources/dojo.css">');
document.write('<link rel="stylesheet" type="text/css" ' +
        'href="/js/external/djk/dijit/themes/claro/claro.css">');
// window.onload to the rescue! IE was not getting the older
// loading method of writing a script tag with the following require code.
window.onload = function() {
  require(['dojo/query', 'dojo/domReady!', lightMain], function($) {
    $('body')[0].setAttribute('class', 'claro');
  })
}