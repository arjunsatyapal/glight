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
  private static final String THIRD_PARTY_CONTEXT = "/thirdparty";

  public static final String PATH_PARAM_EXTERNAL_ID = "externalId";
  public static final String PATH_EXTERNAL_ID = "/{" + PATH_PARAM_EXTERNAL_ID + "}";

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

  public static final String PATH_ME = "/me";

  public static final String PATH_ME_HTML = PATH_ME + "/html";
  /*
   * *********************************
   * Now Path for Different Resources.
   * *********************************
   */

  // Path for Collection Resource
  public static final String RESOURCE_PATH_COLLECTION = "/collection";
  public static final String URI_RESOURCE_PATH_COLLECTION = JERSEY_CONTEXT
      + RESOURCE_PATH_COLLECTION;
  public static final String URI_RESOURCE_PATH_COLLECTION_ME = URI_RESOURCE_PATH_COLLECTION
      + PATH_ME;
  public static final String URI_RESOURCE_PATH_COLLECTION_ME_HTML = URI_RESOURCE_PATH_COLLECTION
      + PATH_ME_HTML;

  // Path for Collection Resources.
  public static final String PATH_COLLECTION_VERSION = PATH_COLLECTION_ID + PATH_VERSION;
  public static final String PATH_COLLECTION_VERSION_CONTENT = PATH_COLLECTION_VERSION + "/"
      + CONTENT;

  // Path for Content Resource
  public static final String PATH_CONTENT_COLLECTION = "/collection/{" + PATH_PARAM_COLLECTION_ID
      + "}";
  public static final String PATH_CONTENT_COLLECTION_VERSION = PATH_CONTENT_COLLECTION + "/{"
      + PATH_PARAM_VERSION + "}";
  public static final String PATH_CONTENT_MODULE = "/module/{" + PATH_PARAM_MODULE_ID + "}";
  public static final String PATH_CONTENT_MODULE_VERSION = PATH_CONTENT_MODULE + "/{"
      + PATH_PARAM_VERSION + "}";
  public static final String PATH_CONTENT_MODULE_VERSION_WITH_SLASH = PATH_CONTENT_MODULE_VERSION
      + "/";
  public static final String PATH_CONTENT_MODULE_VERSION_RESOURCE = PATH_CONTENT_MODULE_VERSION
      + PATH_RESOURCE_TYPE + PATH_RESOURCE;

  public static final String PATH_CONTENT_MODULE_IN_COLLECTION_VERSION =
      PATH_CONTENT_COLLECTION_VERSION + "/{" + PATH_PARAM_MODULE_ID + "}";
  public static final String PATH_CONTENT_MODULE_IN_COLLECTION_VERSION_WITH_SLASH =
      PATH_CONTENT_MODULE_IN_COLLECTION_VERSION + "/";
  public static final String PATH_CONTENT_MODULE_IN_COLLECTION_VERSION_RESOURCE =
      PATH_CONTENT_MODULE_IN_COLLECTION_VERSION + PATH_RESOURCE_TYPE
          + PATH_RESOURCE;

  public static final String RESOURCE_PATH_CONTENT = "/content/general";
  public static final String URI_RESOURCE_PATH_CONTENT = "/rest/content/general";

  // Path for Cron Resource.
  public static final String RESOURCE_PATH_CRON = GAE_ADMIN_CONTEXT + "/cron";
  public static final String URI_RESOURCE_PATH_CRON = JERSEY_CONTEXT + RESOURCE_PATH_CRON;

  public static final String PATH_CRON_SEARCH_INDEX_REFRESH = "/searchIndexRefresh";
  public static final String URI_CRON_SEARCH_INDEX_REFRESH = URI_RESOURCE_PATH_CRON
      + PATH_CRON_SEARCH_INDEX_REFRESH;

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
  public static final String RESOURCE_PATH_THIRD_PARTY_GOOGLE_DOC = THIRD_PARTY_CONTEXT
      + "/google/gdoc";
  public static final String URI_RESOURCE_PATH_THIRD_PARTH_GOOGLE_DOC = JERSEY_CONTEXT
      + RESOURCE_PATH_THIRD_PARTY_GOOGLE_DOC;

  public static final String PATH_GOOGLE_DOC_LIST = "/list";
  public static final String URI_GOOGLE_DOC_LIST = JERSEY_CONTEXT +
      RESOURCE_PATH_THIRD_PARTY_GOOGLE_DOC + PATH_GOOGLE_DOC_LIST;

  public static final String PATH_GOOGLE_DOC_INFO = "/info";
  public static final String PATH_GOOGLE_FOLDER_INFO = "/info/folder";

  public static final String PATH_GOOGLE_DOC_IMPORT_POST = "/import";
  public static final String URI_GOOGLE_DOC_IMPORT_POST = URI_RESOURCE_PATH_THIRD_PARTH_GOOGLE_DOC
      + PATH_GOOGLE_DOC_IMPORT_POST;

  public static final String PATH_GOOGLE_DOC_IMPORT_PUT = PATH_GOOGLE_DOC_IMPORT_POST
      + PATH_EXTERNAL_ID;

  // Path for Import Resource
  public static final String RESOURCE_IMPORT = "/import";
  public static final String URI_RESOURCE_IMPORT = JERSEY_CONTEXT + RESOURCE_IMPORT;

  public static final String PATH_IMPORT_BATCH = "/batch";
  public static final String URI_IMPORT_BATCH = URI_RESOURCE_IMPORT + PATH_IMPORT_BATCH;

  // Path for Job Resource
  public static final String RESOURCE_JOB = "/job";
  public static final String URI_RESOURCE_JOB = JERSEY_CONTEXT + RESOURCE_JOB;

  public static final String URI_RESOURCE_PATH_JOBS_ME = URI_RESOURCE_JOB + PATH_ME;

  public static final String PATH_JOB_DETAIL = PATH_JOB_ID + "/detail";
  public static final String URI_JOB_DETAIL = URI_RESOURCE_JOB + "/0/detail";

  // Path for Module Resources.
  public static final String RESOURCE_PATH_MODULE = "/module";
  public static final String URI_RESOURCE_PATH_MODULE = JERSEY_CONTEXT + RESOURCE_PATH_MODULE;
  public static final String URI_RESOURCE_PATH_MODULE_ME = URI_RESOURCE_PATH_MODULE + PATH_ME;
  public static final String URI_RESOURCE_PATH_MODULE_ME_HTML = URI_RESOURCE_PATH_MODULE
      + PATH_ME_HTML;

  public static final String PATH_MODULE_VERSION = PATH_MODULE_ID + PATH_VERSION;
  public static final String PATH_MODULE_VERSION_CONTENT = PATH_MODULE_VERSION + "/"
      + CONTENT;
  public static final String PATH_MODULE_VERSION_RESOURCE = PATH_MODULE_VERSION
      + PATH_RESOURCE_TYPE
      + PATH_RESOURCE;

  public static final String PATH_MODULE_SEARCH = "/search";
  public static final String URI_MODULE_SEARCH = URI_RESOURCE_PATH_MODULE + PATH_MODULE_SEARCH;

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

  // Path for Test Admin Resource
  public static final String RESOURCE_PATH_TEST_ADMIN = GAE_ADMIN_CONTEXT + "/test";
  public static final String URI_RESOURCE_PATH_TEST_ADMIN = JERSEY_CONTEXT
      + RESOURCE_PATH_TEST_ADMIN;

  public static final String PATH_TEST_DELETE_ALL = "/deleteall";
  public static final String URI_TEST_ADMIN_DELETE_ALL = URI_RESOURCE_PATH_TEST_ADMIN
      + PATH_TEST_DELETE_ALL;

  public static final String PATH_TEST_ADMIN_PURGE_QUEUES = "/purgequeues";
  public static final String URI_TEST_ADMIN_PURGE_QUEUES = URI_RESOURCE_PATH_TEST_ADMIN
      + PATH_TEST_ADMIN_PURGE_QUEUES;

  // Path for Search Resource
  public static final String RESOURCE_PATH_SEARCH_INDEX = GAE_ADMIN_CONTEXT + "/search";
  public static final String URI_RESOURCE_PATH_SEARCH_INDEX = JERSEY_CONTEXT
      + RESOURCE_PATH_SEARCH_INDEX;

  public static final String PATH_SEARCH_INDEX_UPDATE_INDICES = "/updateIndices";
  public static final String PATH_SEARCH_INDEX_UPDATE_INDICES_GSS = "/updateIndicesGSS";
  public static final String URI_SEARCH_INDEX_UPDATE_INDICES =
      URI_RESOURCE_PATH_SEARCH_INDEX + PATH_SEARCH_INDEX_UPDATE_INDICES;
  public static final String URI_SEARCH_INDEX_UPDATE_INDICES_GSS =
      URI_RESOURCE_PATH_SEARCH_INDEX + PATH_SEARCH_INDEX_UPDATE_INDICES_GSS;

  // Some getter methods.
  public static String getJerseyContext() {
    return JERSEY_CONTEXT;
  }

  public static String getAdminContext() {
    return GAE_ADMIN_CONTEXT;
  }
}
