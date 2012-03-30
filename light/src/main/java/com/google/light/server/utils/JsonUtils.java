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

import org.codehaus.jackson.JsonGenerationException;

import org.codehaus.jackson.map.ObjectWriter;

import java.io.IOException;
import java.io.StringReader;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;

/**
 * Utility class for Json.
 * 
 * D : DTO for this Json.
 * 
 * TODO(arjuns) : Add test for this.
 * 
 * @author Arjun Satyapal
 */
public class JsonUtils {
  // TODO(arjuns): Add javadocs in this class.
  // TODO(arjuns): See if this class can return object only after validation. Same for Xml Utils
  public static <D> D getDto(String jsonString, Class<D> dtoClass) throws JsonParseException,
      JsonMappingException, IOException {
    ObjectMapper mapper = new ObjectMapper();
    AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
    // make serializer use JAXB annotations (only)
    mapper.getSerializationConfig().withAnnotationIntrospector(introspector);
    return mapper.readValue(new StringReader(jsonString), dtoClass);
  }

  public static <T> String toJson(T object) throws JsonGenerationException, JsonMappingException,
      IOException {
    return toJson(object, true);
  }

  public static <T> String toJson(T object, boolean prettyPrint) throws JsonGenerationException,
      JsonMappingException,
      IOException {
    ObjectMapper mapper = new ObjectMapper();
    AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
    // make serializer use JAXB annotations (only)
    mapper.getSerializationConfig().withAnnotationIntrospector(introspector);

    ObjectWriter writer = null;
    
    if (prettyPrint) {
      writer = mapper.writerWithDefaultPrettyPrinter();
    } else {
      writer = mapper.writer();
    }
    return writer.writeValueAsString(object);
  }

  // Utility class.
  private JsonUtils() {
  }
}
