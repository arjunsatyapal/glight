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
package com.google.light.server.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


import javax.servlet.http.HttpServletRequest;

import org.junit.Test;

import com.google.common.collect.ImmutableMapBuilder;
import com.google.light.server.exception.unchecked.httpexception.BadRequestException;
import com.google.light.testingutils.TestingUtils;

public class QueryUtilsTest {

  @Test
  public void test_getDto() {
    // Positive Test
    HttpServletRequest req = TestingUtils.getMockedRequestWithParameterMap(new ImmutableMapBuilder<String, String[]>()
        .put("field", new String[] { "fieldValue" })
        .put("someOtherField", new String[] { "3" })
        .getMap());

    QueryUtilsTestSampleDto instance = QueryUtils.getValidDto(req, QueryUtilsTestSampleDto.class);

    assertEquals("fieldValue", instance.getField());
    assertEquals(3, instance.getSomeOtherField());
    assertTrue(".validate must be called!", instance.wasValidated());

    // Negative Test: field missing
    try {
      req = TestingUtils.getMockedRequestWithParameterMap(new ImmutableMapBuilder<String, String[]>()
          .put("nonExistingField", new String[] { "fieldValue" })
          .getMap());

      instance = QueryUtils.getValidDto(req, QueryUtilsTestSampleDto.class);

      fail("should have failed.");
    } catch (BadRequestException e) {
      // expected.
    }

  }
}
