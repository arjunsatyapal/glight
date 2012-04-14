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
define(['light/controllers/RegisterFormController', 'light/stores/PersonStore',
        'light/views/RegisterFormView', 'light/utils/URLUtils',
        'light/utils/PersonUtils', 'lightTest/TestUtils',
        'light/enums/PagesEnum'],
        function(RegisterFormController, PersonStore, RegisterFormView,
                 URLUtils, PersonUtils, TestUtils, PagesEnum) {
  describe('light.controllers.RegisterFormController', function() {
    var controller, personStore, view;

    beforeEach(function() {
      this.stub(personStore = new PersonStore());
      view = TestUtils.stubView(RegisterFormView);
      controller = new RegisterFormController(personStore);
      controller.setView(view);
    });

    describe('setup', function() {
      var SAMPLE_PERSON = {
        firstName: 'firstName',
        lastName: 'lastName'
      };
      beforeEach(function() {
        this.stub(PersonUtils, 'getCurrent').withArgs().returns(SAMPLE_PERSON);
      });
      describe('when we have a valid hash', function() {
        it('should properly set the redirectPath', function() {
          this.stub(URLUtils, 'getHash')
              .withArgs().returns('redirectPath=somePath');

          controller.setup();

          expect(controller._redirectPath).toBe('somePath');
        });
      });
      describe('when we don\'t have a valid hash', function() {
        it('should properly keep the redirectPath to default one', function() {
          this.stub(URLUtils, 'getHash').withArgs().returns('-');

          controller.setup();

          expect(controller._redirectPath).toBe(PagesEnum.SEARCH.path);
        });
      });


    });

    describe('onSubmit', function() {
      describe('when form is valid', function() {
        it('should disable the form and its callbacks should work', function() {

          var SAMPLE_FORM_DATA = {
            firstName: 'firstName',
            lastName: 'lastName'
          };
          var SAMPLE_STORED_DATA = {
            id: 'me',
            firstName: 'firstName',
            lastName: 'lastName',
            someOtherField: 'someOtherField'
          };
          controller._person = {
            someOtherField: 'someOtherField'
          };

          view.validate.withArgs().returns(true);
          view.getData.withArgs().returns(SAMPLE_FORM_DATA);

          controller.onSubmit();

          expect(view.disable).toHaveBeenCalled();
          expect(personStore.newItem).toHaveBeenCalledWith(SAMPLE_STORED_DATA);
          expect(personStore.save).toHaveBeenCalled();

          // Testing the callbacks
          var callbacks = personStore.save.args[0][0];

          // onComplete
          this.stub(URLUtils, 'redirect');
          callbacks.onComplete();
          expect(URLUtils.redirect)
              .toHaveBeenCalledWith(controller._redirectPath);

          // onError
          callbacks.onError();
          expect(view.warnError).toHaveBeenCalled();
          expect(view.enable).toHaveBeenCalled();

        });
      });

      describe('when form is not valid', function() {
        it('should not talk with the backend nor disable the form', function() {

          view.validate.withArgs().returns(false);

          controller.onSubmit();

          expect(view.disable).not.toHaveBeenCalled();
          expect(personStore.newItem).not.toHaveBeenCalled();
          expect(personStore.save).not.toHaveBeenCalled();

        });

      });
    });

  });
});
