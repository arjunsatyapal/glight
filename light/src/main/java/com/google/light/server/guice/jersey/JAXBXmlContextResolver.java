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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.light.server.constants.JavaDtos;
import com.google.light.server.constants.http.ContentTypeConstants;
import com.google.light.server.dto.DtoInterface;
import java.util.List;
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
@Produces(ContentTypeConstants.TEXT_XML)
@Provider
public final class JAXBXmlContextResolver implements ContextResolver<Marshaller> {
  @SuppressWarnings("rawtypes")
  Map<Class, Marshaller> marshallerMap;
  
  @SuppressWarnings("rawtypes")
  private final Set<Class> types;

  @SuppressWarnings("rawtypes")
  public JAXBXmlContextResolver() throws Exception {
    List<Class> list = JavaDtos.getListOfDtoClasses();
    this.types = Sets.newHashSet(list);
    
    
    marshallerMap = Maps.newHashMap();
    for(Class<? extends DtoInterface> currClass : list) {
      marshallerMap.put(currClass, generateMarshaller(currClass));
    }
  }

  @Override
  public Marshaller getContext(Class<?> objectType) {
    if (types.contains(objectType)) {
      return marshallerMap.get(objectType);
    }

    return null;
  }

  /**
   * @param objectType
   * @return
   * @throws JAXBException
   * @throws PropertyException
   */
  private Marshaller generateMarshaller(Class<?> objectType) throws JAXBException,
      PropertyException {
    JAXBContext context = JAXBContext.newInstance(objectType);
    Marshaller marshaller = context.createMarshaller();
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
    return marshaller;
  }
}
