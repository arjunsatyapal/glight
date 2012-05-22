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

import static com.google.light.server.constants.LightEnvEnum.UNIT_TEST;
import static com.google.light.server.constants.OAuth2ProviderService.GOOGLE_DOC;
import static com.google.light.server.constants.OAuth2ProviderService.GOOGLE_LOGIN;
import static com.google.light.server.utils.LightPreconditions.checkEmail;
import static com.google.light.server.utils.LightPreconditions.checkIntegerIsInRage;
import static com.google.light.server.utils.LightPreconditions.checkIsEnv;
import static com.google.light.server.utils.LightPreconditions.checkIsNotEnv;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.LightPreconditions.checkNotEmptyCollection;
import static com.google.light.server.utils.LightPreconditions.checkNull;
import static com.google.light.server.utils.LightPreconditions.checkPersonId;
import static com.google.light.server.utils.LightPreconditions.checkPersonIsGaeAdmin;
import static com.google.light.server.utils.LightPreconditions.checkPositiveLong;
import static com.google.light.server.utils.LightPreconditions.checkProviderUserId;
import static com.google.light.server.utils.LightPreconditions.checkValidSession;
import static com.google.light.server.utils.LightPreconditions.checkValidUri;
import static com.google.light.server.utils.LightUtils.getUUIDString;
import static com.google.light.testingutils.TestingUtils.gaeSetup;
import static com.google.light.testingutils.TestingUtils.getMockSessionForTesting;
import static com.google.light.testingutils.TestingUtils.getRandomEmail;
import static com.google.light.testingutils.TestingUtils.getRandomPersonId;
import static com.google.light.testingutils.TestingUtils.getRandomProviderUserId;
import static com.google.light.testingutils.TestingUtils.getRandomString;
import static com.google.light.testingutils.TestingUtils.getRequestScopedValueProvider;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.Lists;
import com.google.inject.Injector;
import com.google.light.server.constants.LightEnvEnum;
import com.google.light.server.constants.OAuth2ProviderService;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.PersonId;
import com.google.light.server.exception.unchecked.BlankStringException;
import com.google.light.server.exception.unchecked.InvalidPersonIdException;
import com.google.light.server.exception.unchecked.InvalidSessionException;
import com.google.light.server.exception.unchecked.ServerConfigurationException;
import com.google.light.server.exception.unchecked.httpexception.UnauthorizedException;
import com.google.light.server.guice.provider.TestRequestScopedValuesProvider;
import com.google.light.server.servlets.SessionManager;
import com.google.light.testingutils.GaeTestingUtils;
import com.google.light.testingutils.TestingUtils;
import java.security.SecureRandom;
import java.util.List;
import javax.servlet.http.HttpSession;
import org.junit.Test;

/**
 * Test for {@link LightPreconditions}.
 * 
 * @author Arjun Satyapal
 */
public class LightPreconditionsTest {
  private static SecureRandom random = new SecureRandom();

  /**
   * Test for {@link LightPreconditions#checkEmail(String)} Some quick tests. Though not required as
   * we are using Apache Commons Library to validate emails.
   */
  @Test
  public void test_checkEmail() {
    //
    checkEmail("a@b.com");
    checkEmail("a@b.com");

    // Negative test : invalid ascii.
    try {
      checkEmail("\u0001abc@gmail.com");
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // expected.
    }

    // Negative test : double @@
    try {
      checkEmail("a@@gmail.com");
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // expected.
    }

    // Negative test : a.@gmail.com
    try {
      checkEmail("a.@gmail.com");
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // expected.
    }

    // Negative test : a@.com
    try {
      checkEmail("a.@.com");
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // expected.
    }

    // Negative test : a@gmail.com.
    try {
      checkEmail("a.@.com");
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // expected.
    }
  }

  /**
   * Test for {@link LightPreconditions#checkIntegerIsInRage(Integer, int, int, String)}.
   */
  @Test
  public void test_checkIntegerIsInRange() {
    int min = -10;
    int max = 10;

    try {
      checkIntegerIsInRage(-15, min, max, "");
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // Expected
    }

    checkIntegerIsInRage(-10, min, max, "");
    checkIntegerIsInRage(-0, min, max, "");
    checkIntegerIsInRage(10, min, max, "");

    try {
      checkIntegerIsInRage(15, min, max, "");
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // Expected
    }

  }

  /**
   * Test for {@link LightPreconditions#checkIsEnv(Object, LightEnvEnum...)}
   */
  @Test
  public void test_checkIsEnv() {
    for (LightEnvEnum currEnv : LightEnvEnum.values()) {
      if (currEnv == LightEnvEnum.DEV_SERVER) {
        // Since DEV_SERVER is never returned as one of the env, so this test doesnt work well.
        continue;
      }

      LightEnvEnum extraEnv = getRandomOtherEnvExcept(currEnv);
      assertTrue(currEnv != extraEnv);

      GaeTestingUtils gaeTestingUtils = TestingUtils.gaeSetup(currEnv);

      // Positive Test : Send currEnv and extraEnv
      checkIsEnv(this, currEnv, extraEnv);

      /*
       * Negative Test : As extraEnv is different from currEnv, so if we limit checkIsEnv only to
       * that, then this test should fail.
       */
      try {
        checkIsEnv(this, extraEnv);
        fail("should have failed when currEnv=" + currEnv + " and extraEnv=" + extraEnv + ".");
      } catch (ServerConfigurationException e) {
        // Expected
      }

      gaeTestingUtils.tearDown();
    }
  }

  private LightEnvEnum getRandomOtherEnvExcept(LightEnvEnum... envs) {
    List<LightEnvEnum> remainingEnvList = Lists.newArrayList(LightEnvEnum.values());
    remainingEnvList.removeAll(Lists.newArrayList(envs));

    int randomExtraEnvIndex = Math.abs(random.nextInt()) % remainingEnvList.size();
    LightEnvEnum extraEnv = remainingEnvList.get(randomExtraEnvIndex);
    return extraEnv;
  }

  /**
   * Test for {@link LightPreconditions#checkIsNotEnv(Object, LightEnvEnum...)}
   */
  @Test
  public void test_checkIsNotEnv() {
    for (LightEnvEnum currEnv : LightEnvEnum.values()) {
      if (currEnv == LightEnvEnum.DEV_SERVER) {
        // Since DEV_SERVER is never returned as one of the env, so this test doesnt work well.
        continue;
      }

      LightEnvEnum otherEnv1 = getRandomOtherEnvExcept(currEnv);
      LightEnvEnum otherEnv2 = getRandomOtherEnvExcept(currEnv, otherEnv1);

      assertTrue(currEnv != otherEnv1);
      assertTrue(currEnv != otherEnv2 && otherEnv1 != otherEnv2);

      GaeTestingUtils gaeTestingUtils = TestingUtils.gaeSetup(currEnv);

      // Positive Test : Send currEnv and extraEnv
      checkIsNotEnv(this, otherEnv1, otherEnv2);

      /*
       * Negative Test : As extraEnv is different from currEnv, so if we limit checkIsEnv only to
       * that, then this test should fail.
       */
      try {
        checkIsNotEnv(this, currEnv);
        fail("should have failed when currEnv=" + currEnv);
      } catch (ServerConfigurationException e) {
        // Expected
      }

      gaeTestingUtils.tearDown();
    }
  }

  /**
   * Test for {@link LightPreconditions#checkNonEmptyList(List)}
   */
  @Test
  public void test_checkNonEmptyList() {
    checkNotEmptyCollection(Lists.newArrayList(1L), "");

    // Negative : List = null.
    try {
      List<Object> tempList = null;
      checkNotEmptyCollection(tempList, "null expected");
      fail("should have failed.");
    } catch (NullPointerException e) {
      // expected.
    }

    try {
      checkNotEmptyCollection(Lists.newArrayList(), "empty list");
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // expected.
    }
  }

  /**
   * Test for {@link LightPreconditions#checkNotBlank(String, String)}
   */
  @Test
  public void test_checkNotBlank_msg() {
    String uuid = getUUIDString();
    // Negative : string=null
    try {
      checkNotBlank(null, uuid);
      fail("should have failed.");
    } catch (BlankStringException e) {
      // expected.
      assertTrue(e.getMessage().contains(uuid));

    }

    // Negative : string=""
    try {
      checkNotBlank("", uuid);
      fail("should have failed.");
    } catch (BlankStringException e) {
      // expected.
      assertTrue(e.getMessage().contains(uuid));
    }
  }

  /**
   * Test for {@link LightPreconditions#checkPersonId(String)}
   */
  @Test
  public void test_checkPersonId() {
    checkPersonId(new PersonId(1234L));
    checkPersonId(new PersonId(1L));
    checkPersonId(new PersonId(Long.MAX_VALUE));

    // try {
    // checkPersonId(null);
    // fail("should have failed.");
    // } catch (InvalidPersonIdException e) {
    // // Expected
    // }

    try {
      checkPersonId(new PersonId(0L));
      fail("should have failed.");
    } catch (InvalidPersonIdException e) {
      // Expected
    }

    // Rest negative tests are tested as part of LightPreconditions#checkPositiveLong
  }

  /**
   * Test for {@link LightPreconditions#checkPersonIsGaeAdmin()}
   */
  @Test
  public void test_checkPersonIsGaeAdmin() {
    GaeTestingUtils gaeTestingUtils = null;
    try {
      gaeTestingUtils = gaeSetup(LightEnvEnum.PROD);
      // Default is non-admin so should fail.
      checkPersonIsGaeAdmin();
      fail("should have failed.");
    } catch (UnauthorizedException e) {
      // Expected
    } finally {
      gaeTestingUtils.tearDown();
    }

    // Now change to admin and check again.
    try {
      gaeTestingUtils.setAdmin(true /* isAdmin */);
      gaeTestingUtils.setUp();
      checkPersonIsGaeAdmin();
    } finally {
      gaeTestingUtils.tearDown();
    }
  }

  /**
   * Test for {@link LightPreconditions#checkPositiveLong(Long)}
   */
  @Test
  public void test_checkPositiveLong() {
    // Positive Tests.
    checkPositiveLong(1L, "usual value failed.");
    checkPositiveLong(Long.MAX_VALUE, "maxValue failed");

    // Negative Test : Null value.
    try {
      checkPositiveLong(null, "null");
      fail("should have failed.");
    } catch (NullPointerException e) {
      // Expected
    }

    // Negative Test : 0L
    try {
      checkPositiveLong(0L, "zero");
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // Expected
    }

    // Negative Test : -1L
    try {
      checkPositiveLong(-1L, "negative");
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // Expected
    }

    // Negative Test : Long.MIN_VALUE
    try {
      checkPositiveLong(Long.MIN_VALUE, "min value");
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // Expected
    }
  }

  /**
   * Test for {@link LightPreconditions#checkNull(Object)}.
   */
  @Test
  public void test_checkNull() {
    checkNull(null, "");

    try {
      checkNull(" ", "should fail");
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // Expected
    }
  }

  /**
   * Test for {@link LightPreconditions#checkProviderUserId(OAuth2ProviderService, String)}.
   */
  @Test
  public void test_checkProviderUserId() {
    // Positive Testing.
    checkProviderUserId(GOOGLE_LOGIN, getRandomString());
    checkProviderUserId(GOOGLE_DOC, null);

    // Negative Testing : GOOGLE_LOGIN with null.
    try {
      checkProviderUserId(GOOGLE_LOGIN, null);
      fail("should have failed.");
    } catch (BlankStringException e) {
      // Expected
    }

    // Negative Testing : GOOGLE_LOGIN with blank string.
    try {
      checkProviderUserId(GOOGLE_LOGIN, " ");
      fail("should have failed.");
    } catch (BlankStringException e) {
      // Expected
    }

    // Negative Testing : GOOGLE_DOC with blank string
    try {
      checkProviderUserId(GOOGLE_DOC, " ");
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // Expected
    }

    // Negative Testing : GOOGLE_DOC with non-null
    try {
      checkProviderUserId(GOOGLE_DOC, getRandomString());
      fail("should have failed.");
    } catch (IllegalArgumentException e) {
      // Expected
    }
  }

  /**
   * Test for {@link LightPreconditions#checkValidSession(SessionManager)}
   */
  @Test
  public void test_checkValidSession() {
    // Positive Testing : provide all parameters for session.
    assertSession(GOOGLE_LOGIN, getRandomProviderUserId(), getRandomPersonId(),
        getRandomEmail(), true /* should pass */);

    // Negative Test : providerUserId = null
    assertSession(GOOGLE_LOGIN, null, getRandomPersonId(),
        getRandomEmail(), false /* should pass */);

    // Negative Test : providerUserId = blank string
    assertSession(GOOGLE_LOGIN, " ", getRandomPersonId(),
        getRandomEmail(), false /* should pass */);

    // Negative Test : personId = null
    assertSession(GOOGLE_LOGIN, getRandomProviderUserId(), null,
        getRandomEmail(), false /* should pass */);

    // Negative Test : personId = null
    assertSession(GOOGLE_LOGIN, getRandomProviderUserId(), null,
        getRandomEmail(), false /* should pass */);

    // Negative Test : email = null
    assertSession(GOOGLE_LOGIN, getRandomProviderUserId(), getRandomPersonId(),
        null, false /* should pass */);

    // Negative Test : email = blank
    assertSession(GOOGLE_LOGIN, getRandomProviderUserId(), getRandomPersonId(),
        null, false /* should pass */);
  }

  private void assertSession(OAuth2ProviderService providerService,
      String providerUserId, PersonId personId, String email, boolean shouldPass) {
    GaeTestingUtils.cheapEnvSwitch(UNIT_TEST);
    HttpSession session = getMockSessionForTesting(UNIT_TEST, providerService, providerUserId,
        personId, email);
    TestRequestScopedValuesProvider testRequestScopedValueProvider = 
        getRequestScopedValueProvider(new PersonId(1L), new PersonId(1L));
    Injector injector = TestingUtils.getInjectorByEnv(UNIT_TEST, testRequestScopedValueProvider, session);

    SessionManager sessionManager = injector.getInstance(SessionManager.class);

    if (shouldPass) {
      checkValidSession(sessionManager);
    } else {
      try {
        checkValidSession(sessionManager);
        fail("should have failed.");
      } catch (InvalidSessionException e) {
        // Expected
      }
    }
  }

  /**
   * Test for {@link LightPreconditions#checkValidUri(String)}.
   */
  @Test
  public void test_checkValidUri() throws Exception {
    // Positive tests.
    checkValidUri("hello");
    checkValidUri("/hello");
    checkValidUri("http://localhost/hello");
    checkValidUri("http://localhost:8080/hello");
    checkValidUri("http://light-qa.appspot.com/hello");
    checkValidUri("http://light-qa.appspot.com/hello?key=value");
    checkValidUri("http://light-qa.appspot.com/hello#1234");
    checkValidUri("http://light-qa.appspot.com/hello?key=value#1234");

    // Negative Test : uri = null
    try {
      checkValidUri(null);
      fail("should have failed.");
    } catch (BlankStringException e) {
      // Expected
    }

    // Negative Test : uri = blank string
    try {
      checkValidUri(" ");
      fail("should have failed.");
    } catch (BlankStringException e) {
      // Expected
    }

    // Negative Test : uri = blank string
    try {
      checkValidUri(" ");
      fail("should have failed.");
    } catch (BlankStringException e) {
      // Expected
    }

  }
}
