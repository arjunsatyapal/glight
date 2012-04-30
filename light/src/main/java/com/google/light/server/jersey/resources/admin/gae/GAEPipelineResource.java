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

import com.google.light.server.jersey.resources.AbstractJerseyResource;

import com.google.appengine.tools.pipeline.JobInfo;
import com.google.appengine.tools.pipeline.PipelineService;
import com.google.appengine.tools.pipeline.PipelineServiceFactory;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.light.server.constants.JerseyConstants;
import com.google.light.server.constants.http.ContentTypeConstants;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@Path(JerseyConstants.RESOURCE_PATH_GAE_PIPELINE)
public class GAEPipelineResource extends AbstractJerseyResource {

  @Inject
  public GAEPipelineResource(Injector injector, HttpServletRequest request,
      HttpServletResponse response) {
    super(injector, request, response);
  }

  
  @GET
  @Path(JerseyConstants.PATH_PIPELINE_ID)
  @Produces(ContentTypeConstants.TEXT_PLAIN)
  public String getGAEPipelineStatus(
      @PathParam(JerseyConstants.PATH_PARAM_PIPELINE_ID) String pipelineId) {
    try {
      PipelineService service = PipelineServiceFactory.newPipelineService();
      JobInfo jobInfo = service.getJobInfo(pipelineId);

      Preconditions.checkNotNull(jobInfo, "JobInfo for PipelineId[" + pipelineId 
          + "] was not found.");
      StringBuilder builder = new StringBuilder();
      
      builder.append("\n Job State = " + jobInfo.getJobState());
      builder.append("\n Job Output = " + jobInfo.getOutput());
      builder.append("\n Details = " + jobInfo.toString());

      return builder.toString();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
