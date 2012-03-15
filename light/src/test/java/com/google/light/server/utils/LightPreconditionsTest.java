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

import static com.google.light.server.utils.LightPreconditions.checkEmail;
import static com.google.light.server.utils.LightPreconditions.checkNonEmptyList;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.LightPreconditions.checkNull;
import static com.google.light.server.utils.LightPreconditions.checkPersonId;
import static com.google.light.server.utils.LightPreconditions.checkPositiveLong;
import static com.google.light.testingutils.TestingUtils.getUUIDString;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.Lists;
import com.google.light.server.exception.unchecked.InvalidPersonIdException;
import java.util.List;
import org.junit.Test;

/**
 * Test for {@link LightPreconditions}.
 * 
 * @author Arjun Satyapal
 */
public class LightPreconditionsTest {

  /**
   * Test for {@link LightPreconditions#checkEmail(String)} Some quick tests. Though not required as
   * we are using Apache Commons Library to validate emails.
   */
  @Test
  public void test_checkEmail() {
    //
    checkEmail("a@b.com");
    checkEmail("a@b.com");

    // Negative test : invalid ascii.
    try {
      checkEmail("\u0001abc@gmail.com");
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // expected.
    }

    // Negative test : double @@
    try {
      checkEmail("a@@gmail.com");
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // expected.
    }

    // Negative test : a.@gmail.com
    try {
      checkEmail("a.@gmail.com");
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // expected.
    }

    // Negative test : a@.com
    try {
      checkEmail("a.@.com");
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // expected.
    }

    // Negative test : a@gmail.com.
    try {
      checkEmail("a.@.com");
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // expected.
    }
  }

  /**
   * Test for {@link LightPreconditions#checkNonEmptyList(List)}
   */
  @Test
  public void test_checkNonEmptyList() {
    checkNonEmptyList(Lists.newArrayList(1L));

    // Negative : List = null.
    try {
      checkNonEmptyList(null);
      fail("should have failed.");
    } catch (NullPointerException e) {
      // expected.
    }

    try {
      checkNonEmptyList(Lists.newArrayList());
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // expected.
    }
  }

  /**
   * Test for {@link LightPreconditions#checkNotBlank(String)}
   */
  @Test
  public void test_checkNotBlank() {
    checkNotBlank("hello");

    // Negative : string=null
    try {
      checkNotBlank(null);
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // expected.
    }

    // Negative : string=""
    try {
      checkNotBlank("");
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // expected.
    }

    // Negative : string=""
    try {
      checkNotBlank("    ");
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // expected.
    }
  }

  /**
   * Test for {@link LightPreconditions#checkNotBlank(String, String)}
   */
  @Test
  public void test_checkNotBlank_msg() {
    String uuid = getUUIDString();
    // Negative : string=null
    try {
      checkNotBlank(null, uuid);
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // expected.
      assertTrue(e.getMessage().contains(uuid));

    }

    // Negative : string=""
    try {
      checkNotBlank("", uuid);
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // expected.
      assertTrue(e.getMessage().contains(uuid));
    }
  }

  /**
   * Test for {@link LightPreconditions#checkPersonId(String)}
   */
  @Test
  public void test_checkPersonId() {
    checkPersonId(1234L);
    checkPersonId(1L);
    checkPersonId(Long.MAX_VALUE);

    try {
      checkPersonId(null);
      fail("should have failed.");
    } catch (InvalidPersonIdException e) {
      // Expected
    }

    try {
      checkPersonId(0L);
      fail("should have failed.");
    } catch (InvalidPersonIdException e) {
      // Expected
    }

    // Rest negative tests are tested as part of LightPreconditions#checkPositiveLong
  }

  /**
   * Test for {@link LightPreconditions#checkPositiveLong(Long)}
   */
  @Test
  public void test_checkPositiveLong() {
    // Positive Tests.
    checkPositiveLong(1L);
    checkPositiveLong(Long.MAX_VALUE);

    // Negative Test : Null value.
    try {
      checkPositiveLong(null);
      fail("should have failed.");
    } catch (NullPointerException e) {
      // Expected
    }

    // Negative Test : 0L
    try {
      checkPositiveLong(0L);
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // Expected
    }

    // Negative Test : -1L
    try {
      checkPositiveLong(-1L);
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // Expected
    }

    // Negative Test : Long.MIN_VALUE
    try {
      checkPositiveLong(Long.MIN_VALUE);
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // Expected
    }
  }

  /**
   * Test for {@link LightPreconditions#checkNull(Object)}.
   */
  @Test
  public void test_checkNull() {
    checkNull(null);

    try {
      checkNull("");
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // Expected
    }
  }
}
