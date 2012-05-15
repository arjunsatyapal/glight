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


import static com.google.light.server.servlets.path.ServletPathEnum.SEARCH_PAGE;


/**
 * Miscellaneous constants for Light.
 * 
 * @author Arjun Satyapal
 */
public class LightConstants {

  public static final int ONE_DAY_IN_MILLIS = 24 * 60 * 60 * 1000;
  public static final int ONE_WEEK_IN_MILLIS = 7*ONE_DAY_IN_MILLIS;
  public static final String GSS_CLIENTLOGIN_APPNAME_SUFFIX = ".appspot.com";
  public static final int GSS_CLIENTLOGIN_TOKEN_TIMEOUT_IN_MILLIS = ONE_WEEK_IN_MILLIS;
  
  public static int SECONDS_IN_HR = 60 * 60;
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

  // TODO(arjuns): Get this value from client.
  /** Number of records to be fetched in Google Doc GDATA Feed. 
   * {@link https://developers.google.com/google-apps/documents-list/#retrieving_fewer_changes_per_request}
   */
  public static final int GDATA_GDOC_MAX_RESULTS = 1000;
  
  
  /**
   * Number of documents that can be requested by client to import in single batch.
   * This includes only first level documents. It is allowed to have folders with multiple-level
   * of hierarchy. The request can even contain 10 folders. This is done to ensure that in real
   * time it can be verified that User has access to all these documents and another assumption is 
   * that Google provides access permissions to whole nested tree. If this changes in future, then
   * this may have to be changed. 
   */
  public static int IMPORT_BATCH_SIZE_MAX = 10;

  public static final int MAX_RESULTS_MIN = 1;
  public static final int MAX_RESULTS_DEFAULT = 10;
  public static final int MAX_RESULTS_MAX = 100;
  
  public static final int TASK_COUNTDOWN_MILLIS = 1000;
  public static final int TASK_MAX_BACKOFF_SEC = SECONDS_IN_HR;
  public static final int TASK_MAX_DOUBLINGS = 5;
  public static final int TASK_MIN_BACKOFF_SECONDS = 2;
  public static final int TASK_MAX_AGE = SECONDS_IN_HR;
  public static final int TASK_RETRY_LIMIT = 3;
  

}
