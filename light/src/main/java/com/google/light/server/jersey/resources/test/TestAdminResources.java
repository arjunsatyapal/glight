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
package com.google.light.server.jersey.resources.test;

import static com.google.light.server.utils.LightPreconditions.checkIsNotEnv;

import com.google.light.server.utils.GaeUtils;

import com.google.light.server.utils.GuiceUtils;

import com.google.light.server.manager.interfaces.FTSManager;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.light.server.constants.QueueEnum;

import com.google.light.server.jersey.resources.AbstractJerseyResource;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.light.server.constants.LightEnvEnum;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.QueryResultIterable;
import com.google.common.collect.Lists;
import com.google.light.server.constants.JerseyConstants;
import com.google.light.server.constants.http.ContentTypeConstants;
import com.google.light.server.persistence.entity.collection.CollectionEntity;
import com.google.light.server.persistence.entity.collection.CollectionVersionEntity;
import com.google.light.server.persistence.entity.jobs.JobEntity;
import com.google.light.server.persistence.entity.module.ExternalIdMappingEntity;
import com.google.light.server.persistence.entity.module.ModuleEntity;
import com.google.light.server.persistence.entity.module.ModuleVersionEntity;
import com.google.light.server.persistence.entity.module.ModuleVersionResourceEntity;
import com.google.light.server.utils.ObjectifyUtils;
import com.googlecode.objectify.Objectify;
import java.util.Iterator;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * Test resources for Admin.
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@Path(JerseyConstants.RESOURCE_PATH_TEST_ADMIN)
public class TestAdminResources extends AbstractJerseyResource {
  @Inject
  public TestAdminResources(Injector injector, HttpServletRequest request,
      HttpServletResponse response) {
    super(injector, request, response);
    checkIsNotEnv(this, LightEnvEnum.PROD);
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @GET
  @Produces(ContentTypeConstants.TEXT_PLAIN)
  @Path(JerseyConstants.PATH_TEST_DELETE_ALL)
  public String deleteAll() throws ClassNotFoundException {
    String appId = GaeUtils.getAppId();
    if (appId.equals("light-demo")) {
      throw new IllegalStateException("DeleteAll is not allowed for " + appId);
    }

    List<String> tableNames = Lists.newArrayList(
        ModuleEntity.class.getName(),
        ModuleVersionEntity.class.getName(),
        ModuleVersionResourceEntity.class.getName(),

        CollectionEntity.class.getName(),
        CollectionVersionEntity.class.getName(),

        ExternalIdMappingEntity.class.getName(),
        JobEntity.class.getName());

    StringBuilder builder = new StringBuilder("\nDeleting Tables.");
    Objectify ofy = ObjectifyUtils.nonTransaction();
    for (String currClassName : tableNames) {
      Class currClass = this.getClass().getClassLoader().loadClass(currClassName);
      builder.append("\n\n  Deleting " + currClass.getSimpleName());
      QueryResultIterable queryIterable = ofy.query(currClass).fetchKeys();
      Iterator iterator = queryIterable.iterator();
      while (iterator.hasNext()) {
        ofy.delete(iterator.next());
      }

      builder.append("\n  Successfully deleted " + currClass.getSimpleName());
    }

    builder.append(purgeQueue());
    
    builder.append(clearFTSIndex());

    return builder.toString();
  }
  
  /**
   * @return
   */
  private String clearFTSIndex() {
    FTSManager ftsManager = GuiceUtils.getInstance(FTSManager.class);
    return null;
  }

  @GET
  @Produces(ContentTypeConstants.TEXT_PLAIN)
  @Path(JerseyConstants.PATH_TEST_ADMIN_PURGE_QUEUES)
  public String purgeQueue() {
    StringBuilder builder = new StringBuilder("\n\n\nPurging Queues.");
    for (QueueEnum currQueue : QueueEnum.values()) {
      if (currQueue == QueueEnum.GAE_CRON_QUEUE) {
        continue;
      }
      builder.append("\n\n  Purging Queue " + currQueue.getName());
      Queue queue = QueueFactory.getQueue(currQueue.getName());
      queue.purge();
      builder.append("\n  Successfully Purged Queue " + currQueue.getName());
    }
    
    return builder.toString();
  }
}
