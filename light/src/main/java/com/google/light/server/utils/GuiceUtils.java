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

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.google.inject.Singleton;
import com.google.light.server.annotations.AnotActor;
import com.google.light.server.annotations.AnotOwner;
import com.google.light.server.dto.pojo.PersonId;
import com.google.light.server.dto.pojo.RequestScopedValues;
import java.lang.annotation.Annotation;
import javax.servlet.http.HttpServletRequest;

/**
 * Utility Methods related to Guice.
 * 
 * @author Arjun Satyapal
 */
@Singleton
public class GuiceUtils {
  private static Injector injector;

  public static void setInjector(Injector injectorFoo) {
    injector = injectorFoo;
  }

  public static Injector getInjector() {
    return checkNotNull(injector);
  }

  /**
   * Utility method to return {@link Key} which uses {@link Key#get(Class, Annotation)}.
   * 
   * E : Entity Class. A : Binding Annotation class.
   * 
   * TODO(arjuns) : Rename this method to getAnnotationBindingKey.
   * 
   * @param entityClass
   * @param anotClazz
   * @return
   */
  public static <E, A extends Annotation> Key<E> getKeyForScopeSeed(Class<E> entityClass,
      Class<A> anotClazz) {
    checkNotNull(entityClass);
    checkNotNull(anotClazz);
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
   * E : Entity to be put under RequestScope. A : Binding annotation.
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

  /**
   * Returns instance for a class.
   * 
   * @param injector
   * @param clazz
   * @return
   */
  public static <T> T getInstance(Class<T> clazz) {
    checkNotNull(injector, "injector");
    return checkNotNull(injector.getInstance(clazz),
        "Failed to get instance for Class[" + clazz.getName() + "].");
  }

  public static <T> Provider<T> getProvider(Injector injector, Class<T> clazz) {
    return checkNotNull(injector.getProvider(clazz));
  }

  /**
   * Returns instance for a class annotated with a {@link BindingAnnotation}.
   */
  public static <T, A extends Annotation> T getInstance(Class<T> clazz,
      Class<A> anotClazz) {
    Key<T> guiceKey = getKeyForScopeSeed(clazz, anotClazz);
    return checkNotNull(injector.getInstance(guiceKey),
        "Failed to get instance for Class[" + clazz.getName()
            + "], AnnotationClass[" + anotClazz + "].");
  }

  /**
   * Provides a Provider for a given class.
   * 
   * @param clazz
   * @return
   */
  public static <T> Provider<T> getProvider(Class<T> clazz) {
    checkNotNull(injector, "injector");
    return checkNotNull(injector.getProvider(clazz),
        "Failed to get Provider for Class[" + clazz.getName() + "].");
  }

  public static RequestScopedValues getRequestScopedValues() {
    Provider<RequestScopedValues> provider = getProvider(RequestScopedValues.class);
    return checkNotNull(provider.get(), "requestScopedValues.");
  }

  public static void enqueueRequestScopedVariables(PersonId ownerId, PersonId actorId) {

    HttpServletRequest request = getInstance(HttpServletRequest.class);
    checkNotNull(request, "request");

    if (ownerId != null) {
      seedEntityInRequestScope(request, PersonId.class, AnotOwner.class, ownerId);
    }

    checkNotNull(actorId, "actorId");
    seedEntityInRequestScope(request, PersonId.class, AnotActor.class, actorId);
    RequestScopedValues participants = getProvider(RequestScopedValues.class).get();
    checkArgument(participants.isValid(), "Invalid state for Participants.");
  }
  
  public static PersonId getOwnerId() {
    RequestScopedValues requestScopedValues = getRequestScopedValues();
    return checkNotNull(requestScopedValues.getOwnerId(), "ownerId");
  }

  // Utility class.
  @Inject
  private GuiceUtils() {
  }
}
