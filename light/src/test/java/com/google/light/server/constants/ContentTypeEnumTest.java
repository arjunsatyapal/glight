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

import static com.google.light.server.constants.ContentTypeEnum.APPLICATION_JSON;
import static com.google.light.server.constants.ContentTypeEnum.APPLICATION_XML;
import static com.google.light.server.constants.ContentTypeEnum.TEXT_HTML;
import static com.google.light.server.constants.ContentTypeEnum.TEXT_PLAIN;
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
    assertEquals("Update test_getContentTypeByString as required.", 5,
        ContentTypeEnum.values().length);
  }

  /**
   * Test for {@link ContentTypeEnum#getContentTypeByString(String)}.
   */
  @Test
  public void test_getContentTypeByString() {
    Map<String, ContentTypeEnum> map = ImmutableMap.<String, ContentTypeEnum> builder()
        .put("application/json", APPLICATION_JSON)
        .put("application/xml", APPLICATION_XML)
        .put("text/html", TEXT_HTML)
        .put("text/plain", TEXT_PLAIN)
        .put("text/javascript", ContentTypeEnum.TEXT_JAVASCRIPT)
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
