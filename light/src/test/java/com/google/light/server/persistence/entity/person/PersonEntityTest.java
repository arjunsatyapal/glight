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
import static com.google.light.testingutils.TestingUtils.getRandomString;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.google.light.server.dto.pojo.typewrapper.longwrapper.PersonId;


import com.google.light.server.dto.person.PersonDto;
import com.google.light.server.exception.unchecked.BlankStringException;
import com.google.light.server.exception.unchecked.InvalidPersonIdException;
import com.google.light.server.persistence.entity.AbstractPersistenceEntityTest;
import com.google.light.testingutils.TestingUtils;
import com.googlecode.objectify.Key;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link PersonEntity}.
 * 
 * @author Arjun Satyapal
 */
public class PersonEntityTest extends AbstractPersistenceEntityTest {

  private PersonId personId;
  private String email;
  private String firstName;
  private String lastName;

  @Before
  public void setUp() {
    personId = getRandomPersonId();
    email = getRandomEmail();
    firstName = getRandomString();
    lastName = getRandomString();
  }

  private PersonEntity.Builder getEntityBuilderWithoutId() {
    return new PersonEntity.Builder()
        .email(email)
        .firstName(firstName)
        .lastName(lastName);
  }

  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_builder_with_constructor() {
    // Valid case is already tested.

    // Positive test : null personId should pass.

    getEntityBuilderWithoutId().personId(null).build();

    PersonEntity entity = getEntityBuilderWithoutId().personId(null).build();
    assertNotNull(entity);
    assertFalse("By default it should be false", entity.getAcceptedTos());

    // Negative test : zero personId
    try {
      getEntityBuilderWithoutId().personId(new PersonId(0L)).build();
      fail("Should have failed.");
    } catch (InvalidPersonIdException e) {
      // Expected.
    }

    // Negative test : negative personId
    try {
      getEntityBuilderWithoutId().personId(new PersonId(-3L)).build();
      fail("Should have failed.");
    } catch (InvalidPersonIdException e) {
      // Expected.
    }

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
    } catch (BlankStringException e) {
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
        .acceptedTos(false)
        .build();
    assertEquals(expectedDto, getEntityBuilderWithoutId().build().toDto());
    assertEquals(expectedDto, getEntityBuilderWithoutId().personId(personId).build().toDto());
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
    testPerson.setPersonId(personId);

    // Negative Test : trying changing value.
    try {
      testPerson.setPersonId(getRandomPersonId());
      fail("should have failed");
    } catch (UnsupportedOperationException e) {
      // expected.
    }

    // Negative test : id=null
    try {
      getEntityBuilderWithoutId().build().setPersonId(null);
      fail("should have failed");
    } catch (InvalidPersonIdException e) {
      // expected.
    }

    // Negative test : id = a negative value.
    try {
      getEntityBuilderWithoutId().build().setPersonId(new PersonId(-1L));
      fail("should have failed");
    } catch (InvalidPersonIdException e) {
      // expected.
    }
  }

  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_getKey() {
    PersonId randomPersonId = TestingUtils.getRandomPersonId();
    PersonEntity entity = getEntityBuilderWithoutId().personId(randomPersonId).build();
    Key<PersonEntity> expectedKey =
        new Key<PersonEntity>(PersonEntity.class, randomPersonId.getValue());
    assertEquals(expectedKey, entity.getKey());
  }

  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_generateKey() {
    // Positive tests are done as part of test_getKey

    // Negative test : persondId = null
    try {
      PersonEntity.generateKey(null);
      fail("should have failed.");
    } catch (InvalidPersonIdException e) {
      // Expected
    }

    // Negative test : persondId = 0
    try {
      PersonEntity.generateKey(new PersonId(0L));
      fail("should have failed.");
    } catch (InvalidPersonIdException e) {
      // Expected
    }

    // Negative test : persondId = negative
    try {
      PersonEntity.generateKey(new PersonId(-3L));
      fail("should have failed.");
    } catch (InvalidPersonIdException e) {
      // Expected
    }

  }
}
