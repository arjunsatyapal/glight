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
 * This file is just a replacement loader to avoid needing to build
 * code using the dojo build system during development.
 */
dojoConfig = {
  async: true,
  isDebug: true,
  waitSeconds: 2,
  locale: lightPreload.locale,
  has: {
    'light-dev': 1
  },
  //deferredOnError: function(e) { console.log(e.message, e.stack); },
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
// window.onload to the rescue! IE was not getting the older
// loading method of writing a script tag with the following require code.
window.onload = function() {
  require(['light/main/LoaderMain'], function() {});
};
