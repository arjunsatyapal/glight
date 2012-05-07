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
package com.google.light.server.jersey.resources.job;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkIsRunningUnderQueue;
import static com.google.light.server.utils.LightPreconditions.checkNotNull;

import com.google.light.server.utils.JsonUtils;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.light.server.constants.JerseyConstants;
import com.google.light.server.constants.http.ContentTypeConstants;
import com.google.light.server.dto.pojo.longwrapper.JobId;
import com.google.light.server.exception.ExceptionType;
import com.google.light.server.jersey.resources.AbstractJerseyResource;
import com.google.light.server.manager.interfaces.JobManager;
import com.google.light.server.persistence.entity.jobs.JobEntity;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 *
 * 
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */
@Path(JerseyConstants.RESOURCE_JOB)
public class JobResource extends AbstractJerseyResource {
  private JobManager jobManager;
  @Inject
  public JobResource(Injector injector, HttpServletRequest request, HttpServletResponse response,
      JobManager jobManager) {
    super(injector, request, response);
    this.jobManager = checkNotNull(jobManager, "jobManager");
  }

  /**
   * Job Creator Queue Posts request here.
   * @return
   */
  @PUT
  @Path(JerseyConstants.PATH_JOB_ID)
  public Response startJob(String jobIdStr) {
    checkIsRunningUnderQueue(request);
    checkNotNull(jobIdStr, "jobId cannot be null");
    
    JobId jobId = new JobId(jobIdStr);
    jobManager.handleJob(jobId);
    return Response.ok().build();
    
  }
  
  @GET
  @Produces(ContentTypeConstants.TEXT_PLAIN)
  @Path(JerseyConstants.PATH_JOB_ID)
  public String getJobStatus(
      @PathParam(JerseyConstants.PATH_PARAM_JOB_ID) String jobIdStr) {
    JobId jobId = new JobId(jobIdStr);
    JobEntity jobEntity = jobManager.get(null, jobId);
    checkNotNull(jobEntity, ExceptionType.CLIENT_PARAMETER, "Job [" + jobId + "] was not found.");
  
    String html = JsonUtils.toJson(jobEntity);
    return html;
  }
}
