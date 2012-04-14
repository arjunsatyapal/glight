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
/**
 * Utilities for handling with the browser's URL
 *
 * @class
 * @name light.enums.PagesEnum
 */
define({
  /** @lends light.enums.PagesEnum */

  REGISTER: {
    build: 'register',
    main: 'RegisterMain',
    path: '/register'
  },
  SEARCH: {
    build: 'search',
    main: 'SearchMain',
    path: '/search'
  },

  /**
   * Get's the page for a given path.
   *
   * @param path
   * @return
   */
  getByPath: function(path) {
    // For now relying on the pattern applied
    var pageMatch = path.match(/\/([a-zA-Z0-9-_]+)$/);
    if (pageMatch) {
      var page = this[pageMatch[1].toUpperCase()];
      if (page)
        return page;
    }
    return null;
  }
});
