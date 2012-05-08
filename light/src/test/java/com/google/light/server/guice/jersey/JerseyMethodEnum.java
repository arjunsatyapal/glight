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
package com.google.light.server.guice.jersey;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;

import com.google.light.server.jersey.resources.job.JobResource;

import com.google.light.server.jersey.resources.notifications.NotificationResource;

import com.google.light.server.jersey.resources.CollectionResource;

import com.google.light.server.jersey.resources.test.TestResources;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.light.server.jersey.resources.AbstractJerseyResource;
import com.google.light.server.jersey.resources.ModuleResource;
import com.google.light.server.jersey.resources.admin.gae.GAEAdminResources;
import com.google.light.server.jersey.resources.admin.gae.GAEPipelineResource;
import com.google.light.server.jersey.resources.thirdparty.google.GoogleDocIntegration;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;

/**
 * 
 * TODO(arjuns) : Add test for consumes.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("rawtypes")
public enum JerseyMethodEnum {
  // Collection Resource Jersey Methods.
  COLLECTION_RESOURCE_GET_COLLECTION(
                                     CollectionResource.class,
                                     "getCollection",
                                     new Class[] {},
                                     GET.class,
                                     "/rest/collection/{collection_id}",
                                     new String[] {}),

  COLLECTION_RESOURCE_GET_COLLECTION_VERSION(
                                             CollectionResource.class,
                                             "getCollectionVersion",
                                             new Class[] { String.class, String.class },
                                             GET.class,
                                             "/rest/collection/{collection_id}/{version}",

                                             new String[] { "application/json; charset=UTF-8",
                                                     "application/xml; charset=UTF-8" }),

  COLLECTION_RESOURCE_GET_COLLECTION_VERSION_CONTENT(
                                                     CollectionResource.class,
                                                     "getCollectionVersionContent",
                                                     new Class[] { String.class, String.class },
                                                     GET.class,
                                                     "/rest/collection/{collection_id}/{version}/content",
                                                     new String[] { "text/html; charset=UTF-8" }),

  COLLECTION_RESOURCE_COLLECTIONS_PUBLISHED_BY_ME(
                                              CollectionResource.class,
                                              "getCollectionsPublishedByMe",
                                              new Class[] { String.class, String.class },
                                              GET.class,
                                              "/rest/collection/me",
                                              new String[] { "application/json; charset=UTF-8",
                                                      "application/xml; charset=UTF-8" }),

  // GAEPipeline Resource Jersey Methods.
  GAE_PIPELINE_RESOURCE_STATUS_GET(
                                   GAEPipelineResource.class,
                                   "getGAEPipelineStatus",
                                   new Class[] { String.class },
                                   GET.class,
                                   "/rest/gaeadmin/gae_pipeline/{pipeline_id}",
                                   new String[] { "text/plain; charset=UTF-8" }),
  // GAEAdmin Resource Jersey Methods.
  GAE_ADMIN_RESOURCE_GET_CONFIG(
                                GAEAdminResources.class,
                                "getConfig",
                                new Class[] {},
                                GET.class,
                                "/rest/gaeadmin/config",
                                new String[] { "text/html; charset=UTF-8" }),

  // Module Resource Jersey Methods.
  MODULE_RESOURCE_GET_MODULE(
                             ModuleResource.class,
                             "getModule",
                             new Class[] {},
                             GET.class,
                             "/rest/module/{module_id}",

                             new String[] { "application/json; charset=UTF-8",
                                     "application/xml; charset=UTF-8" }),

  MODULE_RESOURCE_GET_MODULE_VERSION(
                                     ModuleResource.class,
                                     "getModuleVersion",
                                     new Class[] { String.class, String.class },
                                     GET.class,
                                     "/rest/module/{module_id}/{version}",

                                     new String[] { "application/json; charset=UTF-8",
                                             "application/xml; charset=UTF-8" }),

  MODULE_RESOURCE_GET_MODULE_VERSION_CONTENT(
                                             ModuleResource.class,
                                             "getModuleVersionContent",
                                             new Class[] { String.class, String.class },
                                             GET.class,
                                             "/rest/module/{module_id}/{version}/content",
                                             new String[] { "text/html; charset=UTF-8" }),

  MODULE_RESOURCE_GET_MODULE_VERSION_RESOURCES(
                                               ModuleResource.class,
                                               "getModuleVersionResources",
                                               new Class[] { String.class, String.class,
                                                       String.class, String.class },
                                               GET.class,
                                               "/rest/module/{module_id}/{version}/{resource_type}/{resource}",

                                               new String[] { "application/json; charset=UTF-8",
                                                       "application/xml; charset=UTF-8" }),
  MODULE_RESOURCE_MODULES_PUBLISHED_BY_ME(
                                          ModuleResource.class,
                                          "getModulesPublishedByMe",
                                          new Class[] { String.class, String.class },
                                          GET.class,
                                          "/rest/module/me",
                                          new String[] { "application/json; charset=UTF-8",
                                                  "application/xml; charset=UTF-8" }),
  // Google Doc Integration Jersey Methods.
  GOOGLE_DOC_GET_DOC_LIST(
                          GoogleDocIntegration.class,
                          "getDocList",
                          new Class[] { String.class, String.class },
                          GET.class,
                          "/rest/thirdparty/google/gdoc/list",

                          new String[] { "application/json; charset=UTF-8",
                                  "application/xml; charset=UTF-8" }),
  GOOGLE_DOC_GET_DOC_INFO(
                          GoogleDocIntegration.class,
                          "getDocInfo",
                          new Class[] { String.class },
                          GET.class,
                          "/rest/thirdparty/google/gdoc/info/{external_key}",
                          new String[] { "application/json; charset=UTF-8",
                                  "application/xml; charset=UTF-8" }),

  GOOGLE_DOC_IMPORT_GOOGLE_DOC_BATCH_FROM_POST(
                                               GoogleDocIntegration.class,
                                               "importGoogleDocBatchFormPost",
                                               new Class[] { String.class },
                                               POST.class,
                                               "/rest/thirdparty/google/gdoc/import",
                                               new String[] {}),

  GOOGLE_DOC_IMPORT_GOOGLE_DOC_POST(
                                    GoogleDocIntegration.class,
                                    "importGoogleDocBatchPost",
                                    new Class[] { String.class },
                                    POST.class,
                                    "/rest/thirdparty/google/gdoc/import",
                                    new String[] { "application/json; charset=UTF-8",
                                            "application/xml; charset=UTF-8" }),

  GOOGLE_DOC_IMPORT_GOOGLE_DOC_PUT(
                                   GoogleDocIntegration.class,
                                   "importGoogleDocBatchPut",
                                   new Class[] { String.class },
                                   PUT.class,
                                   "/rest/thirdparty/google/gdoc/import/{external_key}",

                                   new String[] { "application/json; charset=UTF-8",
                                           "application/xml; charset=UTF-8" }),
  GOOGLE_DOC_GET_FOLDER_CONTENTS(
                                 GoogleDocIntegration.class,
                                 "getFolderContents",
                                 new Class[] { String.class, String.class },
                                 GET.class,
                                 "/rest/thirdparty/google/gdoc/info/folder/{external_key}",
                                 new String[] { "application/json; charset=UTF-8",
                                         "application/xml; charset=UTF-8" }),

  // Job Resource
  JOB_RESOURCE_START_JOB(
                         JobResource.class,
                         "startJob",
                         new Class[] { String.class },
                         PUT.class,
                         "/rest/job/{job_id}",
                         new String[] {}),

  JOB_RESOURCE_GET_JOB_STATUS(
                              JobResource.class,
                              "getJobStatus",
                              new Class[] { String.class },
                              GET.class,
                              "/rest/job/{job_id}",
                              new String[] { "text/plain; charset=UTF-8" }),
  // Notification Resource
  NOTIFICATION_RESOURCE_CHILD_COMPLETION_EVENT(
                                               NotificationResource.class,
                                               "childCompletionEvent",
                                               new Class[] { String.class },
                                               POST.class,
                                               "/rest/notification/job",
                                               new String[] {}),

  // Test Resource Jersey Methods.
  TEST_RESOURCE_SESSION(
                        TestResources.class,
                        "getSessionDetails",
                        new Class[] {},
                        GET.class,
                        "/rest/test/session",
                        new String[] { "text/html; charset=UTF-8" }),
  TEST_RESOURCE_LINKS(
                      TestResources.class,
                      "getTestLinks",
                      new Class[] {},
                      GET.class,
                      "/rest/test/links",
                      new String[] { "text/html; charset=UTF-8" }),

  ;

  private Class<? extends AbstractJerseyResource> clazz;
  private String methodName;

  private Class<?>[] parameterTypes;

  private Class jerseyMethodAnnotation;
  private String path;
  private String[] produces;

  private JerseyMethodEnum(Class<? extends AbstractJerseyResource> clazz, String methodName,
      Class[] parameterTypes, Class jerseyMethodAnnotation, String path, String[] produces) {
    this.clazz = checkNotNull(clazz, "clazz");
    this.methodName = checkNotBlank(methodName, "empty method name.");
    this.parameterTypes = parameterTypes;
    this.jerseyMethodAnnotation = jerseyMethodAnnotation;
    this.path = path;
    this.produces = produces;
  }

  public Method getMethod() throws SecurityException, NoSuchMethodException {
    return clazz.getMethod(getMethodName(), parameterTypes);
  }

  public static Set<Class<? extends AbstractJerseyResource>> getSetOfJerseyResources() {
    Set<Class<? extends AbstractJerseyResource>> set = Sets.newHashSet();

    for (JerseyMethodEnum curr : JerseyMethodEnum.values()) {
      set.add(curr.getClazz());
    }

    return set;
  }

  public static JerseyMethodEnum getEnum(Class<? extends AbstractJerseyResource> clazz,
      String methodName, Class<?>[] parameterTypes) {
    // checkArgument(clazz.isAssignableFrom(AbstractJerseyResource.class));

    for (JerseyMethodEnum curr : JerseyMethodEnum.values()) {
      if (curr.clazz.getName() == clazz.getName() &&
          curr.getMethodName().equals(methodName)) {
        ArrayList<Class<?>> currList = Lists.newArrayList(curr.getParameterTypes());
        ArrayList<Class<?>> passList = Lists.newArrayList(parameterTypes);
        if (currList.equals(passList)) {
          return curr;
        }
      }
    }
    return null;
  }

  public Class<? extends AbstractJerseyResource> getClazz() {
    return clazz;
  }

  public String getMethodName() {
    return methodName;
  }

  public Class<?>[] getParameterTypes() {
    return parameterTypes;
  }

  public Class getJerseyMethodAnnotation() {
    return jerseyMethodAnnotation;
  }

  public String getPath() {
    return path;
  }

  public String[] getProduces() {
    return produces;
  }
}
