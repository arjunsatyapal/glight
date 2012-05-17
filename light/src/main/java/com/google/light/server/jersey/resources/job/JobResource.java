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
import static com.google.light.server.utils.LightPreconditions.checkPersonLoggedIn;
import static com.google.light.server.utils.LightUtils.getNow;
import static com.google.light.server.utils.LightUtils.getURI;
import static com.google.light.server.utils.LightUtils.getWrapperValue;

import com.google.light.server.persistence.entity.jobs.JobEntity.JobType;

import com.google.light.server.serveronlypojos.GAEQueryWrapper;

import com.google.light.server.servlets.SessionManager;
import com.google.light.server.utils.GuiceUtils;

import com.google.light.server.utils.LightUtils;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.light.server.constants.JerseyConstants;
import com.google.light.server.constants.http.ContentTypeConstants;
import com.google.light.server.constants.http.HttpStatusCodesEnum;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.JobId;
import com.google.light.server.exception.ExceptionType;
import com.google.light.server.exception.unchecked.taskqueue.TaskQueueRetriableException;
import com.google.light.server.jersey.resources.AbstractJerseyResource;
import com.google.light.server.jobs.handlers.JobHandler;
import com.google.light.server.manager.interfaces.JobManager;
import com.google.light.server.persistence.entity.jobs.JobEntity;
import com.google.light.server.persistence.entity.jobs.JobState;
import com.google.light.server.utils.HtmlBuilder;
import java.util.List;
import java.util.logging.Logger;
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
  private static final Logger logger = Logger.getLogger(JobResource.class.getName());

  private JobManager jobManager;
  private JobHandler jobHandler;

  List<String> listOfHeaders = Lists.newArrayList("JobId", "TaskType", "State", "CreationTime",
      "LastUpdateTime", "TimeToCompleteion(s)", "JobType", "ParentJob", "ChildJobs", "RootJob", "Owner");

  @Inject
  public JobResource(Injector injector, HttpServletRequest request, HttpServletResponse response,
      JobManager jobManager, JobHandler jobHandler) {
    super(injector, request, response);
    this.jobManager = checkNotNull(jobManager, "jobManager");
    this.jobHandler = checkNotNull(jobHandler, "jobHandler");
  }

  /**
   * Job Creator Queue Posts request here.
   * 
   * @return
   */
  @PUT
  @Path(JerseyConstants.PATH_JOB_ID)
  public Response startJob(String jobIdStr) {
    checkIsRunningUnderQueue(request);
    checkNotNull(jobIdStr, "jobId cannot be null");

    JobId jobId = new JobId(jobIdStr);
    try {
      jobHandler.handleJob(jobId);
      return Response.ok().build();
    } catch (TaskQueueRetriableException e) {
      logger.info("Retry after some time for " + jobId + " due to : " + e.getClass());
      return Response.status(HttpStatusCodesEnum.PRECONDITIONS_FAILED.getStatusCode()).build();
    }
  }

  @GET
  @Produces(ContentTypeConstants.TEXT_HTML)
  @Path(JerseyConstants.PATH_JOB_ID)
  public String getJobStatus(
      @PathParam(JerseyConstants.PATH_PARAM_JOB_ID) String jobIdStr) {
    JobId jobId = new JobId(jobIdStr);
    JobEntity jobEntity = jobManager.get(null, jobId);
    checkNotNull(jobEntity, ExceptionType.CLIENT_PARAMETER, "Job [" + jobId + "] was not found.");

    HtmlBuilder htmlBuilder = new HtmlBuilder();

    htmlBuilder.appendDoctype();
    htmlBuilder.appendHeadStart();
    htmlBuilder.appendStyle(getTableStyle());
    htmlBuilder.appendHeadEnd();

    htmlBuilder.appendBodyStart();

    // Appending Parent.
    htmlBuilder.appendSectionHeader("Parent");
    appendParent(jobEntity, htmlBuilder);

    // Appending Self.
    htmlBuilder.appendSectionHeader("Current : " + jobId);
    appendSelf(jobId, htmlBuilder);

    // appending childs
    htmlBuilder.appendSectionHeader("Childs");
    appendChilds(jobId, htmlBuilder);

    htmlBuilder.appendBodyEnd();

    return htmlBuilder.toString();
  }

  /**
   * @param jobEntity
   * @param htmlBuilder
   */
  private void appendChilds(JobId jobId, HtmlBuilder htmlBuilder) {
    JobEntity jobEntity = jobManager.get(null, jobId);
    if (jobEntity.hasChildJobs()) {
      htmlBuilder.appendSectionHeader("Childs for : " + jobEntity.getJobId());
      appendTableAndHeader(htmlBuilder);

      for (JobId currChild : jobEntity.getChildJobs()) {
        appendJobDetailRow(currChild, htmlBuilder);
      }

      htmlBuilder.appendTableEnd();
      htmlBuilder.appendNewLine();

      for (JobId currChild : jobEntity.getChildJobs()) {
        JobEntity tempEntity = jobManager.get(null, currChild);
        if (tempEntity.hasChildJobs()) {
          appendChilds(currChild, htmlBuilder);
        }
      }
    }
  }

  /**
   * @param jobEntity
   * @param htmlBuilder
   */
  private void appendParent(JobEntity jobEntity, HtmlBuilder htmlBuilder) {
    if (jobEntity.getParentJobId() == null) {
      return;
    }

    appendSelf(jobEntity.getParentJobId(), htmlBuilder);
  }

  private void appendSelf(JobId jobId, HtmlBuilder htmlBuilder) {
    appendTableAndHeader(htmlBuilder);
    appendJobDetailRow(jobId, htmlBuilder);
    htmlBuilder.appendTableEnd();
    htmlBuilder.appendNewLine();
  }

  private void appendTableAndHeader(HtmlBuilder htmlBuilder) {
    htmlBuilder.appendTableStart(getTableStyle());
    htmlBuilder.appendTableHeaderRow(listOfHeaders);
  }

  private void appendJobDetailRow(JobId jobId, HtmlBuilder htmlBuilder) {
    JobEntity jobEntity = jobManager.get(null, jobId);
    convertJobToTableRows(htmlBuilder, jobEntity);
  }

  /**
   * @param htmlBuilder
   */
  private void convertJobToTableRows(HtmlBuilder htmlBuilder, JobEntity jobEntity) {
    List<Object> list = Lists.newArrayList();

    list.add(getWrapperValue(jobEntity.getJobId()));

    list.add(jobEntity.getTaskType());
    list.add(jobEntity.getJobState());
    list.add(jobEntity.getCreationTime());
    list.add(jobEntity.getLastUpdateTime());

    if (jobEntity.getJobState() == JobState.COMPLETE) {
      long timeToComplete =
          jobEntity.getLastUpdateTime().getMillis() - jobEntity.getCreationTime().getMillis();
      list.add(timeToComplete / 1000);
    } else {
      long runningTime = getNow().getMillis() - jobEntity.getCreationTime().getMillis(); 
      list.add(jobEntity.getJobState().getCategory() + "(" + runningTime / 1000 + ")");
    }

    list.add(jobEntity.getJobType());
    list.add(jobEntity.getParentJobId());
    list.add(Iterables.toString(jobEntity.getChildJobs()));
    list.add(getWrapperValue(jobEntity.getRootJobId()));
    list.add(getWrapperValue(jobEntity.getOwnerId()));

    htmlBuilder.appendTableRow(list);
  }

  private String getTableStyle() {
    StringBuilder builder = new StringBuilder();
    builder.append("table {").append("\n")
        .append("border-collapse : collapse;").append("\n")
        .append("width : 100%;").append("\n")
        .append("}").append("\n")
        .append("table, th, td {").append("\n")
        .append("border: 1px solid black;'").append("\n")
        .append("border-collapse:collapse;'").append("\n")
        .append("width: 100%;").append("\n")
        .append("word-break : break-all;").append("\n")
        .append("table-layout: fixed;").append("\n")
        .append("}");

    return builder.toString();
  }
  
  
  @GET
  @Produces(ContentTypeConstants.TEXT_HTML)
  @Path(JerseyConstants.PATH_ME)
  public String getJobsCreatedByMe() {
    SessionManager sessionManager = GuiceUtils.getInstance(SessionManager.class);
    checkPersonLoggedIn(sessionManager);
    
    GAEQueryWrapper<JobEntity> wrapper = jobManager.findJobsCreatedByMe(
        GuiceUtils.getOwnerId(), null, 5000);
    
    HtmlBuilder htmlBuilder = new HtmlBuilder();
    htmlBuilder.appendSectionHeader("My Jobs : ");

    int counter = 0;
    for (JobEntity curr : wrapper.getList()) {
      Long value = getWrapperValue(curr.getJobId());
      String item = "" + value;
      if (curr.getJobType() == JobType.ROOT_JOB) {
        item = item + " (root)";
      }
      
      counter++;
      htmlBuilder.appendString("" + counter);
      htmlBuilder.appendHref(null, getURI("/rest/job/" + value), item);
      htmlBuilder.appendNewLine();
    }
    
    return htmlBuilder.toString();
  }
}
