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
package com.google.light.server.jersey.resources;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.GuiceUtils.getInstance;
import static com.google.light.server.utils.GuiceUtils.seedEntityInRequestScope;
import static com.google.light.server.utils.LightPreconditions.checkIsRunningUnderQueue;
import static com.google.light.server.utils.ServletUtils.getRequestHeaderValue;

import com.google.light.server.dto.pojo.typewrapper.longwrapper.PersonId;

import com.google.light.server.utils.JsonUtils;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.light.server.annotations.AnotHttpSession;
import com.google.light.server.annotations.AnotOwner;
import com.google.light.server.constants.HttpHeaderEnum;
import com.google.light.server.constants.QueueEnum;
import com.google.light.server.constants.http.ContentTypeEnum;
import com.google.light.server.servlets.SessionManager;
import com.google.light.server.utils.GuiceUtils;
import com.google.light.server.utils.ServletUtils;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * 
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */
public abstract class AbstractJerseyResource {
  @Inject Injector injector;
  protected HttpServletRequest request;
  protected HttpServletResponse response;
  private QueueEnum queue;
  private String taskName;
  private int taskRetryCount;
  // TODO(arjuns); See what is the dataType for this.
  private String failFast;
  // TOOD(arjuns0: See what is the dataType for this.
  private String taskEta;
  
  public boolean isRunningUnderTaskQueue() {
    return queue == null;
  }
  
  public QueueEnum getQueue() {
    checkIsRunningUnderQueue(request);
    return queue;
  }

  public String getTaskName() {
    checkIsRunningUnderQueue(request);
    return taskName;
  }

  public int getTaskRetryCount() {
    checkIsRunningUnderQueue(request);
    return taskRetryCount;
  }

  public String getFailFast() {
    checkIsRunningUnderQueue(request);
    return failFast;
  }

  public String getTaskEta() {
    checkIsRunningUnderQueue(request);
    return taskEta;
  }

  @Inject
  public AbstractJerseyResource(Injector injector, HttpServletRequest request, 
      HttpServletResponse response) {
    this.injector = checkNotNull(injector, "injector");
    GuiceUtils.setInjector(injector);
    this.request = checkNotNull(request, "request");
    checkNotNull(request, "request");
    
    this.response = checkNotNull(response);
    
    HttpSession session = request.getSession();
    seedEntityInRequestScope(request, HttpSession.class, AnotHttpSession.class, session);
    
    SessionManager sessionManager = GuiceUtils.getInstance(SessionManager.class);
    sessionManager.seedPersonIds(request);

    PersonId foo = getInstance(PersonId.class, AnotOwner.class);
    Preconditions.checkNotNull(foo);
    
    initQueueParamsIfRequired(request);
  }

  /**
   * @param request2
   */
  private void initQueueParamsIfRequired(HttpServletRequest request) {
    String queueName = getRequestHeaderValue(request, HttpHeaderEnum.GAE_QUEUE_NAME);
    
    if (queueName == null) {
      return;
    }
    
    queue = QueueEnum.getByName(queueName);
    taskName = getRequestHeaderValue(request, HttpHeaderEnum.GAE_TASK_NAME);
    taskRetryCount = Integer.parseInt(getRequestHeaderValue(request, 
        HttpHeaderEnum.GAE_TASK_RETRY_COUNT));
    failFast = getRequestHeaderValue(request, HttpHeaderEnum.GAE_FAILFAST);
    taskEta = getRequestHeaderValue(request, HttpHeaderEnum.GAE_TASK_ETA);
    
    initOwner();
  }
  
  /**
   * 
   */
  private void initOwner() {
    String ownerIdStr = getRequestHeaderValue(request, HttpHeaderEnum.LIGHT_OWNER_HEADER);
    PersonId ownerId = JsonUtils.getPojo(ownerIdStr, PersonId.class);
    // TODO(arjuns): Fix the actor id part.
    GuiceUtils.enqueueRequestScopedVariables(ownerId, ownerId);
  }

  protected ContentTypeEnum getContentType() {
    String contentTypeStr = ServletUtils.getRequestHeaderValue(
        request, HttpHeaderEnum.CONTENT_TYPE);
    
    return ContentTypeEnum.getContentTypeByString(contentTypeStr);
  }
}
