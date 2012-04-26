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

import com.google.common.collect.Lists;
import com.google.light.server.constants.http.HttpStatusCategoryEnum;
import java.util.List;
import org.junit.Test;

/**
 * Test for {@link HttpStatusCodesEnum}.
 * 
 * @author Arjun Satyapal
 */
public class HttpStatusCategoryEnumTest implements EnumTestInterface {
  /**
   * {@inheritDoc}
   * In addition, also add/modify/delete other tests as required according to modifications.
   */
  @Override
  @Test
  public void test_count() {
    assertEquals(5, HttpStatusCategoryEnum.values().length);
  }
  
  /**
   * Test for ensuring that Category values are as per the Http Specification.
   */
  @Test
  public void test_min_max() {
    List<HttpStatusCategoryEnum> list = Lists.newArrayList(
        HttpStatusCategoryEnum.INFORMATIONAL,
        HttpStatusCategoryEnum.SUCCESS,
        HttpStatusCategoryEnum.REDIRECTION,
        HttpStatusCategoryEnum.CLIENT_ERROR,
        HttpStatusCategoryEnum.SERVER_ERROR
        );

    int min = 0;
    int max = 99;
    for (HttpStatusCategoryEnum curr : list) {
      min += 100;
      max += 100;
      assertEquals(min, curr.getMin());
      assertEquals(max, curr.getMax());
    }
    
    // Min value for SERVER_ERROR.
    assertEquals(500, min);
    // Max value for SERVER_ERROR.
    assertEquals(599, max);
  }
}
