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
define(['light/controllers/SearchResultListController', 'light/services/SearchService',
        'light/views/SearchResultListView', 'lightTest/TestUtils',
        'dojo/_base/connect', 'light/enums/EventsEnum',
        'light/schemas/SearchStateSchema'],
        function(SearchResultListController, SearchService,
                 SearchResultListView,
                 TestUtils, connect, EventsEnum, SearchStateSchema) {

  var ADDITION_QUERY_REQUEST = {
    query: 'addition',
    page: 1
  };

  var BLANK_QUERY_REQUEST = {
    query: '  ',
    page: 1
  };
  var EMPTY_QUERY_REQUEST = {
    query: '',
    page: 1
  };
  var ADDITION_QUERY_SAMPLE_RESULT = {
    some: 'Thing'
  };

  describe('lightTest.SearchResultListControllerTest inputs', function() {
    it('should be good', function() {
      TestUtils.validateSchema(ADDITION_QUERY_REQUEST, SearchStateSchema);
      TestUtils.validateSchema(BLANK_QUERY_REQUEST, SearchStateSchema);
      TestUtils.validateSchema(EMPTY_QUERY_REQUEST, SearchStateSchema);
    });
  });

  describe('light.controllers.SearchResultListController', function() {
    var controller, searchService, view;

    beforeEach(function() {
      this.stub(searchService = new SearchService());
      view = TestUtils.stubView(SearchResultListView);
      controller = new SearchResultListController(searchService);
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
      describe('when receiving a empty query request', function() {
        it('should clear the view', function() {
          controller._onSearchStateChange(EMPTY_QUERY_REQUEST);

          expect(view.clear).toHaveBeenCalled();
        });
      });
      describe('when receiving a blank query request', function() {
        it('should clear the view', function() {
          controller._onSearchStateChange(BLANK_QUERY_REQUEST);

          expect(view.clear).toHaveBeenCalled();
        });
      });
      describe('when receiving a valid query request', function() {
        it('should issue the request to the server and call view.show',
                function() {
          var successCallback;
          searchService.search.withArgs(ADDITION_QUERY_REQUEST).returns({
            then: function(cbk) {
              successCallback = cbk;
            }
          });

          controller._onSearchStateChange(ADDITION_QUERY_REQUEST);

          expect(searchService.search)
              .toHaveBeenCalledWith(ADDITION_QUERY_REQUEST);

          successCallback(ADDITION_QUERY_SAMPLE_RESULT);

          expect(view.show).toHaveBeenCalledWith(ADDITION_QUERY_REQUEST,
                  ADDITION_QUERY_SAMPLE_RESULT);
        });
      });
    });


  });
});
