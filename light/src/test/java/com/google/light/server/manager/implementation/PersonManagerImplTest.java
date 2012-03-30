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
import static com.google.light.testingutils.TestingUtils.getRandomEmail;
import static com.google.light.testingutils.TestingUtils.getRandomPersonId;
import static com.google.light.testingutils.TestingUtils.getRandomUserId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.inject.ProvisionException;
import com.google.light.server.AbstractLightServerTest;
import com.google.light.server.exception.unchecked.IdShouldNotBeSet;
import com.google.light.server.exception.unchecked.InvalidPersonIdException;
import com.google.light.server.exception.unchecked.httpexception.PersonLoginRequiredException;
import com.google.light.server.manager.interfaces.PersonManager;
import com.google.light.server.persistence.entity.person.PersonEntity;
import com.google.light.server.utils.GuiceUtils;
import org.junit.Ignore;
import org.junit.Test;

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

  @Override
  public void tearDown() {
    personManager = null;
    super.tearDown();
  }

  /**
   * Test for {@link PersonLoginRequiredException}
   */
  @Ignore(value="TODO(arjuns): Fix this eventually.")
  @Test
  public void test_UserLoginRequired() throws Exception {
    /*
     * Explicitly creating our own gaeTestingUtil as things are binded in LightScope and
     * AbstractLightTest inits the LightScope by default.
     */
    try {
      tearDown();
      // If user is not Logged In, then essentially any of the PersonManager methods cannot
      // be called. Here we have to override default setup values.
      gaeEnvReset(getRandomEmail(), false /* isUserLoggedIn */, false /* isGaeAdmin */);
      setUp();

      PersonEntity dummyPerson = new PersonEntity.Builder()
          .firstName(testFirstName)
          .lastName(testLastName)
          .build();
      personManager.createPerson(dummyPerson);
      fail("should have failed.");
    } catch (ProvisionException e) {
      assertTrue(GuiceUtils.isExpectedException(e, PersonLoginRequiredException.class));
    }
  }

  /**
   * Test for {@link PersonManagerImpl#createPerson(PersonEntity)}
   */
  @Ignore(value="TODO(arjuns) : fix me.")
  @Test
  public void test_createPerson() throws Exception {
    // Success Testing.
    PersonEntity person1 = new PersonEntity.Builder()
        .firstName(testFirstName)
        .lastName(testLastName)
        .build();

    PersonEntity expectedPerson = person1;
    PersonEntity afterCreate = personManager.createPerson(expectedPerson);

    assertEquals(testFirstName, afterCreate.getFirstName());
    assertEquals(testLastName, afterCreate.getLastName());
    assertEquals(testEmail, afterCreate.getEmail());

    // Negative Testing : Try to create Person with Id. This should fail.
    try {
      PersonEntity dummy = new PersonEntity.Builder()
          .firstName(testFirstName)
          .lastName(testLastName)
          .id(getRandomPersonId()) // This should trigger negative scenario.
          .build();

      personManager.createPerson(dummy);
      fail("Should have failed.");
    } catch (IdShouldNotBeSet e) {
      // Expected.
    }

    /*
     * Negative Testing : Try to create a person with entity having a email different from Login
     * Email. This should fail till we don't support other federated logins.
     */
    try {
      PersonEntity dummy = new PersonEntity.Builder()
          .firstName(testFirstName)
          .lastName(testLastName)
          .email(getRandomEmail()) // This is going to be different from what env is initialized.
          .build();

      // ensuring that test is init with a different email.
      gaeEnvReset(getRandomEmail(), true /* isUserLoggedIn */, false /* isGaeAdmin */);
      personManager.createPerson(dummy);
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // expected.
    }

  }

  /**
   * Test for {@link PersonManagerImpl#getPerson(Long)}
   */
  @Test
  public void test_getPerson() throws Exception {
    PersonEntity person1 = new PersonEntity.Builder()
        .firstName(testFirstName)
        .lastName(testLastName)
        .build();

    PersonEntity expectedPerson = personManager.createPerson(person1);
    PersonEntity actualPerson = personManager.getPerson(expectedPerson.getId());

    assertEquals(expectedPerson, actualPerson);

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
     * Negative Testing : Reset env to some other email, so that GAE returns different UserId.
     */
    assertNull(personManager.getPerson(getRandomPersonId()));

  }

  /**
   * Test for {@link PersonManager#getPersonByEmail(String)}
   */
  @Ignore(value="TODO(arjuns): Fix me.")
  @Test
  public void test_getPersonByEmail() throws Exception {
    String userId1 = getRandomUserId();
    String email1 = getRandomEmail();
    gaeEnvReset(email1, true /* isUserLoggedIn */, false /* isGaeAdmin */);

    PersonEntity person = new PersonEntity.Builder()
        .firstName(testFirstName)
        .lastName(testLastName)
        .build();

    PersonEntity expectedPerson1 = personManager.createPerson(person);

    PersonEntity actualPerson1 = personManager.getPersonByEmail(email1);
    assertNotNull(actualPerson1);
    assertEquals(expectedPerson1, actualPerson1);

    // Now creating one more Person.
    String userId2 = getRandomUserId();
    assertTrue(!userId1.equals(userId2));

    String email2 = getRandomEmail();
    assertTrue(!email1.equals(email2));

    gaeEnvReset(email2, true /* isUserLoggedIn */, false /* isGaeAdmin */);

    PersonEntity newPerson = new PersonEntity.Builder()
        .firstName(testFirstName)
        .lastName(testLastName)
        .build();
    PersonEntity expectedPerson2 = personManager.createPerson(newPerson);
    assertTrue(!expectedPerson1.equals(expectedPerson2));

    PersonEntity actualPerson2 = personManager.getPersonByEmail(email2);
    assertNotNull(actualPerson2);
    assertEquals(expectedPerson2, actualPerson2);

    // Some more validation
    assertTrue(!actualPerson1.equals(actualPerson2));

    // Downloading again and confirming to ensure no data has changed while storing Person2.
    actualPerson1 = personManager.getPersonByEmail(email1);
    assertNotNull(actualPerson1);
    assertTrue(!actualPerson1.equals(actualPerson2));

    // Negative Testing : Check for non-existing email.
    assertNull(personManager.getPersonByEmail(getRandomEmail()));
  }
}
