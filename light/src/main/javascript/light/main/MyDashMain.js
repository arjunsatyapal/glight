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
define(['light/views/SearchBarView',
        'light/controllers/SearchBarController',
        'light/controllers/SearchResultListController',
        'light/views/SearchResultListView', 'light/SearchService',
        'light/views/LoginToolbarView',
        'light/controllers/LoginToolbarController',
        'light/views/MyDashSidebarView',
        'light/controllers/MyDashSidebarController',
        'light/utils/PersonUtils',
        'light/utils/URLUtils',
        'light/enums/PagesEnum',
        'dojo/domReady!'],
        function(SearchBarView, SearchBarController,
                 SearchResultListController, SearchResultListView,
                 SearchService, LoginToolbarView, LoginToolbarController,
                 MyDashSidebarView, MyDashSidebarController,
                 PersonUtils, URLUtils, PagesEnum) {

  // Going to the anonymous search page
  if (!PersonUtils.isLogged()) {
    URLUtils.replace(PagesEnum.SEARCH.path);
  }

  var searchResultListView = new SearchResultListView({}, 'searchResults');
  var searchResultListController =
    new SearchResultListController(new SearchService());
  searchResultListController.setView(searchResultListView);
  searchResultListView.setController(searchResultListController);

  var searchBarView = new SearchBarView({}, 'searchBar');
  var searchBarController = new SearchBarController();
  searchBarController.setView(searchBarView);
  searchBarView.setController(searchBarController);

  searchResultListController.watch();
  searchBarController.watch();

  var loginToolbarView = new LoginToolbarView({}, 'loginToolbar');
  var loginToolbarController = new LoginToolbarController();
  loginToolbarController.setView(loginToolbarView);
  loginToolbarView.setController(loginToolbarController);
  loginToolbarController.setup();

  var myDashSidebarView = new MyDashSidebarView({}, 'sidebar');
  var myDashSidebarController = new MyDashSidebarController();
  myDashSidebarController.setView(myDashSidebarView);
  myDashSidebarView.setController(myDashSidebarController);
  myDashSidebarController.watch();

});
