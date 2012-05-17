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
define(['light/utils/BuilderUtils',
        'light/enums/BrowseContextsEnum'],
        function(BuilderUtils, BrowseContextsEnum) {
  // TODO(waltercacau): Create a schema for this
  /**
   * Builder for the Browse Context State.
   *
   * @class
   * @name light.builders.BrowseContextStateBuilder
   */
  return BuilderUtils.createBuilderClass(
      'light.builders.BrowseContextStateBuilder',
      ['context', 'subcontext'],
      {
        defaults: {
          context: BrowseContextsEnum.ALL
        },
        normalize: function() {
          if (this.context == BrowseContextsEnum.COLLECTION) {
            // we require a string because of a dojo limitation for now
            // in getNodesByItem
            // TODO(waltercacau): check if we can remove this limitation
            if (typeof this.subcontext != 'string' && typeof this.subcontext != 'number') {
              throw new Error('subcontext should be specified for collection');
            }
          }
          // Assuring that we were given a valid context
          for (var context in BrowseContextsEnum) {
            if (BrowseContextsEnum[context] == this.context) {
              return;
            }
          }
          throw new Error('Context not found');
        }
      }
  );
});
