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
package com.google.light.server.jersey.resources.notifications;

import static com.google.light.server.utils.LightPreconditions.checkIsUnderTaskQueue;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.LightPreconditions.checkNotNull;
import static com.google.light.server.utils.ObjectifyUtils.repeatInTransaction;
import static com.google.light.server.utils.ServletUtils.getRequestHeaderValue;

import java.util.logging.Logger;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.light.server.constants.HttpHeaderEnum;
import com.google.light.server.constants.JerseyConstants;
import com.google.light.server.constants.NotificationType;
import com.google.light.server.constants.PlacementOrder;
import com.google.light.server.constants.http.ContentTypeConstants;
import com.google.light.server.dto.notifications.ChildJobCompletionNotification;
import com.google.light.server.dto.pojo.ChangeLogEntryPojo;
import com.google.light.server.exception.ExceptionType;
import com.google.light.server.exception.unchecked.taskqueue.ParentNotReadyForChildCompleteNotification;
import com.google.light.server.jersey.resources.AbstractJerseyResource;
import com.google.light.server.manager.interfaces.JobManager;
import com.google.light.server.persistence.entity.jobs.JobEntity;
import com.google.light.server.persistence.entity.jobs.JobState;
import com.google.light.server.utils.JsonUtils;
import com.google.light.server.utils.Transactable;
import com.googlecode.objectify.Objectify;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@Path(JerseyConstants.RESOURCE_PATH_NOTIFICATION)
public class NotificationResource extends AbstractJerseyResource {
  private static final Logger logger = Logger.getLogger(NotificationResource.class.getName());
  
  private JobManager jobManager;

  @Inject
  public NotificationResource(Injector injector, HttpServletRequest request,
      HttpServletResponse response, JobManager jobManager) {
    super(injector, request, response);
    this.jobManager = checkNotNull(jobManager, ExceptionType.SERVER_GUICE_INJECTION, "jobManager");

  }

  @Path(JerseyConstants.PATH_NOTIFICATION_JOB)
  @POST
  @Consumes({ ContentTypeConstants.APPLICATION_JSON, ContentTypeConstants.TEXT_PLAIN })
  public Response childCompletionEvent(String body) {
    checkIsUnderTaskQueue(request);

    String typeStr = getRequestHeaderValue(request, HttpHeaderEnum.LIGHT_NOTIIFCATION_TYPE);
    checkNotBlank(typeStr, ExceptionType.SERVER, "Notification Type is not set.");
    NotificationType type = NotificationType.valueOf(typeStr);

    switch (type) {
      case CHILD_JOB_COMPLETION:
        handlChildJobCompletion(body);
        break;

      default:
        throw new IllegalStateException("Unsupported type : " + type);
    }

    return Response.ok().build();
  }

  /**
   * 
   */
  private void handlChildJobCompletion(String body) {
    final ChildJobCompletionNotification jobNotification =
        JsonUtils.getDto(body, ChildJobCompletionNotification.class);
    logger.info("Notification : " + jobNotification.toJson());

    repeatInTransaction(new Transactable<Void>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Void run(Objectify ofy) {
        JobEntity parentJobEntity = jobManager.get(ofy, jobNotification.getParentJobId());
        checkNotNull(parentJobEntity, ExceptionType.SERVER,
            "parentJob :" + jobNotification.getParentJobId() + " was not found");

        PlacementOrder placement = parentJobEntity.getJobState().getPlacement(
            JobState.WAITING_FOR_CHILD_COMPLETE_NOTIFICATION);

        switch (placement) {
          case BEFORE:
            throw new ParentNotReadyForChildCompleteNotification(" ParentJob.JobState is "
                + parentJobEntity.getJobState());

          case EQUAL:
            parentJobEntity.setJobState(JobState.POLLING_FOR_CHILDS);
            jobManager.put(ofy, parentJobEntity,
                new ChangeLogEntryPojo("Starting polling for childs"));
            jobManager.enqueueJobForPolling(ofy, parentJobEntity.getJobId());
            break;

          case AFTER:
            return null;

          default:
            throw new IllegalArgumentException("Unsupported placement : " + placement);
        }
        return null;
      }
    });

  }
}
