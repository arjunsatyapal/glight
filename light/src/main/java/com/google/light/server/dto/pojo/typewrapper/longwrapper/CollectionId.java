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
import static com.google.light.server.utils.LightUtils.getLongWrapper;

import com.google.light.server.dto.pojo.typewrapper.AbstractTypeWrapper;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.CollectionId.CollectionIdDeserializer;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.CollectionId.CollectionIdSerializer;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.CollectionId.CollectionIdXmlAdapter;
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
@JsonSerialize(using = CollectionIdSerializer.class)
@JsonDeserialize(using = CollectionIdDeserializer.class)
@XmlJavaTypeAdapter(CollectionIdXmlAdapter.class)
@XmlRootElement(name = "collectionId")
public class CollectionId extends AbstractTypeWrapper<Long, CollectionId> {
  /**
   * @param value
   */
  public CollectionId(Long value) {
    super(value);
  }

  public CollectionId(String value) {
    this(Long.parseLong(value));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CollectionId validate() {
    checkPositiveLong(getValue(), "Invalid CollectionId");
    return this;
  }

  // For JAXB.
  @SuppressWarnings("unused")
  private CollectionId() {
    super();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public CollectionId createInstance(Long value) {
    return new CollectionId(value);
  }

  // Adding Custom Serializer/Deserializer and XmlAdapter.

  /**
   * Xml Adapter for {@link CollectionId}.
   */
  public static class CollectionIdXmlAdapter extends XmlAdapter<Long, CollectionId> {
    /**
     * {@inheritDoc}
     */
    @Override
    public CollectionId unmarshal(Long value) throws Exception {
      return new CollectionId(value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long marshal(CollectionId value) throws Exception {
      return value.getValue();
    }
  }

  /**
   * {@link JsonDeserializer} for {@link CollectionId}.
   */
  public static class CollectionIdDeserializer extends JsonDeserializer<CollectionId> {
    /**
     * {@inheritDoc}
     */
    @Override
    public CollectionId deserialize(JsonParser jsonParser, DeserializationContext ctxt)
        throws IOException,
        JsonProcessingException {
      String value = jsonParser.getText();

      return getLongWrapper(value, CollectionId.class);
    }
  }

  /**
   * {@link JsonSerializer} for {@link CollectionId}.
   */
  public static class CollectionIdSerializer extends JsonSerializer<CollectionId> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(CollectionId value, JsonGenerator jsonGenerator,
        SerializerProvider provider)
        throws IOException, JsonProcessingException {
      if (value == null) {
        return;
      }
      jsonGenerator.writeString("" + value.getValue());
    }
  }
}
