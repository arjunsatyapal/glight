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

import static com.google.light.server.utils.LightUtils.appendKeyValue;
import static com.google.light.server.utils.LightUtils.appendSectionHeader;
import static com.google.light.server.utils.LightUtils.getPST8PDTime;
import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import org.joda.time.DateTime;
import org.junit.Test;

/**
 * Test for {@link LightUtils}.
 *
 * @author Arjun Satyapal
 */
public class LightUtilsTest {
  /**
   * Test for {@link LightUtils#getInputStreamAsString(InputStream)}
   */
  @Test
  public void test_getInputStreamAsString() {
    // Not much to test here.
  }
  
  /**
   * Test for {@link LightUtils#appendSectionHeader(StringBuilder, String)}
   */
  @Test
  public void test_appendSectionHeader() {
    StringBuilder builder = new StringBuilder();
    String sectionHeader = "sectionHeader";
    
    String expected = "<br><br><b>sectionHeader: </b>";
    appendSectionHeader(builder, sectionHeader);
    assertEquals(expected, builder.toString());
  }
  
  /**
   * Test for {@link LightUtils#appendKeyValue(StringBuilder, String, Object)}
   */
  @Test
  public void test_appendKeyValue() {
    StringBuilder builder = new StringBuilder();
    String key = "key";
    String value = "value";
    String expected = "<br>key = value"; 
    appendKeyValue(builder, key, value);
    assertEquals(expected, builder.toString());
  }
  
  /**
   * Test for {@link LightUtils#getPST8PDTime(long)}.
   */
  @Test
  public void test_getPST8PDTTime() {
    DateTime dateTime = new DateTime(2012, 03, 13, 2, 30);
    System.out.println(dateTime);
    long timeInMillis = dateTime.toInstant().getMillis();
    assertEquals(dateTime.toString(), getPST8PDTime(timeInMillis).toString());
    
    /*
     * Moving back by 3 days.
     * In 2012, DayLight saving was enabled on Tue Mar 11.
     */
    timeInMillis = timeInMillis - 3 * 24 * 60 * 60 * 1000;
    DateTime expectedDateTime = new DateTime(2012, 03, 10, 1, 30);
    assertEquals(expectedDateTime.toString(), getPST8PDTime(timeInMillis).toString());
  }
}
