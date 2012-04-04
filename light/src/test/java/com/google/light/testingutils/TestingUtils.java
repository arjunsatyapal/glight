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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.constants.OAuth2ProviderService.GOOGLE_LOGIN;
import static com.google.light.server.constants.RequestParamKeyEnum.DEFAULT_EMAIL;
import static com.google.light.server.constants.RequestParamKeyEnum.LOGIN_PROVIDER_ID;
import static com.google.light.server.constants.RequestParamKeyEnum.LOGIN_PROVIDER_USER_ID;
import static com.google.light.server.constants.RequestParamKeyEnum.PERSON_ID;
import static com.google.light.server.utils.GuiceUtils.getInstance;
import static com.google.light.server.utils.LightPreconditions.checkNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.light.server.servlets.SessionManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.mockito.Mockito;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.servlet.ServletModule;
import com.google.light.server.constants.LightEnvEnum;
import com.google.light.server.constants.OAuth2ProviderService;
import com.google.light.server.guice.LightServletModule;
import com.google.light.server.guice.module.UnitTestModule;
import com.google.light.server.guice.modules.DevServerModule;
import com.google.light.server.guice.modules.ProdModule;
import com.google.light.server.guice.modules.QaModule;
import com.google.light.server.manager.implementation.SearchManagerGSSImplTest;
import com.google.light.server.manager.interfaces.PersonManager;
import com.google.light.server.persistence.entity.person.PersonEntity;
import com.google.light.server.utils.LightUtils;

/**
 * 
 * @author Arjun Satyapal
 */
public class TestingUtils {
  private static AtomicLong atomicLong = new AtomicLong(System.currentTimeMillis());

  /**
   * Get UUID.
   * 
   * @return
   */
  public static UUID getUUID() {
    return UUID.randomUUID();
  }

  /**
   * Get UUID as String.
   * 
   * @return
   */
  public static String getUUIDString() {
    return getUUID().toString();
  }

  /**
   * Get a random string based on UUID.
   * 
   * @return
   */
  public static String getRandomString() {
    return getUUIDString().replace("-", "");
  }

  /**
   * Get a random Email.
   * 
   * @return
   */
  public static String getRandomEmail() {
    return "email." + getRandomLongNumberString() + "@" + getRandomLongNumberString() + ".com";
  }

  /**
   * Get a random Long Number.
   * 
   * @return
   */
  public static Long getRandomLongNumber() {
    return atomicLong.incrementAndGet();
  }

  /**
   * Get a String which represents a Random Long Number.
   * 
   * @return
   */
  public static String getRandomLongNumberString() {
    String returnString = Long.toString(getRandomLongNumber());
    return returnString;
  }

  /**
   * Get a random ProviderUserId.
   * 
   * @return
   */
  public static String getRandomProviderUserId() {
    return getRandomString();
  }

  /**
   * Get a random PersonId.
   * 
   * @return
   */
  public static Long getRandomPersonId() {
    return getRandomLongNumber();
  }

  /**
   * Returns a resource as a string.
   * 
   * @param resourcePath
   * @return
   */
  public static String getResourceAsString(String resourcePath) {
    try {
      InputStream is = getResourceAsStream(resourcePath);
      return LightUtils.getInputStreamAsString(is);
    } catch (Exception e) {
      throw new RuntimeException("Failed to load " + resourcePath, e);
    }
  }

  public static InputStream getResourceAsStream(String resourcePath)
      throws IOException {
    return System.class.getResourceAsStream(resourcePath);
  }

  /**
   * Get Injector on the basis of the Environment. TODO(arjuns): Move other injector creations to
   * use this.
   * 
   * @param env
   * @param session Session is required only for Unit Test Env.
   * @return
   */
  public static Injector getInjectorByEnv(LightEnvEnum env) {
    return getInjectorByEnv(env, null);
  }

  /**
   * Get Injector on the basis of the Environment. TODO(arjuns): Move other injector creations to
   * use this.
   * 
   * @param env
   * @param session Session is required only for Unit Test Env.
   * @return
   */
  public static Injector getInjectorByEnv(LightEnvEnum env, @Nullable HttpSession session) {
    if (env == LightEnvEnum.UNIT_TEST) {
      checkNotNull(session, "session should be set for UNIT_TEST env.");
    } else {
      checkNull(session, "Session should not be set for non UNIT-TEST env.");
    }

    ServletModule servletModule = new LightServletModule();
    switch (env) {
      case DEV_SERVER:
        return Guice.createInjector(new DevServerModule(), servletModule);

      case PROD:
        return Guice.createInjector(new ProdModule(), servletModule);

      case QA:
        return Guice.createInjector(new QaModule(), servletModule);

      case UNIT_TEST:
        return UnitTestModule.getTestInjector(checkNotNull(session), servletModule);

      default:
        throw new IllegalArgumentException("Unknown env : " + env);
    }
  }

  /**
   * Utility method so that a test which needs to have GAE env setup but does not care about basic
   * things, then can use this method. TODO(arjuns): Move all the GAE setups to use this eventually.
   */
  public static GaeTestingUtils gaeSetup(LightEnvEnum env) {
    GaeTestingUtils gaeTestingUtils = new GaeTestingUtils(env, GOOGLE_LOGIN,
        getRandomProviderUserId(), getRandomEmail(), getRandomPersonId(), false /* isAdmin */);
    gaeTestingUtils.setUp();

    return gaeTestingUtils;
  }

  /**
   * Create a Mock session using {@link OAuth2ProviderService}, userId and email.
   * 
   * @param provider
   * @param userId
   * @param email
   * @return
   */
  public static HttpSession getMockSessionForTesting(LightEnvEnum env,
      OAuth2ProviderService providerService, String providerUserId, Long personId, String email) {
    if (env != LightEnvEnum.UNIT_TEST) {
      return null;
    }

    HttpSession mockSession = Mockito.mock(HttpSession.class);
    when(mockSession.getAttribute(LOGIN_PROVIDER_ID.get())).thenReturn(providerService.name());
    when(mockSession.getAttribute(LOGIN_PROVIDER_USER_ID.get())).thenReturn(providerUserId);
    when(mockSession.getAttribute(PERSON_ID.get())).thenReturn(personId);
    when(mockSession.getAttribute(DEFAULT_EMAIL.get())).thenReturn(email);

    return mockSession;
  }

  /**
   * Create file with given content at given ResourcePath.
   * 
   * @param content
   * @param resourcePath
   * @throws IOException
   */
  public static void createFile(String content, TestResourcePaths resourcePath) throws IOException {
    File file = new File(resourcePath.get());
    Files.createParentDirs(file);
    Files.write(content, file, Charsets.UTF_8);
  }

  /**
   * Read file as string in UTF-8 encoding.
   * 
   * @param filePath
   * @return
   * @throws IOException
   */
  public static String getFileAsString(String filePath) throws IOException {
    return Files.toString(new File(filePath), Charsets.UTF_8);
  }

  /**
   * This method tries to compare scopes. e.g. Lists.of("a", "b") and {"a b"} are comparable. Order
   * of "a" and "b" inside both list and array does not matter.
   * 
   * @param listOfScopes Here the scopes are part of a list as defined in
   *          {@link OAuth2ProviderService}.
   * @param strScopes This is returned by Google.
   * @return returns true if they are equivalent.
   */
  public static boolean compareScopes(List<String> listOfScopes, String strScopes) {
    if (!strScopes.contains(" ")) {
      return listOfScopes.contains(strScopes);
    }

    String[] arrayStr = strScopes.split(" ");
    for (String currStr : arrayStr) {
      if (!listOfScopes.contains(currStr)) {
        return false;
      }
    }

    return true;
  }

  /**
   * Create a Random Person.
   */
  public static PersonEntity createRandomPerson(LightEnvEnum env, HttpSession session) {
    Injector injector = TestingUtils.getInjectorByEnv(env, session);
    
    SessionManager sessionManager = getInstance(injector, SessionManager.class);
    sessionManager.checkPersonLoggedIn();
    
    PersonManager personManager = getInstance(injector, PersonManager.class);

    PersonEntity personEntity = new PersonEntity.Builder()
        .firstName(getRandomString())
        .lastName(getRandomString())
        .email(sessionManager.getEmail())
        .build();
    return personManager.create(personEntity);
  }

  /**
   * Returns a mocked HttpServletRequest generated by Mockito that returns
   * this map when getParameterMap method is called.
   * 
   * @param map
   * @return
   */
  public static HttpServletRequest getMockedRequestWithParameterMap(Map<String, String[]> map) {
    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getParameterMap()).thenReturn(map);
    return req;
  }

  /**
   * Return's a simple mock implementation of a Guice Provider that
   * will return the given provided value
   * 
   * @param provided Provided value
   * @return A Provider<T> implementation
   */
  public static <T> Provider<T> getMockProviderFor(final T provided) {
    return new Provider<T>() {
      @Override
      public T get() {
        return provided;
      }
    };
  }

  /**
   * Utility method that will create an mocked HttpTransport which will return for a GET request an
   * HttpResponse with the content set to {@link SearchManagerGSSImplTest#fakeContent}.
   */
  public static HttpTransport createHttpTransportMockForGetRequest(final InputStream fakeContent) {
    return new MockHttpTransport() {
      @Override
      public LowLevelHttpRequest buildGetRequest(String url) throws IOException {
        return new MockLowLevelHttpRequest() {
          @Override
          public LowLevelHttpResponse execute() throws IOException {
            MockLowLevelHttpResponse result = new MockLowLevelHttpResponse();
            result.setContent(fakeContent);
            return result;
          }
        };
      }
    };
  }
}
