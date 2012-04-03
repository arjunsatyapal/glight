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
package com.google.light.server.constants;

/**
 * Miscellaneous constants for Light.
 * 
 * @author Arjun Satyapal
 */
public class LightConstants {
  public static int SESSION_MAX_INACTIVITY_PERIOD = 3600;

  /** URL from where a Google Customer's Profile and Email can be fetched. */
  public static final String GOOGLE_USER_INFO_URL = "https://www.googleapis.com/oauth2/v1/userinfo";
  
  
  public static int TASK_QUEUUE_TIMEOUT_IN_SEC = 10 * 60; // Seconds.

}
