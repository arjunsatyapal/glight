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
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.light.server.utils.LightUtils.isCollectionEmpty;
import static com.google.light.server.utils.ServletUtils.getRequestHeaderValue;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.light.server.constants.HttpHeaderEnum;
import com.google.light.server.constants.LightEnvEnum;
import com.google.light.server.constants.OAuth2ProviderService;
import com.google.light.server.dto.module.ModuleTypeProvider;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.CollectionId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.JobId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.ModuleId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.PersonId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.Version;
import com.google.light.server.dto.pojo.typewrapper.stringwrapper.ExternalId;
import com.google.light.server.exception.ExceptionType;
import com.google.light.server.exception.unchecked.BlankStringException;
import com.google.light.server.exception.unchecked.InvalidJobIdException;
import com.google.light.server.exception.unchecked.InvalidModuleIdException;
import com.google.light.server.exception.unchecked.InvalidPersonIdException;
import com.google.light.server.exception.unchecked.InvalidSessionException;
import com.google.light.server.exception.unchecked.InvalidVersionException;
import com.google.light.server.exception.unchecked.ServerConfigurationException;
import com.google.light.server.exception.unchecked.httpexception.InternalServerErrorException;
import com.google.light.server.exception.unchecked.httpexception.NotFoundException;
import com.google.light.server.exception.unchecked.httpexception.PersonLoginRequiredException;
import com.google.light.server.exception.unchecked.httpexception.UnauthorizedException;
import com.google.light.server.persistence.entity.person.PersonEntity;
import com.google.light.server.servlets.SessionManager;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.IntegerValidator;
import org.apache.commons.validator.routines.LongValidator;

/**
 * Some Additional Preconditions which are not available with Guava Library's {@link Preconditions}.
 * 
 * TODO(arjuns) : See which HTTP Exception should be thrown.
 * 
 * @author Arjun Satyapal
 */
public class LightPreconditions {
  private static EmailValidator emailValidator = EmailValidator.getInstance();
  private static LongValidator longValidator = LongValidator.getInstance();
  private static IntegerValidator integerValidator = IntegerValidator.getInstance();

  /**
   * Ensures that the Email passed to the referenced method is valid.
   * 
   * @param email
   * @return
   */
  public static String checkEmail(String email) {
    checkArgument(emailValidator.isValid(email), "email:" + email);
    return email;
  }

  /**
   * Ensures that PersonId is valid.
   */
  public static PersonId checkPersonId(PersonId personId) {
    try {
      checkPositiveLong(personId.getValue(), "Invalid [" + personId.getValue() + "].");
    } catch (Exception e) {
      throw new InvalidPersonIdException(e);
    }

    return personId;
  }

  /**
   * Ensures that ModuleId is valid.
   * TODO(arjuns): Add test for this.
   */
  public static ModuleId checkModuleId(ModuleId moduleId) {
    try {
      Preconditions.checkNotNull(moduleId, "moduleId");
      checkPositiveLong(moduleId.getValue(), "Invalid [" + moduleId.getValue() + "].");
    } catch (Exception e) {
      throw new InvalidModuleIdException(e);
    }

    return moduleId;
  }

  /**
   * Ensures that CollectionId is valid.
   * TODO(arjuns): Add test for this.
   */
  public static CollectionId checkCollectionId(CollectionId collectionId) {
    try {
      Preconditions.checkNotNull(collectionId, "collectionId");
      checkPositiveLong(collectionId.getValue(), "Invalid [" + collectionId.getValue() + "].");
    } catch (Exception e) {
      throw new InvalidModuleIdException(e);
    }

    return collectionId;
  }

//  TODO(arjuns): Add test for this.
  public static <T, C extends Collection<T>> C
      checkNotEmptyCollection(C collection, String message) {
    checkArgument(!isCollectionEmpty(collection), message);
    return collection;
  }

  /**
   * Ensures that ModuleId is valid.
   * TODO(arjuns): Add test for this.
   */
  public static Version checkVersion(Version version) {
    try {
      Preconditions.checkNotNull(version, "version");
      checkPositiveLong(version.getValue(), "Invalid [" + version.getValue() + "].");
    } catch (Exception e) {
      throw new InvalidVersionException(e);
    }

    return version;
  }

  /**
   * Ensures that PersonId is valid.
   */
  public static JobId checkJobId(JobId jobId) {
    try {
      Preconditions.checkNotNull(jobId, "jobId");
      checkPositiveLong(jobId.getValue(), "Invalid [" + jobId.getValue() + "].");
    } catch (Exception e) {
      throw new InvalidJobIdException(e);
    }

    return jobId;
  }

  /**
   * TODO(arjuns): Add test for this.
   * Ensures that Key<PersonEntity> is valid.
   */
  public static Key<PersonEntity> checkPersonKey(Key<PersonEntity> personKey) {
    checkPersonId(new PersonId(personKey.getId()));
    return personKey;
  }

  /**
   * Ensures that the passed value is a valid positive long and its range is [1, Long.Max_value].
   * TODO(arjuns): Update test.
   * 
   * @param value
   * @return
   */
  public static Long checkPositiveLong(Long value, String message) {
    Preconditions.checkNotNull(value, message);
    checkArgument(longValidator.isInRange(value, 1, Long.MAX_VALUE), message + "[" + value + "].");
    return value;
  }

  /**
   * Ensures that the passed value is a valid positive long and its range is [1, Long.Max_value].
   * TODO(arjuns): Update test.
   * 
   * @param value
   * @return
   */
  public static Long checkNonNegativeLong(Long value, String message) {
    Preconditions.checkNotNull(value, message);
    checkArgument(longValidator.isInRange(value, 0, Long.MAX_VALUE), message + "[" + value + "].");
    return value;
  }

  public static Integer checkIntegerIsInRage(Integer value, int min, int max, String message) {
    String errorMessage = message + " Allowed Range : [" + min + ":" + max + "].";
    Preconditions.checkNotNull(value, errorMessage);
    checkArgument(integerValidator.isInRange(value, min, max), errorMessage);
    return value;
  }

  /**
   * Ensures that the object passed to the referenced method is null. This is opposite of
   * {@link Preconditions#checkNotNull(Object)}.
   */
  public static void checkNull(Object object, String message) {
    if (object != null) {
      throw new IllegalArgumentException(message);
    }
  }

  /**
   * Ensures that Person is Admin.
   */
  public static void checkPersonIsGaeAdmin() {
    if (!GaeUtils.isUserAdmin()) {
      throw new UnauthorizedException("Admin priviliges required.");
    }
  }

  /**
   * Ensures that the the given String is a valid URI.
   * 
   * @throws URISyntaxException
   */
  public static String checkValidUri(String uri) throws URISyntaxException {
    checkNotBlank(uri, "uri");
    new URI(uri);
    return uri;
  }

  /**
   * Ensures that given object was instantiated in one of the allowedEnvs.
   * 
   * TODO(arjuns) : Add test for this.
   * 
   * @param allowedEnvs
   * @param object
   */
  public static void checkIsEnv(Object object, LightEnvEnum... allowedEnvs) {
    if (containsCurrEnvInVarArgs(allowedEnvs)) {
      return;
    }

    throw new ServerConfigurationException(object.getClass().getName()
        + " should not have been instantiated in " + LightEnvEnum.getLightEnv()
        + " because it is allowed to be instantiated only in "
        + Iterables.toString(newArrayList(allowedEnvs)));
  }

  /**
   * Ensures that given object was not instantiated in notAllowedEnvs.
   * 
   * TODO(arjuns) : Add test for this.
   * 
   * @param allowedEnvs
   * @param object
   */
  public static void checkIsNotEnv(Object object, LightEnvEnum... notAllowedEnvs) {
    if (!containsCurrEnvInVarArgs(notAllowedEnvs)) {
      return;
    }

    throw new ServerConfigurationException(object.getClass().getName()
        + " should not have been instantiated in " + LightEnvEnum.getLightEnv()
        + " because it is not allowed to be instantiated in "
        + Iterables.toString(Lists.newArrayList(notAllowedEnvs)));
  }

  private static boolean containsCurrEnvInVarArgs(LightEnvEnum... varargs) {
    LightEnvEnum currEnv = LightEnvEnum.getLightEnv();

    for (LightEnvEnum currVarArg : varargs) {
      if (currVarArg == currEnv) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks whether providerUserId is required or not.
   * 
   * @param providerService
   * @param providerUserId
   * @return
   */
  public static String checkProviderUserId(OAuth2ProviderService providerService,
      String providerUserId) {
    if (providerService.isUsedForLogin()) {
      checkNotBlank(providerUserId, "providerUserId should not be blank for " + providerService);
    } else {
      checkArgument(isNullOrEmpty(providerUserId),
          "for " + providerService + ", providerUserId should be null");
    }

    return providerUserId;
  }

  // TODO(arjuns): Add test for this.
  public static void checkPersonLoggedIn(SessionManager sessionManager) {
    if (!sessionManager.isPersonLoggedIn()) {
      throw new PersonLoginRequiredException("");
    }
  }

  /**
   * Ensures that Session is valid.
   * NOTE : Unlike other check functions, this does not return anything.
   * 
   * @param sessionManager
   */
  public static void checkValidSession(SessionManager sessionManager) {
    if (!sessionManager.isValidSession()) {
      throw new InvalidSessionException("Invalid Session.");
    }
  }

  /**
   * Ensures that Objectify Transaction is running.
   */
  // TODO(arjuns): Add test for this.
  public static void checkTxnIsRunning(Objectify ofy) {
    Preconditions.checkNotNull(ofy, "txn is null.");
    checkArgument(ofy.getTxn().isActive(), "txn is inactive.");
  }

  public static void checkIsUnderTaskQueue(HttpServletRequest request) {
    String queueName = getRequestHeaderValue(request, HttpHeaderEnum.GAE_QUEUE_NAME);
    Preconditions.checkNotNull(queueName, "Currently request is not running under TaskQueue.");
  }

  public static void checkIsUnderCron(HttpServletRequest request) {
    String cronHeader = getRequestHeaderValue(request, HttpHeaderEnum.GAE_CRON);
    Preconditions.checkNotNull(cronHeader, "Current Request was not invoked by Cron.");
  }

  public static <T> T checkNotNull(T object, ExceptionType type, String message) {
    try {
      return Preconditions.checkNotNull(object);
    } catch (Exception e) {
      handleError(type, message);
    }

    throw new IllegalStateException("Code should not reach here.");
  }

  /**
   * Javadoc is same as for {{@link #checkNotBlank(String)}. This throws an exception with cause as
   * errorString.
   * 
   * @param reference
   * @param errorString
   * @return
   */
  public static String checkNotBlank(String reference, String errorString) {
    if (StringUtils.isBlank(reference)) {
      throw new BlankStringException(errorString, ExceptionType.SERVER);
    }

    return reference;
  }

  /**
   * Javadoc is same as for {{@link #checkNotBlank(String)}. This throws an exception with cause as
   * errorString.
   * 
   * @param reference
   * @param errorString
   * @return
   */
  public static String checkNotBlank(String reference, ExceptionType type,
      String errorString) {
    if (StringUtils.isBlank(reference)) {
      throw new BlankStringException(errorString, type);
    }

    return reference;
  }

  private static void handleError(ExceptionType exceptionType, String message) {
    switch (exceptionType) {
      case CLIENT_PARAMETER:
        throw new NotFoundException(message);

      case SERVER:
        throw new InternalServerErrorException(message);

      default:
        throw new IllegalArgumentException("Unsupported type : " + exceptionType);
    }
  }

  public static ExternalId checkExternalIdIsGDocResource(ExternalId externalId) {
    checkArgument(externalId.getModuleType().getModuleTypeProvider() == ModuleTypeProvider.GOOGLE_DOC);
    return externalId;
  }

  // Utility class.
  private LightPreconditions() {
  }
}
