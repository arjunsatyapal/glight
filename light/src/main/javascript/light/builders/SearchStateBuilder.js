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
        'light/RegexCommon',
        'dojo/has!light-dev?light/schemas/SearchStateSchema'],
        function(BuilderUtils, RegexCommon, schema) {
  /**
   * Builder for the Search State
   *
   * @class
   * @name light.builders.SearchStateBuilder
   */
  return BuilderUtils.createBuilderClass(
      'light.builders.SearchStateBuilder',
      ['query', 'page'],
      {
        defaults: {
          query: '',
          page: 1
        },
        schema: schema,
        normalize: function() {
          if (!(''+ this.page).match(RegexCommon.INTEGER)) {
            throw new Error('Page is not an integer');
          }
          this.page = parseInt(''+ this.page, 10);
          if (this.page <= 0) {
            throw new Error('Page is less then 1');
          }
          if (typeof this.query != 'string') {
            throw new Error('Query is not a string');
          }
          return this;
        }
      }

  );
});
