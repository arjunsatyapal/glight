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

import static com.google.light.server.utils.LightPreconditions.checkPositiveLong;

import org.codehaus.jackson.annotate.JsonTypeName;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.google.light.server.dto.pojo.typewrapper.AbstractTypeWrapper;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.ModuleId.ModuleIdDeserializer;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.ModuleId.ModuleIdSerializer;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.ModuleId.ModuleIdXmlAdapter;
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
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
@JsonSerialize(using = ModuleIdSerializer.class)
@JsonDeserialize(using = ModuleIdDeserializer.class)
@XmlJavaTypeAdapter(ModuleIdXmlAdapter.class)
@JsonTypeName(value = "moduleId")
@XmlRootElement(name = "moduleId")
@XmlAccessorType(XmlAccessType.FIELD)
public class ModuleId extends AbstractTypeWrapper<Long, ModuleId> {
  public ModuleId(Long value) {
    super(value);
    validate();
  }

  public ModuleId(String value) {
    this(Long.parseLong(value));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ModuleId validate() {
    checkPositiveLong(getValue(), "Invalid ModuleId");
    return this;
  }

  // For JAXB.
  @SuppressWarnings("unused")
  private ModuleId() {
    super();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ModuleId createInstance(Long value) {
    return new ModuleId(value);
  }

  // Adding Custom Serializer/Deserializer and XmlAdapter.

  /**
   * Xml Adapter for {@link ModuleId}.
   */
  public static class ModuleIdXmlAdapter extends XmlAdapter<Long, ModuleId> {
    /**
     * {@inheritDoc}
     */
    @Override
    public ModuleId unmarshal(Long value) throws Exception {
      return new ModuleId(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long marshal(ModuleId value) throws Exception {
      return value.getValue();
    }
  }

  /**
   * {@link JsonDeserializer} for {@link ModuleId}.
   */
  public static class ModuleIdDeserializer extends JsonDeserializer<ModuleId> {
    /**
     * {@inheritDoc}
     */
    @Override
    public ModuleId deserialize(JsonParser jsonParser, DeserializationContext ctxt)
        throws IOException,
        JsonProcessingException {
      String value = jsonParser.getText();
      return new ModuleId(value);
    }
  }

  /**
   * {@link JsonSerializer} for {@link ModuleId}.
   */
  public static class ModuleIdSerializer extends JsonSerializer<ModuleId> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(ModuleId value, JsonGenerator jsonGenerator, SerializerProvider provider)
        throws IOException, JsonProcessingException {
      if (value == null) {
        return;
      }
      jsonGenerator.writeString("" + value.getValue());
    }
  }
}
