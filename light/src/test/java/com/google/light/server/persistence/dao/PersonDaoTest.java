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
import static com.google.light.server.utils.ObjectifyUtils.nonTransaction;
import static com.google.light.testingutils.TestingUtils.getRandomEmail;
import static com.google.light.testingutils.TestingUtils.getRandomLongNumber;
import static com.google.light.testingutils.TestingUtils.getRandomString;
import static com.google.light.testingutils.TestingUtils.getRandomUserId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.light.server.exception.unchecked.EmailInUseException;
import com.google.light.server.exception.unchecked.IllegalKeyTypeException;
import com.google.light.server.persistence.entity.person.PersonEntity;
import com.google.light.server.utils.ObjectifyUtils;
import com.googlecode.objectify.Objectify;
import org.junit.Test;

/**
 * Test for {@link PersonDao}
 * 
 * @author Arjun Satyapal
 */
public class PersonDaoTest extends AbstractBasicDaoTest {
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
   * Test for {@link PersonDao#getByEmail(String)}
   */
  @Test
  public void test_getByEmail() {
    String userId = getRandomUserId();
    PersonEntity testPerson = defaultPersonBuilder.id(userId).build();
    PersonEntity savedPerson = personDao.put(testPerson);
    PersonEntity getPerson = personDao.getByEmail(testEmail);
    assertEquals(savedPerson, getPerson);

    // Negative Test : Search by invalid email.
    assertNull(personDao.getByEmail(getRandomEmail()));

    // Negative Test : Put two different person with same email. This should fail.
    try {
      PersonEntity dummy = new PersonEntity.Builder()
          .id(getRandomUserId())
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

  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_get_longId() {
    // Negative Test.
    try {
      personDao.get(1234L);
      fail("should have failed.");
    } catch (IllegalKeyTypeException e) {
      // Expected
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void test_get_stringId() {
    String userId = getRandomUserId();
    PersonEntity testPerson = defaultPersonBuilder.id(userId).build();
    
    PersonEntity savedPerson = personDao.put(testPerson);
    PersonEntity getPerson = personDao.get(savedPerson.getId());
    assertEquals(savedPerson, getPerson);

    assertNull(personDao.get(getRandomUserId()));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void test_get_txn_key() {
    String userId = getRandomUserId();
    PersonEntity testPerson = defaultPersonBuilder.id(userId).build();
    
    PersonEntity savedPerson = personDao.put(testPerson);
    Objectify txn = nonTransaction();
    PersonEntity getPerson = personDao.get(txn, savedPerson.getId());
    assertEquals(savedPerson, getPerson);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void test_get_txn_longId() {
    Objectify txn = ObjectifyUtils.initiateTransaction();
    try {
      personDao.get(txn, getRandomLongNumber());
      fail("should have failed.");
    } catch (IllegalKeyTypeException e) {
      // Expected
    } finally {
      ObjectifyUtils.rollbackTransaction(txn);
    }
    // TODO(arjuns) : Add negative tests.
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void test_get_txn_stringId() {
    String userId = getRandomUserId();
    PersonEntity testPerson = defaultPersonBuilder.id(userId).build();
    
    PersonEntity savedPerson = personDao.put(testPerson);
    Objectify txn = ObjectifyUtils.initiateTransaction();
    try {
      PersonEntity getPerson = personDao.get(txn, savedPerson.getId());
      assertEquals(savedPerson, getPerson);
    } finally {
      ObjectifyUtils.commitTransaction(txn);
    }
    // TODO(arjuns) : Add negative tests.
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void test_getKey_long() {
    try {
      personDao.get(1234L);
      fail("should have failed.");
    } catch (IllegalKeyTypeException e) {
      // Expected
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void test_getKey_string() {
    assertEquals(getKey(PersonEntity.class, "2134"), personDao.getKey("2134"));
  }

  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_put() {
    String userId = getRandomUserId();
    PersonEntity testPerson = defaultPersonBuilder.id(userId).build();
    
    PersonEntity savedPerson = personDao.put(testPerson);
    PersonEntity getPerson = personDao.getByEmail(testEmail);
    assertEquals(savedPerson, getPerson);

    // Same entity can be persisted twice. Change email and persist.
    testPerson.setEmail(getRandomEmail());
    assertEquals(savedPerson, personDao.put(testPerson));
    assertEquals(savedPerson, personDao.put(testPerson));
    
    // Now original email can be set back.
    testPerson.setEmail(testEmail);
    assertEquals(savedPerson, personDao.put(testPerson));
    

    // Negative : Persist without id.
    try {
      PersonEntity dummy = new PersonEntity.Builder()
          .email(testEmail)
          .firstName(testFirstName)
          .lastName(testLastName)
          .build();

      personDao.put(dummy);
      fail("should have failed");
    } catch (IllegalArgumentException e) {
      // expected.
    }

    // Negative : Try to persist another person with existing email.
    try {
      PersonEntity dummy = new PersonEntity.Builder()
          .id(getRandomUserId())
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

  /**
   * {@inheritDoc}
   * Most negative tests are tested in previous. Here we will test one success and one failure. 
   * To ensure that txn are properly handled.
   */
  @Test
  @Override
  public void test_put_txn() {
    String userId = getRandomUserId();
    PersonEntity testPerson = defaultPersonBuilder.id(userId).build();
    
    // 
    Objectify txn = initiateTransaction();
    assertTrue(txn.getTxn().isActive());
    
    personDao.put(txn, testPerson);
    commitTransaction(txn);
    PersonEntity getPerson = personDao.get(userId);
    assertEquals(testPerson, getPerson);
    
    // Negative : Test with some randomId.
    txn = initiateTransaction();
    assertTrue(txn.getTxn().isActive());
    try {
      PersonEntity dummy = defaultPersonBuilder.id(getRandomUserId()).build();
      personDao.put(txn, dummy);
      fail("should have failed.");
    } catch (EmailInUseException e) {
      // Expected
    } finally {
      assertTrue(txn.getTxn().isActive());
    }
  }
}
