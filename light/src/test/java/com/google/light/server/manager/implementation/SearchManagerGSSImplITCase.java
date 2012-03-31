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
package com.google.light.server.manager.implementation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import com.google.light.server.constants.LightConstants;
import com.google.light.server.constants.LightEnvEnum;
import com.google.light.server.dto.search.SearchRequestDto;
import com.google.light.server.dto.search.SearchResultDto;
import com.google.light.testingutils.TestingUtils;

/**
 * Integration Test for {@link SearchManagerGSSImpl}.
 * 
 * @author Walter Cacau
 */
public class SearchManagerGSSImplITCase {

  /**
   * This test query needs to be such a generic query that brings enougth results to fill the first
   * and second pages
   */
  private static final String TEST_QUERY = "Addition";
  private SearchManagerGSSImpl searchManager;

  @Before
  public void setUp() {
    searchManager =
        TestingUtils.getInjectorByEnv(LightEnvEnum.DEV_SERVER).getInstance(
            SearchManagerGSSImpl.class);
  }

  /**
   * Integration Test for {@link SearchManagerGSSImpl#search(SearchRequestDto)}
   */
  @Test
  public void test_search() {
    for (int i = 1; i <= 2; i++) {
      SearchResultDto search =
          searchManager.search(new SearchRequestDto.Builder().query(TEST_QUERY).page(i).build());
      assertNotNull("Page " + i + " has a null items list.", search.getItems());
      assertEquals(
          "The test query ("
              + TEST_QUERY
              + ") didn't returned enough results to fill page " + i + ".",
          LightConstants.SEARCH_RESULTS_PER_PAGE,
          search.getItems().size());
    }
  }
}
