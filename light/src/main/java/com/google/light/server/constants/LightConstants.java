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


import com.google.appengine.tools.pipeline.JobSetting;


import static com.google.light.server.servlets.path.ServletPathEnum.SEARCH_PAGE;


/**
 * Miscellaneous constants for Light.
 * 
 * @author Arjun Satyapal
 */
public class LightConstants {
  public static int SECONDS_IN_A_DAY = 24 * 60 * 60;
  
  public static int SESSION_MAX_INACTIVITY_PERIOD = SECONDS_IN_A_DAY;

  /** URL from where a Google Customer's Profile and Email can be fetched. */
  public static final String GOOGLE_USER_INFO_URL = "https://www.googleapis.com/oauth2/v1/userinfo";
  
  /** Timeout for Task Queues.*/
  public static int TASK_QUEUUE_TIMEOUT_IN_SEC = 10 * 60; // Seconds.


  /** Number of milliseconds to wait before executing the task. */
  public static int TASK_QUEUE_COUNTDOWN_MILLIS = 30 * 1000; // sixty seconds. 
  
  
  /** HTTP Connection Timeout in milli-seconds*/
  public static int HTTP_CONNECTION_TIMEOUT_IN_MILLIS = 10 * 1000;


  public static String REDIRECT_PATH_AFTER_LOGOUT = SEARCH_PAGE.get();


  public static final int SEARCH_RESULTS_PER_PAGE = 10;

  /**
   * Default value for {@link SearchRequestDto#page}.
   * 
   * Note: Client side is assuming this to be 1
   */
  public static final int FIRST_SEARCH_PAGE_NUMBER = 1;
  
  /** EmailId for Light Bot. */
  public static final String LIGHT_BOT_EMAIL = "light-bot@myopenedu.com";

  // AppEngine pipeline Settings.
  public static final JobSetting.BackoffFactor JOB_BACK_OFF_FACTOR = new JobSetting.BackoffFactor(2);
  public static final JobSetting.BackoffSeconds JOB_BACK_OFF_SECONDS = new JobSetting.BackoffSeconds(1);
  public static final JobSetting.MaxAttempts JOB_MAX_ATTEMPTES = new JobSetting.MaxAttempts(3);
  
  /** Number of recrds to be fetched in GDATA Feed */
  public static int GDATA_MAX_RESULT = 3;

  public static final int MAX_RESULTS_MIN = 10;
  public static final int MAX_RESULTS_DEFAULT = MAX_RESULTS_MIN;
  public static final int MAX_RESULTS_MAX = 100;
}
