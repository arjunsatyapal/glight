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

import static com.google.light.server.utils.GuiceUtils.getInstance;
import static com.google.light.server.utils.ObjectifyUtils.commitTransaction;
import static com.google.light.server.utils.ObjectifyUtils.initiateTransaction;
import static com.google.light.testingutils.TestingUtils.getRandomEmail;
import static com.google.light.testingutils.TestingUtils.getRandomPersonId;
import static com.google.light.testingutils.TestingUtils.getRandomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.light.server.dto.pojo.longwrapper.PersonId;




import com.google.light.testingutils.TestingUtils;

import com.google.light.server.dto.person.PersonDto;
import com.google.light.server.exception.unchecked.EmailInUseException;
import com.google.light.server.persistence.entity.person.PersonEntity;
import com.google.light.server.utils.ObjectifyUtils;
import com.googlecode.objectify.Objectify;
import org.junit.Test;

/**
 * Test for {@link PersonDao}
 * 
 * @author Arjun Satyapal
 */
public class PersonDaoTest extends AbstractBasicDaoTest<PersonDto, PersonEntity> {

  public PersonDaoTest() {
    super(PersonEntity.class);
  }

  private String email;
  private PersonDao dao;

  @Override
  public void setUp() {
    super.setUp();
    email = TestingUtils.getRandomEmail();
    dao = getInstance(PersonDao.class);
  }

  private PersonEntity.Builder getDefaultEntityBuilder() {
    return new PersonEntity.Builder()
        .email(email)
        .firstName(testFirstName)
        .lastName(testLastName);
  }

  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_get_by_key_in_txn() {
    PersonId personId = getRandomPersonId();
    PersonEntity testEntity = getDefaultEntityBuilder().personId(personId).build();
    PersonEntity savedEntity = dao.put(testEntity);

    Objectify ofy = ObjectifyUtils.initiateTransaction();
    try {
      PersonEntity getEntiy = dao.get(ofy, savedEntity.getKey());
      assertEquals(savedEntity, getEntiy);
    } catch (Exception e) {
      ObjectifyUtils.rollbackTransactionIfStillActive(ofy);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_get_by_key() {
    PersonId id = getRandomPersonId();
    PersonEntity testEntity = getDefaultEntityBuilder().personId(id).build();

    PersonEntity savedEntity = dao.put(testEntity);
    PersonEntity getEntity = dao.get(savedEntity.getPersonId());
    assertEquals(savedEntity, getEntity);

    // Negative test : Try to fetch with randomId.
    assertNull(dao.get(getRandomPersonId()));
  }

  @Test
  @Override
  public void test_put() {
    PersonEntity testEntity = getDefaultEntityBuilder().build();
    PersonEntity savedEntity = dao.put(testEntity);
    PersonId entityId = savedEntity.getPersonId();

    PersonEntity getEntity = dao.findByEmail(email);
    assertEquals(savedEntity, getEntity);

    // Same entity can be persisted twice. Change email and persist.
    String newEmail = getRandomEmail();
    testEntity = getDefaultEntityBuilder()
        .personId(entityId)
        .email(newEmail)
        .build();
    PersonEntity newSavedEntity = dao.put(testEntity);
    assertEquals(newSavedEntity, dao.put(testEntity));
    assertEquals(newSavedEntity, dao.put(testEntity));

    // Ensures that savedEntity and newSavedEntity refer to same Entity.
    assertEquals(savedEntity.getPersonId(), newSavedEntity.getPersonId());
    // Ensures that savedEntity and newSavedEntity are not equal post modification.
    assertTrue(!savedEntity.equals(newSavedEntity));

    // Now original email can be set back.
    testEntity = getDefaultEntityBuilder()
        .personId(entityId)
        .email(email)
        .build();
    newSavedEntity = dao.put(testEntity);
    assertEquals(savedEntity, newSavedEntity);

    // Negative : Try to persist another person with existing email.
    try {
      PersonEntity dummy = new PersonEntity.Builder()
          .personId(getRandomPersonId())
          .email(email)
          .firstName(testFirstName)
          .lastName(testLastName)
          .build();

      PersonEntity savedPerson2 = dao.put(dummy);
      assertEquals(savedEntity.toString(), savedPerson2.toString());
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
  public void test_put_txn() {
    PersonId personId = getRandomPersonId();
    PersonEntity testEntity = getDefaultEntityBuilder()
        .personId(personId)
        .build();

    // Positive Test :
    Objectify txn = initiateTransaction();
    assertTrue(txn.getTxn().isActive());

    dao.put(txn, testEntity);
    commitTransaction(txn);
    PersonEntity getEntity = dao.get(personId);
    assertEquals(testEntity, getEntity);

    // Negative : Test with email already in use.
    txn = initiateTransaction();
    assertTrue(txn.getTxn().isActive());
    try {
      PersonEntity dummy = getDefaultEntityBuilder()
          .personId(getRandomPersonId())
          .build();
      dao.put(txn, dummy);
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
  public void test_findByEmail() {
    PersonEntity testPerson = getDefaultEntityBuilder().build();
    PersonEntity savedPerson = dao.put(testPerson);

    // Positive Test :
    PersonEntity getPerson = dao.findByEmail(email);
    assertEquals(savedPerson, getPerson);

    // Negative Test : Search by invalid email.
    assertNull(dao.findByEmail(getRandomEmail()));

    // Negative Test : Put two different person with same email. This should fail.
    try {
      PersonEntity dummy = new PersonEntity.Builder()
          .email(email)
          .firstName(getRandomString())
          .lastName(getRandomString())
          .build();
      dao.put(dummy);
      fail("should have failed.");
    } catch (EmailInUseException e) {
      // expected.
    }
  }
}
