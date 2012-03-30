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

import static com.google.light.server.utils.LightPreconditions.checkEmail;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.LightPreconditions.checkPersonId;

import com.google.light.server.dto.person.PersonDto;
import com.google.light.server.persistence.PersistenceToDtoInterface;
import javax.persistence.Id;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Persistence object for {@link PersonDto}.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class PersonEntity implements PersistenceToDtoInterface<PersonEntity, PersonDto> {
  @Id
  Long id;
  String firstName;
  String lastName;

  // This is used in Objectify Query.
  public static final String OFY_EMAIL_QUERY_STRING = "email";
  String email;

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
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
  public PersonDto toDto() {
    PersonDto dto = new PersonDto.Builder()
        .firstName(firstName)
        .lastName(lastName)
        .email(email)
        .build();
    return dto.validate();
  }


  // Getters and setters.
  public Long getId() {
    return id;
  }

  // TODO(arjuns) : Add a test.
  /**
   * Id can be set only when a User is Logged In.
   */
  public void setId(Long id) {
    if (this.id != null) {
      throw new UnsupportedOperationException("Id cannot be changed once it is set.");
    }

    this.id = checkPersonId(id);
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = checkEmail(email);
  }

  public static class Builder {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;

    public Builder id(Long id) {
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

    @SuppressWarnings("synthetic-access")
    public PersonEntity build() {
      return new PersonEntity(this);
    }
  }

  @SuppressWarnings("synthetic-access")
  private PersonEntity(Builder builder) {
    this.id = builder.id != null ? checkPersonId(builder.id) : null;
    this.firstName = checkNotBlank(builder.firstName, "firstName");
    this.lastName = checkNotBlank(builder.lastName, "lastName");
    this.email = builder.email != null ? checkEmail(builder.email) : null;
  }
  
  // For Objectify.
  private PersonEntity() {
  }
}
