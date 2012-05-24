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
define(['dojo/_base/declare', 'dojo/_base/array', 'light/utils/XHRUtils',
        'dojo', 'light/utils/DialogUtils'],
        function(declare, array, XHRUtils, dojo, DialogUtils) {
  return declare('light.services.ImportService', null, {
    import_: function(list) {
      return this._takeCareOfErrors(XHRUtils.post({
        url: '/rest/import/batch',
        jsonData: {
          list: list
        }
      }));
    },
    addToCollection: function(list, collectionId, collectionTitle) {
      // TODO(waltercacau): Remove the collectionTitle requirement
      return this._takeCareOfErrors(XHRUtils.post({
        url: '/rest/import/batch',
        jsonData: {
          list: list,
          collectionId: collectionId,
          collectionTitle: collectionTitle,
          baseVersion: -1
        }
      }));
    },
    _takeCareOfErrors: function(promise) {
      return promise.then(function(result) {
            /*DialogUtils.alert({
            title: "Error importing",
            content: "An error ocurred when trying to import. Please try again later."
          });*/
          var failedList = [];
          array.forEach(result.list, function(item) {
            if (item.moduleState == 'FAILED') {
              failedList.push(item.title || item.externalId);
            }
          });
          if (failedList.length > 0) {
            DialogUtils.alert({
              title: 'Error importing',
              content: 'The following could not be imported:\n'+ failedList.join('\n')
            });
            throw new Error('Failed to import some');
          }
          return result;
        }, function(err) {
          DialogUtils.alert({
            title: 'Error importing',
            content: 'An error ocurred when trying to import. Please try again later.'
          });
          throw err;
        });
    }
  });
});
