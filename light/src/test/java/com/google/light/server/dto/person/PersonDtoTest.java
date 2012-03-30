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
package com.google.light.server.dto.person;

import static com.google.light.server.constants.TestResourceMappings.CREATE_PERSON;
import static com.google.light.testingutils.TestingUtils.getRandomEmail;
import static com.google.light.testingutils.TestingUtils.getRandomLongNumber;
import static com.google.light.testingutils.TestingUtils.getRandomString;
import static com.google.light.testingutils.TestingUtils.getResourceAsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import com.google.light.server.dto.AbstractDtoToPersistenceTest;
import com.google.light.server.exception.unchecked.BlankStringException;
import com.google.light.server.persistence.entity.person.PersonEntity;
import com.google.light.server.utils.JsonUtils;
import com.google.light.server.utils.XmlUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * Test for {@link PersonDto}.
 * 
 * @author Arjun Satyapal
 */
public class PersonDtoTest extends AbstractDtoToPersistenceTest {
  private String firstName;
  private String lastName;
  private String email;

  @Before
  public void setUp() {
    firstName = getRandomString();
    lastName = getRandomString();
    email = getRandomEmail();
  }
  
  private PersonDto.Builder getDtoBuilder() {
    return new PersonDto.Builder()
        .firstName(firstName)
        .lastName(lastName)
        .email(email);
  }

  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_builder() {
    // No validation logic in constructor. So nothing to test here.
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Test
  public void test_toJson() throws Exception {
    String xmlString = getResourceAsString(CREATE_PERSON.getXmlResPath());
    PersonDto createPersonDto = XmlUtils.getDto(xmlString);
    createPersonDto.validate();
    
    String jsonString = createPersonDto.toJson();
    String expectedJsonString = getResourceAsString(CREATE_PERSON.getJsonResPath());
    
    assertEquals(expectedJsonString, jsonString);
  }

  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_toPersistenceEntity() {
    PersonEntity.Builder entityBuilder = new PersonEntity.Builder()
        .firstName(firstName)
        .lastName(lastName)
        .email(email);

    PersonDto personDto = getDtoBuilder().build();
    assertEquals(entityBuilder.build(), personDto.toPersistenceEntity(null));

    Long value = getRandomLongNumber();
    assertEquals(entityBuilder.id(value).build(), personDto.toPersistenceEntity(value));
  }

  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_validate() {
    // Positive Test
    PersonDto person1 = getDtoBuilder().build();
    person1.validate();

    // Negative Test : firstName=null
    try {
      getDtoBuilder().firstName(null).build();
      fail("should have failed.");
    } catch (BlankStringException e) {
      // expected.
    }

    // Negative Test : firstName=" "
    try {
      getDtoBuilder().firstName(" ").build();
      fail("should have failed.");
    } catch (BlankStringException e) {
      // expected.
    }

    // Negative Test : lastName=null
    try {
      getDtoBuilder().lastName(null).build();
      fail("should have failed.");
    } catch (BlankStringException e) {
      // expected.
    }

    // Negative Test : lastName=" "
    try {
      getDtoBuilder().lastName(" ").build();
      fail("should have failed.");
    } catch (BlankStringException e) {
      // expected.
    }

    // Negative tests for Email are done with LightPreconditions.
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @Test
  public void test_toXml() throws Exception {
    String jsonString = getResourceAsString(CREATE_PERSON.getJsonResPath());
    PersonDto createPersonDto = JsonUtils.getDto(jsonString, PersonDto.class);
    createPersonDto.validate();
    
    String xmlString = createPersonDto.toXml();
    String expectedXmlString = getResourceAsString(CREATE_PERSON.getXmlResPath());
    
    assertEquals(expectedXmlString, xmlString);
  }
}
