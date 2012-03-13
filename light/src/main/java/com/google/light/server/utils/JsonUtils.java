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
 * TODO(arjuns) : Add test for this.
 * 
 * @author Arjun Satyapal
 */
public class JsonUtils {
  public static <T> T getDto(String jsonString, Class<T> dtoClass) throws JsonParseException,
      JsonMappingException, IOException {
    ObjectMapper mapper = new ObjectMapper();
    AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
    // make serializer use JAXB annotations (only)
    mapper.getSerializationConfig().withAnnotationIntrospector(introspector);
    return mapper.readValue(new StringReader(jsonString), dtoClass);
  }

  public static <T> String toJson(T object) throws JsonGenerationException, JsonMappingException,
      IOException {
    ObjectMapper mapper = new ObjectMapper();
    AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
    // make serializer use JAXB annotations (only)
    mapper.getSerializationConfig().withAnnotationIntrospector(introspector);

    ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();
    return writer.writeValueAsString(object);
  }

  // Utility class.
  private JsonUtils() {
  }
}
