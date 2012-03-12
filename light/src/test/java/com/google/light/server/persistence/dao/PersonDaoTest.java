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
package com.google.light.server.persistence.dao;

import static com.google.light.server.utils.ObjectifyUtils.commitTransaction;
import static com.google.light.server.utils.ObjectifyUtils.getKey;
import static com.google.light.server.utils.ObjectifyUtils.initiateTransaction;
import static com.google.light.testingutils.TestingUtils.getRandomEmail;
import static com.google.light.testingutils.TestingUtils.getRandomPersonId;
import static com.google.light.testingutils.TestingUtils.getRandomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.light.server.dto.person.PersonDto;
import com.google.light.server.exception.unchecked.EmailInUseException;
import com.google.light.server.persistence.entity.person.PersonEntity;
import com.google.light.server.utils.ObjectifyUtils;
import com.google.light.testingutils.TestingUtils;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import org.junit.Test;

/**
 * Test for {@link PersonDao}
 * 
 * @author Arjun Satyapal
 */
public class PersonDaoTest extends AbstractBasicDaoTest<PersonDto, PersonEntity, Long> {

  public PersonDaoTest() {
    super(PersonEntity.class, Long.class);
  }

  private PersonDao personDao;
  private PersonEntity.Builder defaultPersonBuilder;

  @Override
  public void setUp() {
    super.setUp();

    personDao = testInstanceProvider.getPersonDao();
    defaultPersonBuilder = new PersonEntity.Builder()
        .email(testEmail)
        .firstName(testFirstName)
        .lastName(testLastName);
  }

  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_get_ofyKey() {
    Long personId = getRandomPersonId();
    PersonEntity testPerson = defaultPersonBuilder.id(personId).build();
    PersonEntity savedPerson = personDao.put(testPerson);

    Objectify ofy = ObjectifyUtils.initiateTransaction();
    try {
      Key<PersonEntity> key = personDao.getKey(savedPerson.getId());
      PersonEntity getPerson = personDao.get(ofy, key);
      assertEquals(savedPerson, getPerson);
    } catch (Exception e) {
      ObjectifyUtils.rollbackTransaction(ofy);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_get_ofyId() {
    Long personId = getRandomPersonId();
    PersonEntity testPerson = defaultPersonBuilder.id(personId).build();

    PersonEntity savedPerson = personDao.put(testPerson);
    PersonEntity getPerson = personDao.get(savedPerson.getId());
    assertEquals(savedPerson, getPerson);

    // Negative test.
    assertNull(personDao.get(getRandomPersonId()));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void test_get_key() {
    Long rlong = TestingUtils.getRandomLongNumber();
    assertEquals(getKey(PersonEntity.class, rlong), personDao.getKey(rlong));
  }

  @Test
  @Override
  public void test_put() {
    PersonEntity testPerson = defaultPersonBuilder.build();
    PersonEntity savedPerson = personDao.put(testPerson);
    PersonEntity getPerson = personDao.getByEmail(testEmail);
    assertEquals(savedPerson, getPerson);

    // Same entity can be persisted twice. Change email and persist.
    // TODO(arjuns): Encapsulate email and PersonId into pojos.

    String newEmail = getRandomEmail();
    testPerson.setEmail(newEmail);
    savedPerson = personDao.put(testPerson);
    assertEquals(savedPerson, personDao.put(testPerson));
    assertEquals(savedPerson, personDao.put(testPerson));

    // Now original email can be set back.
    testPerson.setEmail(testEmail);
    assertEquals(savedPerson, personDao.put(testPerson));

    // Negative : Try to persist another person with existing email.
    try {
      PersonEntity dummy = new PersonEntity.Builder()
          .id(getRandomPersonId())
          .email(testEmail)
          .firstName(testFirstName)
          .lastName(testLastName)
          .build();

      PersonEntity savedPerson2 = personDao.put(dummy);
      assertEquals(savedPerson.toString(), savedPerson2.toString());
      fail("should have failed.");
    } catch (EmailInUseException e) {
      // Expected
    }
  }

  //
  /**
   * {@inheritDoc} Most negative tests are tested in previous. Here we will test one success and one
   * failure. To ensure that txn are properly handled.
   */
  @Test
  @Override
  public void test_put_ofyEntity() {
    Long personId = getRandomPersonId();
    PersonEntity testPerson = defaultPersonBuilder.id(personId).build();

    //
    Objectify txn = initiateTransaction();
    assertTrue(txn.getTxn().isActive());

    personDao.put(txn, testPerson);
    commitTransaction(txn);
    PersonEntity getPerson = personDao.get(personId);
    assertEquals(testPerson, getPerson);

    // Negative : Test with email already in use.
    txn = initiateTransaction();
    assertTrue(txn.getTxn().isActive());
    try {
      PersonEntity dummy = defaultPersonBuilder.id(getRandomPersonId()).build();
      personDao.put(txn, dummy);
      fail("should have failed.");
    } catch (EmailInUseException e) {
      // Expected
    } finally {
      assertTrue(txn.getTxn().isActive());
    }
  }

  /** Tests specific for {@link PersonDao}. */

  /**
   * Test for {@link PersonDao#getByEmail(String)}
   */
  @Test
  public void test_getByEmail() {
    PersonEntity testPerson = defaultPersonBuilder.build();
    PersonEntity savedPerson = personDao.put(testPerson);

    // Positive Test :
    PersonEntity getPerson = personDao.getByEmail(testEmail);
    assertEquals(savedPerson, getPerson);

    // Negative Test : Search by invalid email.
    assertNull(personDao.getByEmail(getRandomEmail()));

    // Negative Test : Put two different person with same email. This should fail.
    try {
      PersonEntity dummy = new PersonEntity.Builder()
          .email(testEmail)
          .firstName(getRandomString())
          .lastName(getRandomString())
          .build();
      personDao.put(dummy);
      fail("should have failed.");
    } catch (EmailInUseException e) {
      // expected.
    }
  }
}
