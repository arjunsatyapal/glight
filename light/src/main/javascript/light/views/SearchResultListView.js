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
define(['dojo/_base/declare',
        'light/views/TemplatedLightView',
        'dojo/text!light/templates/SearchResultListTemplate.html',
        'dojo/text!light/templates/SearchResultListItemTemplate.html',
        'dojo/text!light/templates/RecentSearchResultListItemTemplate.html',
        'light/utils/TemplateUtils',
        'dojox/html/entities',
        'dojo/_base/lang',
        'dojo',
        'dojo/string',
        'light/utils/DOMUtils',
        'dojo/dom-construct',
        'dojo/dom-class',
        'light/utils/LanguageUtils',
        'light/builders/SearchStateBuilder',
        'light/utils/RouterManager',
        'light/enums/EventsEnum',
        'dojo/i18n!light/nls/SearchPageMessages',
        'light/widgets/ListWidget',
        'light/builders/ModuleDndTypeBuilder',
        'light/enums/DndConstantsEnum'],
        function(declare, TemplatedLightView, template, itemTemplate,
                 recentItemTemplate,
                 TemplateUtils, htmlEntities, lang, dojo, string, DOMUtils,
                 domConstruct, domClass, LanguageUtils, SearchStateBuilder,
                 RouterManager,
                 EventsEnum, messages, ListWidget, ModuleDndTypeBuilder,
                 DndConstantsEnum) {
  return declare('light.views.SearchResultListView', TemplatedLightView, {
    _searchResultList: null,
    _recentSearchResultList: null,
    templateString: template,
    messages: messages,
    destroy: function() {
      if (this._searchResultList) {
        this._searchResultList.destroy();
        this._searchResultList = null;
      }
      this.inherited(arguments);
    },
    clear: function() {
      this.clearSearch();
      this.clearRecentSearch();
    },
    clearSearch: function() {
      if (this._searchResultList) {
        this._searchResultList.destroy();
        this._searchResultList = null;
      }
      domConstruct.empty(this._searchResultsNode);
    },
    clearRecentSearch: function() {
      domClass.remove(this.domNode, 'withRecentSearchResults');
      if (this._recentSearchResultList) {
        this._recentSearchResultList.destroy();
        this._recentSearchResultList = null;
      }
      domConstruct.empty(this._recentSearchResultsNode);
      DOMUtils.hide(this._recentSearchResultsBoxNode);
    },
    _removeSelectionFromRecentSearchResultList: function() {
      if(this._recentSearchResultList !== null) {
        this._recentSearchResultList.selectNoneWithoutTrigger();
      }
    },
    _removeSelectionFromSearchResultList: function() {
      if(this._searchResultList !== null) {
        this._searchResultList.selectNoneWithoutTrigger();
      }
    },
    showRecent: function(data) {
      var items = data.list;
      this.clearRecentSearch();
      if (items.length === 0) {
        return;
      }
      DOMUtils.show(this._recentSearchResultsBoxNode);
      domClass.add(this.domNode, 'withRecentSearchResults');
      var searchResultListDomNode = domConstruct.toDom('<div></div>');
      this._recentSearchResultsNode.appendChild(searchResultListDomNode);
      this._recentSearchResultList = new ListWidget(searchResultListDomNode, {
        accept: [],
        type: DndConstantsEnum.MODULE_SOURCE_TYPE,
        selfAccept: false,
        copyOnly: true,
        selfCopy: false,
        rawCreator: function(item) {
          var node = TemplateUtils.toDom(recentItemTemplate, item);
          return {
            node: node,
            data: new ModuleDndTypeBuilder()
                .title(DOMUtils.asText(item.title))
                .externalId(item.link)
                .rawData(item)
                .build(),
            focusNode: dojo.query('a', node)[0],
            type: [DndConstantsEnum.MODULE_TYPE]
          };
        },
        onSelectionChange: lang.hitch(this, this._removeSelectionFromSearchResultList)
      });

      this._recentSearchResultList.insertNodes(false /* addSelected */, items);
    },
    show: function(request, data) {
      this.clearSearch();

      // Showing suggestions if any
      if (data.suggestion) {
        var suggestionRequest = new SearchStateBuilder()
            .query(data.suggestionQuery).build();

        this._searchResultsNode.appendChild(TemplateUtils.toDom(
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
      if (len === 0) {
        this._searchResultsNode.appendChild(TemplateUtils.toDom(
                '<div class="searchInfo">${noResults}</div>', messages));
      } else {
        // Showing items
        var searchResultListDomNode = domConstruct.toDom('<div></div>');
        this._searchResultsNode.appendChild(searchResultListDomNode);

        this._searchResultList = new ListWidget(searchResultListDomNode, {
          accept: [],
          type: DndConstantsEnum.MODULE_SOURCE_TYPE,
          selfAccept: false,
          copyOnly: true,
          selfCopy: false,
          rawCreator: function(item) {
            var node = TemplateUtils.toDom(itemTemplate, item);
            return {
              node: node,
              data: new ModuleDndTypeBuilder()
                  .title(DOMUtils.asText(item.title))
                  .externalId(item.link)
                  .rawData(item)
                  .build(),
              focusNode: dojo.query('a', node)[0],
              type: [DndConstantsEnum.MODULE_TYPE]
            };
          },
          onSelectionChange: lang.hitch(this, this._removeSelectionFromRecentSearchResultList)
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

        this._searchResultsNode.appendChild(pageInfo);

      }
    }
  });
});
