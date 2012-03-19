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

import org.apache.commons.lang.builder.HashCodeBuilder;

import org.apache.commons.lang.builder.EqualsBuilder;

import org.apache.commons.lang.builder.ToStringBuilder;

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
  Long id;
  String firstName;
  String lastName;

  // This is used in Objectify Query.
  public static final String OFY_EMAIL_QUERY_STRING = "email";
  String email;

  @Embedded
  List<IdProviderDetail> idProviderDetails;

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

  // TODO(arjuns) : Add test for constructor.
  // TODO(arjuns): Make constructor private to promote builder.
  public PersonEntity(@Nullable Long id, String firstName, String lastName, 
      @Nullable String email, @Nullable List<IdProviderDetail> idProviderDetails) {
    this.id = id;
    this.firstName = checkNotBlank(firstName);
    this.lastName = checkNotBlank(lastName);
    this.email = email == null ? null : checkEmail(email);
    this.idProviderDetails = idProviderDetails;
  }

  @Override
  public PersonDto toDto() {
    PersonDto dto = new PersonDto(firstName, lastName, email);
    return dto.validate();
  }

  // For Objectify.
  @SuppressWarnings("unused")
  private PersonEntity() {
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
    this.email = checkEmail(email);
  }

  public static class Builder {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private List<IdProviderDetail> idProviderDetails;
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
