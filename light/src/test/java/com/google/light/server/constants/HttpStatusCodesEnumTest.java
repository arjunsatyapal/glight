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

import com.google.light.server.constants.http.HttpStatusCodesEnum;

import org.junit.Test;

/**
 * Test for {@link HttpStatusCodesEnum}.
 * 
 * @author Arjun Satyapal
 */
public class HttpStatusCodesEnumTest implements EnumTestInterface {
  /**
   * {@inheritDoc}
   * 
   */
  
  @Override
  @Test
  public void test_count() {
    assertEquals(41, HttpStatusCodesEnum.values().length);
  }
  
  
  /**
   * Test for ensuring that HttpCodes are put in right categories.
   */
  @Test
  public void test_constructor() {
    for (HttpStatusCodesEnum curr : HttpStatusCodesEnum.values()) {
      assertTrue(curr.getStatusCode() >= curr.getCategory().getMin());
      assertTrue(curr.getStatusCode() <= curr.getCategory().getMax());
    }
  }
  
}
