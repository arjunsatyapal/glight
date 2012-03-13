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
dojoConfig = {
  async: true,
  isDebug: true,
  waitSeconds: 2,
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
function setupLightPage() {
  /*require({
    trace:{
      "loader-inject":1, // turn the loader-inject group on
      "loader-define":1 // turn the loader-define group off
    }
  });*/
  
  require(['dojo/query', 'light/RegisterFormView', 'light/RegisterFormController', 'light/PersonStore'],
          function($, RegisterFormView, RegisterFormController, PersonStore) {
    $('body')[0].setAttribute('class', 'claro');

    // Actual Register page code
    registerFormView = new RegisterFormView({}, "registerForm");
    registerFormController = new RegisterFormController(new PersonStore());
    registerFormController.setView(registerFormView);
    registerFormView.setController(registerFormController);
    
  });
}
document.write('<script src="/js/external/djk/dojo/dojo.js"></script>');
document.write('<link rel="stylesheet" type="text/css" href="/js/external/djk/dijit/themes/claro/claro.css">');
document.write('<script>setupLightPage();</script>');
