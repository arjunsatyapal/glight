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
define(['dojo/i18n!light/nls/ContentViewerMessages', 'dojo/query',
        'light/utils/DOMUtils',
        'dojox/html/entities',
        'dojo/domReady!'],
        function(messages, $, DOMUtils, htmlEntities) {
  $("a.previousLink").forEach(function(node) {
    DOMUtils.setText(node, messages.previous);
  });
  $("a.nextLink").forEach(function(node) {
    DOMUtils.setText(node, messages.next);
  });
  $(".collectionToolbar").forEach(function(node) {
    node.innerHTML = htmlEntities.encode(messages.inCollection).replace("${collection}", node.innerHTML);
  });
  $("a.collectionStartLink").forEach(function(node) {
    DOMUtils.setText(node, messages.start);
  });
  $(".hiddenNode").forEach(function(node) {
    DOMUtils.show(node);
  });
});
