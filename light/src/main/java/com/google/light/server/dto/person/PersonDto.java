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

import static com.google.light.server.utils.LightPreconditions.checkNotEmptyString;

import com.google.light.server.persistence.entity.person.PersonEntity;


/**
 * DTO for {@link PersonEntity}.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class PersonDto implements DtoToPersistenceInterface<PersonDto, PersonEntity>{
  private String id;
  private String firstName;
  private String lastName;
  private String email;

  @Override
  public PersonDto validate() {
    checkNotEmptyString(firstName);
    checkNotEmptyString(lastName);
    checkNotEmptyString(email);
    return this;
  }

  @Override
  public PersonEntity toPersistenceEntity() {
    return new PersonEntity(id, firstName, lastName, email, null);
  }
  
  public PersonDto(String id, String firstName, String lastName, String email) {
    this.id = id;
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
  }
  
  // Getters and setters.
  public String getId() {
    return id;
  }
}
