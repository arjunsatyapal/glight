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
package com.google.light.testingutils;

import com.google.api.client.http.GenericUrl;

/**
 * Some constants for Testing Purposes.
 * 
 * @author Arjun Satyapal
 */
public class TestingConstants {
  public static final GenericUrl devServerUrl = new GenericUrl("http://localhost:8080/_ah/login");

  /**
   * Following values should match in file mentioned at
   * {@link TestResourcePaths#GOOGLE_TOKEN_INFO_JSON}
   */
  public static final String DEFAULT_GOOGLE_USER_ID = "115639870677665060321";
  public static final String RESOURCE_OWNER_EMAIL = "unit-test1@myopenedu.com";
  public static final String DEFAULT_ACCESS_TOKEN = "access_token";
  public static final String DEFAULT_REFRESH_TOKEN = "refresh_token";
  public static final String DEFAULT_ACCESS_TYPE = "offline";
  public static final String DEFAULT_TOKEN_TYPE = "Bearer";
  public static final String DEFAULT_GOOGLE_CLIENT_ID = "160638920188.apps.googleusercontent.com";
  public static final long DEFAULT_TOKEN_EXPIRES_IN = 3600L;
  public static final long DEFAULT_TOKEN_EXPIRES_IN_MILLIS = 1332491059527L;
  public static final long DEFAULT_PERSON_ID = 1234L;
}