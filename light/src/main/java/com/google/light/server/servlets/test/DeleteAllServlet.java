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
package com.google.light.server.servlets.test;

import static com.google.appengine.api.datastore.Entity.KEY_RESERVED_PROPERTY;
import static com.google.light.server.constants.LightConstants.JOB_BACK_OFF_FACTOR;
import static com.google.light.server.constants.LightConstants.JOB_BACK_OFF_SECONDS;
import static com.google.light.server.constants.LightConstants.JOB_MAX_ATTEMPTES;
import static com.google.light.server.utils.GuiceUtils.getRequestScopedValues;

import com.google.light.server.utils.ObjectifyUtils;

import com.google.light.server.dto.pojo.LightJobContextPojo;
import com.google.light.server.dto.pojo.RequestScopedValues;

import java.util.List;

import com.google.common.collect.Lists;

import com.google.appengine.api.datastore.Entity;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;

import com.google.appengine.api.datastore.Query;

import com.google.appengine.tools.pipeline.PipelineService;
import com.google.appengine.tools.pipeline.PipelineServiceFactory;
import com.google.light.server.manager.interfaces.JobManager;
import com.google.light.server.persistence.entity.jobs.JobEntity;
import com.google.light.server.persistence.entity.jobs.JobEntity.JobHandlerType;
import com.google.light.server.persistence.entity.jobs.JobEntity.JobState;
import com.google.light.server.persistence.entity.jobs.JobEntity.JobType;
import com.google.light.server.servlets.test.jobs.DeleteJobs;
import com.google.light.server.utils.GuiceUtils;
import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class DeleteAllServlet extends HttpServlet {
  private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // First deleting all the existing Pipelines.

    List<String> list = Lists.newArrayList("pipeline-job", "pipeline-barrier",
        "pipeline-fanoutTask", "pipeline-jobInstanceRecord", "pipeline-slot");

    for (String curr : list) {
      Query query =
          new Query(curr)
              .addFilter(KEY_RESERVED_PROPERTY, Query.FilterOperator.GREATER_THAN_OR_EQUAL,
                  DeleteJobs.makeKindKey("a"))
              .setKeysOnly();

      for (Entity e : datastore.prepare(query).asIterable()) {
        datastore.delete(e.getKey());
      }
    }

    JobManager jobManager = GuiceUtils.getInstance(JobManager.class);
    // First creating a Job from ImportJob without PipelineId.
    JobEntity jobEntity = new JobEntity.Builder()
        .jobHandlerType(JobHandlerType.PIPELINE)
        .jobType(JobType.DELETE)
        .taskId("some task id")
        .jobState(JobState.PRE_START)
        .build();
    @SuppressWarnings("unused")
    JobEntity savedJobEntity = jobManager.put(ObjectifyUtils.nonTransaction(), jobEntity);

    RequestScopedValues participants = getRequestScopedValues();

    LightJobContextPojo context = new LightJobContextPojo.Builder()
        .ownerId(participants.getOwnerId())
        .actorId(participants.getActorId())
        .jobEntity(jobEntity)
        .build();

    PipelineService service = PipelineServiceFactory.newPipelineService();
    service.startNewPipeline(new DeleteJobs.CreateDeleteJobs(), context, JOB_BACK_OFF_FACTOR,
        JOB_BACK_OFF_SECONDS, JOB_MAX_ATTEMPTES);
  }
}
