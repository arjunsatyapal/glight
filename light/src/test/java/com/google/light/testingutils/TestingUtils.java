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
import static com.google.light.server.constants.RequestParmKeyEnum.LOGIN_PROVIDER_ID;
import static com.google.light.server.constants.RequestParmKeyEnum.LOGIN_PROVIDER_USER_EMAIL;
import static com.google.light.server.constants.RequestParmKeyEnum.LOGIN_PROVIDER_USER_ID;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.testingutils.TestResourcePaths.UNIT_TEST_OAUTH_2_OWNER_TOKEN_INFO;
import static org.mockito.Mockito.when;

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

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.mockito.Mockito;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.ServletModule;
import com.google.light.server.constants.LightEnvEnum;
import com.google.light.server.constants.OAuth2Provider;
import com.google.light.server.guice.LightServletModule;
import com.google.light.server.guice.module.UnitTestModule;
import com.google.light.server.guice.modules.DevServerModule;
import com.google.light.server.guice.modules.ProdModule;
import com.google.light.server.guice.modules.QaModule;
import com.google.light.server.servlets.oauth2.google.pojo.GoogleTokenInfo;
import com.google.light.server.utils.JsonUtils;
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
  
  public static InputStream getResourceAsStream(String resourcePath)
      throws IOException {
    return System.class.getResourceAsStream(resourcePath);
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
            OAuth2Provider.GOOGLE_LOGIN,
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
   * Create a Mock session using {@link OAuth2Provider}, userId and email.
   * 
   * @param provider
   * @param userId
   * @param email
   * @return
   */
  public static HttpSession getMockSessionForTesting(OAuth2Provider provider, String userId,
      String email) {
    HttpSession mockSession = Mockito.mock(HttpSession.class);
    when(mockSession.getAttribute(LOGIN_PROVIDER_ID.get())).thenReturn(provider.name());
    when(mockSession.getAttribute(LOGIN_PROVIDER_USER_ID.get())).thenReturn(userId);
    when(mockSession.getAttribute(LOGIN_PROVIDER_USER_EMAIL.get())).thenReturn(email);

    return mockSession;
  }

  /**
   * Get GoogleTokenInfo for a Owner from File. If it is PROD/QA, then a
   * 
   * @param env
   * @return
   * @throws JsonParseException
   * @throws JsonMappingException
   * @throws IOException
   */
  public static GoogleTokenInfo getGoogleTokenInfo(LightEnvEnum env) throws JsonParseException,
      JsonMappingException, IOException {

    if (env == LightEnvEnum.PROD || env == LightEnvEnum.QA) {
      throw new IllegalStateException("This should not be called for PROD/QA env.");
    }
    String jsonString = getFileAsString(
        TestResourcePaths.valueOf(env.name() + "_OAUTH_2_OWNER_TOKEN_INFO").get());
    return JsonUtils.getDto(jsonString, GoogleTokenInfo.class);
  }

  /**
   * Update Google Token info on FileSystem.
   * 
   * @param updatedTokenInfo
   * @throws JsonGenerationException
   * @throws JsonMappingException
   * @throws IOException
   */
  public static void updateGoogleTokenInfo(GoogleTokenInfo updatedTokenInfo)
      throws JsonGenerationException, JsonMappingException, IOException {
    String jsonString = JsonUtils.toJson(updatedTokenInfo, false);
    createFile(jsonString, UNIT_TEST_OAUTH_2_OWNER_TOKEN_INFO);
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
   * @param listOfScopes Here the scopes are part of a list as defined in {@link OAuth2Provider}.
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
}
