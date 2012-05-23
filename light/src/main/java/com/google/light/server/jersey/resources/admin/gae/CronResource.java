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
package com.google.light.server.jersey.resources.admin.gae;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkIsUnderCron;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.light.server.constants.JerseyConstants;
import com.google.light.server.jersey.resources.AbstractJerseyResource;
import com.google.light.server.manager.interfaces.QueueManager;
import com.google.light.server.utils.GaeUtils;
import com.google.light.server.utils.ObjectifyUtils;
import com.google.light.server.utils.Transactable;
import com.googlecode.objectify.Objectify;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * Handler for cron tasks.
 * 
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */
@Path(JerseyConstants.RESOURCE_PATH_CRON)
public class CronResource extends AbstractJerseyResource {
  private static final Logger logger = Logger.getLogger(CronResource.class.getName());
  
  private QueueManager queueManager;
  
  @Inject
  public CronResource(Injector injector, HttpServletRequest request, HttpServletResponse response,
      QueueManager queueManager) {
    super(injector, request, response);
    this.queueManager = checkNotNull(queueManager, "queueManager");
    
    if (!GaeUtils.isUserAdmin()) {
      checkIsUnderCron(request);
    }
  }

  @GET
  @Path(JerseyConstants.PATH_CRON_SEARCH_INDEX_REFRESH)
  public Response refreshSearchIndex() {
    ObjectifyUtils.repeatInTransaction(new Transactable<Void>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Void run(Objectify ofy) {
        queueManager.enqueueSearchIndexTask(ofy);
        queueManager.enqueueSearchIndexGSSTask(ofy);
        return null;
      }
    });
    
    logger.info("Successfully enqueued a task to update search-index.");
    return Response.ok("Task scheduled for updating Search Indices.").build();
  }
}
