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
import static org.mockito.Mockito.when;

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
import com.google.light.server.utils.LightUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

  public static String getRandomLongNumberString() {
    String returnString = Long.toString(getRandomLongNumber());
    return returnString;
  }

  public static String getRandomUserId() {
    return getRandomString();
  }

  public static Long getRandomPersonId() {
    return getRandomLongNumber();
  }

  public static String getRandomFederatedId() {
    return "federatedId:" + getRandomString();
  }

  public static String getRandomFederatedAuthority() {
    return "federatedAuthority:" + getRandomString();
  }

  public static String getResourceAsString(String resourcePath)
      throws IOException {
    InputStream is = System.class.getResourceAsStream(resourcePath);
    return LightUtils.getInputStreamAsString(is);
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
            getRandomFederatedId(),
            true /* isFederatedUser */,
            getRandomUserId(),
            true /* isUserLoggedIn */,
            false /* isGaeAdmin */);
    gaeTestingUtils.setUp();

    return gaeTestingUtils;
  }

  public static String readLineFromConsole(String message) throws IOException {
    System.out.println(message);
    InputStreamReader converter = new InputStreamReader(System.in);
    BufferedReader in = new BufferedReader(converter);

    return checkNotBlank(in.readLine());
  }

  public static HttpSession getMockSessionForTesting(OAuth2Provider provider, String userId,
      String email) {
    HttpSession mockSession = Mockito.mock(HttpSession.class);
    when(mockSession.getAttribute(LOGIN_PROVIDER_ID.get())).thenReturn(provider.name());
    when(mockSession.getAttribute(LOGIN_PROVIDER_USER_ID.get())).thenReturn(userId);
    when(mockSession.getAttribute(LOGIN_PROVIDER_USER_EMAIL.get())).thenReturn(email);

    return mockSession;
  }
}
