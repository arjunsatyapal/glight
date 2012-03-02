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

import com.google.common.base.Preconditions;

import com.google.light.server.exception.unchecked.PersonLoginRequiredException;

import com.google.common.base.Strings;
import java.util.List;

/**
 * Some Additional Preconditions which are not available with Guava Library.
 * 
 * @author Arjun Satyapal
 */
public class LightPreconditions {
  public static String checkNotEmptyString(String reference) {
    return checkNotEmptyString(reference, "");
  }

  public static String checkNotEmptyString(String reference, String errorString) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(reference), errorString);
    return reference;
  }

  // TODO(arjuns) : Fix this validation eventually.
  public static String checkEmail(String email) {
    return checkNotEmptyString(email);
  }

  // TODO(arjuns) : Fix this validation eventually.
  public static String checkPersonId(String id) {
    return checkNotEmptyString(id);
  }

  public static void checkPersonIsLoggedIn() {
    if (!GaeUtils.isUserLoggedIn()) {
      throw new PersonLoginRequiredException();
    }
  }

  public static <T> List<T> checkNonEmptyList(List<T> list) {
    checkNotNull(list);
    checkArgument(list.size() > 0);
    return list;
  }

  public static <T> boolean isListEmpty(List<T> list) {
    if (list == null || list.size() == 0) {
      return true;
    }

    return false;
  }

  public static Long checkPositiveLong(Long number) {
    checkNotNull(number);
    checkArgument(number > 0);
    return number;
  }

  public static boolean isEqual(Object expected, Object actual) {
    if (expected == actual) {
      return true;
    } else if ((expected == null && actual != null) || (expected != null && actual == null)) {
      return false;
    }

    return expected.equals(actual);
  }

  // Utility class.
  private LightPreconditions() {
  }
}
