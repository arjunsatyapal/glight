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

import org.junit.Ignore;

import org.junit.Test;

import com.google.light.server.dto.AbstractDtoTest;

/**
 * Test for {@link SearchResultItemDto}
 * 
 * @author waltercacau
 */
public class SearchResultItemDtoTest extends AbstractDtoTest {
  private final String SAMPLE_TITLE = "Some <b>cool</b> title";
  private final String SAMPLE_DESCRIPTION = "Some <b>cool</b> description";
  private final String SAMPLE_LINK = "http://somecooloer.com";

  private SearchResultItemDto.Builder getDtoBuilder() {
    return new SearchResultItemDto.Builder()
        .title(SAMPLE_TITLE)
        .description(SAMPLE_DESCRIPTION)
        .link(SAMPLE_LINK);
  }

  @Override
  @Test
  public void test_validate() {
    // Positive Test
    SearchResultItemDto searchResultItem = getDtoBuilder().build();
    assertNotNull(searchResultItem);

    // Negative Test : title=null
    try {
      getDtoBuilder().title(null).build();
      fail("should have failed.");
    } catch (NullPointerException e) {
      // expected.
    }

    // Negative Test : description=null
    try {
      getDtoBuilder().description(null).build();
      fail("should have failed.");
    } catch (NullPointerException e) {
      // expected.
    }

    // Negative Test : link=null
    try {
      getDtoBuilder().link(null).build();
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
  
  @Ignore(value = "TODO(arjuns): Check with walter why this is not allowed")
  @Test
  @Override
  public void test_toJson() throws Exception {
    // Not used as JSON
    try {
      getDtoBuilder().build().toJson();
      fail("should have failed.");
    } catch (UnsupportedOperationException e) {
      // expected.
    }
  }

  @Ignore(value = "TODO(arjuns): Check with walter why this is not allowed")
  @Test
  @Override
  public void test_toXml() throws Exception {
    // Not used as XML
    try {
      getDtoBuilder().build().toXml();
      fail("should have failed.");
    } catch (UnsupportedOperationException e) {
      // expected.
    }
  }
}
