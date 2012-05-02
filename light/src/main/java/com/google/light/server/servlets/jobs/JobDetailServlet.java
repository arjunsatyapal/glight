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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.GuiceUtils.getInstance;
import static com.google.light.server.utils.LightPreconditions.checkValidSession;
import static com.google.light.server.utils.LightUtils.appendKeyValue;
import static com.google.light.server.utils.LightUtils.appendLine;
import static com.google.light.server.utils.LightUtils.appendSectionHeader;
import static com.google.light.server.utils.ServletUtils.getRequestParameterValue;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.light.server.constants.RequestParamKeyEnum;
import com.google.light.server.constants.http.ContentTypeEnum;
import com.google.light.server.dto.pojo.ChangeLogEntryPojo;
import com.google.light.server.exception.unchecked.httpexception.NotFoundException;
import com.google.light.server.manager.interfaces.ImportManager;
import com.google.light.server.manager.interfaces.JobManager;
import com.google.light.server.persistence.entity.jobs.JobEntity;
import com.google.light.server.persistence.entity.queue.importflow.ImportJobEntity;
import com.google.light.server.servlets.AbstractLightServlet;
import com.google.light.server.servlets.SessionManager;
import com.google.light.server.utils.LightPreconditions;
import com.google.light.server.utils.LightUtils;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet to handle Imports from External integrations.
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class JobDetailServlet extends AbstractLightServlet {
  private JobManager jobManager;
  private ImportManager importManager1;
  private SessionManager sessionManager;

  @Inject
  public JobDetailServlet(Injector injector) {
    checkNotNull(injector, "injector");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void service(HttpServletRequest request, HttpServletResponse response) {
    // First verify that this is called by a Queue.
    jobManager = getInstance(JobManager.class);
    importManager1 = getInstance(ImportManager.class);
    sessionManager = getInstance(SessionManager.class);
    checkValidSession(sessionManager);

    super.service(request, response);
  }

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
    String jobIdStr = getRequestParameterValue(request, RequestParamKeyEnum.JOB_ID);
    Long jobId = Long.parseLong(jobIdStr);
    LightPreconditions.checkPositiveLong(jobId, "jobId");

    JobEntity jobEntity = jobManager.get(null, jobId);
    if (jobEntity == null) {
      throw new NotFoundException("ImportEntity[" + jobId + "], not found.");
    }

    StringBuilder strBuilder = new StringBuilder();
    appendSectionHeader(strBuilder, "Job Handler Details");
    appendKeyValue(strBuilder, "Status", jobEntity.getJobState());

    switch (jobEntity.getJobState()) {
      case COMPLETED_SUCCESSFULLY:
        break;

      case RUNNING:
        break;

      case STOPPED_BY_ERROR:
        appendKeyValue(strBuilder, "Error", jobEntity.getStopReason());
        break;

      case STOPPED_BY_REQUEST:
        appendKeyValue(strBuilder, "Stop Reason", jobEntity.getStopReason());
        break;

      case WAITING_TO_RETRY:
        break;

      default:
        // do nothing.
    }

    switch (jobEntity.getTaskType()) {
      case IMPORT:
        createReportForImport(jobEntity, strBuilder);
        break;
      default:
        throw new IllegalArgumentException("Invalid jobType[" + jobEntity.getTaskType() + "].");
    }

    try {
      response.setContentType(ContentTypeEnum.TEXT_HTML.get());
      response.getWriter().println(strBuilder.toString());
    } catch (Exception e) {
      // TODO(arjuns): Add exception handling.
      throw new RuntimeException();
    }
  }

  /**
   * 
   */
  private void createReportForImport(JobEntity jobEntity, StringBuilder strBuilder) {
    ImportJobEntity importEntity = importManager1.get(jobEntity.getTaskId());
    checkNotNull(importEntity, "importEnity not found.");

    if (!importEntity.getPersonId().equals(sessionManager.getPersonId())) {
      throw new NotFoundException("Tried to access Import from another user. Expected PersonId["
          + importEntity.getPersonId() + "] but found [" + sessionManager.getPersonId() + "].");
    }

    LightUtils.appendSectionHeader(strBuilder, "Job Details");
    List<ChangeLogEntryPojo> listOfChanges = jobEntity.getChangeLogs();

    for (int counter = listOfChanges.size() - 1; counter >= 0; counter--) {
      ChangeLogEntryPojo currEntry = listOfChanges.get(counter);
      appendLine(strBuilder, currEntry.toLineItem());
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
    // TODO(arjuns): Auto-generated method stub
    throw new UnsupportedOperationException();
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
