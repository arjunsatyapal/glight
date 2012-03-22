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
  public static final GenericUrl devServerUrl =  new GenericUrl("http://localhost:8080/_ah/login");
  
  public static final String GOOGLE_OAUTH_CB_CMD_LINE_URI = "urn:ietf:wg:oauth:2.0:oob";
  
  public static final String RESOURCE_OWNER_EMAIL = "unit-test1@myopenedu.com";
}
