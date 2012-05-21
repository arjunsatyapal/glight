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
define({
  /**
   * Search state changed.
   *
   * <p>Example of publish call (connect is 'dojo/_base/connect'):
   *
   * <pre>
   * PubSubUtils.publish(EventsEnum.SEARCH_STATE_CHANGED, [{
   *    query: 'myquery', // the query value
   *    page: 1 // The page
   *  }, source // the source of this event
   *  ]);
   * </pre>
   * <p>We specify the source because some of the sources are also
   * subscribed for the same event and embedding the source in the callback
   * arguments allows such sources to ignore their own published events.
   *
   * @const
   */
  SEARCH_STATE_CHANGED: '/light/search/searchStateChanged',
  BROWSE_CONTEXT_STATE_CHANGED: '/light/mydash/browseContextStateChanged',
  REDIRECT_STATE_CHANGED: '/light/register/redirectStateChanged'
});
