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
package com.google.light.server.servlets.jobs;

import static com.google.light.server.constants.RequestParamKeyEnum.PIPELINE_ID;
import static com.google.light.server.utils.ServletUtils.getRequestParameterValue;

import com.google.appengine.tools.pipeline.JobInfo;
import com.google.appengine.tools.pipeline.PipelineService;
import com.google.appengine.tools.pipeline.PipelineServiceFactory;
import com.google.light.server.constants.http.ContentTypeEnum;
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
public class PipelineStatusServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) {
    try {
      PipelineService service = PipelineServiceFactory.newPipelineService();

      String pipelineId = getRequestParameterValue(request, PIPELINE_ID);
      JobInfo jobInfo = service.getJobInfo(pipelineId);

      StringBuilder builder = new StringBuilder();
      switch (jobInfo.getJobState()) {
        case COMPLETED_SUCCESSFULLY:
          builder.append("completed.");
          builder.append("\n Output = " + jobInfo.getOutput());
          service.deletePipelineRecords(pipelineId);
          break;

        case STOPPED_BY_ERROR:
          builder.append("error");
          builder.append("\n Reason = " + jobInfo.getError());
          break;

        case RUNNING:
          builder.append("still running");
          break;

        case STOPPED_BY_REQUEST:
          builder.append("stopped by request.");
          break;

        case WAITING_TO_RETRY:
          builder.append("waiting to retry.");
          break;
      }

      response.setContentType(ContentTypeEnum.TEXT_PLAIN.get());
      response.getWriter().println(builder.toString());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
