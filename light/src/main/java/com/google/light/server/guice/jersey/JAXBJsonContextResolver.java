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
package com.google.light.server.guice.jersey;

import com.google.light.server.constants.http.ContentTypeConstants;
import com.google.light.server.utils.JsonUtils;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;
import org.codehaus.jackson.jaxrs.Annotations;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.map.ObjectMapper;


/**
 * JAXB Context Resolver for 
 * {@link com.google.light.server.constants.http.ContentTypeEnum#APPLICATION_JSON}. This will
 * return {@link JSONJAXBContext} with {@link JSONConfiguration#natural()} notation.
 * 
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */
@Produces(ContentTypeConstants.APPLICATION_JSON)
@Provider
public final class JAXBJsonContextResolver extends JacksonJaxbJsonProvider /*implements ContextResolver<JAXBContext> */{
//  private final JAXBContext context;
//
//  @SuppressWarnings("rawtypes")
//  private final Set<Class<? extends AbstractDto>> types;

  public JAXBJsonContextResolver() throws Exception {
    super(null, Annotations.JAXB);
    
    ObjectMapper mapper = JsonUtils.getJsonMapper();
    
    setMapper(mapper);
    
    
//    List<Class<? extends AbstractDto>> list = LightDtos.getListOfDtoClasses();
//    this.types = Sets.newHashSet(list);
//    
//    NaturalBuilder configBuilder = JSONConfiguration.natural();
//    configBuilder.humanReadableFormatting(true);
//    
//    this.context = new JSONJAXBContext(configBuilder.build(), LightDtos.getArrayOfDtoClasses());
  }

//  @Override
//  public JAXBContext getContext(Class<?> objectType) {
//    return (types.contains(objectType)) ? context : null;
//  }
}
