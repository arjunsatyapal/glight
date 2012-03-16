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

import com.google.appengine.api.users.UserServiceFactory;

import com.google.light.server.exception.unchecked.httpexception.UnauthorizedException;

import com.google.common.base.Preconditions;
import com.google.light.server.exception.unchecked.InvalidPersonIdException;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
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

  /**
   * Ensures that the passed String reference is neither null nor empty string.
   * 
   * @param reference
   * @return
   */
  public static String checkNotBlank(String reference) {
    return checkNotBlank(reference, "");
  }

  /**
   * Javadoc is same as for {{@link #checkNotBlank(String)}. This throws an exception with
   * cause as errorString.
   * 
   * @param reference
   * @param errorString
   * @return
   */
  public static String checkNotBlank(String reference, String errorString) {
    Preconditions.checkArgument(!StringUtils.isBlank(reference), errorString);
    return reference;
  }

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

  // TODO(arjuns) : Fix this validation eventually.
  public static Long checkPersonId(Long personId) {
    try {
      return checkPositiveLong(personId);
    } catch (Exception e) {
      throw new InvalidPersonIdException(e);
    }
  }

  /**
   * Ensures that the list passed to the referenced method is not empty.
   * 
   * @param list
   * @return
   */
  public static <T> List<T> checkNonEmptyList(List<T> list) {
    checkNotNull(list);
    checkArgument(list.size() > 0);
    return list;
  }

  /**
   * Ensures that the passed value is a valid positive long and its range is [1, Long.Max_value].
   * 
   * @param value
   * @return
   */
  public static Long checkPositiveLong(Long value) {
    checkNotNull(value);
    checkArgument(longValidator.isInRange(value, 1, Long.MAX_VALUE));
    return value;
  }

  /**
   * Ensures that the object passed to the referenced method is null. This is opposite of
   * {@link Preconditions#checkNotNull(Object)}.
   */
  public static void checkNull(Object object) {
    if (object != null) {
      throw new IllegalArgumentException("Expected null.");
    }
  }
  
  /**
   * Returns true if current user is GAE Admin. Returns false if user is not admin / user is not
   * logged in.
   * 
   * For GAE Admin, we always trust on AppEngine Environment.
   * 
   * @return
   */
  public static boolean isGaeAdmin() {
    return UserServiceFactory.getUserService().isUserAdmin();
  }
  
  /**
   * Ensures that Person is Admin.
   * 
   * TODO(arjuns) : Add test for this.
   */
  public static void checkPersonIsGaeAdmin() {
    if (!isGaeAdmin()) {
      throw new UnauthorizedException("Admin priviliges required.");
    }
  }

  // Utility class.
  private LightPreconditions() {
  }
}
