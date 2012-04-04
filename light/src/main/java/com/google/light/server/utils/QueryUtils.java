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

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.google.common.base.Throwables;
import com.google.light.server.dto.DtoInterface;
import com.google.light.server.exception.unchecked.httpexception.BadRequestException;

/**
 * Utility class for converting query strings parameters from an {@link HttpServletRequest} to a
 * DTO.
 * 
 * @author Walter Cacau
 */
public class QueryUtils {
  private static final Logger logger = Logger.getLogger(QueryUtils.class.getName());

  /**
   * Converts query strings parameters from an {@link HttpServletRequest} to a DTO of the given
   * dtoClass and
   * validates the DTO before returning it.
   * 
   * @param request
   * @param dtoClass
   * @return
   */
  @SuppressWarnings("unchecked")
  public static <D extends DtoInterface<D>> D getValidDto(HttpServletRequest request,
      Class<D> dtoClass) {

    // Creating a dtoClass instance
    D instance = null;
    try {
      Constructor<D> constructor = dtoClass.getDeclaredConstructor();
      if (constructor.isAccessible()) {
        instance = constructor.newInstance();
      } else {
        constructor.setAccessible(true);
        try {
          instance = constructor.newInstance();
        } finally {
          constructor.setAccessible(false);
        }
      }
    } catch (Exception e) {
      String message = "Could not create a instance of " + dtoClass.getName();
      logger.severe(message + ": "
          + Throwables.getStackTraceAsString(e));
      throw new RuntimeException(message, e);
    }

    // Populating the fields using the query string parameters.
    // If we have more then one parameter value for the same name, just get the first one.
    Map<String, String[]> parameterMap = (Map<String, String[]>) request.getParameterMap();
    for (Entry<String, String[]> entry : parameterMap.entrySet()) {
      String property = entry.getKey();

      String[] value = entry.getValue();
      if (value.length > 1)
        throw new BadRequestException("Multiple values for queryKey " + property);

      if (!PropertyUtils.isWriteable(instance, property)) {
        throw new BadRequestException("Unknown queryKey " + property);
      }

      try {
        BeanUtils.setProperty(instance, property, value[0]);
      } catch (Exception e) {
        String message =
            "Could not set property " + property + " to "
                + ToStringBuilder.reflectionToString(entry.getValue()[0]);
        logger.severe(message + ": "
            + Throwables.getStackTraceAsString(e));
        // TODO(waltercacau): Recheck with there is a better exception to throw here.
        throw new RuntimeException(
            message + ": "
                + Throwables.getStackTraceAsString(e));
      }

    }

    // Validating instance and returning
    return instance.validate();
  }
}
