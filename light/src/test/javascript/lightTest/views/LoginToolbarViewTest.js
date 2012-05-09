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
 define(['light/controllers/LoginToolbarController',
         'light/views/LoginToolbarView', 'lightTest/robot',
         'light/utils/DOMUtils'],
         function(LoginToolbarController, LoginToolbarView, robot, DOMUtils) {
   describe('light.views.LoginToolbarView', function() {
     var controller, view;

     beforeEach(function() {
       this.spy(DOMUtils, 'show');
       this.spy(DOMUtils, 'setText');
       this.spy(DOMUtils, 'hide');
       this.stub(controller = new LoginToolbarController());
       view = new LoginToolbarView();
       view.setController(controller);
       robot.attach(view);
     });

     describe('when contructed', function() {
       it('should not throw and not show its contents', function() {
         expect(DOMUtils.hide).toHaveBeenCalledWith(view._unloggedDiv);
         expect(DOMUtils.hide).toHaveBeenCalledWith(view._loggedDiv);
       });
     });

     describe('showLoggedForm when called with a personName', function() {
       it('should show the logged subview and fill the personNameSpan',
               function() {

         view.showLoggedForm('unit test');

         expect(DOMUtils.setText).toHaveBeenCalledWith(view._personNameSpan,
                 'unit test');
         expect(DOMUtils.hide).toHaveBeenCalledWith(view._unloggedDiv);
         expect(DOMUtils.show).toHaveBeenCalledWith(view._loggedDiv);
       });
     });

     describe('showUnloggedForm when called with a list of providers',
             function() {
       it('should not throw and show one button for each provider', function() {
         var providerList = ['provider1', 'provider2'];
         view.showUnloggedForm(providerList);
         for (var i = 0, len = providerList.length; i < len; i++) {
           var id = view.id + '_' + providerList[i] +
             'LoginProviderButton';
           expect(robot.count('#'+id)).toBe(1);
         }
       });
     });

     describe('_createLoginClickCallback when called with a provider',
             function() {
       it('should return a callback that correctly calls the controller',
               function() {
         view._createLoginClickCallback('provider')();

         expect(controller.login).toHaveBeenCalledWith('provider');
       });
     });

     describe('_logout when called', function() {
       it('should delegate to the controller', function() {
         view._logout();
         expect(controller.logout).toHaveBeenCalled();
       });
     });

   });
 });
