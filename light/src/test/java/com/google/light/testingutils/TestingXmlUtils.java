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
package com.google.light.testingutils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

public class TestingXmlUtils {

  private static final String HTTP_DTD_PLACEHOLDER = "http://DTD_PLACEHOLDER";

  public static void assertValidXmlAccordingToDTD(InputStream inputStream, final String DTDUrl) throws Exception {

    final InputStream dtdStream = new URL(DTDUrl).openStream();

    /*
     * First we read the XML and transform it in a XML with the DOCTYPE set
     */
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setValidating(false);
    DocumentBuilder builder = factory.newDocumentBuilder();

    Document xmlDocument = builder.parse(inputStream);
    DOMSource source = new DOMSource(xmlDocument);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    StreamResult result = new StreamResult(out);
    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer transformer = tf.newTransformer();
    transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, HTTP_DTD_PLACEHOLDER);
    transformer.transform(source, result);

    /*
     * Now we validate the XML against the DTD by fooling the parser system
     * and giving the wanted DTD instead of the HTTP_DTD_PLACEHOLDER
     */
    SAXBuilder saxBuilder = new SAXBuilder();
    saxBuilder.setValidation(true);
    saxBuilder.setEntityResolver(new EntityResolver() {
      @Override
      public InputSource resolveEntity(String publicId, String systemId) {
        if (HTTP_DTD_PLACEHOLDER.equals(systemId))
          return new InputSource(dtdStream);
        else
          throw new RuntimeException("XML Parsing unexpectedly asked for entity " + publicId + "/"
              + systemId);
      }
    });

    org.jdom.Document doc =
        saxBuilder.build(new InputSource(new ByteArrayInputStream(out.toByteArray())));
    
    // Forcing the parser to read and validate the input
    new XMLOutputter().outputString(doc);

  }

}
