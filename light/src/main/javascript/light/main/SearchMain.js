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
define(['light/views/SearchBarView', 'light/SearchRouter',
        'light/controllers/SearchBarController', 
        'light/controllers/SearchResultListController',
        'light/views/SearchResultListView', 'light/SearchService',
        'dojo/domReady!'],
        function(SearchBarView, SearchRouter, SearchBarController,
                 SearchResultListController, SearchResultListView,
                 SearchService) {
  searchResultListView = new SearchResultListView({}, 'searchResults');
  searchResultListController =
    new SearchResultListController(new SearchService());
  searchResultListController.setView(searchResultListView);
  searchResultListView.setController(searchResultListController);
  
  searchBarView = new SearchBarView({}, 'searchBar');
  searchBarController = new SearchBarController();
  searchBarController.setView(searchBarView);
  searchBarView.setController(searchBarController);
  
  searchRouter = new SearchRouter();
  /*
   * It is important that the searchBarController and
   * searchResultListController are wired to the event system before the
   * searchRouter, because the searchRouter will analyze the current
   * hash and dispatch an event for
   * SearchEventsEnum.SEARCH_STATE_CHANGED
   */
  searchResultListController.watch();
  searchBarController.watch();
  searchRouter.watch();
  
});
