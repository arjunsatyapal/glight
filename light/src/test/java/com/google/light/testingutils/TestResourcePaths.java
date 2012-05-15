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
  SEARCH_GSS_ADDITION_QUERY_INPUT_XML("/search/gss/addition_query_input.xml"),
  SEARCH_GSS_ADDITION_QUERY_OUTPUT_JSON("/search/gss/addition_query_output.json"),
  SEARCH_GSS_ADDXITION_QUERY_INPUT_XML("/search/gss/addxition_query_input.xml"),
  SEARCH_GSS_ADDXITION_QUERY_OUTPUT_JSON("/search/gss/addxition_query_output.json"),

  GSS_CLIENTLOGIN_TOKEN_INFO_SAMPLE_JSON("/search/gss/clientlogin_token_info.json"),
  GSS_CLIENTLOGIN_SAMPLE_FORBIDDEN_RESPONSE_TXT("/search/gss/clientlogin_forbidden_response.txt"),

  // Values inside this file are randomly created.
  GOOGLE_TOKEN_INFO_JSON("/login/oauth2/google/google_token_info.json"),
  /*
   * Values inside this file are one's that are pulled from Google. So if some one changes values,
   * then those things will have to be fixed.
   */
  GOOGLE_USER_INFO_JSON("/login/oauth2/google/google_user_info.json"),
  
  // Wrapper Objects.
  COLLECTION_ID_XML("/server/dto/pojo/typewrapper/longwrapper/collectionId.xml"),
  EXTERNAL_ID_XML("/server/dto/pojo/typewrapper/stringwrapper/externalId.xml"),
  JOB_ID_XML("/server/dto/pojo/typewrapper/longwrapper/jobId.xml"),
  MODULE_ID_XML("/server/dto/pojo/typewrapper/longwrapper/moduleId.xml"),
  PERSON_ID_XML("/server/dto/pojo/typewrapper/longwrapper/personId.xml"),
  
  VERSION_SPECIFIC_XML("/server/dto/pojo/typewrapper/longwrapper/versionSpecific.xml"),
  VERSION_LATEST_XML("/server/dto/pojo/typewrapper/longwrapper/versionLatest.xml"),
  VERSION_NO_VERSION_XML("/server/dto/pojo/typewrapper/longwrapper/versionNoVersion.xml"),
  
  
  REDIRECT_JSON("/login/oauth2/redirect.json"),
  JS_VARIABLES_PRELOAD_JSON("/js_variables_preload.json");

  private String path;

  private TestResourcePaths(String path) {
    this.path = checkNotBlank(path, "path");
  }

  public String get() {
    return path;
  }
}
