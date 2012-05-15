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

import static com.google.light.server.utils.LightUtils.getStringWrapper;

import com.google.light.server.dto.module.ModuleTypeProvider;

import com.google.light.server.dto.module.ModuleType;
import com.google.light.server.dto.pojo.typewrapper.AbstractTypeWrapper;
import com.google.light.server.dto.pojo.typewrapper.stringwrapper.ExternalId.ExternalIdDeserializer;
import com.google.light.server.dto.pojo.typewrapper.stringwrapper.ExternalId.ExternalIdSerializer;
import com.google.light.server.dto.pojo.typewrapper.stringwrapper.ExternalId.ExternalIdXmlAdapter;
import com.google.light.server.jersey.resources.thirdparty.google.GoogleDocUrl;
import com.google.light.server.utils.LightUtils;
import java.io.IOException;
import java.net.URL;
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
@JsonSerialize(using = ExternalIdSerializer.class)
@JsonDeserialize(using = ExternalIdDeserializer.class)
@XmlJavaTypeAdapter(ExternalIdXmlAdapter.class)
@XmlRootElement(name = "externalId")
public class ExternalId extends AbstractTypeWrapper<String, ExternalId> {
  /**
   * @param value
   */
  public ExternalId(String value) {
    super(value);
  }

  /** 
   * {@inheritDoc}
   */
  @Override
  public ExternalId createInstance(String value) {
    return new ExternalId(value);
  }

  public URL getExternalId() {
    return getURL();
  }
  
  /** 
   * {@inheritDoc}
   */
  @Override
  public ExternalId validate() {
    // This will attempt to create a url and thus will automatically get validated.
    getExternalId();
    return this;
  }
  
  // For JAXB
  @SuppressWarnings("unused")
  private ExternalId() {
    super();
  }
  
  private URL getURL() {
    return LightUtils.getURL(getValue());
  }
  
  
  public GoogleDocUrl getGoogleDocURL() {
    return new GoogleDocUrl(getURL());
  }
  
  public  ModuleType getModuleType() {
    if (getURL().getHost().equals("docs.google.com")
        || getURL().getHost().equals("drive.google.com")) {
      GoogleDocUrl docUrl = getGoogleDocURL();
      return docUrl.getModuleType();
    } 
    
    return ModuleType.LIGHT_SYNTHETIC_MODULE;
  }

  public boolean isGoogleDocResource() {
    return getModuleType().getModuleTypeProvider() == ModuleTypeProvider.GOOGLE_DOC;
  }

  // Adding Custom Serializer/Deserializer and XmlAdapter.
  
  /**
   * Xml Adapter for {@link ExternalId}.
   */
  public static class ExternalIdXmlAdapter extends XmlAdapter<String, ExternalId> {
    /** 
     * {@inheritDoc}
     */
    @Override
    public ExternalId unmarshal(String value) throws Exception {
      return new ExternalId(value);
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public String marshal(ExternalId value) throws Exception {
      return value.getValue();
    }
  }
  
  /**
   * {@link JsonDeserializer} for {@link ExternalId}.
   */
  public static class ExternalIdDeserializer extends JsonDeserializer<ExternalId>{
    /** 
     * {@inheritDoc}
     */
    @Override
    public ExternalId deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException,
        JsonProcessingException {
      String value = jsonParser.getText();
      
      return getStringWrapper(value, ExternalId.class);
    }
  }
  
  /**
   * {@link JsonSerializer} for {@link ExternalId}.
   */
  public static class ExternalIdSerializer extends JsonSerializer<ExternalId>{

    /** 
     * {@inheritDoc}
     */
    @Override
    public void serialize(ExternalId value, JsonGenerator jsonGenerator, SerializerProvider provider)
        throws IOException, JsonProcessingException {
      if (value == null) {
        return;
      }
      jsonGenerator.writeString(value.getValue());
    }
  }
}
