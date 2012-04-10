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
 * This file is responsible for configuring the Dojo Build for production use.
 *
 * <p> It was based on the app.profile.js file from the
 * Dojo Boilerplate project.
 *
 * @see https://github.com/csnover/dojo-boilerplate
 */
var profile = (function() {
  /**
   * Used to determine whether or not a resource should
   * be tagged as copy-only.
   *
   * @see http://dojotoolkit.org/reference-guide/1.7/build/buildSystem.html#resource-tags
   */
  function copyOnly(mid) {
    return mid in {
      'light/loader-dev': 1
    };
  }

  return {

    // Supported locales
    localeList: ['en-us', 'pt-br', 'fa'],

    // Packages as seen by the build system
    packages: [{
      name: 'light',
      location: './light'
    }, {
      name: 'dojo',
      location: './external/djk/dojo'
    }, {
      name: 'dijit',
      location: './external/djk/dijit'
    }, {
      name: 'dojox',
      location: './external/djk/dojox'
    }],

    basePath: '..',

    /*
     * Dojo configuration for runtime
     * 
     * It is encapsulated in a function so we can add some
     * runtime logic.
     */
    userConfig: "("+function() {
      
      var config = {
        // Packages as seen by the runtime enviroment
        packages: [{
          name: 'light',
          location: '/js/light'
        }, {
          name: 'dojo',
          location: '/js/dojo'
        }, {
          name: 'dijit',
          location: '/js/dijit'
        }, {
          name: 'dojox',
          location: '/js/dojox'
        }]
      };
      if(lightPreload && lightPreload.locale) {
        config.locale = lightPreload.locale;
      }
      
      return config;
      
    }.toString()+")()",

    // Builds a new release.
    action: 'release',

    // Strips all comments from CSS files.
    // TODO(waltercacau): See how to proper use this
    cssOptimize: 'comments',

    // Excludes tests, demos, and original template files from being
    // included in the built version.
    mini: true,

    // We’re building layers, so we need to set the minifier to use for
    // those, too. This defaults to "shrinksafe" if
    // it is not provided.
    layerOptimize: 'closure',

    /*
     * The default selector engine is not included by default in a
     * dojo.js build in order to make mobile builds
     * smaller. We add it back here to avoid that extra HTTP request.
     * There is also a "lite" selector available; if
     * you use that, you’ll need to set selectorEngine in app/run.js
     * too. (The "lite" engine is only suitable if you
     * are not supporting IE7 and earlier.)
     */
    selectorEngine: 'acme',

    /*
     * Builds can be split into multiple different JavaScript files
     * called “layers”. This allows applications to defer loading large
     * sections of code until they are actually required while still
     * allowing multiple modules to be compiled into a single file.
     */
    layers: {

      /*
       * This is the main loader layer. It should be the only script
       * that you need to declare in an HTML page.
       *
       * **IMPORTANT**: You shouldn't put any localized resource here. Dojo
       * build system does not produce (at least on version 1.7.2) the localized
       * i18n bundles for the loader layer. That's the reason why there is a
       * loader layer and a core layer. If your built app fails because it
       * couldn't load a resource like /js/light/build/nls/loader_en-us.js is
       * a sign that some localized stuff was included in the loader layer.
       */
      'light/build/loader': {
        include: ['dojo/i18n', 'dojo/domReady', 'light/main/LoaderMain'],

        /*
         * This option makes dojo issue a require call for all member modules
         * of this layer.
         *
         * This turn's out calling Light's LoaderMain code
         * that will then load the correct javascript file depending on
         * the host html page.
         */
        compat: '1.6',

        /*
         * By default, the build system will try to include dojo/main in
         * the built dojo/dojo layer, which adds a bunch of stuff we
         * don’t want or need. We want the initial script load to be as
         * small and quick as possible, so we configure it as a custom,
         * bootable base.
         */
        boot: true,
        customBase: true
      },
      
      // Making the usual hardcoded dojo/dojo layer just a dummy file
      'dojo/dojo': {
        customBase: true
      },

      /*
       * This is the core layer. It contains stuff that usually
       * should be loaded in every light page so it can be
       * properly cached by the browsers.
       */
      'light/build/core': {
        include: [
          'light/main/CoreMain',
          'light/views/AbstractLightView',
          'light/controllers/AbstractLightController',
          'light/stores/AbstractLightStore',
          'light/views/TemplatedLightView'
        ],
        exclude: ['light/build/loader']
      },

      // The next layers should map one to one with light's html pages.
      'light/build/register': {
        include: [
          'light/main/RegisterMain'
        ],
        exclude: ['light/build/loader', 'light/build/core']
      },
      'light/build/search': {
        include: [
          'light/main/SearchMain'
        ],
        exclude: ['light/build/loader', 'light/build/core']
      }

    },

    /*
     * Providing hints to the build system allows code to be
     * conditionally removed on a more granular level than simple module
     * dependencies can allow. This is especially useful for creating
     * tiny mobile builds. Keep in mind that dead code removal only
     * happens in minifiers that support it! Currently, ShrinkSafe does
     * not support dead code removal; Closure Compiler and UglifyJS do.
     */
    staticHasFeatures: {
      // The trace & log APIs are used for debugging the loader, so we
      // don’t need them in the build
      'dojo-trace-api': 0,
      'dojo-log-api': 0,

      // This causes normally private loader data to be exposed for
      // debugging, so we don’t need that either
      'dojo-publish-privates': 0,

      // We’re fully async, so get rid of the legacy loader
      'dojo-sync-loader': 0,

      // dojo-xhr-factory relies on dojo-sync-loader
      'dojo-xhr-factory': 0,

      // We aren’t loading tests in production
      'dojo-test-sniff': 0,

      // Are we in development? This will make the loader layer ignore loading
      // code from other layers and go direct to fresh code.
      'light-dev': 0
    },

    /*
     * Resource tags are functions that provide hints to the compiler
     * about a given file. The first argument is the filename of the
     * file, and the second argument is the module ID for the file.
     */
    resourceTags: {
      // Files that contain test code.
      test: function(filename, mid) {
        return false;
      },

      // Files that should be copied as-is without being modified by the
      // build system.
      copyOnly: function(filename, mid) {
        return copyOnly(mid);
      },

      // Files that are AMD modules
      // (for light basically any .js file inside the light package).
      amd: function(filename, mid) {
        return !copyOnly(mid) && /\.js$/.test(filename);
      },

      // Files that should not be copied when the “mini” compiler flag
      // is set to true.
      miniExclude: function(filename, mid) {
        return false;
      }
    }
  };
})();
