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
define(['light/controllers/SearchBarController',
        'light/views/SearchBarView', 'lightTest/TestUtils',
        'dojo/_base/connect', 'light/enums/EventsEnum',
        'light/schemas/SearchStateSchema'],
        function(SearchBarController,
                 SearchBarView,
                 TestUtils, connect, EventsEnum, SearchStateSchema) {

  var ADDITION_QUERY = 'addition';
  var ADDITION_QUERY_REQUEST = {
    query: ADDITION_QUERY,
    page: 1
  };
  var ANOTHER_SOURCE = {};

  describe('lightTest.SearchBarControllerTest inputs', function() {
    it('should be good', function() {
      TestUtils.validateSchema(ADDITION_QUERY_REQUEST, SearchStateSchema);
    });
  });

  describe('light.controllers.SearchBarController', function() {
    var controller, view;

    beforeEach(function() {
      view = TestUtils.stubView(SearchBarView);
      controller = new SearchBarController();
      controller.setView(view);
    });

    describe('watch when called', function() {
      it('should wire the object to the dojo event system to watch' +
         'search state changes', function() {
        var subscribeStub = this.stub(connect, 'subscribe');

        controller.watch();

        expect(subscribeStub).toHaveBeenCalledWith(
          EventsEnum.SEARCH_STATE_CHANGED,
          controller, controller._onSearchStateChange
        );

      });
    });

    describe('_onSearchStateChange', function() {
      describe('when receiving a search state change event from another source',
              function() {
        it('should set the query in the view', function() {
          controller._onSearchStateChange(ADDITION_QUERY_REQUEST,
                  ANOTHER_SOURCE);

          expect(view.setQuery).toHaveBeenCalledWith(ADDITION_QUERY);
        });
      });
      describe('when receiving a search state change event from itself',
              function() {
        it('should set the query in the view', function() {
          controller._onSearchStateChange(ADDITION_QUERY_REQUEST, controller);

          expect(view.setQuery).not.toHaveBeenCalled();
        });
      });
    });

    describe('onSubmit when called', function() {
      it('should publish a search state change event', function() {
        var publishStub = this.stub(connect, 'publish');
        view.getQuery.withArgs().returns(ADDITION_QUERY);

        controller.onSubmit();

        expect(publishStub)
            .toHaveBeenCalledWith(EventsEnum.SEARCH_STATE_CHANGED, [{
              query: ADDITION_QUERY,
              page: 1 // important to be 1!
            }, controller]);

      });
    });

  });
});
