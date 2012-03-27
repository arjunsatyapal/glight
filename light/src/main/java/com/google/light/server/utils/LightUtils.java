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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.constants.LightConstants.SESSION_MAX_INACTIVITY_PERIOD;
import static com.google.light.server.constants.RequestParmKeyEnum.LOGIN_PROVIDER_ID;
import static com.google.light.server.constants.RequestParmKeyEnum.LOGIN_PROVIDER_USER_EMAIL;
import static com.google.light.server.constants.RequestParmKeyEnum.LOGIN_PROVIDER_USER_ID;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.light.server.constants.OAuth2Provider;
import com.google.light.server.exception.unchecked.httpexception.LightHttpException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.logging.Logger;
import javax.servlet.http.HttpSession;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * General Utility methods for Light.
 * 
 * @author Arjun Satyapal
 */
public class LightUtils {
  private static final Logger logger = Logger.getLogger(LightUtils.class.getName());

  public static String getInputStreamAsString(InputStream is) throws IOException {
    checkNotNull(is);
    return CharStreams.toString(new InputStreamReader(is, Charsets.UTF_8));
  }

  public static void appendSectionHeader(StringBuilder builder, String sectionHeader) {
    builder.append("<br><br><b>").append(sectionHeader).append(": </b>");
  }

  public static void appendKeyValue(StringBuilder builder, String key, Object value) {
    checkNotNull(builder);
    builder.append("<br>").append(key).append(" = ").append(value);
  }

  public static DateTime getPST8PDTime(long instantInMillis) {
    return new DateTime(instantInMillis, DateTimeZone.forID("PST8PDT"));
  }

  // TODO(arjuns): Abstract out common userInfo.
  // TODO(arjuns): Add token expiry time here.
  public static void prepareSession(HttpSession session, OAuth2Provider loginProvider,
      String providerUserId, String providerUserEmail) {
    synchronized (session) {
      logger.info("Prepairing session with provider[" + loginProvider 
          + ", providerUserId[" + providerUserId
          + "], providerUserEmail[" + providerUserEmail + "].");
      session.setAttribute(LOGIN_PROVIDER_ID.get(), loginProvider.name());
      session.setAttribute(LOGIN_PROVIDER_USER_ID.get(), providerUserId);
      session.setAttribute(LOGIN_PROVIDER_USER_EMAIL.get(), providerUserEmail);
      session.setMaxInactiveInterval(SESSION_MAX_INACTIVITY_PERIOD);
    }
  }

  public static void appendSessionData(StringBuilder builder, HttpSession session) {
    appendSectionHeader(builder, "Session Details : ");
    if (session == null) {
      appendKeyValue(builder, "status", "no session found.");
      return;
    } else {
      appendKeyValue(builder, "status", "session found.");
    }

    @SuppressWarnings("rawtypes")
    Enumeration attrNames = session.getAttributeNames();

    while (attrNames.hasMoreElements()) {
      Object currAttrName = attrNames.nextElement();
      appendKeyValue(builder, currAttrName.toString(),
          session.getAttribute(currAttrName.toString()));
    }

    appendKeyValue(builder, "creationTimeInMillis", session.getCreationTime());
    appendKeyValue(builder, "creationTime", getPST8PDTime(session.getCreationTime()));
    appendKeyValue(builder, "sessionId", session.getId());
    appendKeyValue(builder, "lastAccessedTimeInMillis", session.getLastAccessedTime());
    appendKeyValue(builder, "lastAccessedTimeInMillis",
        getPST8PDTime(session.getLastAccessedTime()));
    appendKeyValue(builder, "maxInactiveIntervalInSec", session.getMaxInactiveInterval());
    appendKeyValue(builder, "servletContext", session.getServletContext());
  }

  // TODO(arjuns): Inject clock instead of using SystemTime so that we can simulate clock
  // forwarding when required.
  public static long getCurrentTimeInMillis() {
    return System.currentTimeMillis();
  }
  
  // Utility class.
  private LightUtils() {
  }
  
  public static void wrapIntoRuntimeExceptionAndThrow(Exception e) {
    // Allowing LightHttpException to pass through so the filters can handle it properly.
    if(LightHttpException.class.isAssignableFrom(e.getClass())) {
      throw (LightHttpException) e;
    } else {
      throw new RuntimeException(e);
    }
  }
}
