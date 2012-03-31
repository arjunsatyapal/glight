/*
 * Copyright (C) Google Inc.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.light.testingutils;

import static com.google.light.server.utils.LightPreconditions.checkNotBlank;

/**
 * Enum to encapsulate paths for Test Resources. Assumption is all the path is relative to
 * classPath.
 * 
 * TODO(arjuns): Add a validation function, which ensures all the resources exist.
 * 
 * @author Arjun Satyapal
 */
public enum TestResourcePaths {
  SEARCH_GSS_ADDITION_QUERY_INPUT_XML("/search/gss/addition_input.xml"),
  SEARCH_GSS_ADDITION_QUERY_OUTPUT_JSON("/search/gss/addition_output.json"),
  SEARCH_GSS_ADDXITION_QUERY_INPUT_XML("/search/gss/addxition_input.xml"),
  SEARCH_GSS_ADDXITION_QUERY_OUTPUT_JSON("/search/gss/addxition_output.json"),
  // Values inside this file are randomly created.
  GOOGLE_TOKEN_INFO_JSON("/login/oauth2/google/google_token_info.json"),
  /*
   * Values inside this file are one's that are pulled from Google. So if some one changes values,
   * then those things will have to be fixed.
   */
  GOOGLE_USER_INFO_JSON("/login/oauth2/google/google_user_info.json");


  private String path;

  private TestResourcePaths(String path) {
    this.path = checkNotBlank(path, "path");
  }

  public String get() {
    return path;
  }
}
