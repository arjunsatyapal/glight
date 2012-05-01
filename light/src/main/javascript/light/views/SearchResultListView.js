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
        'dojox/html/entities',
        'dojo/_base/lang',
        'dojo/string',
        'dojo/dom-construct',
        'light/utils/LanguageUtils',
        'light/builders/SearchStateBuilder',
        'light/utils/RouterManager',
        'light/enums/EventsEnum',
        'dojo/i18n!light/nls/SearchPageMessages',
        'light/widgets/ListWidget'],
        function(declare, AbstractLightView, itemTemplate, TemplateUtils,
                 htmlEntities, lang, string,
                 domConstruct, LanguageUtils, SearchStateBuilder, RouterManager,
                 EventsEnum, messages, ListWidget) {
  return declare('light.views.SearchResultListView', AbstractLightView, {
    _searchResultList: null,
    destroy: function() {
      if (this._searchResultList) {
        this._searchResultList.destroy();
        this._searchResultList = null;
      }
      this.inherited(arguments);
    },
    clear: function() {
      if (this._searchResultList) {
        this._searchResultList.destroy();
        this._searchResultList = null;
      }
      domConstruct.empty(this.domNode);
    },
    show: function(request, data) {

      this.clear();

      // Showing suggestions if any
      if (data.suggestion) {
        var suggestionRequest = new SearchStateBuilder()
            .query(data.suggestionQuery).build();

        this.domNode.appendChild(TemplateUtils.toDom(
                '<div class="searchInfo">' +
                string.substitute(htmlEntities.encode(messages.didYouMeanBox), {
                  suggestion: '<a href=\"${link}\">${!suggestion}</a>'
                }) +
                '</div>',
                {
                  suggestion: data.suggestion,
                  link: RouterManager.buildLocalLink(
                          EventsEnum.SEARCH_STATE_CHANGED, suggestionRequest)
                }));
      }

      // Showing items or no results message
      var items = data.items;
      var len = items.length;
      if (len == 0) {
        this.domNode.appendChild(TemplateUtils.toDom(
                '<div class="searchInfo">${noResults}</div>', messages));
      } else {
        // Showing items
        var searchResultListDomNode = domConstruct.toDom('<div></div>');
        this.domNode.appendChild(searchResultListDomNode);

        this._searchResultList = new ListWidget(searchResultListDomNode, {
          accept: [],
          type: 'searchResultDndSource',
          selfAccept: false,
          copyOnly: true,
          selfCopy: false,
          rawCreator: function(item) {
            var node = TemplateUtils.toDom(itemTemplate, item);
            return {
              node: node,
              data: item,
              focusNode: dojo.query('a', node)[0],
              type: ['searchResult']
            };
          }
        });

        this._searchResultList.insertNodes(false /* addSelected */, items);
        this._searchResultList.focusFirstItem();

        var pageInfo = domConstruct.toDom('<div class="searchInfo"></div>');

        // Prev page link
        if (request.page > 1) {
          var prevPageState = new SearchStateBuilder(request, true)
              .page(request.page - 1).build();
          pageInfo.appendChild(TemplateUtils.toDom(
                  '<a href="${link}">${messages.previous}</a> | ',
                  {
                    messages: messages,
                    link: RouterManager.buildLocalLink(
                            EventsEnum.SEARCH_STATE_CHANGED, prevPageState)
                  }));
        }

        // Current page number
        pageInfo.appendChild(domConstruct.toDom(
                htmlEntities.encode(string.substitute(messages.pageInfo, {
                  page: LanguageUtils.formatNumber(request.page)
                }))));

        // Next page link
        if (data.hasNextPage) {
          var nextPageState = new SearchStateBuilder(request, true)
              .page(request.page + 1).build();
          pageInfo.appendChild(TemplateUtils.toDom(
                  ' | <a href="${link}">${messages.next}</a>',
                  {
                    messages: messages,
                    link: RouterManager.buildLocalLink(
                            EventsEnum.SEARCH_STATE_CHANGED, nextPageState)
                  }));
        }

        this.domNode.appendChild(pageInfo);
        
        
        
      }
    }
  });
});
