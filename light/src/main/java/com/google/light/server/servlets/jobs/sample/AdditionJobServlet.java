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
package com.google.light.server.servlets.jobs.sample;

import static com.google.light.server.constants.RequestParamKeyEnum.PIPELINE_ID;
import static com.google.light.server.utils.ServletUtils.getRequestParameterValue;

import com.google.appengine.tools.pipeline.JobInfo;
import com.google.appengine.tools.pipeline.PipelineService;
import com.google.appengine.tools.pipeline.PipelineServiceFactory;
import com.google.light.server.constants.RequestParamKeyEnum;
import com.google.light.server.constants.http.ContentTypeEnum;
import com.google.light.server.jobs.Jobs;
import com.google.light.server.servlets.AbstractLightServlet;
import com.google.light.server.servlets.path.ServletPathEnum;
import java.util.logging.Logger;
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
public class AdditionJobServlet extends AbstractLightServlet {
  private static final Logger logger = Logger.getLogger(AdditionJobServlet.class.getName());
  private PipelineService service = PipelineServiceFactory.newPipelineService();

  /**
   * {@inheritDoc}
   */
  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response) {
    // TODO(arjuns): Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) {
    try {
      String pipelineId = getRequestParameterValue(request, PIPELINE_ID);

      if (pipelineId == null) {
        pipelineId = service.startNewPipeline(new Jobs.ComplexJob(), 3, 4, 6);
        logger.info("PipelineId = " + pipelineId);

        // Later, check on the status and get the final output
        JobInfo jobInfo = service.getJobInfo(pipelineId);
        JobInfo.State state = jobInfo.getJobState();
        if (JobInfo.State.COMPLETED_SUCCESSFULLY == state) {
          System.out.println("The output is " + jobInfo.getOutput());
        }

        response.setContentType(ContentTypeEnum.TEXT_PLAIN.get());
        response.getWriter().println(
            "Watch for pipelinId @: " + ServletPathEnum.ADDITION_JOB_SERVLET.get() + "?"
                + RequestParamKeyEnum.PIPELINE_ID.get() + "=" + pipelineId);
      } else {

        JobInfo jobInfo = service.getJobInfo(pipelineId);

        StringBuilder builder = new StringBuilder();
        switch (jobInfo.getJobState()) {
          case COMPLETED_SUCCESSFULLY:
            builder.append("completed.");
            builder.append("\n Output = " + jobInfo.getOutput());
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
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doHead(HttpServletRequest request, HttpServletResponse response) {
    // TODO(arjuns): Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doOptions(HttpServletRequest request, HttpServletResponse response) {
    // TODO(arjuns): Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) {

  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) {
    // TODO(arjuns): Auto-generated method stub
    throw new UnsupportedOperationException();
  }

}
