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
import static com.google.light.server.constants.RequestParmKeyEnum.DEFAULT_EMAIL;
import static com.google.light.server.constants.RequestParmKeyEnum.LOGIN_PROVIDER_ID;
import static com.google.light.server.constants.RequestParmKeyEnum.PERSON_ID;
import static com.google.light.server.utils.GuiceUtils.getInstance;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static org.mockito.Mockito.when;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.ServletModule;
import com.google.light.server.constants.LightEnvEnum;
import com.google.light.server.constants.OAuth2ProviderService;
import com.google.light.server.guice.LightServletModule;
import com.google.light.server.guice.module.UnitTestModule;
import com.google.light.server.guice.modules.DevServerModule;
import com.google.light.server.guice.modules.ProdModule;
import com.google.light.server.guice.modules.QaModule;
import com.google.light.server.manager.interfaces.PersonManager;
import com.google.light.server.persistence.entity.person.PersonEntity;
import com.google.light.server.utils.LightUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nullable;
import javax.servlet.http.HttpSession;
import org.mockito.Mockito;

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
   * Get a random UserId.
   * 
   * @return
   */
  public static String getRandomUserId() {
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
   * Get a random FederatedId.
   * 
   * @deprecated
   * @return
   */
  @Deprecated
  public static String getRandomFederatedId() {
    return "federatedId:" + getRandomString();
  }

  /**
   * Get a random FederatedAuthority.
   * 
   * @deprecated
   * @return
   */
  @Deprecated
  public static String getRandomFederatedAuthority() {
    return "federatedAuthority:" + getRandomString();
  }

  /**
   * Returns a resource as a string.
   * 
   * @param resourcePath
   * @return
   */
  public static String getResourceAsString(String resourcePath) {
    try {
      InputStream is = System.class.getResourceAsStream(resourcePath);
      return LightUtils.getInputStreamAsString(is);
    } catch (Exception e) {
      throw new RuntimeException("Failed to load " + resourcePath);
    }
  }

  /**
   * Get Injector on the basis of the Environment. TODO(arjuns): Move other injector creations to
   * use this.
   * 
   * @param env
   * @return
   */
  public static Injector getInjectorByEnv(LightEnvEnum env, @Nullable HttpSession session) {
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
    GaeTestingUtils gaeTestingUtils =
        new GaeTestingUtils(env,
            OAuth2ProviderService.GOOGLE_LOGIN,
            getRandomEmail(),
            getRandomUserId(),
            false /* isAdmin */);
    gaeTestingUtils.setUp();

    return gaeTestingUtils;
  }

  /**
   * Utility method to read a line of String from Command Line.
   * 
   * @param message
   * @return
   * @throws IOException
   */
  public static String readLineFromConsole(String message) throws IOException {
    System.out.println(message);
    InputStreamReader converter = new InputStreamReader(System.in);
    BufferedReader in = new BufferedReader(converter);

    return checkNotBlank(in.readLine());
  }

  /**
   * Create a Mock session using {@link OAuth2ProviderService}, userId and email.
   * 
   * @param provider
   * @param userId
   * @param email
   * @return
   */
  public static HttpSession getMockSessionForTesting(OAuth2ProviderService provider, String userId,
      String email) {
    HttpSession mockSession = Mockito.mock(HttpSession.class);
    when(mockSession.getAttribute(LOGIN_PROVIDER_ID.get())).thenReturn(provider.name());
    when(mockSession.getAttribute(PERSON_ID.get())).thenReturn(userId);
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
   * @param listOfScopes Here the scopes are part of a list as defined in {@link OAuth2ProviderService}.
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
  public static PersonEntity createRandomPerson(Injector injector) {
    PersonManager personManager = getInstance(injector, PersonManager.class);
    
    PersonEntity personEntity = new PersonEntity.Builder()
        .firstName(getRandomString())
        .lastName(getRandomString())
        .email(getRandomEmail())
        .build();
    
    return personManager.createPerson(personEntity);
  }
}
