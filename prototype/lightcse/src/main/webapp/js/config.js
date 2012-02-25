// Bootstrapping code should be here
requirejs.config({
  paths: {
    'jquery':
      'https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min',
    'jquery-ui-base':
      'https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.16/jquery-ui.min',
    'light': '/js/light/light',
    'backbone': '/js/external/backbone',
    'underscore': '/js/external/underscore'
  },
  priority: ['jquery'],
  baseUrl: '/js'
});

define("jquery-ui", ["jquery", "jquery-ui-base"], function($) {
  return $;
});