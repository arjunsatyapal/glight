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
define(['light/views/ActivityIndicatorView',
        'light/controllers/ActivityIndicatorController',
        'light/views/SearchBarView',
        'light/controllers/SearchBarController',
        'light/views/ImportModuleView',
        'light/controllers/ImportModuleController',
        'light/views/ImportedByMeView',
        'light/controllers/ImportedByMeController',
        'light/views/SearchResultListView',
        'light/controllers/SearchResultListController',
        'light/services/SearchService',
        'light/views/LoginToolbarView',
        'light/controllers/LoginToolbarController',
        'light/views/MyDashSidebarView',
        'light/controllers/MyDashSidebarController',
        'light/views/CollectionEditorView',
        'light/controllers/CollectionEditorController',
        'light/services/CollectionService',
        'light/services/ImportService',
        'light/utils/PersonUtils',
        'light/utils/URLUtils',
        'light/enums/PagesEnum',
        'dojo/domReady!'],
        function(ActivityIndicatorView, ActivityIndicatorController,
                 SearchBarView, SearchBarController,
                 ImportModuleView, ImportModuleController,
                 ImportedByMeView, ImportedByMeController,
                 SearchResultListView, SearchResultListController,
                 SearchService,
                 LoginToolbarView, LoginToolbarController,
                 MyDashSidebarView, MyDashSidebarController,
                 CollectionEditorView, CollectionEditorController,
                 CollectionService, ImportService, PersonUtils,
                 URLUtils, PagesEnum) {

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

  var collectionService = new CollectionService();
  var importService = new ImportService();
  var myDashSidebarView = new MyDashSidebarView({}, 'sidebar');
  var myDashSidebarController = new MyDashSidebarController(collectionService, importService);
  myDashSidebarController.setView(myDashSidebarView);
  myDashSidebarView.setController(myDashSidebarController);
  myDashSidebarController.watch();

  var collectionEditorView = new CollectionEditorView({}, 'collectionEditor');
  var collectionEditorController = new CollectionEditorController(collectionService);
  collectionEditorController.setView(collectionEditorView);
  collectionEditorView.setController(collectionEditorController);
  collectionEditorController.watch();

  var importModuleView = new ImportModuleView({}, 'importModule');
  var importModuleController = new ImportModuleController(importService);
  importModuleController.setView(importModuleView);
  importModuleView.setController(importModuleController);
  importModuleController.watch();

  var importedByMeView = new ImportedByMeView({}, 'importedByMe');
  var importedByMeController = new ImportedByMeController();
  importedByMeController.setView(importedByMeView);
  importedByMeView.setController(importedByMeController);
  importedByMeController.watch();

  var activityIndicatorView = new ActivityIndicatorView({}, 'activityIndicator');
  var activityIndicatorController = new ActivityIndicatorController();
  activityIndicatorController.setView(activityIndicatorView);
  activityIndicatorView.setController(activityIndicatorController);
  activityIndicatorController.watch();
});
