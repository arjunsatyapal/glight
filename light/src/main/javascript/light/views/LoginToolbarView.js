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
define(['dojo/_base/declare', 'light/views/TemplatedLightView',
    'dojo/i18n!light/nls/LoginToolbarMessages',
    'dijit/_WidgetsInTemplateMixin',
    'dojo/text!light/templates/LoginToolbarTemplate.html',
    'dojo', 'light/utils/DOMUtils', 'dijit/form/Button', 'dojo/_base/NodeList',
    'dijit/form/DropDownButton', 'dijit/TooltipDialog'],
    function(declare, TemplatedLightView,
        messages, _WidgetsInTemplateMixin, template, dojo,
        DOMUtils, Button, NodeList) {

  /**
   * @class
   * @name light.views.LoginToolbarView
   */
  return declare('light.views.LoginToolbarView', [TemplatedLightView,
      _WidgetsInTemplateMixin], {
    /** @lends light.views.LoginToolbarView# */
    templateString: template,
    messages: messages,
    _earlyTemplatedStartup: true,

    postCreate: function() {
      DOMUtils.hide(this._unloggedDiv);
      DOMUtils.hide(this._loggedDiv);
    },

    /**
     * Shows the logged form of this view
     * @param {string} personName Person's name to be displayed.
     */
    showLoggedForm: function(personName) {
      DOMUtils.hide(this._unloggedDiv);
      DOMUtils.show(this._loggedDiv);
      DOMUtils.setText(this._personNameSpan, personName);
    },

    /**
     * Show's the unlogged form of this view.
     *
     * Note that we will show a dropdown selection of login provider's
     * and for presentation purposes this method will look for an
     * provider's representation in
     * {@link light.views.LoginToolbarView#messages}
     * with the key {providername}LoginProvider
     *
     * @param {Array.<string>} providers Login Provider's list.
     */
    showUnloggedForm: function(providers) {
      var providerButtonNodes = [];
      for (var i = 0, len = providers.length; i < len; i++) {
        var provider = providers[i];
        providerButtonNodes.push(new Button({
          id: this.id + '_' + provider + 'LoginProviderButton',
          onClick: this._createLoginClickCallback(provider),
          label: this.messages[provider + 'LoginProvider']
        }).domNode);
      }
      this._loginDialog.set('content', providerButtonNodes);
      DOMUtils.show(this._unloggedDiv);
      DOMUtils.hide(this._loggedDiv);
    },

    /**
     * Utility method used by
     * {@link light.views.LoginToolbarView#showUnloggedForm}
     * to create the callback for the particular login provider button.
     *
     * @param {string} provider Login Provider.
     */
    _createLoginClickCallback: function(provider) {
      var controller = this._controller;
      return function() { controller.login(provider); };
    },

    /**
     * Callback invoked when the Logout button is pressed.
     */
    _logout: function() {
      this._controller.logout();
    }
  });

});
