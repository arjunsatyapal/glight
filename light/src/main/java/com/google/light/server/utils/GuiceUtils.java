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

import javax.servlet.http.HttpServletRequest;

import com.google.inject.Key;
import java.lang.annotation.Annotation;

import com.google.inject.ProvisionException;

/**
 * Utility Methods related to Guice.
 * 
 * @author Arjun Satyapal
 */
public class GuiceUtils {

  /**
   * Utility method to return {@link Key} which uses {@link Key#get(Class, Annotation)}.
   * 
   * E : Entity Class.
   * A : Binding Annotation class.
   * 
   * TODO(arjuns): Add test.
   * 
   * @param entityClass
   * @param anotClazz
   * @return
   */
  public static <E, A extends Annotation> Key<E> getKeyForScopeSeed(Class<E> entityClass,
      Class<A> anotClazz) {
    return Key.get(entityClass, anotClazz);
  }

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

  /**
   * Utility method to enqueue an entity under a RequestScope.
   * 
   * E : Entity to be put under RequestScope.
   * A : Binding annotation.
   * 
   * TODO(arjuns): Add test for this.
   * 
   * @param request
   * @param entityClass
   * @param anotClass
   * @param object
   */
  public static <E, A extends Annotation> void seedEntityInRequestScope(
      HttpServletRequest request, Class<E> entityClass, Class<A> anotClass, E object) {
    Key<E> key = getKeyForScopeSeed(entityClass, anotClass); 
    request.setAttribute(key.toString(), object);
  }

  // Utility class.
  private GuiceUtils() {
  }
}
