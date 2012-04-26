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

import com.google.light.server.exception.unchecked.XmlException;

import com.google.gdata.data.BaseEntry;

import com.google.gdata.data.ExtensionProfile;
import com.google.gdata.util.common.xml.XmlWriter;

import com.google.gdata.data.BaseFeed;

import org.jdom.JDOMException;

import org.jdom.Document;

import org.jdom.input.SAXBuilder;

import org.jdom.output.Format;

import org.jdom.output.XMLOutputter;

import com.google.light.server.dto.DtoInterface;

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

import com.google.light.server.dto.person.PersonDto;

/**
 * Utility class for Xml.
 * 
 * TODO(arjuns) : Add test for this.
 * 
 * @author Arjun Satyapal
 */
public class XmlUtils {
  @SuppressWarnings("unchecked")
  public static <D extends DtoInterface<D>> D getDto(String xmlString) throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(PersonDto.class);
    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

    D dto = ((D) unmarshaller.unmarshal(new StringReader(xmlString)));
    return dto.validate();
  }

  public static <T> String toXml(T object) {
    try {
      JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
      Marshaller marshaller = jaxbContext.createMarshaller();
      StringWriter sw = new StringWriter();
      marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      marshaller.marshal(object, sw);
      return sw.toString();
    } catch (Exception e) {
      throw new XmlException(e);
    }

  }

  /**
   * Convert Object to XML and then validate it againsted provided XSD.
   * 
   * @param object
   * @param xsdUrl
   * @return
   * @throws JAXBException
   * @throws SAXException
   * @throws IOException
   */
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

  /**
   * Takes a XML string as input and outputs a pretty-formatted XML.
   * 
   * TODO(arjuns): Add test for this.
   * 
   * @param xml
   * @return
   * @throws JDOMException
   * @throws IOException
   */
  public static String pretyfyXml(String xml) throws JDOMException, IOException {
    SAXBuilder builder = new SAXBuilder();
    Document document = builder.build(new StringReader(xml));

    XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());

    return xmlOutputter.outputString(document);
  }

  /**
   * Return a pretty XML for a GData Base Entry
   */
  public static String getXmlFeed(@SuppressWarnings("rawtypes") BaseFeed feed) {
    try {
      StringWriter stringWriter = new StringWriter();
      feed.generateAtom(new XmlWriter(stringWriter), new ExtensionProfile());
      String xmlFeed = stringWriter.toString();
      return pretyfyXml(xmlFeed);
    } catch (Exception e) {
      throw new XmlException(e);
    }
  }

  /**
   * Return a pretty XML for a GData Base Entry
   */
  public static String getXmlEntry(@SuppressWarnings("rawtypes") BaseEntry entry) {
    try {
      StringWriter stringWriter = new StringWriter();
      entry.generateAtom(new XmlWriter(stringWriter), new ExtensionProfile());
      String xmlFeed = stringWriter.toString();
      return pretyfyXml(xmlFeed);
    } catch (Exception e) {
      throw new XmlException(e);
    }
  }

  // Utility class.
  private XmlUtils() {
  }
}
