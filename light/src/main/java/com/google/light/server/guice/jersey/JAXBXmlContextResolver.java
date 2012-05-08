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

/**
 *
 * 
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */

import com.google.light.server.constants.LightDtos;
import com.google.light.server.constants.http.ContentTypeConstants;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;

/**
 * JAXB Context for {@link ContentTypeEnum#TEXT_XML} which will provide a Marshaller with
 * PrettyPrinting enabled.
 * 
 * TODO(arjuns): Add test for this.
 *
 * @author arjuns@google.com (Arjun Satyapal)
 */
@Produces(ContentTypeConstants.APPLICATION_XML)
@Provider
public final class JAXBXmlContextResolver implements ContextResolver<Marshaller> {
  @SuppressWarnings("rawtypes")
  Map<Class, Marshaller> marshallerMap;
  Marshaller marshaller;
  
  @SuppressWarnings("rawtypes")
  Set<Class> types;

  @SuppressWarnings("rawtypes")
  public JAXBXmlContextResolver() throws Exception {
    Class[] list = LightDtos.getArrayOfDtoClasses();
//    this.types = Sets.newHashSet(list);
//    
//    
//    marshallerMap = Maps.newHashMap();
//    for(Class<? extends AbstractDto> currClass : list) {
//      marshallerMap.put(currClass, generateMarshaller(currClass));
//    }
    
    marshaller = generateMarshaller(list);
  }

  @Override
  public Marshaller getContext(Class<?> objectType) {
//    if (types.contains(objectType)) {
//      return marshallerMap.get(objectType);
//    }

    return marshaller;
//    return null;
  }

  /**
   * @param objectType
   * @return
   * @throws JAXBException
   * @throws PropertyException
   */
  @SuppressWarnings("rawtypes")
  private Marshaller generateMarshaller(Class[] list) throws JAXBException,
      PropertyException {
    JAXBContext context = JAXBContext.newInstance(list);
    Marshaller marshaller = context.createMarshaller();
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
    return marshaller;
  }
}
