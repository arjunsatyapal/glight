/*
 * Copyright 2012 Google Inc.
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

import static com.google.light.server.utils.LightPreconditions.checkNotBlank;

/**
 * Path to HTML files.
 * 
 * @author Arjun Satyapal
 */
public enum HtmlPathEnum {
  PUT_OAUTH2_CONSUMER_CREDENTIAL("/html/admin/put_oauth2_consumer_credential.html");
  
  private String path;
  
  private HtmlPathEnum(String path) {
    this.path = checkNotBlank(path, "path");
  }
  
  public String get() {
    return path;
  }
}