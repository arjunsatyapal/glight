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
package com.google.light.server.dto.pojo.typewrapper.stringwrapper;

import org.codehaus.jackson.annotate.JsonTypeName;

import com.google.light.server.dto.pojo.typewrapper.AbstractTypeWrapper;
import com.google.light.server.dto.pojo.typewrapper.stringwrapper.FTSDocumentId.FTSDocumentIdDeserializer;
import com.google.light.server.dto.pojo.typewrapper.stringwrapper.FTSDocumentId.FTSDocumentIdSerializer;
import com.google.light.server.dto.pojo.typewrapper.stringwrapper.FTSDocumentId.FTSDocumentIdXmlAdapter;
import com.google.light.server.utils.LightPreconditions;
import com.google.light.server.utils.LightUtils;
import java.io.IOException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
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
@JsonSerialize(using = FTSDocumentIdSerializer.class)
@JsonDeserialize(using = FTSDocumentIdDeserializer.class)
@XmlJavaTypeAdapter(FTSDocumentIdXmlAdapter.class)
@JsonTypeName(value = "ftsDocumentId")
@XmlRootElement(name = "ftsDocumentId")
@XmlAccessorType(XmlAccessType.FIELD)
public class FTSDocumentId extends AbstractTypeWrapper<String, FTSDocumentId> {
  /**
   * @param value
   */
  public FTSDocumentId(String value) {
    super(value);
  }

  /** 
   * {@inheritDoc}
   */
  @Override
  public FTSDocumentId createInstance(String value) {
    throw new UnsupportedOperationException();
  }

  /** 
   * {@inheritDoc}
   */
  @Override
  public FTSDocumentId validate() {
    LightPreconditions.checkNotBlank(getValue(), "value");
    return this;
  }
  
  // For JAXB
  @SuppressWarnings("unused")
  private FTSDocumentId() {
    super();
  }
  
//Adding Custom Serializer/Deserializer and XmlAdapter.

 /**
  * Xml Adapter for {@link FTSDocumentId}.
  */
 public static class FTSDocumentIdXmlAdapter extends XmlAdapter<String, FTSDocumentId> {
   /**
    * {@inheritDoc}
    */
   @Override
   public FTSDocumentId unmarshal(String value) throws Exception {
     return new FTSDocumentId(value);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String marshal(FTSDocumentId value) throws Exception {
     return value.getValue();
   }
 }

 /**
  * {@link JsonDeserializer} for {@link FTSDocumentId}.
  */
 public static class FTSDocumentIdDeserializer extends JsonDeserializer<FTSDocumentId> {
   /**
    * {@inheritDoc}
    */
   @Override
   public FTSDocumentId deserialize(JsonParser jsonParser, DeserializationContext ctxt)
       throws IOException,
       JsonProcessingException {
     String value = jsonParser.getText();

     return LightUtils.getStringWrapper(value, FTSDocumentId.class);
   }
 }

 /**
  * {@link JsonSerializer} for {@link FTSDocumentId}.
  */
 public static class FTSDocumentIdSerializer extends JsonSerializer<FTSDocumentId> {

   /**
    * {@inheritDoc}
    */
   @Override
   public void serialize(FTSDocumentId value, JsonGenerator jsonGenerator,
       SerializerProvider provider)
       throws IOException, JsonProcessingException {
     if (value == null) {
       return;
     }
     jsonGenerator.writeString("" + value.getValue());
   }
 }
}
