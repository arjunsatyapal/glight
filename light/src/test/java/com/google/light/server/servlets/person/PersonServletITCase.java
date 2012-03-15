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
package com.google.light.server.servlets.person;

import static com.google.light.server.constants.TestResourceMappings.CREATE_PERSON;
import static com.google.light.testingutils.TestingUtils.getResourceAsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Ignore;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.util.Strings;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.light.server.constants.ContentTypeEnum;
import com.google.light.server.servlets.path.ServletPathEnum;
import com.google.light.server.utils.LightUtils;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration test for {@link PersonServlet}
 * 
 * @author Arjun Satyapal
 */
public class PersonServletITCase extends AbstractLightIntegrationTest {
  private HttpHeaders headers;

  @Before
  public void setUp() throws Exception {
    headers =  new HttpHeaders();
    headers.set("Cookie", loginProvider.getCookie());
  }

  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_doDelete() {
    // TODO(arjuns): Auto-generated method stub
  }

  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_doGet_Json() {
    // TODO(arjuns): Auto-generated method stub
  }

  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_doGet_Xml() {
    // TODO(arjuns): Auto-generated method stub
  }

  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_getLastModified_Json() {
    // TODO(arjuns): Auto-generated method stub
  }

  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_getLastModified_Xml() {
    // TODO(arjuns): Auto-generated method stub
  }

  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_doHead_Json() {
    // TODO(arjuns): Auto-generated method stub
  }

  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_doHead_Xml() {
    // TODO(arjuns): Auto-generated method stub
  }

  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_doOptions_Json() {
    // TODO(arjuns): Auto-generated method stub
  }

  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_doOptions_Xml() {
    // TODO(arjuns): Auto-generated method stub
  }

  /**
   * {@inheritDoc} Test for createPerson via JSON API.
   */
  @Test
  @Override
  public void test_doPost_Json()
      throws Exception {
    String jsonReqString = getResourceAsString(CREATE_PERSON.getJsonReqPath());
    GenericUrl requestUrl = new GenericUrl(serverUrl + ServletPathEnum.PERSON.get());

    InputStream is = new ByteArrayInputStream(Strings.toBytesUtf8(jsonReqString));
    InputStreamContent isContent =
        new InputStreamContent(ContentTypeEnum.APPLICATION_JSON.get(), is);
    isContent.setLength(jsonReqString.length());

    HttpRequest newrequest = requestFactory.buildPostRequest(requestUrl, isContent);
    newrequest.setHeaders(headers);

    HttpResponse response = newrequest.execute();
    assertTrue(response.isSuccessStatusCode());

    String expJsonResString = getResourceAsString(CREATE_PERSON.getJsonResPath());
    String actualResString = LightUtils.getInputStreamAsString(response.getContent());
    assertEquals(expJsonResString, actualResString);
  }

  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_doPost_Xml() throws Exception {
    String xmlReqString = getResourceAsString(CREATE_PERSON.getXmlReqPath());
    GenericUrl requestUrl = new GenericUrl(serverUrl + ServletPathEnum.PERSON.get());

    InputStream is = new ByteArrayInputStream(Strings.toBytesUtf8(xmlReqString));
    InputStreamContent isContent =
        new InputStreamContent(ContentTypeEnum.APPLICATION_XML.get(), is);
    isContent.setLength(xmlReqString.length());

    HttpRequest newrequest = requestFactory.buildPostRequest(requestUrl, isContent);
    newrequest.setHeaders(headers);

    HttpResponse response = newrequest.execute();
    assertTrue(response.isSuccessStatusCode());

    String expXmlResString = getResourceAsString(CREATE_PERSON.getXmlResPath());
    String actualResString = LightUtils.getInputStreamAsString(response.getContent());
    assertEquals(expXmlResString, actualResString);
  }

  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_doPut_Json() {
    // TODO(arjuns): Auto-generated method stub
  }

  /**
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_doPut_Xml() {
    // TODO(arjuns): Auto-generated method stub
  }

  /**
   * For Person, Login is required.
   */
  @Ignore(value="fix me")
  @Test
  public void test_login() throws Exception {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Cookie", loginProvider.getCookie());

    GenericUrl requestUrl = new GenericUrl(serverUrl + ServletPathEnum.TEST_LOGIN.get());

    HttpRequest newrequest = requestFactory.buildGetRequest(requestUrl);
    newrequest.setHeaders(headers);

    HttpResponse response = newrequest.execute();
    String myString = CharStreams.toString(new InputStreamReader(response.getContent(),
        Charsets.UTF_8));
    assertEquals("Hello " + email, myString.trim());
  }
}
