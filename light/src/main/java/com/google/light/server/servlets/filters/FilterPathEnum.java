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
package com.google.light.server.servlets.filters;

import javax.servlet.Filter;

/**
 * Enum to Map Filter Implementations with URL Patterns.
 * 
 * @author Arjun Satyapal
 */
public enum FilterPathEnum {
  API(ServletFilter.class, "/api/*"),
  TEST(ServletFilter.class, "/test/*");
  
  private Class<? extends Filter> clazz;
  private String urlPattern;
  
  private FilterPathEnum(Class<? extends Filter> clazz, String urlPattern) {
    this.clazz = clazz;
    this.urlPattern = urlPattern;
  }
  
  public Class<? extends Filter> getClazz() {
    return clazz;
  }
  
  public String getUrlPattern() {
    return urlPattern;
  }
}
