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
import static com.google.light.server.utils.GuiceUtils.isExpectedException;
import static com.google.light.server.utils.LightPreconditions.checkPositiveLong;
import static com.google.light.testingutils.TestingUtils.getInjectorByEnv;
import static com.google.light.testingutils.TestingUtils.getMockSessionForTesting;
import static com.google.light.testingutils.TestingUtils.getRandomEmail;
import static com.google.light.testingutils.TestingUtils.getRandomPersonId;
import static com.google.light.testingutils.TestingUtils.getRandomProviderUserId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.base.Throwables;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;
import com.google.light.server.AbstractLightServerTest;
import com.google.light.server.exception.unchecked.IdShouldNotBeSet;
import com.google.light.server.exception.unchecked.InvalidPersonIdException;
import com.google.light.server.exception.unchecked.httpexception.PersonLoginRequiredException;
import com.google.light.server.manager.interfaces.PersonManager;
import com.google.light.server.persistence.entity.person.PersonEntity;
import javax.servlet.http.HttpSession;
import org.junit.Test;
import org.mockito.Mockito;

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
    this.personManager = getInstance(injector, PersonManager.class);
  }

  private PersonEntity.Builder getEntityBuilder() {
    return new PersonEntity.Builder()
        .firstName(testFirstName)
        .lastName(testLastName);
  }

  /**
   * Test for {@link PersonLoginRequiredException}
   */
  @Test
  public void test_UserLoginRequired() throws Exception {
    try {
      // First create a empty session and bind it under RequestScope.
      Injector tempInjector = getInjectorByEnv(defaultEnv, Mockito.mock(HttpSession.class));
      getInstance(tempInjector, PersonManager.class);
      fail("should have failed.");
    } catch (ProvisionException e) {
      // Expected
      assertTrue(isExpectedException(e, PersonLoginRequiredException.class));
    }
  }

  /**
   * Test for {@link PersonManagerImpl#createPerson(PersonEntity)}
   */
  @Test
  public void test_createPerson() throws Exception {
    // Success Testing.
    PersonEntity person1 = getEntityBuilder().build();
    PersonEntity afterCreate = personManager.createPerson(person1);

    assertNotNull(afterCreate.getId());
    checkPositiveLong(afterCreate.getId(), "expected positive id.");
    assertEquals(testFirstName, afterCreate.getFirstName());
    assertEquals(testLastName, afterCreate.getLastName());
    assertEquals(testEmail, afterCreate.getEmail());

    // Negative Testing : Try to create Person with Id. This should fail.
    try {
      PersonEntity dummy = getEntityBuilder().id(getRandomPersonId()).build();

      personManager.createPerson(dummy);
      fail("Should have failed.");
    } catch (IdShouldNotBeSet e) {
      // Expected.
    }

    /*
     * Negative Testing : Try to create a person with email set. This should fail.
     */
    try {
      PersonEntity dummy = getEntityBuilder().email(getRandomEmail()).build();
      personManager.createPerson(dummy);
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // expected.
      assertTrue(Throwables.getStackTraceAsString(e).contains("email should not be set"));
    }

  }

  /**
   * Test for {@link PersonManagerImpl#getPerson(Long)}
   */
  @Test
  public void test_getPerson() throws Exception {
    PersonEntity person = getEntityBuilder().build();

    PersonEntity savedPerson = personManager.createPerson(person);
    PersonEntity fetchedPerson = personManager.getPerson(savedPerson.getId());
    assertEquals(savedPerson, fetchedPerson);

    // Negative Testing : Try to search with nullId.
    try {
      personManager.getPerson(null);
      fail("should have failed");
    } catch (InvalidPersonIdException e) {
      // expected.
    }

    // Negative Testing : Try to search with Invalid PersonId.
    try {
      personManager.getPerson(0L);
      fail("should have failed");
    } catch (InvalidPersonIdException e) {
      // expected.
    }

    /*
     * Negative Testing : Search with random PersonId.
     */
    assertNull(personManager.getPerson(getRandomPersonId()));

  }

  /**
   * Test for {@link PersonManager#getPersonByEmail(String)}
   */
  @Test
  public void test_getPersonByEmail() throws Exception {
    PersonEntity person1 = getEntityBuilder().build();
    PersonEntity expectedPerson1 = personManager.createPerson(person1);
    PersonEntity fetchedPerson1_1 = personManager.getPersonByEmail(testEmail);
    assertNotNull(fetchedPerson1_1);
    assertEquals(expectedPerson1, fetchedPerson1_1);

    // Now creating one more Person.
    String email2 = getRandomEmail(); // Emai will be added to PersonEntity via Session.
    assertTrue(!testEmail.equals(email2));
    PersonEntity person2 = getEntityBuilder().build();

    /*
     * In order to persist Person2 properly, we will have to create a new session2 and a new
     * PersonManager which will get this session2 as part of the SessionManager.
     */
    
    HttpSession session2 = getMockSessionForTesting(defaultEnv, defaultProviderService, 
        getRandomProviderUserId(), null, email2);
    Injector tempInjector = getInjectorByEnv(defaultEnv, session2);
    PersonManager personManager2 = getInstance(tempInjector, PersonManager.class);
    
    PersonEntity expectedPerson2 = personManager2.createPerson(person2);
    assertTrue(!expectedPerson1.equals(expectedPerson2));
    
    // After this personManager2 is not required.
    System.out.println(email2);
    PersonEntity fetchedPerson2_1 = personManager.getPersonByEmail(email2);
    System.out.println(fetchedPerson2_1.getEmail());
    assertNotNull(fetchedPerson2_1);
    assertEquals(expectedPerson2, fetchedPerson2_1);

    // Some more validation
    assertTrue(!person1.equals(person2));
    assertTrue(!fetchedPerson1_1.equals(fetchedPerson2_1));

    // Downloading again and confirming to ensure no data has changed for Person1.
    PersonEntity fetchedPerson1_2 = personManager.getPersonByEmail(testEmail);
    assertEquals(fetchedPerson1_1, fetchedPerson1_2);

    // Downloading again and confirming to ensure no data has changed for Person2
    PersonEntity fetchedPerson2_2 = personManager.getPersonByEmail(email2);
    assertEquals(fetchedPerson2_1, fetchedPerson2_2);
    
    assertTrue(!fetchedPerson1_2.equals(fetchedPerson2_2));

    // Negative Testing : Check for non-existing email.
    assertNull(personManager.getPersonByEmail(getRandomEmail()));
  }
}
