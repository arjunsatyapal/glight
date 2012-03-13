dojoConfig = {
  //has: {'config-dojo-loader-catches': true},
  async: true,
  //cacheBust: new Date(), // Needed to avoid some browsers from caching the code
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
