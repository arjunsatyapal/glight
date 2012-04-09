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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.google.common.collect.ImmutableMapBuilder;
import com.google.light.server.constants.LightConstants;
import com.google.light.server.dto.AbstractDtoTest;
import com.google.light.server.dto.search.SearchRequestDto.Builder;
import com.google.light.server.exception.unchecked.BlankStringException;
import com.google.light.server.utils.QueryUtils;
import com.google.light.testingutils.TestingUtils;

/**
 * Test for {@link SearchRequestDto}
 * 
 * @author waltercacau
 */
public class SearchRequestDtoTest extends AbstractDtoTest {

  private static final String SAMPLE_LANGUAGE_CODE = "en";
  /**
   * SAMPLE_PAGE must differ from 1 because 1 is the default value and we want to use
   * this to test if the builder and QueryUtils are correctly setting the page number
   */
  private static final int SAMPLE_PAGE = 3;
  private static final String SAMPLE_QUERY = "Addition";

  /**
   * Test constructing this Dto through QueryUtils.
   */
  @Test
  public void testConstructThroughQueryUtils() {
    // Just positive tests. We can count with enough negative tests in QueryUtilsTest

    SearchRequestDto searchRequest =
        QueryUtils.getValidDto(TestingUtils
            .getMockedRequestWithParameterMap(new ImmutableMapBuilder<String, String[]>()
                .put("query", new String[] { SAMPLE_QUERY })
                .put("clientLanguageCode", new String[] { SAMPLE_LANGUAGE_CODE })
                .getMap()), SearchRequestDto.class);

    assertNotNull(searchRequest);
    assertEquals(SAMPLE_QUERY, searchRequest.getQuery());
    assertEquals(SAMPLE_LANGUAGE_CODE, searchRequest.getClientLanguageCode());
    assertEquals("Default page must be " + LightConstants.FIRST_SEARCH_PAGE_NUMBER, LightConstants.FIRST_SEARCH_PAGE_NUMBER, searchRequest.getPage());

    searchRequest = QueryUtils.getValidDto(
        TestingUtils.getMockedRequestWithParameterMap(new ImmutableMapBuilder<String, String[]>()
            .put("query", new String[] { SAMPLE_QUERY })
            .put("page", new String[] { Integer.toString(SAMPLE_PAGE) })
            .put("clientLanguageCode", new String[] { SAMPLE_LANGUAGE_CODE })
            .getMap()), SearchRequestDto.class);

    assertNotNull(searchRequest);
    assertEquals(SAMPLE_QUERY, searchRequest.getQuery());
    assertEquals(SAMPLE_PAGE, searchRequest.getPage());
    assertEquals(SAMPLE_LANGUAGE_CODE, searchRequest.getClientLanguageCode());

  }

  @Test
  public void test_validate() {
    // Positive test
    SearchRequestDto searchRequest = getDefaultBuilder().build();
    assertNotNull(searchRequest);
    assertEquals(SAMPLE_QUERY, searchRequest.getQuery());
    assertEquals(SAMPLE_LANGUAGE_CODE, searchRequest.getClientLanguageCode());

    // Default page must be one
    assertEquals("Default page must be " + LightConstants.FIRST_SEARCH_PAGE_NUMBER, LightConstants.FIRST_SEARCH_PAGE_NUMBER, searchRequest.getPage());

    // Positive test: page = 3
    searchRequest = getDefaultBuilder().page(SAMPLE_PAGE).build();
    assertNotNull(searchRequest);
    assertEquals(SAMPLE_QUERY, searchRequest.getQuery());
    assertEquals(SAMPLE_PAGE, searchRequest.getPage());
    assertEquals(SAMPLE_LANGUAGE_CODE, searchRequest.getClientLanguageCode());

    // Negative test: query = null
    try {
      getDefaultBuilder().query(null).build();
      fail("should have failed.");
    } catch (BlankStringException e) {
      // expected.
    }

    // Negative test: query = blank
    try {
      getDefaultBuilder().query("").build();
      fail("should have failed.");
    } catch (BlankStringException e) {
      // expected.
    }

    // Negative test: page = 0
    try {
      getDefaultBuilder().page(0).build();
      fail("should have failed.");
    } catch (IllegalStateException e) {
      // expected.
    }

    // Negative test: clientLanguageCode = null
    try {
      getDefaultBuilder().clientLanguageCode(null).build();
      fail("should have failed.");
    } catch (BlankStringException e) {
      // expected.
    }

    // Negative test: clientLanguageCode = " "
    try {
      getDefaultBuilder().clientLanguageCode(" ").build();
      fail("should have failed.");
    } catch (BlankStringException e) {
      // expected.
    }

  }

  private Builder getDefaultBuilder() {
    return new SearchRequestDto.Builder().query(SAMPLE_QUERY).clientLanguageCode(
        SAMPLE_LANGUAGE_CODE);
  }

  @Override
  public void test_builder() throws Exception {
    // No validation logic in constructor. So nothing to test here.
  }

  @Override
  public void test_toJson() throws Exception {
    // Not used as JSON
    try {
      getDefaultBuilder().build().toJson();
      fail("should have failed.");
    } catch (UnsupportedOperationException e) {
      // expected.
    }
  }

  @Override
  public void test_toXml() throws Exception {
    // Not used as XML
    try {
      getDefaultBuilder().build().toXml();
      fail("should have failed.");
    } catch (UnsupportedOperationException e) {
      // expected.
    }
  }

}
