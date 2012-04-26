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
package com.google.light.server.manager.implementation;

import static com.google.light.server.utils.GuiceUtils.getInstance;

import com.google.light.server.utils.LightPreconditions;

import javax.servlet.http.HttpSession;

import org.mockito.Mockito;

import com.google.light.server.guice.provider.TestRequestScopedValuesProvider;

import com.google.light.server.constants.LightEnvEnum;

import com.google.light.testingutils.TestingUtils;

import static com.google.light.server.utils.LightPreconditions.checkPersonId;

import static com.google.light.testingutils.TestingUtils.getInjectorByEnv;
import static com.google.light.testingutils.TestingUtils.getMockSessionForTesting;
import static com.google.light.testingutils.TestingUtils.getRandomEmail;
import static com.google.light.testingutils.TestingUtils.getRandomPersonId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import com.google.light.server.dto.pojo.PersonId;

import org.junit.Test;
import com.google.inject.Injector;

import com.google.light.server.AbstractLightServerTest;
import com.google.light.server.exception.unchecked.IdShouldNotBeSet;
import com.google.light.server.exception.unchecked.InvalidPersonIdException;
import com.google.light.server.exception.unchecked.httpexception.PersonLoginRequiredException;
import com.google.light.server.manager.interfaces.PersonManager;
import com.google.light.server.persistence.entity.person.PersonEntity;

/**
 * Tests for {@link PersonManagerImpl}
 * 
 * @author Arjun Satyapal
 */
public class PersonManagerImplTest extends AbstractLightServerTest {
  private PersonManager personManager;

  @Override
  public void setUp() {
    super.setUp();
    this.personManager = getInstance(PersonManager.class);
  }

  private PersonEntity.Builder getEntityBuilder() {
    return new PersonEntity.Builder()
        .firstName(testFirstName)
        .lastName(testLastName)
        .email(testEmail);
  }

  /**
   * Test for {@link PersonManagerImpl#create(PersonEntity)}
   */
  @Test
  public void test_create() throws Exception {
    // Success Testing.
    // we dont want to use TestEmail as two differnt person need to have two different emails.
    String email1 = getRandomEmail();
    PersonEntity person1 = getEntityBuilder().email(email1).build();
    PersonEntity afterCreate = personManager.create(person1);

    assertNotNull(afterCreate.getPersonId());
    checkPersonId(afterCreate.getPersonId());

    assertEquals(testFirstName, afterCreate.getFirstName());
    assertEquals(testLastName, afterCreate.getLastName());
    assertEquals(email1, afterCreate.getEmail());

    // Negative Testing : Try to create Person with Id. This should fail.
    try {
      PersonEntity dummy = getEntityBuilder().personId(getRandomPersonId()).build();

      personManager.create(dummy);
      fail("Should have failed.");
    } catch (IdShouldNotBeSet e) {
      // Expected.
    }

    /*
     * Negative Testing : email = null
     */
    try {
      PersonEntity dummy = getEntityBuilder().email(null).build();
      personManager.create(dummy);
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // expected.
    }

    /*
     * Negative Testing : email = blank
     */
    try {
      PersonEntity dummy = getEntityBuilder().email(" ").build();
      personManager.create(dummy);
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // expected.
    }

    // Negative testing for non-login.
    Injector nonLoginInjector = getInjectorByEnv(LightEnvEnum.UNIT_TEST,
        Mockito.mock(TestRequestScopedValuesProvider.class),
        Mockito.mock(HttpSession.class));
    PersonManager personManager = nonLoginInjector.getInstance(PersonManager.class);
    assertNotNull(personManager);

    try {
      personManager.create(getEntityBuilder().build());
      fail("should have failed.");
    } catch (PersonLoginRequiredException e) {
      // Expected
    }
  }

  /**
   * Test for {@link PersonManagerImpl#delete(PersonId)}
   */
  @Test
  public void test_delete() throws Exception {
    try {
      personManager.delete(getRandomPersonId());
      fail("Should have failed.");
    } catch (UnsupportedOperationException e) {
      // Expected.
    }
  }

  /**
   * Test for {@link PersonManagerImpl#update(PersonEntity)}
   */
  @Test
  public void test_update() throws Exception {
    // Success Testing.
    String email = getRandomEmail(); // client side provided email which is different from testEmail
    assertTrue(!email.equals(testEmail));
    PersonEntity person = getEntityBuilder().acceptedTos(true).email(email).build();
    PersonEntity afterUpdate = personManager.update(person);

    assertNotNull(afterUpdate.getPersonId());
    checkPersonId(afterUpdate.getPersonId());
    assertEquals(testFirstName, afterUpdate.getFirstName());
    assertEquals(testLastName, afterUpdate.getLastName());
    assertEquals(testEmail, afterUpdate.getEmail());

    // important to test, because if it is not persisted the user might
    // be trapped into being redirected to the register page for
    // every page he access loggining in.
    assertTrue(afterUpdate.getAcceptedTos());

    // Negative Testing : Try to create Person with Id. This should fail.
    try {
      PersonEntity dummy = getEntityBuilder().personId(getRandomPersonId()).build();

      personManager.create(dummy);
      fail("Should have failed.");
    } catch (IdShouldNotBeSet e) {
      // Expected.
    }
  }

  /**
   * Test for {@link PersonManagerImpl#get(PersonId)}
   */
  @Test
  public void test_get() throws Exception {
    PersonEntity person = getEntityBuilder().build();

    PersonEntity savedPerson = personManager.create(person);
    PersonEntity fetchedPerson = personManager.get(savedPerson.getPersonId());
    assertEquals(savedPerson, fetchedPerson);

    // Negative Testing : Try to search with nullId.
    try {
      personManager.get(null);
      fail("should have failed");
    } catch (InvalidPersonIdException e) {
      // expected.
    }

    // Negative Testing : Try to search with Invalid PersonId.
    try {
      personManager.get(new PersonId(0L));
      fail("should have failed");
    } catch (InvalidPersonIdException e) {
      // expected.
    }

    /*
     * Negative Testing : Search with random PersonId.
     */
    assertNull(personManager.get(getRandomPersonId()));

  }

  /**
   * Test for {@link PersonManager#findByEmail(String)}
   */
  @Test
  public void test_findByEmail() throws Exception {
    // Dont use testEmail for full control on this test.
    String email1 = getRandomEmail();

    PersonEntity person1 = getEntityBuilder().email(email1).build();
    PersonEntity expectedPerson1 = personManager.create(person1);
    PersonEntity fetchedPerson1_1 = personManager.findByEmail(email1);
    assertNotNull(fetchedPerson1_1);
    assertEquals(expectedPerson1, fetchedPerson1_1);

    // Now creating one more Person.
    String email2 = getRandomEmail(); // Email will be added to PersonEntity via Session.
    assertTrue(!email1.equals(email2));
    PersonEntity person2 = getEntityBuilder().email(email2).build();

    /*
     * Ideally new session should be created but we are punting on that.
     */

    PersonEntity expectedPerson2 = personManager.create(person2);
    assertTrue(!expectedPerson1.equals(expectedPerson2));

    PersonEntity fetchedPerson2_1 = personManager.findByEmail(email2);
    assertNotNull(fetchedPerson2_1);
    assertEquals(expectedPerson2, fetchedPerson2_1);

    // Some more validation
    assertTrue(!person1.equals(person2));
    assertTrue(!fetchedPerson1_1.equals(fetchedPerson2_1));

    // Downloading again and confirming to ensure no data has changed for Person1.
    PersonEntity fetchedPerson1_2 = personManager.findByEmail(email1);
    assertEquals(fetchedPerson1_1, fetchedPerson1_2);

    // Downloading again and confirming to ensure no data has changed for Person2
    PersonEntity fetchedPerson2_2 = personManager.findByEmail(email2);
    assertEquals(fetchedPerson2_1, fetchedPerson2_2);

    assertTrue(!fetchedPerson1_2.equals(fetchedPerson2_2));

    // Negative Testing : Check for non-existing email.
    assertNull(personManager.findByEmail(getRandomEmail()));
  }

  /**
   * Test for {@link PersonManagerImpl#getCurrent()}
   */
  public void test_getCurrent() {

    // If the logged person was created in the datastore
    assertEquals(testPerson, personManager.getCurrent());

    // If not:
    testSession = getMockSessionForTesting(defaultEnv, defaultProviderService,
        testProviderUserId, null /* personId */, testEmail);
    Injector tempInjector =
        getInjectorByEnv(defaultEnv, testRequestScopedValueProvider, testSession);
    PersonManager tempPersonManager = tempInjector.getInstance(PersonManager.class);
    assertEquals(null, tempPersonManager.getCurrent());

  }
}
