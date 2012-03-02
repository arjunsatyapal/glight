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
package com.google.light.server.constants;

import com.google.light.server.servlets.AbstractLightServlet;

import com.google.light.server.servlets.person.PersonJsonServlet;

/**
 * Enum to Map Servlets with their Paths and URL Patterns.
 * 
 * @author Arjun Satyapal
 */
public enum ServletPathEnum {
  // TODO(arjuns): Add regex checks here.
  PERSON(PersonJsonServlet.class, "/json/person");

  private Class<? extends AbstractLightServlet> clazz;
  private String servletPath;

  private ServletPathEnum(Class<? extends AbstractLightServlet> clazz, String servletPath) {
    this.clazz = clazz;
    this.servletPath = servletPath;
  }

  public Class<? extends AbstractLightServlet> getClazz() {
    return clazz;
  }

  public String get() {
    return servletPath;
  }

  public static ServletPathEnum getServletPathEnum(String servletPath) {
    for (ServletPathEnum currServletPath : ServletPathEnum.values()) {
      if (currServletPath.get().equals(servletPath)) {
        return currServletPath;
      }
    }

    throw new IllegalArgumentException("ServletPath not found for servletPath[" + servletPath
        + "].");
  }
}
