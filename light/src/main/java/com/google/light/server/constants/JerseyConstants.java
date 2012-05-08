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

import static com.google.light.server.constants.LightStringConstants.CONTENT;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class JerseyConstants {
  private static final String JERSEY_CONTEXT = "/rest";
  private static final String GAE_ADMIN_CONTEXT = "/gaeadmin";

  public static final String PATH_PARAM_EXTERNAL_KEY = "external_key";
  public static final String PATH_EXTERNAL_KEY = "/{" + PATH_PARAM_EXTERNAL_KEY + "}";

  public static final String PATH_PARAM_COLLECTION_ID = "collection_id";
  public static final String PATH_COLLECTION_ID = "/{" + PATH_PARAM_COLLECTION_ID + "}";

  public static final String PATH_PARAM_JOB_ID = "job_id";
  public static final String PATH_JOB_ID = "/{" + PATH_PARAM_JOB_ID + "}";
  
  public static final String PATH_PARAM_JOB_ID_PARENT = "job_id_PARENT";
  public static final String PATH_JOB_ID_PARENT = "/{" + PATH_PARAM_JOB_ID_PARENT + "}";

  public static final String PATH_PARAM_MODULE_ID = "module_id";
  public static final String PATH_MODULE_ID = "/{" + PATH_PARAM_MODULE_ID + "}";

  public static final String PATH_PARAM_PERSON_ID = "person_id";
  public static final String PATH_PERSON_ID = "/{" + PATH_PARAM_PERSON_ID + "}";

  public static final String PATH_PARAM_PIPELINE_ID = "pipeline_id";
  public static final String PATH_PIPELINE_ID = "/{" + PATH_PARAM_PIPELINE_ID + "}";

  public static final String PATH_PARAM_RESOURCE = "resource";
  public static final String PATH_RESOURCE = "/{" + PATH_PARAM_RESOURCE + "}";

  public static final String PATH_PARAM_RESOURCE_TYPE = "resource_type";
  public static final String PATH_RESOURCE_TYPE = "/{" + PATH_PARAM_RESOURCE_TYPE + "}";

  public static final String PATH_PARAM_VERSION = "version";
  public static final String PATH_VERSION = "/{" + PATH_PARAM_VERSION + "}";

  /*
   * *********************************
   * Now Path for Different Resources.
   * *********************************
   */

  // Path for Collection Resource
  public static final String RESOURCE_PATH_COLLECTION = "/collection";
  public static final String URI_RESOURCE_PATH_COLLECTION = JERSEY_CONTEXT
      + RESOURCE_PATH_COLLECTION;

  public static final String PATH_MODULE_VERSION = PATH_MODULE_ID + PATH_VERSION;
  public static final String PATH_MODULE_VERSION_CONTENT = PATH_MODULE_VERSION + "/"
      + CONTENT;
  public static final String PATH_MODULE_RESOURCE = PATH_MODULE_VERSION + PATH_RESOURCE_TYPE
      + PATH_RESOURCE;

  // Path for Miscallaneous Admin Resource.
  public static final String RESOURCE_PATH_MISC_ADMIN = GAE_ADMIN_CONTEXT;
  public static final String URI_RESOURCE_PATH_MISC_ADMIN = JERSEY_CONTEXT
      + RESOURCE_PATH_MISC_ADMIN;
  public static final String PATH_CONFIG = "/config";
  public static final String URI_MISC_ADMIN_CONFIG = URI_RESOURCE_PATH_MISC_ADMIN + PATH_CONFIG;

  // Path for GAE Pipeline Resource.
  public static final String RESOURCE_PATH_GAE_PIPELINE = GAE_ADMIN_CONTEXT + "/gae_pipeline";
  public static final String URI_RESOURCE_PATH_GAE_PIPELINE = JERSEY_CONTEXT
      + RESOURCE_PATH_GAE_PIPELINE;

  // Path for Gogole Doc Integration.
  public static final String RESOURCE_PATH_THIRD_PARTY_GOOGLE_DOC = "/thirdparty/google/gdoc";
  public static final String URI_RESOURCE_PATH_THIRD_PARTH_GOOGLE_DOC = JERSEY_CONTEXT
      + RESOURCE_PATH_THIRD_PARTY_GOOGLE_DOC;

  public static final String PATH_GOOGLE_DOC_LIST = "/list";
  public static final String URI_GOOGLE_DOC_LIST = JERSEY_CONTEXT +
      RESOURCE_PATH_THIRD_PARTY_GOOGLE_DOC + PATH_GOOGLE_DOC_LIST;

  public static final String PATH_GOOGLE_DOC_INFO = "/info" + PATH_EXTERNAL_KEY;
  public static final String PATH_GOOGLE_FOLDER_INFO = "/info/folder" + PATH_EXTERNAL_KEY;

  public static final String PATH_GOOGLE_DOC_IMPORT_POST = "/import";
  public static final String URI_GOOGLE_DOC_IMPORT_POST = URI_RESOURCE_PATH_THIRD_PARTH_GOOGLE_DOC
      + PATH_GOOGLE_DOC_IMPORT_POST;

  public static final String PATH_GOOGLE_DOC_IMPORT_PUT = PATH_GOOGLE_DOC_IMPORT_POST
      + PATH_EXTERNAL_KEY;

  // Path for Job Resource
  public static final String RESOURCE_JOB = "/job";
  public static final String URI_RESOURCE_JOB = JERSEY_CONTEXT + RESOURCE_JOB;

  // Path for Module Resources.
  public static final String RESOURCE_PATH_MODULE = "/module";
  public static final String URI_RESOURCE_PATH_MODULE = JERSEY_CONTEXT + RESOURCE_PATH_COLLECTION;

  public static final String PATH_COLLECTION_VERSION = PATH_COLLECTION_ID + PATH_VERSION;
  public static final String PATH_COLLECTION_VERSION_CONTENT = PATH_COLLECTION_VERSION + "/"
      + CONTENT;

  // Path for Notification Resources.
  public static final String RESOURCE_PATH_NOTIFICATION = "/notification";
  public static final String URI_RESOURCE_PATH_NOTIFICATION = JERSEY_CONTEXT
      + RESOURCE_PATH_NOTIFICATION;
  
  public static final String PATH_NOTIFICATION_JOB = "/job";
  public static final String URI_RESOURCE_PATH_NOTIFICATION_JOB = URI_RESOURCE_PATH_NOTIFICATION
      + PATH_NOTIFICATION_JOB;

  
  
  // Path for Person Resource.
  public static final String RESOURCE_PATH_PERSON = "/person";
  public static final String PATH_PERSON = PATH_PERSON_ID;

  // Path for Test Resource
  public static final String RESOURCE_PATH_TEST = "/test";
  public static final String URI_RESOURCE_PATH_TEST = JERSEY_CONTEXT + RESOURCE_PATH_TEST;

  public static final String PATH_SESSION = "/session";
  public static final String URI_TEST_SESSION = URI_RESOURCE_PATH_TEST + PATH_SESSION;

  public static final String PATH_TEST_LINKS = "/links";
  public static final String URI_TEST_LINKS = URI_RESOURCE_PATH_TEST + PATH_TEST_LINKS;

  // Some getter methods.
  public static String getJerseyContext() {
    return JERSEY_CONTEXT;
  }

  public static String getAdminContext() {
    return GAE_ADMIN_CONTEXT;
  }
}
