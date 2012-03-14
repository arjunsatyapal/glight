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
        'light/RegisterFormView'],
        function(RegisterFormController, PersonStore, RegisterFormView) {
  describe('light.controllers.RegisterFormController', function() {
    var controller, personStore, view;

    beforeEach(function() {
      this.stub(personStore = new PersonStore());
      this.stub(view = new RegisterFormView());
      controller = new RegisterFormController(personStore);
      controller.setView(view);
    });

    describe('onSubmit', function() {
      describe('when form is valid', function() {
        it('should disable the form', function() {

          var sampleData = {
            firstName: 'firstName',
            lastName: 'lastName'
          };

          view.validate.withArgs().returns(true);
          view.getData.withArgs().returns(sampleData);

          controller.onSubmit();

          expect(view.disable).toHaveBeenCalled();
          expect(personStore.newItem).toHaveBeenCalledWith(sampleData);
          expect(personStore.save).toHaveBeenCalled();

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
