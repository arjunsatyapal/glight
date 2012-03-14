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
define(['light/views/RegisterFormView',
        'light/controllers/RegisterFormController',
        'dojo/query', 'lightTest/robot'],
        function(RegisterFormView, RegisterFormController, query, robot) {
  describe('light.views.RegisterFormView', function() {
    var controller, view;

    beforeEach(function() {
      this.stub(controller = new RegisterFormController());
      view = new RegisterFormView();
      view.setController(controller);
      robot.attach(view);
    });

    describe('_button when clicked', function() {
      it('should call controller.onSubmit', function() {
        waitsFor(robot.click(view._button));
        runs(function() {
          expect(controller.onSubmit).toHaveBeenCalled();
        });
      });
    });

    describe('_tosCheckbox initially', function() {
      it('should not be checked', function() {
        expect(view._tosCheckbox.get('checked')).toBeFalsy();
      });
    });

    describe('_tosValidation', function() {
      describe('when the _tosCheckbox is unchecked', function() {
        it('should return false', function() {
          view._tosCheckbox.set('checked', false);
          expect(view._tosValidation()).toBeFalsy();
        });
      });
      describe('when the _tosCheckbox is checked', function() {
        it('should return true', function() {
          view._tosCheckbox.set('checked', true);
          expect(view._tosValidation()).toBeTruthy();
        });
      });
    });

    describe('', function() {
      beforeEach(function() {
        view._firstNameTextBox.set('value', 'FirstName');
        view._lastNameTextBox.set('value', 'LastName');
        view._tosCheckbox.set('checked', true);
      });

      describe('validate', function() {
        describe('when all fields are correct', function() {
          it('should return true', function() {
            expect(view.validate()).toBeTruthy();
          });
        });
        describe('when firstName is empty', function() {
          it('should return false', function() {
            view._firstNameTextBox.set('value', '');
            expect(view.validate()).toBeFalsy();
          });
        });
        describe('when lastName is empty', function() {
          it('should return false', function() {
            view._lastNameTextBox.set('value', '');
            expect(view.validate()).toBeFalsy();
          });
        });
        describe('when the _tosCheckbox is unchecke', function() {
          it('should return false', function() {
            view._tosCheckbox.set('checked', false);
            expect(view.validate()).toBeFalsy();
          });
        });
      });

      describe('getData', function() {
        it('should return the current filled data', function() {
          expect(view.getData()).toEqual({
            firstName: 'FirstName',
            lastName: 'LastName'
          });
        });
      });
    });

  });
});
