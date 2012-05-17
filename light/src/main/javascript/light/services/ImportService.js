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
define(['dojo/_base/declare', 'light/utils/XHRUtils', 'dojo'],
        function(declare, XHRUtils, dojo) {
  return declare('light.services.ImportService', null, {
    import_: function(list) {
      return XHRUtils.post({
        url: '/rest/import/batch',
        jsonData: {
          list: list
        }
      });
    },
    addToCollection: function(list, collectionId, collectionTitle) {
      // TODO(waltercacau): Remove the collectionTitle requirement
      return XHRUtils.post({
        url: '/rest/import/batch',
        jsonData: {
          list: list,
          collectionId: collectionId,
          collectionTitle: collectionTitle,
          baseVersion: -1
        }
      });
    }
  });
});
