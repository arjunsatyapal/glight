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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableMap;
import com.google.light.server.constants.http.ContentTypeEnum;
import com.google.light.server.exception.unchecked.httpexception.UnsupportedMediaTypeException;
import java.util.Map;
import org.junit.Test;

/**
 * Test for {@link ContentTypeEnum}.
 * 
 * @author Arjun Satyapal
 */
public class ContentTypeEnumTest implements EnumTestInterface {
  private int expectedCount = 31;
  /**
   * {@inheritDoc} When you change {@link ContentTypeEnum} update
   * {@link #test_getContentTypeByString}.
   */
  @Test
  @Override
  public void test_count() {
    assertEquals("Update test_getContentTypeByString as required.", expectedCount,
        ContentTypeEnum.values().length);
  }

  /**
   * Test for {@link ContentTypeEnum#getContentTypeByString(String)}.
   */
  @Test
  public void test_getContentTypeByString() {
    Map<String, ContentTypeEnum> map =
        ImmutableMap
            .<String, ContentTypeEnum> builder()
            .put("application/json", ContentTypeEnum.APPLICATION_JSON)
            .put("application/pdf", ContentTypeEnum.APPLICATION_PDF)
            .put("application/x-www-form-urlencoded", ContentTypeEnum.APPLICATION_URL_ENCODED)
            .put("application/xml", ContentTypeEnum.APPLICATION_XML)
            .put("application/zip", ContentTypeEnum.APPLICATION_ZIP)

            .put("application/vnd.ms-excel", ContentTypeEnum.MS_EXCEL)
            .put("application/msword", ContentTypeEnum.MS_WORD)
            .put("application/vnd.ms-powerpoint", ContentTypeEnum.MS_POWERPOINT)
            .put("application/x-msmetafile", ContentTypeEnum.MS_DRAWING)

            .put("application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                ContentTypeEnum.OPENXML_DOCUMENT)
            .put("application/vnd.openxmlformats-officedocument.presentationml.presentation",
                ContentTypeEnum.OPENXML_PRESENTATION)
            .put("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                ContentTypeEnum.OPENEXML_SPREADSHEET)
            .put("application/vnd.google-apps.document", ContentTypeEnum.GOOGLE_DOC)
            .put("application/vnd.google-apps.spreadsheet", ContentTypeEnum.GOOGLE_SPREADSHEET)
            .put("application/vnd.google-apps.form", ContentTypeEnum.GOOGLE_FORM)
            .put("application/vnd.google-apps.presentation", ContentTypeEnum.GOOGLE_PRESENTATION)
            .put("application/vnd.google-apps.drawing", ContentTypeEnum.GOOGLE_DRAWING)

            .put("application/vnd.oasis.opendocument.text", ContentTypeEnum.OASIS_DOCUMENT)
            .put("application/x-vnd.oasis.opendocument.spreadsheet",
                ContentTypeEnum.OASIS_SPREADSHEET)

            .put("image/gif", ContentTypeEnum.IMAGE_GIF)
            .put("image/jpeg", ContentTypeEnum.IMAGE_JPEG)
            .put("image/bmp", ContentTypeEnum.IMAGE_BMP)
            .put("image/png", ContentTypeEnum.IMAGE_PNG)
            .put("image/svg+xml", ContentTypeEnum.IMAGE_SVG_XML)
            .put("application/rtf", ContentTypeEnum.RTF)
            .put("text/html", ContentTypeEnum.TEXT_HTML)
            .put("text/javascript", ContentTypeEnum.TEXT_JAVASCRIPT)
            .put("text/plain", ContentTypeEnum.TEXT_PLAIN)
            .put("text/csv", ContentTypeEnum.TEXT_CSV)
            .put("text/tab-separated-values", ContentTypeEnum.TEXT_TSV)
            .put("application/vnd.sun.xml.writer", ContentTypeEnum.SUN_XML_WRITE)
            .build();

    
    for (ContentTypeEnum curr : ContentTypeEnum.values()) {
      assertTrue(curr + " is missing from the map.", map.containsValue(curr));
    }
    
    
    for (String curr : map.keySet()) {
      assertEquals(map.get(curr), ContentTypeEnum.getContentTypeByString(curr));
    }

    // Plus charset and spaces
    for (String curr : map.keySet()) {
      assertEquals(map.get(curr), ContentTypeEnum.getContentTypeByString(curr + "; charset=UTF-8"));
      assertEquals(map.get(curr), ContentTypeEnum.getContentTypeByString(curr + " ; charset=UTF-8"));
      assertEquals(map.get(curr),
          ContentTypeEnum.getContentTypeByString(" " + curr + " ; charset=UTF-8"));
      assertEquals(map.get(curr), ContentTypeEnum.getContentTypeByString(curr + " "));
      assertEquals(map.get(curr), ContentTypeEnum.getContentTypeByString(" " + curr + " "));
      assertEquals(map.get(curr), ContentTypeEnum.getContentTypeByString(" " + curr));
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
