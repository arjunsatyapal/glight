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

import com.google.inject.ProvisionException;

/**
 * Utility Methods related to Guice.
 * 
 * @author Arjun Satyapal
 */
public class GuiceUtils {
  /**
   * Use this method for checking if the cause for Guice Provision Exception is of expected type.
   * 
   * @param provisionEx
   * @param className
   * @return
   */
  public static <T extends Exception> boolean isExpectedException(ProvisionException provisionEx,
      Class<?> clazz) {
    // TODO(arjuns) : See if this could be made better.
    if (provisionEx.getCause().getClass().getName().equals(clazz.getName())) {
      return true;
    }
    return false;
  }

  // Utility class.
  private GuiceUtils() {
  }
}
