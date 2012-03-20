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
import static com.google.light.server.constants.RequestParmKeyEnum.LOGIN_PROVIDER_ID;
import static com.google.light.server.constants.RequestParmKeyEnum.LOGIN_PROVIDER_USER_EMAIL;
import static com.google.light.server.constants.RequestParmKeyEnum.LOGIN_PROVIDER_USER_ID;
import static com.google.light.server.utils.LightUtils.appendKeyValue;
import static com.google.light.server.utils.LightUtils.appendSectionHeader;
import static com.google.light.server.utils.LightUtils.getPST8PDTime;
import static com.google.light.server.utils.LightUtils.prepareSession;
import static com.google.light.testingutils.TestingUtils.getRandomEmail;
import static com.google.light.testingutils.TestingUtils.getRandomUserId;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableMapBuilder;
import com.google.light.server.constants.OAuth2Provider;
import com.google.light.testingutils.TestingUtils;
import java.io.InputStream;
import java.util.Map;
import javax.servlet.http.HttpSession;
import org.joda.time.DateTime;
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

    String expected = "<br><br><b>sectionHeader: </b>";
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
   * Test for {@link LightUtils#prepareSession(HttpSession, OAuth2Provider, String, String)}.
   */
  @Test
  public void test_prepareSession() {
    OAuth2Provider provider = OAuth2Provider.GOOGLE_LOGIN;

    String userId = TestingUtils.getRandomUserId();
    String userEmail = TestingUtils.getRandomEmail();

    HttpSession session = mock(HttpSession.class);
    handleMockSession(session, provider, userId, userEmail, false);
    prepareSession(session, provider, userId, userEmail);
    handleMockSession(session, provider, userId, userEmail, true);

    // Negative Test : Now try to verify with some userId.
    try {
      handleMockSession(session, provider, getRandomUserId(), userEmail, true);
      fail("should have failed.");
    } catch (WantedButNotInvoked e) {
       // Expected
      assertTrue(getStackTraceAsString(e).contains(LOGIN_PROVIDER_USER_ID.get()));
    }
    
    // Negative Test : Now try to verify with some email.
    try {
      handleMockSession(session, provider, userId, getRandomEmail(), true);
      fail("should have failed.");
    } catch (WantedButNotInvoked e) {
       // Expected
      assertTrue(getStackTraceAsString(e).contains(LOGIN_PROVIDER_USER_EMAIL.get()));
    }
    
    // TODO(arjuns): See if there is a way to write a multi-threaded session modification test
    // At present we are assuming that synchronized will take care of it.
  }

  private void handleMockSession(HttpSession session, OAuth2Provider provider,
      String userId, String userEmail, boolean isVerifyMode) {

    Map<String, String> map = new ImmutableMapBuilder<String, String>()
        .put(LOGIN_PROVIDER_ID.get(), provider.name())
        .put(LOGIN_PROVIDER_USER_ID.get(), userId)
        .put(LOGIN_PROVIDER_USER_EMAIL.get(), userEmail)
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
