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
define(['dojo/_base/declare', 'light/views/AbstractLightView',
        'dojo/text!light/templates/SearchResultListItemTemplate.html',
        'light/utils/TemplateUtils',
        'light/SearchRouter',
        'dojox/html/entities',
        'dojo/_base/lang',
        'dojo/string',
        'dojo/dom-construct',
        'dojo/i18n!light/nls/SearchPageMessages'],
        function(declare, AbstractLightView, itemTemplate, TemplateUtils,
                 SearchRouter, htmlEntities, lang, string,
                 domConstruct, messages) {
  return declare('light.views.SearchResultListView', AbstractLightView, {
    clear: function() {
      domConstruct.empty(this.domNode);
    },
    show: function(request, data) {

      this.clear();

      if (data.suggestion) {
        var suggestionRequest = lang.mixin(lang.clone(request), {
          query: data.suggestionQuery
        });

        this.domNode.appendChild(TemplateUtils.toDom(
                '<div class="searchInfo">' +
                string.substitute(htmlEntities.encode(messages.didYouMeanBox), {
                  suggestion: '<a href=\"${link}\">${!suggestion}</a>'
                }) +
                '</div>',
                {
                  suggestion: data.suggestion,
                  link: '#' + SearchRouter.searchStateToHash(suggestionRequest)
                }));
      }
      var items = data.items;
      var len = items.length;
      if (len == 0) {
        this.domNode.appendChild(TemplateUtils.toDom(
                '<div class="searchInfo">${noResults}</div>', messages));
      } else {
        for (var i = 0; i < len; i++) {
          this.domNode.appendChild(TemplateUtils.toDom(itemTemplate, items[i]));
        }
        
        
        var pageInfo = domConstruct.toDom('<div class="searchInfo"></div>')
        if(request.page > 1) {
          var prevPageRequest = lang.mixin(lang.clone(request), {
            page: request.page-1
          });
          pageInfo.appendChild(TemplateUtils.toDom(
                  '<a href="${link}">${messages.shortPrevious}</a> | ',
                  {
                    messages: messages,
                    link: "#"+SearchRouter.searchStateToHash(prevPageRequest)
                  }));
        }
        
        pageInfo.appendChild(domConstruct.toDom(
                htmlEntities.encode(string.substitute(messages.pageInfo, {
                  page: request.page
                }))));
        
        if(data.hasNextPage) {
          var nextPageRequest = lang.mixin(lang.clone(request), {
            page: request.page+1
          });
          pageInfo.appendChild(TemplateUtils.toDom(
                  ' | <a href="${link}">${messages.shortNext}</a>',
                  {
                    messages: messages,
                    link: "#"+SearchRouter.searchStateToHash(nextPageRequest)
                  }));
        }
        this.domNode.appendChild(pageInfo);
      }
    }
  });
});
