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

import static com.google.light.server.utils.LightPreconditions.checkEmail;
import static com.google.light.server.utils.LightPreconditions.checkNotEmptyString;

import com.google.light.server.persistence.entity.person.PersonEntity;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * DTO for {@link PersonEntity}.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class PersonDto implements DtoToPersistenceInterface<PersonDto, PersonEntity> {
  private String id;
  private String firstName;
  private String lastName;
  private String email;

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  @Override
  public PersonDto validate() {
    checkNotEmptyString(firstName);
    checkNotEmptyString(lastName);
    checkEmail(email);
    return this;
  }

  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  @Override
  public PersonEntity toPersistenceEntity() {
    return new PersonEntity(id, firstName, lastName, email, null);
  }

  /**
   * Constructor for PersonDto.
   * @param id 
   * @param firstName 
   * @param lastName
   * @param email
   */
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

  public static class Builder {
    private String id;
    private String firstName;
    private String lastName;
    private String email;

    public Builder id(String id) {
      this.id = id;
      return this;
    }

    public Builder firstName(String firstName) {
      this.firstName = firstName;
      return this;
    }

    public Builder lastName(String lastName) {
      this.lastName = lastName;
      return this;
    }

    public Builder email(String email) {
      this.email = email;
      return this;
    }
    
    public PersonDto build() {
      PersonDto dto = new PersonDto(id, firstName, lastName, email);
      return dto.validate();
    }
  }
}
