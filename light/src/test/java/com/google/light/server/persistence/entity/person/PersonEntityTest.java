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

import static com.google.light.testingutils.TestingUtils.getRandomEmail;
import static com.google.light.testingutils.TestingUtils.getRandomPersonId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.google.light.server.exception.unchecked.BlankStringException;

import com.google.light.server.exception.unchecked.InvalidPersonIdException;

import com.google.common.collect.ImmutableList;
import com.google.light.server.constants.OpenIdAuthDomain;
import com.google.light.server.dto.person.PersonDto;
import com.google.light.server.persistence.entity.AbstractPersistenceEntityTest;
import org.junit.Test;

/**
 * Test for {@link PersonEntity}.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("deprecation")
public class PersonEntityTest extends AbstractPersistenceEntityTest {
  private final Long userId = 1234L;
  private final String email = "email@gmail.com";
  private final String firstName = "first name";
  private final String lastName = "last name";

  private final OpenIdAuthDomain authDomain = OpenIdAuthDomain.GOOGLE;
  private final String testFederatedId = "federatedId:2222";

  private final IdProviderDetail idProviderDetail = new IdProviderDetail(authDomain, email,
      testFederatedId);

  private final PersonEntity personWithoutId = getEntityBuilderWithoutId().build();

  private final PersonEntity personWithId = getEntityBuilderWithoutId()
      .id(userId)
      .build();

  private PersonEntity.Builder getEntityBuilderWithoutId() {
    return new PersonEntity.Builder()
        .email(email)
        .firstName(firstName)
        .lastName(lastName)
        .idProviderDetails(ImmutableList.of(idProviderDetail));
  }

  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_builder() {
    // Valid case is already tested.

    // Negative test : First Name=null
    try {
      getEntityBuilderWithoutId().firstName(null).build();
      fail("Should have failed.");
    } catch (BlankStringException e) {
      // Expected.
    }

    // Negative test : First Name=" "
    try {
      getEntityBuilderWithoutId().firstName(" ").build();
      fail("Should have failed.");
    } catch (BlankStringException e) {
      // Expected.
    }

    // Negative test : Last Name=null
    try {
      getEntityBuilderWithoutId().lastName(null).build();
      fail("Should have failed.");
    } catch (BlankStringException e) {
      // Expected.
    }

    // Negative test : Last Name=" "
    try {
      getEntityBuilderWithoutId().lastName(" ").build();
      fail("Should have failed.");
    } catch (BlankStringException  e) {
      // Expected.
    }

    /*
     * Negative test : Email negative tests are tested as part of the test for the setEmail. Here
     * testing only the constructor part.
     */

    // Email == null is allowed. so no negative test for that.
    
    // Negative test : email=""
    try {
      getEntityBuilderWithoutId().email(" ").build();
      fail("Should have failed.");
    } catch (IllegalArgumentException e) {
      // Expected.
    }
  }

  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_toDto() {
    PersonDto expectedDto = new PersonDto.Builder()
      .firstName(firstName)
      .lastName(lastName)
      .email(email)
      .build();
    assertEquals(expectedDto, personWithoutId.toDto());
    assertEquals(expectedDto, personWithId.toDto());
  }

  /**
   * Test for {@link PersonEntity#setEmail(String)}
   */
  @Test
  public void test_setEmail() {
    // Negative test : Email = null
    try {
      getEntityBuilderWithoutId().build().setEmail(null);
      fail("Should have failed.");
    } catch (IllegalArgumentException e) {
      // Expected.
    }

    // Negative test : Email = ""
    try {
      getEntityBuilderWithoutId().email("").build();
      fail("Should have failed.");
    } catch (IllegalArgumentException e) {
      // Expected.
    }

    // Negative test : Email = xyz
    try {
      getEntityBuilderWithoutId().email("xyz").build();
      fail("Should have failed.");
    } catch (IllegalArgumentException e) {
      // Expected.
    }

    // Negative test : Email = xyz@
    try {
      getEntityBuilderWithoutId().email("xyz@").build();
      fail("Should have failed.");
    } catch (IllegalArgumentException e) {
      // Expected.
    }

    // Negative test : Email = xyz@
    try {
      getEntityBuilderWithoutId().email("xyz@@").build();

      fail("Should have failed.");
    } catch (IllegalArgumentException e) {
      // Expected.
    }

    // Negative test : Email = xyz@abc
    try {
      getEntityBuilderWithoutId().email("xyz@abc").build();
      fail("Should have failed.");
    } catch (IllegalArgumentException e) {
      // Expected.
    }

    // Negative test : Email = xyz@abc@
    try {
      getEntityBuilderWithoutId().email("xyz@abc@").build();
      fail("Should have failed.");
    } catch (IllegalArgumentException e) {
      // Expected.
    }

    // Ensuring that TestingUtils is generating right emails :)
    getEntityBuilderWithoutId().email(getRandomEmail()).build();
  }

  /**
   * Test for {@link PersonEntity#setId(String)}
   */
  @Test
  public void test_setId() {
    // Positive Test
    PersonEntity testPerson = getEntityBuilderWithoutId().build();
    testPerson.setId(userId);

    // Negative Test : trying changing value.
    try {
      testPerson.setId(getRandomPersonId());
      fail("should have failed");
    } catch (UnsupportedOperationException e) {
      // expected.
    }

    // Negative test : id=null
    try {
      getEntityBuilderWithoutId().build().setId(null);
      fail("should have failed");
    } catch (InvalidPersonIdException e) {
      // expected.
    }

    // Negative test : id = a negative value.
    try {
      getEntityBuilderWithoutId().build().setId(-1L);
      fail("should have failed");
    } catch (InvalidPersonIdException e) {
      // expected.
    }
  }
}
