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
package com.google.light.server.constants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableMap;
import com.google.light.server.exception.unchecked.httpexception.UnsupportedMediaTypeException;
import java.util.Map;
import org.junit.Test;

/**
 * Test for {@link ContentTypeEnum}.
 * 
 * @author Arjun Satyapal
 */
public class ContentTypeEnumTest implements EnumTestInterface {

  /**
   * {@inheritDoc} When you change {@link ContentTypeEnum} update
   * {@link #test_getContentTypeByString}.
   */
  @Test
  @Override
  public void test_count() {
    assertEquals("Update test_getContentTypeByString as required.", 2,
        ContentTypeEnum.values().length);
  }

  /**
   * Test for {@link ContentTypeEnum#getContentTypeByString(String)}.
   */
  @Test
  public void test_getContentTypeByString() {
    Map<String, ContentTypeEnum> map = ImmutableMap.<String, ContentTypeEnum> builder()
        .put("application/json", ContentTypeEnum.APPLICATION_JSON)
        .put("application/xml", ContentTypeEnum.APPLICATION_XML)
        .build();

    for (String curr : map.keySet()) {
      assertEquals(map.get(curr), ContentTypeEnum.getContentTypeByString(curr));
    }

    // Negative Testing : Test for random value.
    try {
      ContentTypeEnum.getContentTypeByString("foo");
      fail("should have failed.");
    } catch (UnsupportedMediaTypeException e) {
      // Expected
    }
  }

}
