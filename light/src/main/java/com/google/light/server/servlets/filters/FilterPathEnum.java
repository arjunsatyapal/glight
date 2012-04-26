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

import static com.google.light.server.utils.LightPreconditions.checkNonEmptyList;

import com.google.common.collect.Lists;
import java.util.List;
import javax.servlet.Filter;

/**
 * Enum to Map Filter Implementations with URL Patterns.
 * TODO(arjuns): Find equivalent solution for Jersey servlets. 
 *  
 * @author Arjun Satyapal
 */
public enum FilterPathEnum {
  API(ProdServletFilter.class, Lists.newArrayList("/api/*", "/admin/*", "/login/*", "/oauth2/*")),
  TASK_QUEUE(PipelineFilter.class, Lists.newArrayList("/_ah/pipeline/*")),
  TEST(TestServletFilter.class, Lists.newArrayList("/api/*", "/admin/*", "/login/*", "/oauth2/*"));
  
  private Class<? extends Filter> clazz;
  private List<String> urlPatterns;
  
  private FilterPathEnum(Class<? extends Filter> clazz, 
      List<String> urlPatterns) {
    this.clazz = clazz;
    this.urlPatterns = checkNonEmptyList(urlPatterns, "urlPatterns");
  }
  
  public Class<? extends Filter> getClazz() {
    return clazz;
  }
  
  public List<String> getUrlPatterns() {
    return urlPatterns;
  }
}
