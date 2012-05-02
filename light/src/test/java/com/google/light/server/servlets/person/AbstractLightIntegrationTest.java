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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.LightPreconditions.checkPersonId;

import com.google.light.server.dto.pojo.longwrapper.PersonId;




import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.light.server.constants.OAuth2ProviderService;
import com.google.light.testingutils.FakeLoginHelper;
import com.google.light.testingutils.TestingUtils;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;

/**
 * All Servlet Tests should inherit from this.
 * 
 * @author Arjun Satyapal
 */
public abstract class AbstractLightIntegrationTest {
  protected FakeLoginHelper loginProvider;
  protected OAuth2ProviderService providerService;
  protected PersonId personId;
  protected String providerEmail;
  protected HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
  protected final JsonFactory JSON_FACTORY = new JacksonFactory();

  protected HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory();
  protected String serverUrl = null;

  protected boolean isLocalHost = false;

  protected AbstractLightIntegrationTest(OAuth2ProviderService providerService, PersonId personId,
      String providerEmail) {
    this.providerService = checkNotNull(providerService);
    this.personId = checkPersonId(personId);
    this.providerEmail = checkNotBlank(providerEmail, "providerEmail");
  }

  @Before
  public void setUp() {
    String testServerUrl = System.getProperty("test.server.url");

    if (testServerUrl != null) {
      serverUrl = checkNotBlank(testServerUrl, "test.server.url should not be empty.");
    } else {
      serverUrl = "http://localhost:8080";
    }

    if (serverUrl.startsWith("http://localhost")) {
      isLocalHost = true;
    } else {
      isLocalHost = false;
    }

    personId = TestingUtils.getRandomPersonId();
    providerEmail = "unit-test@myopenedu.com";

    try {
      loginProvider =
          new FakeLoginHelper(serverUrl, providerService, personId, providerEmail);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Test for
   * {@link AbstractLightServlet#doDelete(HttpServletRequest request, HttpServletResponse response)}
   * ;
   */
  @Test
  public abstract void test_doDelete() throws Exception;

  /**
   * Test with JSON API for
   * {@link AbstractLightServlet#doGet(HttpServletRequest request, HttpServletResponse response)};
   * 
   */
  @Test
  public abstract void test_doGet_Json() throws Exception;

  /**
   * Test with XML api for
   * {@link AbstractLightServlet#doGet(HttpServletRequest request, HttpServletResponse response)};
   */
  @Test
  public abstract void test_doGet_Xml() throws Exception;

  /**
   * Test with JSON API for
   * {@link AbstractLightServlet#doGetLastModified(HttpServletRequest request)}
   */
  @Test
  public abstract void test_getLastModified_Json() throws Exception;

  /**
   * Test with XML API for {@link AbstractLightServlet#doGetLastModified(HttpServletRequest request)}
   */
  @Test
  public abstract void test_getLastModified_Xml() throws Exception;

  /**
   * Test with JSON API for
   * {@link AbstractLightServlet#doHead(HttpServletRequest request, HttpServletResponse response)};
   */
  @Test
  public abstract void test_doHead_Json() throws Exception;

  /**
   * Test with Xml API for
   * {@link AbstractLightServlet#doHead(HttpServletRequest request, HttpServletResponse response)};
   */
  @Test
  public abstract void test_doHead_Xml() throws Exception;

  /**
   * Test with JSON API for
   * {@link AbstractLightServlet#doOptions(HttpServletRequest request, HttpServletResponse response)}
   * ;
   */
  @Test
  public abstract void test_doOptions_Json() throws Exception;

  /**
   * Test with Xml API for
   * {@link AbstractLightServlet#doOptions(HttpServletRequest request, HttpServletResponse response)}
   * ;
   */
  @Test
  public abstract void test_doOptions_Xml() throws Exception;

  /**
   * Test with JSON API for
   * {@link AbstractLightServlet#doPost(HttpServletRequest request, HttpServletResponse response)};
   * 
   * @throws Exception
   */
  @Test
  public abstract void test_doPost_Json() throws Exception;

  /**
   * Test with Xml API for
   * {@link AbstractLightServlet#doPost(HttpServletRequest request, HttpServletResponse response)};
   */
  @Test
  public abstract void test_doPost_Xml() throws Exception;

  /**
   * Test with JSON API for
   * {@link AbstractLightServlet#doPut(HttpServletRequest request, HttpServletResponse response)};
   */
  @Test
  public abstract void test_doPut_Json() throws Exception;

  /**
   * Test with XML API for
   * {@link AbstractLightServlet#doPut(HttpServletRequest request, HttpServletResponse response)};
   */
  @Test
  public abstract void test_doPut_Xml() throws Exception;
}
