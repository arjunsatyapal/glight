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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkEmail;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;


import com.google.light.server.dto.pojo.PersonId;



import com.google.light.server.constants.XsdPath;
import com.google.light.server.dto.DtoToPersistenceInterface;
import com.google.light.server.persistence.entity.person.PersonEntity;
import com.google.light.server.utils.JsonUtils;
import com.google.light.server.utils.XmlUtils;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

/**
 * DTO for {@link PersonEntity}.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
@XmlRootElement(name = "person")
@XmlType(name = "personType", propOrder = { "firstName", "lastName", "email", "acceptedTos" })
@XmlAccessorType(XmlAccessType.FIELD)
@JsonSerialize(include = Inclusion.NON_NULL)
public class PersonDto implements DtoToPersistenceInterface<PersonDto, PersonEntity, PersonId> {
  @XmlElement
  private String firstName;

  @XmlElement
  private String lastName;

  @XmlElement
  private String email;
  
  @XmlElement
  private boolean acceptedTos;

  @Override
  public PersonDto validate() {
    checkNotBlank(firstName, "firstName");
    checkNotBlank(lastName, "lastName");
    checkNotNull(acceptedTos, "acceptedTos");
    checkEmail(email);
    return this;
  }

  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }
  
  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  @Override
  public String toJson() {
    try {
      return JsonUtils.toJson(this);
    } catch (Exception e) {
      // TODO(arjuns) : Add exception handling later.
      throw new RuntimeException(e);
    }
  }

  @Override
  public PersonEntity toPersistenceEntity(PersonId personId) {
    return new PersonEntity.Builder()
        .personId(personId)
        .firstName(firstName)
        .lastName(lastName)
        .email(email)
        .acceptedTos(acceptedTos)
        .build();
  }

  @Override
  public String toXml() {
    try {
      return XmlUtils.toValidXml(this, this.getClass().getResource(XsdPath.PERSON.get()));
    } catch (Exception e) {
      // TODO(arjuns) : Add exception handling later.
      throw new RuntimeException(e);
    }
  }

  // Getters and setters.
  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public boolean getAcceptedTos() {
    return acceptedTos;
  }

  public void setAcceptedTos(boolean acceptedTos) {
    this.acceptedTos = acceptedTos;
  }

  public static class Builder {
    private String firstName;
    private String lastName;
    private String email;
    private boolean acceptedTos;

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

    public Builder acceptedTos(boolean acceptedTos) {
      this.acceptedTos = acceptedTos;
      return this;
    }

    @SuppressWarnings("synthetic-access")
    public PersonDto build() {
      return new PersonDto(this).validate();
    }
  }

  @SuppressWarnings("synthetic-access")
  private PersonDto(Builder builder) {
    this.firstName = builder.firstName;
    this.lastName = builder.lastName;
    this.email = builder.email;
    this.acceptedTos = builder.acceptedTos;
  }
  
  // For JAXB.
  @JsonCreator
  private PersonDto() {
  }
}
