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
         'light/views/LoginToolbarView',
         'light/utils/URLUtils',
         'light/utils/PersonUtils',
         'light/enums/PagesEnum',
         'lightTest/TestUtils',
         'dojo/text!testResources/person/json/create_person_response.json',
         'dojo/_base/json'],
         function(LoginToolbarController, LoginToolbarView, URLUtils,
                  PersonUtils, PagesEnum, TestUtils, SAMPLE_PERSON, JSON) {

   describe('light.controllers.LoginToolbarController', function() {
     var controller, view;

     beforeEach(function() {
       view = TestUtils.stubView(LoginToolbarView);
       controller = new LoginToolbarController();
       controller.setView(view);
     });

     describe('setup', function() {
       describe('when there is no person logged in', function() {
         it('should make the view show its unlogged form', function() {
           this.stub(PersonUtils, 'getCurrent').withArgs().returns(null);

           controller.setup();

           // You will need to change this test when adding more providers.
           expect(view.showUnloggedForm).toHaveBeenCalledWith(['google']);
         });
       });

       describe('when there is a person logged in', function() {
         it('should make the view show its logged form', function() {
           this.stub(PersonUtils, 'getCurrent')
               .withArgs().returns(JSON.fromJson(SAMPLE_PERSON));

           controller.setup();

           // You will need to change this test when adding more providers.
           expect(view.showLoggedForm)
               .toHaveBeenCalledWith('first name last name');
         });
       });
     });

     describe('login when called with a certain provider', function() {
       it('should redirect to the proper servlet', function() {
         this.stub(URLUtils, 'getPathWithHash').withArgs().returns('path#hash');
         this.stub(URLUtils, 'redirect');
         this.stub(PagesEnum, 'getCurrentPage').withArgs().returns(PagesEnum.REGISTER);

         controller.login('google');

         expect(URLUtils.redirect)
             .toHaveBeenCalledWith('/login/google?redirectPath=path%23hash');
       });
     });

     describe('logout', function() {
       it('should redirect to the proper servlet', function() {
         this.stub(URLUtils, 'redirect');

         controller.logout();

         expect(URLUtils.redirect)
             .toHaveBeenCalledWith('/logout');
       });
     });
   });
 });
