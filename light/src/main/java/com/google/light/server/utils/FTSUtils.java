/*
 * Copyright 2012 Google Inc.
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
package com.google.light.server.utils;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import com.google.light.server.constants.LightStringConstants;

import com.google.appengine.api.search.Consistency;
import com.google.appengine.api.search.Cursor;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.Field.FieldType;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.ListIndexesRequest;
import com.google.appengine.api.search.ListIndexesResponse;
import com.google.appengine.api.search.Query;
import com.google.appengine.api.search.QueryOptions;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.Schema;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.search.SearchServiceFactory;
import com.google.appengine.api.search.SortExpression;
import com.google.appengine.api.search.SortOptions;
import com.google.common.collect.Lists;
import com.google.light.server.constants.JerseyConstants;
import com.google.light.server.constants.fts.FTSFieldCategory;
import com.google.light.server.constants.fts.FTSIndex;
import com.google.light.server.dto.pages.PageDto;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.ModuleId;
import com.google.light.server.dto.pojo.typewrapper.stringwrapper.FTSDocumentId;
import com.google.light.server.dto.pojo.wrappers.FTSDocumentWrapper;
import com.google.light.server.dto.search.SearchResultItemDto;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Instant;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class FTSUtils {
  private static final Logger logger = Logger.getLogger(FTSUtils.class.getName());

  public static Field createTitleField(String title) {
    checkNotBlank(title, "title");
    return Field.newBuilder()
        .setName(FTSFieldCategory.TITLE.getName())
        .setText(title)
        .build();
  }

  public static Field createHtmlContentField(String html) {
    checkNotBlank(html, "html");
    return Field.newBuilder()
        .setName(FTSFieldCategory.CONTENT.getName())
        .setHTML(html)
        .build();
  }

  public static Field createTextContentField(String text) {
    checkNotBlank(text, "text");
    return Field.newBuilder()
        .setName(FTSFieldCategory.CONTENT.getName())
        .setHTML(text)
        .build();
  }

  @SuppressWarnings("deprecation")
  public static Field createPublishedField(Instant publishTime) {
    checkNotNull(publishTime, "publishTime");
    DateTime dateTime = new DateTime(publishTime);
    Date date = new Date(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth());

    Field field = Field.newBuilder()
        .setName(FTSFieldCategory.PUBLISHED.getName())
        .setDate(date)
        .build();
    return field;
  }

  public static IndexSpec getIndexSpec(FTSIndex ftsIndex) {
    return IndexSpec.newBuilder()
        .setName(ftsIndex.name())
        .setConsistency(Consistency.PER_DOCUMENT)
        .build();

  }

  public static Index getIndex(FTSIndex ftsIndex) {
    IndexSpec indexSpec = getIndexSpec(ftsIndex);
    return SearchServiceFactory.getSearchService().getIndex(indexSpec);
  }

  /**
   * @param ftsDocument
   * @param module
   */
  public static void addDocumentToIndex(FTSDocumentWrapper ftsDocumentWrapper, FTSIndex ftsIndex) {
    Index index = getIndex(ftsIndex);
    index.add(ftsDocumentWrapper.getDocument());
    logger.info("Added " + ftsDocumentWrapper.getFtsDocumentId() + " for FTS indexing.");
  }

  public static void removeDocumentsFromIndex(List<FTSDocumentId> listOfIds, FTSIndex ftsIndex) {
    List<String> docIds = LightUtils.convertWrapperListToListOfValues(listOfIds);

    getIndex(ftsIndex).remove(docIds);
  }

  // TODO(arjuns) : Refine this method.
  public static void getIndexSchema() {
    ListIndexesResponse response = SearchServiceFactory.getSearchService().listIndexes(
        ListIndexesRequest.newBuilder().setSchemaFetched(true).build());

    // List out elements of each Schema
    for (Index index : response) {
      Schema schema = index.getSchema();
      for (String fieldName : schema.getFieldNames()) {
        List<FieldType> typesForField = schema.getFieldTypes(fieldName);
      }
    }
  }

  public static PageDto findDocuments(String queryString, int maxResults,
      String startIndex, FTSIndex ftsIndex) {

    Cursor cursor = null;
    if (StringUtils.isNotBlank(startIndex)) {
      cursor = Cursor.newBuilder().build(startIndex);
    } else {
      cursor = Cursor.newBuilder().build();
    }

    SortOptions sortOptions = getSortOptions(maxResults);
    QueryOptions options = getQueryOptions(maxResults, cursor, sortOptions);

    Query.Builder queryBuilder = Query.newBuilder().setOptions(options);
    
    Query query = null;
    
    if (StringUtils.isBlank(queryString)) {
      checkNotBlank(startIndex, "startIndex and query both cannot be blank");
      query = queryBuilder.build();
    } else {
      query = queryBuilder.build(queryString);
    }
    
    Results<ScoredDocument> results = getIndex(ftsIndex).search(query);

    List<SearchResultItemDto> listOfResults = Lists.newArrayList();
    for (ScoredDocument curr : results.getResults()) {
      System.out.println(curr.getId());
      FTSDocumentId ftsDocumentId = new FTSDocumentId(curr.getId());
      ModuleId moduleId = LightUtils.convertFTSDocumentIdToModuleId(ftsDocumentId);
      URI uri = LocationUtils.getModuleLocation(moduleId);

      List<Field> expressions = curr.getExpressions();
      checkArgument(expressions.size() == 1, "Expected is only for Content.");
      Field expression = expressions.get(0);
      checkArgument(expression.getType() == FieldType.HTML);

      Iterable<Field> fieldIterable = curr.getField(FTSFieldCategory.TITLE.getName());
      Field textField = fieldIterable.iterator().next();

      System.out.println(uri.toString());
      SearchResultItemDto item = new SearchResultItemDto.Builder()
          .link(uri.toString())
          .description(expression.getHTML())
          .title(textField.getText())
          .build();
      listOfResults.add(item);
    }
    
    String newStartIndex = null;
    if(results.getNumberFound() > maxResults) {
      Cursor newCursor = results.getCursor();
      newStartIndex = getNewStartIndex(queryString, newCursor);
    }
  
    PageDto pageDto = new PageDto.Builder()
        .startIndex(newStartIndex)
        .handlerUri(JerseyConstants.URI_MODULE_SEARCH)
        .list(listOfResults)
        .build();
    System.out.println(pageDto.toJson());

    System.out.println("Found : " + results.getNumberFound());
    System.out.println("Returned : " + results.getNumberReturned());
    System.out.println("OperationResult : " + results.getOperationResult());

    return pageDto;
  }

  /**
   * @param queryString
   * @param newCursor
   * @return
   */
  private static String getNewStartIndex(String queryString, Cursor cursor) {
    String queryPart = LightStringConstants.FILTER + "=" + queryString;
    String startIndexPart = cursor.toWebSafeString();
    String unEncodedStartIndex = startIndexPart + "&" + queryPart; 
    
    return unEncodedStartIndex;
  }

  /**
   * @param maxResults
   * @param cursor
   * @param sortOptions
   * @return
   */
  private static QueryOptions
      getQueryOptions(int maxResults, Cursor cursor, SortOptions sortOptions) {
    QueryOptions options =
        QueryOptions
            .newBuilder()
            .setLimit(maxResults)
            .setFieldsToReturn(FTSFieldCategory.CONTENT.getName(),
                FTSFieldCategory.PUBLISHED.getName(), FTSFieldCategory.TITLE.getName())
            .setFieldsToSnippet(FTSFieldCategory.CONTENT.getName())
            .setSortOptions(sortOptions)
            .setCursor(cursor)
            .build();
    return options;
  }

  /**
   * @param maxResults
   * @return
   */
  private static SortOptions getSortOptions(int maxResults) {
    SortOptions sortOptions = SortOptions.newBuilder()
        .addSortExpression(SortExpression.newBuilder().setExpression(
            FTSFieldCategory.PUBLISHED.getName())
            .setDirection(SortExpression.SortDirection.DESCENDING)
            .setDefaultValue(""))
        .setLimit(maxResults)
        .build();
    return sortOptions;
  }

}
