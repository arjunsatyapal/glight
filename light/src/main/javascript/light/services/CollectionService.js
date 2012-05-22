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
  return declare('light.services.CollectionService', null, {
    get: function(collectionId) {
      return XHRUtils.get({
        url: '/rest/collection/' + collectionId
      });
    },
    getVersion: function(collectionId, version, ioPublish) {
      if(typeof ioPublish === "undefined") {
        ioPublish = true;
      }
      return XHRUtils.get({
        url: '/rest/collection/' + collectionId + '/' + version,
        ioPublish: ioPublish
      });
    },
    create: function(collection) {
      // TODO(waltercacau): Fix this hack of using the body as the title.
      return XHRUtils.post({
        url: '/rest/collection',
        postData: collection.title
      });
    },
    updateLatest: function(collectionId, collectionVersion) {
      return XHRUtils.put({
        url: '/rest/collection/' + collectionId + '/latest',
        jsonData: collectionVersion
      });
    }
  });
});
