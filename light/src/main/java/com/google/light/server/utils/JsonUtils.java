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
package com.google.light.server.utils;


import com.google.light.server.dto.AbstractDto;
import com.google.light.server.dto.AbstractPojo;
import com.google.light.server.exception.unchecked.JsonException;
import java.io.StringReader;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;

/**
 * Utility class for Json.
 * 
 * D : DTO for this Json.
 * 
 * TODO(arjuns) : Add test for this.
 * TODO(arjuns) : Do we need this class after Jersey?
 * 
 * @author Arjun Satyapal
 */
public class JsonUtils {
  // TODO(arjuns): Add javadocs in this class.
  // TODO(arjuns): See if this class can return object only after validation. Same for Xml Utils
  
  public static <D extends AbstractDto<D>> D getDto(String jsonString, Class<D> dtoClass) {
    return getPojo(jsonString, dtoClass).validate();
  }
  
  public static <D extends AbstractPojo<D>> D getPojo(String jsonString, Class<D> dtoClass) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
    // make serializer use JAXB annotations (only)
    mapper.getSerializationConfig().withAnnotationIntrospector(introspector);
    mapper.setVisibilityChecker(mapper.getSerializationConfig().getDefaultVisibilityChecker()
        .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
        .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
        .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
        .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
        .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));

    D pojo = null;
    try {
      pojo = mapper.readValue(new StringReader(jsonString), dtoClass);
    } catch (Exception e) {
      throw new JsonException(e);
    }
    
    return pojo.validate();
  }

  public static <T> String toJson(T object) {
    return toJson(object, true);
  }

  public static <T> String toJson(T object, boolean prettyPrint) {
    ObjectMapper mapper = getJsonMapper();
    
    ObjectWriter writer = null;

    if (prettyPrint) {
      writer = mapper.writerWithDefaultPrettyPrinter();
    } else {
      writer = mapper.writer();
    }
    try {
      return writer.writeValueAsString(object);
    } catch (Exception e) {
      throw new JsonException(e);
    }
  }
  /**
   * Json Mapper used by both {@link JsonUtils} and {@link com.google.light.server.guice.jersey.JAXBJsonContextResolver}
   */
  public static  ObjectMapper getJsonMapper() {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS, true);
    mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
    mapper.configure(SerializationConfig.Feature.SORT_PROPERTIES_ALPHABETICALLY, true);
    mapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);

    mapper.setVisibilityChecker(mapper.getSerializationConfig().getDefaultVisibilityChecker()
        .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
        .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
        .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
        .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
        .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
    return mapper;
  }
  // Utility class.
  private JsonUtils() {
  }
}
