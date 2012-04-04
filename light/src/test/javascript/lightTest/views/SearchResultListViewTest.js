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
define(['light/views/SearchResultListView',
        'light/controllers/SearchResultListController', 'dojo/_base/event',
        'dojo/query', 'lightTest/robot', 'dojo/dom-construct',
        'dojo/text!testResources/search/gss/addition_query_output.json',
        'dojo/text!testResources/search/gss/addxition_query_output.json',
        'dojo/_base/json', 'lightTest/TestUtils',
        'light/schemas/SearchStateSchema'],
        function(SearchResultListView, SearchResultListController,
                 eventUtil, query, robot, domConstruct,
                 ADDITION_QUERY_RESULT,
                 ADDXITION_QUERY_RESULT,
                 JSON, TestUtils, SearchStateSchema) {

  var ADDITION_QUERY_REQUEST = {
    query: 'addition',
    page: 1
  };
  var ADDXITION_QUERY_REQUEST = {
    query: 'addxition',
    page: 1
  };

  describe('lightTest.SearchResultListViewTest inputs', function() {
    it('should be good', function() {
      TestUtils.validateSchema(ADDITION_QUERY_REQUEST, SearchStateSchema);
      TestUtils.validateSchema(ADDXITION_QUERY_REQUEST, SearchStateSchema);
    });
  });

  describe('light.views.SearchResultListView', function() {
    var controller, view;

    beforeEach(function() {
      this.stub(controller = new SearchResultListController());
      view = new SearchResultListView();
      view.setController(controller);
      robot.attach(view);
    });

    describe('show', function() {
      describe('when called with search results for addition query',
               function() {
        it('should not throw', function() {
          view.show(ADDITION_QUERY_REQUEST,
                    JSON.fromJson(ADDITION_QUERY_RESULT));
        });
      });
      describe('when called with search results for addxition query',
               function() {
        it('should not throw', function() {
          view.show(ADDXITION_QUERY_REQUEST,
                    JSON.fromJson(ADDXITION_QUERY_RESULT));
        });
      });
    });

    describe('clear', function() {
      describe('when called', function() {
        it('should safely empty this.domNode', function() {
          var emptyStub = this.stub(domConstruct, 'empty');

          view.clear();

          expect(emptyStub).toHaveBeenCalledWith(view.domNode);
        });
      });
    });

  });
});
