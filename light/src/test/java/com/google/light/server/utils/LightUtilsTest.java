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

import static com.google.common.base.Throwables.getStackTraceAsString;
import static com.google.light.server.constants.LightConstants.SESSION_MAX_INACTIVITY_PERIOD;
import static com.google.light.server.constants.OAuth2ProviderService.GOOGLE_LOGIN;
import static com.google.light.server.constants.RequestParmKeyEnum.DEFAULT_EMAIL;
import static com.google.light.server.constants.RequestParmKeyEnum.LOGIN_PROVIDER_ID;
import static com.google.light.server.constants.RequestParmKeyEnum.PERSON_ID;
import static com.google.light.server.utils.LightUtils.appendKeyValue;
import static com.google.light.server.utils.LightUtils.appendSectionHeader;
import static com.google.light.server.utils.LightUtils.getPST8PDTime;
import static com.google.light.server.utils.LightUtils.prepareSession;
import static com.google.light.testingutils.TestingUtils.getRandomEmail;
import static com.google.light.testingutils.TestingUtils.getRandomPersonId;
import static com.google.light.testingutils.TestingUtils.getRandomString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableMapBuilder;
import com.google.light.server.constants.OAuth2ProviderService;
import java.util.Map;
import javax.servlet.http.HttpSession;
import org.joda.time.DateTime;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.exceptions.verification.WantedButNotInvoked;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Test for {@link LightUtils}.
 * 
 * @author Arjun Satyapal
 */
public class LightUtilsTest {
  /**
   * Test for {@link LightUtils#getInputStreamAsString(InputStream)}
   */
  @Test
  public void test_getInputStreamAsString() {
    // Not much to test here.
  }

  /**
   * Test for {@link LightUtils#appendSectionHeader(StringBuilder, String)}
   */
  @Test
  public void test_appendSectionHeader() {
    StringBuilder builder = new StringBuilder();
    String sectionHeader = "sectionHeader";

    String expected = "<br><br><b>sectionHeader: </b><br>";
    appendSectionHeader(builder, sectionHeader);
    assertEquals(expected, builder.toString());
  }

  /**
   * Test for {@link LightUtils#appendKeyValue(StringBuilder, String, Object)}
   */
  @Test
  public void test_appendKeyValue() {
    StringBuilder builder = new StringBuilder();
    String key = "key";
    String value = "value";
    String expected = "<br>key = value";
    appendKeyValue(builder, key, value);
    assertEquals(expected, builder.toString());
  }

  /**
   * Test for {@link LightUtils#getPST8PDTime(long)}.
   */
  @Test
  public void test_getPST8PDTTime() {
    DateTime dateTime = new DateTime(2012, 03, 13, 2, 30);
    long timeInMillis = dateTime.toInstant().getMillis();
    assertEquals(dateTime.toString(), getPST8PDTime(timeInMillis).toString());

    /*
     * Moving back by 3 days. In 2012, DayLight saving was enabled on Tue Mar 11.
     */
    timeInMillis = timeInMillis - 3 * 24 * 60 * 60 * 1000;
    DateTime expectedDateTime = new DateTime(2012, 03, 10, 1, 30);
    assertEquals(expectedDateTime.toString(), getPST8PDTime(timeInMillis).toString());
  }

  /**
   * Test for {@link LightUtils#appendSessionData(StringBuilder, HttpSession)}
   */
  @Test
  public void test_appendSessionData() {
    // Not much to test here. So ignoring it.
  }

  /**
   * Test for {@link LightUtils#prepareSession(HttpSession, OAuth2ProviderService, String, String)}.
   */
  @Ignore(value="TODO(arjuns) : fix me.")
  @Test
  public void test_prepareSession() {
    OAuth2ProviderService providerService = GOOGLE_LOGIN;

    long personId = getRandomPersonId();
    String userEmail = getRandomEmail();
    String providerUserId = getRandomString();

    HttpSession session = mock(HttpSession.class);
    handleMockSession(session, providerService, personId, userEmail, false);
    prepareSession(session, providerService, personId, providerUserId, userEmail);
    handleMockSession(session, providerService, personId, userEmail, true);

    // Negative Test : Now try to verify with some userId.
    try {
      handleMockSession(session, providerService, getRandomPersonId(), userEmail, true);
      fail("should have failed.");
    } catch (WantedButNotInvoked e) {
       // Expected
      assertTrue(getStackTraceAsString(e).contains(PERSON_ID.get()));
    }
    
    // Negative Test : Now try to verify with some email.
    try {
      handleMockSession(session, providerService, personId, getRandomEmail(), true);
      fail("should have failed.");
    } catch (WantedButNotInvoked e) {
       // Expected
      assertTrue(getStackTraceAsString(e).contains(DEFAULT_EMAIL.get()));
    }
    
    // TODO(arjuns): See if there is a way to write a multi-threaded session modification test
    // At present we are assuming that synchronized will take care of it.
  }

  private void handleMockSession(HttpSession session, OAuth2ProviderService providerService,
      long personId, String userEmail, boolean isVerifyMode) {

    Map<String, String> map = new ImmutableMapBuilder<String, String>()
        .put(LOGIN_PROVIDER_ID.get(), providerService.name())
        .put(PERSON_ID.get(), Long.toString(personId))
        .put(DEFAULT_EMAIL.get(), userEmail)
        .getMap();

    Answer<String> answer = new Answer<String>() {
      @Override
      public String answer(InvocationOnMock invocation) throws Throwable {
        Object[] args = invocation.getArguments();
        return "called with arguments: " + args;
      }
    };

    for (String currKey : map.keySet()) {
      String value = map.get(currKey);

      if (isVerifyMode) {
        verify(session, times(1)).setAttribute(currKey, value);
      } else {
        doAnswer(answer).when(session).setAttribute(currKey, value);
      }
    }
    
    // Additional things.
    if (isVerifyMode) {
      verify(session, times(1)).setMaxInactiveInterval(SESSION_MAX_INACTIVITY_PERIOD);
    } else {
      doAnswer(answer).when(session).setMaxInactiveInterval(SESSION_MAX_INACTIVITY_PERIOD);
    }
    
  }
}
