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

import static org.junit.Assert.*;

import org.junit.Test;

import com.google.common.collect.ImmutableMapBuilder;
import com.google.light.server.exception.unchecked.BlankStringException;
import com.google.light.server.utils.QueryUtils;
import com.google.light.server.utils.QueryUtilsTest;

/**
 * Test for {@link SearchRequestDto}
 * 
 * @author waltercacau
 */
public class SearchRequestDtoTest {

  private static final int SAMPLE_PAGE = 3;
  private static final String SAMPLE_QUERY = "Addition";

  @Test
  public void testConstructThroughQueryUtils() {
    // Just positive tests. We can count with enough negative tests in QueryUtilsTest

    SearchRequestDto searchRequest =
        QueryUtils.getValidDto(QueryUtilsTest
            .getMockedRequestWithParameterMap(new ImmutableMapBuilder<String, String[]>()
                .put("query", new String[] { SAMPLE_QUERY })
                .getMap()), SearchRequestDto.class);

    assertEquals("Default page must be 1", 1, searchRequest.getPage());

    QueryUtils.getValidDto(
        QueryUtilsTest.getMockedRequestWithParameterMap(new ImmutableMapBuilder<String, String[]>()
            .put("query", new String[] { SAMPLE_QUERY })
            .put("page", new String[] { Integer.toString(SAMPLE_PAGE) })
            .getMap()), SearchRequestDto.class);

  }

  @Test
  public void test_validate() {
    // Positive test
    SearchRequestDto searchRequest = new SearchRequestDto.Builder().query(SAMPLE_QUERY).build();

    // Default page must be one
    assertEquals("Default page must be 1", 1, searchRequest.getPage());

    // Positive test: page = 3
    new SearchRequestDto.Builder().query(SAMPLE_QUERY).page(SAMPLE_PAGE).build();

    // Negative test: query = null
    try {
      new SearchRequestDto.Builder().query(null).build();
      fail("should have failed.");
    } catch (BlankStringException e) {
      // expected.
    }

    // Negative test: query = blank
    try {
      new SearchRequestDto.Builder().query("").build();
      fail("should have failed.");
    } catch (BlankStringException e) {
      // expected.
    }

    // Negative test: page = 0
    try {
      new SearchRequestDto.Builder().query(SAMPLE_QUERY).page(0).build();
      fail("should have failed.");
    } catch (IllegalStateException e) {
      // expected.
    }

  }

}
