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

import com.google.light.server.dto.person.PersonDto;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.xml.sax.SAXException;

/**
 * Utility class for Xml.
 * 
 * TODO(arjuns) : Add test for this.
 * 
 * @author Arjun Satyapal
 */
public class XmlUtils {
  @SuppressWarnings("unchecked")
  public static <T> T getDto(String xmlString) throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(PersonDto.class);
    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

    return ((T) unmarshaller.unmarshal(new StringReader(xmlString)));
  }

  public static <T> String toXml(T object) throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
    Marshaller marshaller = jaxbContext.createMarshaller();
    StringWriter sw = new StringWriter();
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
    marshaller.marshal(object, sw);
    return sw.toString();

  }

  public static <T> String toValidXml(T object, URL xsdUrl) throws JAXBException, SAXException,
      IOException {
    String xml = toXml(object);

    SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    Schema schema = sf.newSchema(xsdUrl);

    JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
    JAXBSource jaxbSource = new JAXBSource(jaxbContext, object);

    Validator validator = schema.newValidator();
    validator.validate(jaxbSource);

    // Now validate Generated XML to be double Sure.
    Source xmlStringSource = new StreamSource(new StringReader(xml));
    validator.validate(xmlStringSource);
    return xml;
  }

  // Utility class.
  private XmlUtils() {
  }
}
