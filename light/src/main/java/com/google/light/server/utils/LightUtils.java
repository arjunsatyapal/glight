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
import static com.google.light.server.constants.RequestParmKeyEnum.GAE_USER_EMAIL;
import static com.google.light.server.constants.RequestParmKeyEnum.GAE_USER_ID;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import com.google.light.server.exception.unchecked.httpexception.PersonLoginRequiredException;
import java.util.Enumeration;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

  public static void prepareSession(HttpSession session, String gaeUserId, String gaeUserEmail) {
    logger.info("Prepairing session with gaseUserId[" + gaeUserId + "], gaseUserEmail["
        + gaeUserEmail + "].");
    session.setAttribute(GAE_USER_ID.get(), gaeUserId);
    session.setAttribute(GAE_USER_EMAIL.get(), gaeUserEmail);
  }
  
  @SuppressWarnings("deprecation")
  public static void appendSessionData(StringBuilder builder, HttpSession session) {
    String testGaeUserId = null;
    String testGaeUserEmail = null;

    if (session.isNew()) {
      session.invalidate();
      throw new PersonLoginRequiredException("First create a session.");
    }

    testGaeUserId = checkNotBlank((String) session.getAttribute(GAE_USER_ID.get()));
    testGaeUserEmail = checkNotBlank((String) session.getAttribute(GAE_USER_EMAIL.get()));

    appendKeyValue(builder, "testGaeUserId", testGaeUserId);
    appendKeyValue(builder, "testGaeUserEmail", testGaeUserEmail);

    appendKeyValue(builder, "creationTimeInMillis", session.getCreationTime());
    appendKeyValue(builder, "creationTime", getPST8PDTime(session.getCreationTime()));
    appendKeyValue(builder, "sessionId", session.getId());
    appendKeyValue(builder, "lastAccessedTimeInMillis", session.getLastAccessedTime());
    appendKeyValue(builder, "lastAccessedTimeInMillis",
        getPST8PDTime(session.getLastAccessedTime()));
    appendKeyValue(builder, "maxInactiveIntervalInSec", session.getMaxInactiveInterval());
    appendKeyValue(builder, "servletContext", session.getServletContext());
    appendKeyValue(builder, "sessionContext", session.getSessionContext());

    @SuppressWarnings("rawtypes")
    Enumeration names = session.getAttributeNames();
    builder.append("<br>names = ");
    while (names.hasMoreElements()) {
      builder.append(" ").append(names.nextElement());
    }
  }

  // Utility class.
  private LightUtils() {
  }
}
