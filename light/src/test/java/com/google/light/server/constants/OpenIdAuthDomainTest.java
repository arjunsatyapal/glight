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

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import org.junit.Test;

/**
 * Test for {@link OpenIdAuthDomain}.
 * 
 * @author Arjun Satyapal
 */
public class OpenIdAuthDomainTest implements EnumTestInterface {

  /**
   * {@inheritDoc} When you change {@link OpenIdAuthDomain} update 
   * {@link #test_getAuthDomainByValue()}.
   */
  @Test
  @Override
  public void test_count() {
    assertEquals("Update test_getAuthDomainByValue as required.", 1,
        OpenIdAuthDomain.values().length);
  }

  /**
   * Test for {@link OpenIdAuthDomain#getAuthDomainByValue(String)}.
   */
  @Test
  public void test_getAuthDomainByValue() {
    Map<String, OpenIdAuthDomain> map = ImmutableMap.<String, OpenIdAuthDomain> builder()
        .put("https://www.google.com/accounts/o8/ud", OpenIdAuthDomain.GOOGLE)
        .build();

    for (String curr : map.keySet()) {
      assertEquals(map.get(curr), OpenIdAuthDomain.getAuthDomainByValue(curr));
    }

    // Negative Testing : Test for invalid string.

    try {
      OpenIdAuthDomain.getAuthDomainByValue("foo");
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // Expected
    }
  }

}
