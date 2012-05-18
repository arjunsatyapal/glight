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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.constants.LightConstants.SESSION_MAX_INACTIVITY_PERIOD;
import static com.google.light.server.constants.LightStringConstants.FTS_MODULE_ID_KEY;
import static com.google.light.server.constants.RequestParamKeyEnum.DEFAULT_EMAIL;
import static com.google.light.server.constants.RequestParamKeyEnum.LOGIN_PROVIDER_ID;
import static com.google.light.server.constants.RequestParamKeyEnum.LOGIN_PROVIDER_USER_ID;
import static com.google.light.server.constants.RequestParamKeyEnum.PERSON_ID;
import static com.google.light.server.utils.LightPreconditions.checkIntegerIsInRage;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.CharStreams;
import com.google.light.server.constants.FileExtensions;
import com.google.light.server.constants.LightConstants;
import com.google.light.server.constants.OAuth2ProviderService;
import com.google.light.server.dto.pojo.typewrapper.AbstractTypeWrapper;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.CollectionId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.JobId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.ModuleId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.PersonId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.Version;
import com.google.light.server.dto.pojo.typewrapper.stringwrapper.ExternalId;
import com.google.light.server.dto.pojo.typewrapper.stringwrapper.FTSDocumentId;
import com.google.light.server.exception.unchecked.httpexception.LightHttpException;
import com.google.light.server.guice.providers.InstantProvider;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;

/**
 * General Utility methods for Light.
 * TOOD(arjuns): Fix tests.
 * 
 * @author Arjun Satyapal
 */
public class LightUtils {
  private static final Logger logger = Logger.getLogger(LightUtils.class.getName());
  
  public static final Version LATEST_VERSION = new Version(Version.LATEST_VERSION);

  public static String getInputStreamAsString(InputStream is) throws IOException {
    checkNotNull(is);
    return CharStreams.toString(new InputStreamReader(is, Charsets.UTF_8));
  }

  public static void appendSectionHeader(StringBuilder builder, String sectionHeader) {
    builder.append("<br><br><b>").append(sectionHeader).append(": </b><br>");
  }

  public static void appendKeyValue(StringBuilder builder, String key, Object value) {
    checkNotNull(builder);
    builder.append("<br>").append(key).append(" = ").append(value);
  }

  public static void appendAttention(StringBuilder builder, String key, String value) {
    // Source : http://www.computerhope.com/cgi-bin/htmlcolor.pl?c=FF0000
    builder.append("<p style=\"color:#FF0000\">");
    appendKeyValue(builder, key, value);
    builder.append("</p>");
  }

  public static void appendLine(StringBuilder builder, String text) {
    builder.append(text).append("<br>");
  }

  public static void appendHref(StringBuilder builder, String id, String href, String name) {
    builder.append("<a " + id + " href=\"").append(href).append("\">").append(name)
        .append("</a><br>");
  }

  // TODO(arjuns): Abstract out common userInfo.
  // TODO(arjuns): Add token expiry time here.
  // TODO(arjuns): Fix the callers of this method.
  public static void prepareSession(HttpSession session, OAuth2ProviderService loginProvider,
      Long personId, String providerUserId, String defaultEmail) {
    synchronized (session) {
      logger.info("Prepairing session with provider[" + loginProvider
          + ", providerUserId[" + personId
          + "], providerUserEmail[" + defaultEmail + "].");
      session.setAttribute(LOGIN_PROVIDER_ID.get(), loginProvider.name());
      session.setAttribute(PERSON_ID.get(), personId);
      session.setAttribute(DEFAULT_EMAIL.get(), defaultEmail);
      session.setAttribute(LOGIN_PROVIDER_USER_ID.get(), providerUserId);
      session.setMaxInactiveInterval(SESSION_MAX_INACTIVITY_PERIOD);
    }
  }

  public static DateTime getPST8PDTime(long instantInMillis) {
    return new DateTime(instantInMillis, DateTimeZone.forID("PST8PDT"));

  }

  public static void appendSessionData(StringBuilder builder, HttpSession session) {
    appendSectionHeader(builder, "Session Details");
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

  public static String getHref(String url, String text) {
    return "<a href=" + url + ">" + text + "</a>";
  }

  // TODO(arjuns): Update methods to use Instant instead of Long.
  public static long getCurrentTimeInMillis() {
    Instant instant = GuiceUtils.getProvider(Instant.class).get();
    return instant.getMillis();
  }

  public static Instant getNow() {
    return new DateTime().toInstant();
  }

  // Utility class.
  private LightUtils() {
  }

  public static void wrapIntoRuntimeExceptionAndThrow(Exception e) {
    // Allowing LightHttpException to pass through so the filters can handle it properly.
    if (LightHttpException.class.isAssignableFrom(e.getClass())) {
      throw (LightHttpException) e;
    } else {
      throw new RuntimeException(e);
    }
  }

  // public static void enqueueParticipants(Injector injector, PersonId watcherId, PersonId
  // performerId) {
  // HttpServletRequest request = getInstance(injector, HttpServletRequest.class);
  // checkNotNull(request, "request");
  //
  // if (watcherId != null) {
  // GuiceUtils.seedEntityInRequestScope(request, PersonId.class, AnotWatcher.class, watcherId);
  // }
  //
  // Preconditions.checkNotNull(performerId);
  // GuiceUtils.seedEntityInRequestScope(request, PersonId.class, AnotPerformer.class, performerId);
  //
  // PersonId gotPerformerId = GuiceUtils.getInstance(PersonId.class, AnotPerformer.class);
  //
  // Participants participants = GuiceUtils.getProvider(Participants.class).get();
  // checkArgument(participants.isValid(), "Invalid state for Participants.");
  // }

  /**
   * TODO(arjuns): Add test for this.
   * Reutrn url for a given String.
   * 
   * @param url
   * @return
   */
  public static URL getURL(String url) {
    try {
      return new URL(url);
    } catch (MalformedURLException e) {
      // TODO(arjuns): Auto-generated catch block
      throw new RuntimeException(e);
    }
  }

  /**
   * TODO(arjuns): Add test for this.
   * Reutrn url for a given String.
   * 
   * @param url
   * @return
   */
  public static URI getURI(String uri) {
    try {
      return new URI(uri);
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  public static String encodeToUrlEncodedString(String string) {
    try {
      return URLEncoder.encode(string, Charsets.UTF_8.displayName());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static String decodeFromUrlEncodedString(String encodedString) {
    try {
      return URLDecoder.decode(encodedString, Charsets.UTF_8.displayName());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

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
   * TODO(arjuns): Add test for this.
   * Utility method to get a random file Name with given extension.
   * 
   * @return
   */
  public static String getRandomFileName(FileExtensions extension) {
    InstantProvider instantProvider = GuiceUtils.getInstance(InstantProvider.class);

    StringBuilder builder = new StringBuilder(getUUIDString())
        .append(".")
        .append(Long.toString(instantProvider.get().getMillis()))
        .append(".")
        .append(extension.get());

    return builder.toString();
  }

  // TODO(arjuns): Add test for this.
  public static <T> String arrayToString(T[] array) {
    List<T> list = Lists.newArrayList(array);
    return Iterables.toString(list);
  }

  public static boolean isCollectionEmpty(Collection<?> collection) {
    if (collection == null || collection.size() == 0) {
      return true;
    }

    return false;
  }

  public static Long getInstantInMillis(Instant instant) {
    if (instant == null) {
      return null;
    }

    return instant.getMillis();
  }

  public static <I, W extends AbstractTypeWrapper<I, W>> I getWrapperValue(W wrapper) {
    if (wrapper == null) {
      return null;
    }

    return wrapper.getValue();
  }

  public static <E, W extends AbstractTypeWrapper<E, W>>
      List<E> convertWrapperListToListOfValues(List<W> listOfWrappers) {
    if (isCollectionEmpty(listOfWrappers)) {
      return Lists.newArrayList();
    }

    List<E> requiredList = Lists.newArrayListWithCapacity(listOfWrappers.size());

    for (W curr : listOfWrappers) {
      E wrappedValue = getWrapperValue(curr);
      requiredList.add(wrappedValue);
    }

    return requiredList;
  }

  public static <E, W extends AbstractTypeWrapper<E, W>>
      Set<E> convertWrapperSetToSetOfValues(Set<W> setOfWrappers) {
    if (isCollectionEmpty(setOfWrappers)) {
      return Sets.newHashSet();
    }

    Set<E> requiredSet = Sets.newHashSet();

    for (W curr : setOfWrappers) {
      E wrappedValue = getWrapperValue(curr);
      requiredSet.add(wrappedValue);
    }

    return requiredSet;
  }

  @VisibleForTesting
  @SuppressWarnings("rawtypes")
  static Map<String, AbstractTypeWrapper> map =
      new ImmutableMap.Builder<String, AbstractTypeWrapper>()
          .put(CollectionId.class.getName(), new CollectionId(Long.MAX_VALUE))
          .put(ExternalId.class.getName(), new ExternalId("http://google.com"))
          .put(JobId.class.getName(), new JobId(Long.MAX_VALUE))
          .put(ModuleId.class.getName(), new ModuleId(Long.MAX_VALUE))
          .put(PersonId.class.getName(), new PersonId(Long.MAX_VALUE))
          .put(Version.class.getName(), new Version(1L))
          .build();

  @SuppressWarnings({ "unchecked", "rawtypes", "deprecation" })
  public static <I, W extends AbstractTypeWrapper<I, W>> W getWrapper(I value, Class<W> clazz) {
    if (value == null) {
      return null;
    }

    AbstractTypeWrapper instanceCreator = map.get(clazz.getName());
    Preconditions.checkNotNull(instanceCreator, "instanceCreator for : " + clazz.getSimpleName());
    return ((W) instanceCreator.createInstance(value));
  }

  @SuppressWarnings({ "cast" })
  public static <W extends AbstractTypeWrapper<Long, W>> W getLongWrapper(String value,
      Class<W> clazz) {
    if (value == null || value.equals("null")) {
      return null;
    }

    return ((W) getWrapper(Long.parseLong(value), clazz));
  }

  @SuppressWarnings({ "cast" })
  public static <W extends AbstractTypeWrapper<String, W>> W getStringWrapper(String value,
      Class<W> clazz) {
    if (value == null || value.equals("null")) {
      return null;
    }

    return ((W) getWrapper(value, clazz));
  }

  @SuppressWarnings({ "unchecked", "rawtypes", "deprecation" })
  public static <I, W extends AbstractTypeWrapper<I, W>>
      List<W> convertListOfValuesToWrapperList(List<I> listOfValues, Class<W> clazz) {
    if (isCollectionEmpty(listOfValues)) {
      return Lists.newArrayList();
    }

    List<W> requiredList = Lists.newArrayListWithCapacity(listOfValues.size());

    for (I curr : listOfValues) {
      AbstractTypeWrapper instanceCreator = map.get(clazz.getName());
      W instance = ((W) instanceCreator.createInstance(curr));
      requiredList.add(instance);
    }

    return requiredList;
  }

  @SuppressWarnings({ "rawtypes", "unchecked", "deprecation" })
  public static <I, W extends AbstractTypeWrapper<I, W>>
      Set<W> convertSetOfValuesToWrapperSet(Set<I> setOfValues, Class<W> clazz) {
    if (isCollectionEmpty(setOfValues)) {
      return Sets.newHashSet();
    }

    Set<W> requiredSet = Sets.newHashSet();

    for (I curr : setOfValues) {
      AbstractTypeWrapper instanceCreator = map.get(clazz.getName());
      W instance = ((W) instanceCreator.createInstance(curr));
      requiredSet.add(instance);
    }

    return requiredSet;
  }

  public static String generateNameForExternalId(ExternalId externalId) {
    DateTime dateTime = new DateTime(getNow());
    StringBuilder builder = new StringBuilder("Untitled : " + externalId.getValue());
    builder.append(":")
        .append(dateTime.getYear())
        .append("/")
        .append(dateTime.getMonthOfYear())
        .append("/")
        .append(dateTime.getDayOfMonth());

    return builder.toString();
  }

  /**
   * Method to replace an existing instance with a new Instance of same type in a List.
   * 
   * @param list
   * @param existingInstance
   * @param newInstance
   */
  public static <D> void replaceInstanceInList(List<D> list, D existingInstance, D newInstance) {
    checkArgument(!isCollectionEmpty(list), "list cannot be empty.");

    int index = list.indexOf(existingInstance);
    checkArgument(index >= 0, "existingInstance was not found in the list.");
    list.remove(index);
    list.add(index, newInstance);
  }

  public static String getStackAsString() {
    try {
      throw new IllegalArgumentException("Trying to get stack.");
    } catch (Exception e) {
      return Throwables.getStackTraceAsString(e);
    }
  }
  
  /**
   * @param maxResultsStr
   * @return
   */
  public static int initializeMaxResults(String maxResultsStr) {
    int maxResult = LightConstants.MAX_RESULTS_DEFAULT;
    if (maxResultsStr != null) {
      maxResult = Integer.parseInt(maxResultsStr);
      checkIntegerIsInRage(maxResult, LightConstants.MAX_RESULTS_MIN,
          LightConstants.MAX_RESULTS_MAX, "Invalid value for maxResult[" + maxResult + "].");
    }
    return maxResult;
  }
  
  /**
   * @param queryStr
   * @return
   */
  public static String initializeFilterStr(String queryStr) {
    if (StringUtils.isBlank(queryStr)) {
      return null;
    }

    return encodeToUrlEncodedString(queryStr);
  }
  
  public static FTSDocumentId convertModuleIdToFtsDocumentId(ModuleId moduleId) {
    return new FTSDocumentId(FTS_MODULE_ID_KEY + getWrapperValue(moduleId));
  }
  
  public static ModuleId convertFTSDocumentIdToModuleId(FTSDocumentId ftsDocumentId) {
    checkArgument(ftsDocumentId.getValue().startsWith(FTS_MODULE_ID_KEY));
    return new ModuleId(ftsDocumentId.getValue().substring(FTS_MODULE_ID_KEY.length()));
  }

}
