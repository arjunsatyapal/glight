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
import static com.google.light.server.utils.LightPreconditions.checkNotEmptyString;
import static com.google.light.server.utils.LightPreconditions.checkPersonIsLoggedIn;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.light.server.dto.person.PersonDto;
import com.google.light.server.persistence.PersistenceToDtoInterface;
import java.util.List;
import javax.annotation.Nullable;
import javax.persistence.Embedded;
import javax.persistence.Id;

/**
 * Persistence object for {@link PersonDto}.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class PersonEntity implements PersistenceToDtoInterface<PersonEntity, PersonDto> {
  @Id
  String id;
  String firstName;
  String lastName;

  // This is used in Objectify Query.
  public static final String OFY_EMAIL_QUERY_STRING = "email";
  String email;

  @Embedded
  List<IdProviderDetail> idProviderDetails;

  @Override
  public String toString() {
    return "PersonEntity [id=" + id + ", firstName=" + firstName + ", lastName=" + lastName
        + ", email=" + email + ", idProviderDetails=" + idProviderDetails + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((email == null) ? 0 : email.hashCode());
    result = prime * result + ((firstName == null) ? 0 : firstName.hashCode());
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result =
        prime * result
            + ((idProviderDetails == null) ? 0 : idProviderDetails.hashCode());
    result = prime * result + ((lastName == null) ? 0 : lastName.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    PersonEntity other = (PersonEntity) obj;
    if (email == null) {
      if (other.email != null)
        return false;
    } else if (!email.equals(other.email))
      return false;
    if (firstName == null) {
      if (other.firstName != null)
        return false;
    } else if (!firstName.equals(other.firstName))
      return false;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
      return false;
    if (idProviderDetails == null) {
      if (other.idProviderDetails != null)
        return false;
    } else if (!idProviderDetails.equals(other.idProviderDetails))
      return false;
    if (lastName == null) {
      if (other.lastName != null)
        return false;
    } else if (!lastName.equals(other.lastName))
      return false;
    return true;
  }

  // TODO(arjuns) : Add test for constructor.
  public PersonEntity(@Nullable String id, String firstName, String lastName, 
      @Nullable String email, @Nullable List<IdProviderDetail> idProviderDetails) {
    this.id = id;
    this.firstName = checkNotEmptyString(firstName);
    this.lastName = checkNotEmptyString(lastName);
    this.email = Strings.isNullOrEmpty(email) ? null : checkEmail(email);
    this.idProviderDetails = idProviderDetails;
  }

  @Override
  public PersonDto toDto() {
    PersonDto dto = new PersonDto(id, firstName, lastName, email);
    return dto.validate();
  }

  // For Objectify.
  @SuppressWarnings("unused")
  private PersonEntity() {
  }

  // Getters and setters.
  public String getId() {
    return id;
  }

  // TODO(arjuns) : Add a test.
  /**
   * Id can be set only when a User is Logged In.
   */
  public void setId(String id) {
    // On PersonEntity id can be set only if a user is logged in.
    // Or it will be read from Persistence, and will be pre-set.
    checkPersonIsLoggedIn();
    this.id = checkNotEmptyString(id);
  }

  public List<IdProviderDetail> getIdProviderDetails() {
    return idProviderDetails;
  }

  public void addIdProviderDetail(IdProviderDetail idProviderDetails) {
    if (this.idProviderDetails == null) {
      this.idProviderDetails = Lists.newArrayList();
    }

    if (!this.idProviderDetails.contains(idProviderDetails)) {
      this.idProviderDetails.add(idProviderDetails);
    }
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
    // On PersonEntity email can be set only if a user is logged in.
    // Or it will be read from Persistence, and will be pre-set.
    checkPersonIsLoggedIn();
    this.email = checkEmail(email);
  }

  public static class Builder {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private List<IdProviderDetail> idProviderDetails;
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
    
    public Builder idProviderDetails(List<IdProviderDetail> idProviderDetails) {
      this.idProviderDetails = idProviderDetails;
      return this;
    }

    public PersonEntity build() {
      // Id should not be set using builder. It should be set separately if required.
      return new PersonEntity(id, firstName, lastName, email, idProviderDetails);
    }
  }
}
