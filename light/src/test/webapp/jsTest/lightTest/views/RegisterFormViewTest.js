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
define(['light/views/RegisterFormView', 'light/controllers/RegisterFormController',
        'dojo/query', 'lightTest/robot'],
        function(RegisterFormView, RegisterFormController, query, robot) {
  describe('light.views.RegisterFormView', function() {
    var controller, view;
    
    beforeEach(function() {
      this.stub(controller = new RegisterFormController());
      view = new RegisterFormView();
      view.setController(controller);
      robot.attach(view);
      document.body.appendChild(view.domNode);
    });

    describe('_button when clicked', function() {
      it('should call controller.onSubmit', function() {
        waitsFor(robot.click(view._button));
        runs(function() {
          expect(controller.onSubmit).toHaveBeenCalled();
        });
      });
    });

  });
});
