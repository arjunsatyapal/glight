/*
 * Copyright 2012 Google Inc.
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
package com.google.light.server.dto.pojo.typewrapper.longwrapper;

import static com.google.light.server.utils.LightPreconditions.checkPersonId;

import javax.xml.bind.annotation.XmlAccessType;
import org.codehaus.jackson.annotate.JsonTypeName;

import javax.xml.bind.annotation.XmlAccessorType;

import com.google.inject.Inject;
import com.google.light.server.dto.pojo.typewrapper.AbstractTypeWrapper;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.PersonId.PersonIdDeserializer;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.PersonId.PersonIdSerializer;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.PersonId.PersonIdXmlAdapter;
import java.io.IOException;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Wrapper class for Id for Persons.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
@JsonSerialize(using = PersonIdSerializer.class)
@JsonDeserialize(using = PersonIdDeserializer.class)
@XmlJavaTypeAdapter(PersonIdXmlAdapter.class)
@JsonTypeName(value = "personId")
@XmlRootElement(name = "personId")
@XmlAccessorType(XmlAccessType.FIELD)
public class PersonId extends AbstractTypeWrapper<Long, PersonId> {
  public PersonId(Long id) {
    super(id);
    validate();
  }

  public PersonId(String id) {
    this(Long.parseLong(id));
  }

  @Override
  public String toString() {
    return "PersonId:" + getValue();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public PersonId validate() {
    checkPersonId(this);
    return this;
  }

  // For JAXB, Guice and Objectify.
  @SuppressWarnings("unused")
  @Inject
  private PersonId() {
    super();
  }

  /**
   * {@inheritDoc}
   */
  @Deprecated
  @Override
  public PersonId createInstance(Long value) {
    return new PersonId(value);
  }

  /**
   * Xml Adapter for {@link PersonId}.
   */
  public static class PersonIdXmlAdapter extends XmlAdapter<Long, PersonId> {
    /**
     * {@inheritDoc}
     */
    @Override
    public PersonId unmarshal(Long value) throws Exception {
      return new PersonId(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long marshal(PersonId value) throws Exception {
      return value.getValue();
    }
  }

  /**
   * {@link JsonDeserializer} for {@link PersonId}.
   */
  public static class PersonIdDeserializer extends JsonDeserializer<PersonId> {
    /**
     * {@inheritDoc}
     */
    @Override
    public PersonId deserialize(JsonParser jsonParser, DeserializationContext ctxt)
        throws IOException,
        JsonProcessingException {
      String value = jsonParser.getText();
      return new PersonId(value);
    }
  }

  /**
   * {@link JsonSerializer} for {@link PersonId}.
   */
  public static class PersonIdSerializer extends JsonSerializer<PersonId> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(PersonId value, JsonGenerator jsonGenerator, SerializerProvider provider)
        throws IOException, JsonProcessingException {
      if (value == null) {
        return;
      }
      jsonGenerator.writeString("" + value.getValue());
    }
  }
}
