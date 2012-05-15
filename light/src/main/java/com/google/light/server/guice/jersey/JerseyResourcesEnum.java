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
import static com.google.light.server.constants.JerseyConstants.URI_RESOURCE_PATH_MISC_ADMIN;
import static com.google.light.server.constants.JerseyConstants.URI_RESOURCE_PATH_MODULE;
import static com.google.light.server.constants.JerseyConstants.URI_RESOURCE_PATH_NOTIFICATION;
import static com.google.light.server.constants.JerseyConstants.URI_RESOURCE_PATH_TEST;
import static com.google.light.server.constants.JerseyConstants.URI_RESOURCE_PATH_THIRD_PARTH_GOOGLE_DOC;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;

import com.google.common.collect.Sets;
import com.google.light.server.constants.JerseyConstants;
import com.google.light.server.jersey.resources.AbstractJerseyResource;
import com.google.light.server.jersey.resources.CollectionResource;
import com.google.light.server.jersey.resources.ContentResource;
import com.google.light.server.jersey.resources.ModuleResource;
import com.google.light.server.jersey.resources.admin.gae.GAEAdminResources;
import com.google.light.server.jersey.resources.job.JobResource;
import com.google.light.server.jersey.resources.notifications.NotificationResource;
import com.google.light.server.jersey.resources.test.TestResources;
import com.google.light.server.jersey.resources.thirdparty.google.GoogleDocIntegration;
import com.google.light.server.jersey.resources.thirdparty.mixed.ImportResource;
import java.util.Set;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public enum JerseyResourcesEnum {
  COLLECTION_RESOURCE(CollectionResource.class, JerseyConstants.URI_RESOURCE_PATH_COLLECTION),
  CONTENT_RESOURCE(ContentResource.class, JerseyConstants.URI_RESOURCE_PATH_CONTENT),
  GOOGLE_DOC_RESOURCE(GoogleDocIntegration.class, URI_RESOURCE_PATH_THIRD_PARTH_GOOGLE_DOC),
  IMPORT_RESOURCE(ImportResource.class, JerseyConstants.URI_IMPORT_BATCH),
  JOB_RESOURCE(JobResource.class, JerseyConstants.URI_RESOURCE_JOB),
  MISC_ADMIN_RESOURCES(GAEAdminResources.class, URI_RESOURCE_PATH_MISC_ADMIN),
  MODULE_RESOURCE(ModuleResource.class, URI_RESOURCE_PATH_MODULE),
  NOTIFICATION_RESOURCE(NotificationResource.class, URI_RESOURCE_PATH_NOTIFICATION),
  TEST_RESOURCES(TestResources.class, URI_RESOURCE_PATH_TEST);

  private Class<? extends AbstractJerseyResource> clazz;
  private String lightUri;

  private JerseyResourcesEnum(Class<? extends AbstractJerseyResource> clazz, String lightUri) {
    this.clazz = checkNotNull(clazz, "clazz");
    this.lightUri = checkNotBlank(lightUri, "lightUri");
  }

  public Class<? extends AbstractJerseyResource> getClazz() {
    return clazz;
  }
  
  public String getLightUri() {
    return lightUri;
  }
  
  public static Set<Class<? extends AbstractJerseyResource>> getSetOfResources() {
    Set<Class<? extends AbstractJerseyResource>> set = Sets.newHashSet();
    
    for (JerseyResourcesEnum curr : JerseyResourcesEnum.values()) {
      set.add(curr.getClazz());
    }
    
    return set;
  }
}
