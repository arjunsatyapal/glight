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
        'light/views/LoginToolbarView',
        'light/controllers/LoginToolbarController',
        'dojo/domReady!'],
        function(SearchBarView, SearchRouter, SearchBarController,
                 SearchResultListController, SearchResultListView,
                 SearchService, LoginToolbarView, LoginToolbarController) {
  var searchResultListView = new SearchResultListView({}, 'searchResults');
  var searchResultListController =
    new SearchResultListController(new SearchService());
  searchResultListController.setView(searchResultListView);
  searchResultListView.setController(searchResultListController);

  var searchBarView = new SearchBarView({}, 'searchBar');
  var searchBarController = new SearchBarController();
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

  var loginToolbarView = new LoginToolbarView({}, 'loginToolbar');
  var loginToolbarController = new LoginToolbarController();
  loginToolbarController.setView(loginToolbarView);
  loginToolbarView.setController(loginToolbarController);
  loginToolbarController.setup();

});
