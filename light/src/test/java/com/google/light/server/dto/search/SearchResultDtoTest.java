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
package com.google.light.server.dto.search;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import com.google.light.server.dto.AbstractDtoTest;

/**
 * Test for {@link SearchResultDto}
 * 
 * @author waltercacau
 */
public class SearchResultDtoTest extends AbstractDtoTest {
  private static final String SAMPLE_SUGGESTION = "suggestion";
  private static final String SAMPLE_SUGGESTION_QUERY = "suggestion query";

  private SearchResultDto.Builder getDtoBuilder() {
    return new SearchResultDto.Builder().items(new ArrayList<SearchResultItemDto>())
        .suggestion(null).suggestionQuery(null).hasNextPage(false);
  }

  @Override
  @Test
  public void test_validate() {
    SearchResultItemDto item1 = Mockito.mock(SearchResultItemDto.class);
    SearchResultItemDto item2 = Mockito.mock(SearchResultItemDto.class);

    // Positive Test : no items and no suggestion
    SearchResultDto searchResult = getDtoBuilder().build();
    assertNotNull(searchResult);

    // Positive Test : one item
    searchResult = getDtoBuilder().items(Lists.newArrayList(item1)).build();
    assertNotNull(searchResult);

    // Positive Test : two items
    searchResult = getDtoBuilder().items(Lists.newArrayList(item1, item2)).build();
    assertNotNull(searchResult);

    // Positive Test : with suggestion, but without items
    searchResult =
        getDtoBuilder().suggestion(SAMPLE_SUGGESTION).suggestionQuery(SAMPLE_SUGGESTION_QUERY)
            .build();
    assertNotNull(searchResult);

    // Positive Test : with suggestion and with items
    searchResult =
        getDtoBuilder().items(Lists.newArrayList(item1, item2)).suggestion(SAMPLE_SUGGESTION)
            .suggestionQuery(SAMPLE_SUGGESTION_QUERY).build();
    assertNotNull(searchResult);

    // Negative Test : suggestion = null and suggestionQuery != null
    try {
      getDtoBuilder().suggestion(null).suggestionQuery(SAMPLE_SUGGESTION_QUERY).build();
      fail("should have failed.");
    } catch (IllegalStateException e) {
      // expected.
    }

    // Negative Test : suggestion != null and suggestionQuery = null
    try {
      getDtoBuilder().suggestion(SAMPLE_SUGGESTION).suggestionQuery(null).build();
      fail("should have failed.");
    } catch (IllegalStateException e) {
      // expected.
    }

    // Negative Test : suggestion = null XOR suggestionQuery = null
    try {
      getDtoBuilder().suggestion(SAMPLE_SUGGESTION).suggestionQuery(null).build();
      fail("should have failed.");
    } catch (IllegalStateException e) {
      // expected.
    }

    // Negative Test : items = null
    try {
      getDtoBuilder().items(null).build();
      fail("should have failed.");
    } catch (NullPointerException e) {
      // expected.
    }
  }
  
  @Test
  @Override
  public void test_builder() throws Exception {
    // No validation logic in constructor. So nothing to test here.
  }

  @Test
  @Override
  public void test_toJson() throws Exception {
    // TODO(waltercacau): Implement this test
  }

  @Test
  @Override
  public void test_toXml() throws Exception {
    // TODO(waltercacau): Implement this test
  }
}
