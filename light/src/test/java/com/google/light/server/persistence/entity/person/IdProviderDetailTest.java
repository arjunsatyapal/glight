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
package com.google.light.server.persistence.entity.person;

import static org.junit.Assert.fail;

import com.google.light.server.constants.OpenIdAuthDomain;
import com.google.light.server.persistence.entity.AbstractPersistenceEntityTest;
import org.junit.Test;

/**
 * Test for {@link IdProviderDetail}
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("deprecation")
public class IdProviderDetailTest extends AbstractPersistenceEntityTest {
  private final OpenIdAuthDomain authDomain = OpenIdAuthDomain.GOOGLE;
  private final String email = "gmail@email.com";
  private final String federatedIdentity = "foo bar";

  private final IdProviderDetail detail1 = new IdProviderDetail(authDomain, email,
      federatedIdentity);

  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_constructor() {
    // Positive part is already tested. Lets test for negative things.

    // Negative Test : Test with authDomain = null.
    try {
      new IdProviderDetail(null, email, federatedIdentity);
      fail("should have failed.");
    } catch (NullPointerException e) {
      // expected.
    }

    // Negative Test : Test with email=null.
    try {
      new IdProviderDetail(authDomain, null, federatedIdentity);
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // expected.
    }

    // Negative Test : Test with email=""
    try {
      new IdProviderDetail(authDomain, "", federatedIdentity);
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // expected.
    }

    // Email tests are done as part of LightPreconditions#checkEmail.

    // Negative Test : Test with federatedIdentity=null.
    try {
      new IdProviderDetail(authDomain, email, null);
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // expected.
    }

    // Negative Test : Test with federatedIdentity="".
    try {
      new IdProviderDetail(authDomain, email, "");
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // expected.
    }
  }

  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_toDto() {
    try {
      detail1.toDto();
      fail("should have failed.");
    } catch (UnsupportedOperationException e) {
      // expected.
    }
  }
}
