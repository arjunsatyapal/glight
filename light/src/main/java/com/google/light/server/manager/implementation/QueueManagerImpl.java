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
package com.google.light.server.manager.implementation;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkTxnIsRunning;
import static com.google.light.server.utils.LightUtils.getURI;
import static com.google.light.server.utils.LocationUtils.getJobLocation;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.RetryOptions;
import com.google.appengine.api.taskqueue.TaskHandle;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.light.server.constants.HttpHeaderEnum;
import com.google.light.server.constants.JerseyConstants;
import com.google.light.server.constants.LightConstants;
import com.google.light.server.constants.QueueEnum;
import com.google.light.server.constants.http.ContentTypeConstants;
import com.google.light.server.dto.notifications.AbstractNotification;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.JobId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.PersonId;
import com.google.light.server.manager.interfaces.PersonManager;
import com.google.light.server.manager.interfaces.QueueManager;
import com.google.light.server.persistence.entity.person.PersonEntity;
import com.google.light.server.utils.GuiceUtils;
import com.google.light.server.utils.JsonUtils;
import com.googlecode.objectify.Objectify;
import java.net.URI;
import java.util.logging.Logger;

/**
 * Implementation for {@link QueueManager}
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class QueueManagerImpl implements QueueManager {
  private static final Logger logger = Logger.getLogger(QueueManagerImpl.class.getName());

  /**
   * {@inheritDoc}
   */
  @Override
  public void enqueueGoogleDocInteractionJob(Objectify ofy, JobId jobId) {
    checkTxnIsRunning(ofy);

    TaskOptions taskOptions = getTaskOptions(GuiceUtils.getOwnerId(),
        QueueEnum.GDOC_INTERACTION.getRetryOptions(),
        TaskOptions.Method.PUT, ContentTypeConstants.TEXT_PLAIN,
        Long.toString(jobId.getValue()), getJobLocation(jobId));

    Queue queue = QueueFactory.getQueue(QueueEnum.GDOC_INTERACTION.getName());
    TaskHandle taskHandle = queue.add(taskOptions);

    logger.info("Enqueued enqueueGoogleDocInteractionJob for " + jobId + " with TaskHandle : "
        + taskHandle);
  }

  /**
   * @param jobId
   * @param retryOptions
   * @return
   */
  private TaskOptions getTaskOptions(PersonId ownerId, RetryOptions retryOptions,
      TaskOptions.Method method, String contentType, String payLoad, URI uri) {
    checkNotNull(ownerId, "OwnerId cannot be null here.");

    TaskOptions taskOptions = TaskOptions.Builder.withDefaults();
    taskOptions.method(method);
    
    if (method != TaskOptions.Method.GET) {
      taskOptions.header(HttpHeaderEnum.CONTENT_TYPE.get(), contentType);
      taskOptions.payload(payLoad.getBytes(UTF_8), UTF_8.displayName());  
    }
    
    taskOptions.countdownMillis(LightConstants.TASK_COUNTDOWN_MILLIS);
    taskOptions.header(HttpHeaderEnum.LIGHT_OWNER_HEADER.get(), JsonUtils.toJson(ownerId, false));
    taskOptions.retryOptions(retryOptions);
    taskOptions.url(uri.toString());
    
    return taskOptions;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void enqueuePollingJob(Objectify ofy, JobId jobId) {
    checkTxnIsRunning(ofy);

    TaskOptions taskOptions = getTaskOptions(GuiceUtils.getOwnerId(),
        QueueEnum.LIGHT_POLLING.getRetryOptions(),
        TaskOptions.Method.PUT, ContentTypeConstants.TEXT_PLAIN,
        Long.toString(jobId.getValue()), getJobLocation(jobId));

    Queue queue = QueueFactory.getQueue(QueueEnum.LIGHT_POLLING.getName());
    TaskHandle taskHandle = queue.add(taskOptions);
    logger.info("Enqueued enqueuePollingJob for " + jobId + " with TaskHandle : " + taskHandle);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void enqueueLightJob(Objectify ofy, JobId jobId) {
    checkTxnIsRunning(ofy);
    TaskOptions taskOptions = getTaskOptions(GuiceUtils.getOwnerId(),
        QueueEnum.LIGHT.getRetryOptions(),
        TaskOptions.Method.PUT, ContentTypeConstants.TEXT_PLAIN,
        Long.toString(jobId.getValue()), getJobLocation(jobId));

    Queue queue = QueueFactory.getQueue(QueueEnum.LIGHT.getName());
    TaskHandle taskHandle = queue.add(taskOptions);
    logger.info("Enqueued enqueueLightJob for " + jobId + " with TaskHandle : " + taskHandle);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void enqueueLightJobWithoutTxn(JobId jobId) {
    TaskOptions taskOptions = getTaskOptions(GuiceUtils.getOwnerId(),
        QueueEnum.LIGHT.getRetryOptions(),
        TaskOptions.Method.PUT, ContentTypeConstants.TEXT_PLAIN,
        Long.toString(jobId.getValue()), getJobLocation(jobId));

    Queue queue = QueueFactory.getQueue(QueueEnum.LIGHT.getName());
    TaskHandle taskHandle = queue.add(taskOptions);
    logger.info("Enqueued enqueueLightJob for " + jobId + " with TaskHandle : " + taskHandle);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T extends AbstractNotification<T>> void enqueueNotification(
      Objectify ofy, T notification) {
    checkTxnIsRunning(ofy);
    TaskOptions taskOptions = getTaskOptions(GuiceUtils.getOwnerId(),
        QueueEnum.LIGHT_NOTIFICATIONS.getRetryOptions(),
        TaskOptions.Method.POST, ContentTypeConstants.TEXT_PLAIN,
        notification.toJson(),
        getURI(JerseyConstants.URI_RESOURCE_PATH_NOTIFICATION_JOB));

    taskOptions.header(HttpHeaderEnum.LIGHT_NOTIIFCATION_TYPE.get(), notification.getType().name());

    Queue queue = QueueFactory.getQueue(QueueEnum.LIGHT_NOTIFICATIONS.getName());
    TaskHandle taskHandle = queue.add(taskOptions);
    logger.info("Enqueued enqueueNotification " + notification.getType()
        + " with TaskHandle : " + taskHandle);
  }

  /** 
   * {@inheritDoc}
   */
  @Override
  public void enqueueSearchIndexTask(Objectify ofy) {
    checkTxnIsRunning(ofy);

    PersonManager personManager = GuiceUtils.getInstance(PersonManager.class);
    PersonEntity lightBot = personManager.findByEmail(LightConstants.LIGHT_BOT_EMAIL);
    checkNotNull(lightBot, "lightBot is missing");
    
    RetryOptions retryOptions = QueueEnum.SEARCH_INDEX.getRetryOptions();
    TaskOptions taskOptions = getTaskOptions(lightBot.getPersonId(),
        retryOptions, TaskOptions.Method.GET, ContentTypeConstants.TEXT_PLAIN, null,
        getURI(JerseyConstants.URI_SEARCH_INDEX_UPDATE_INDICES));

    Queue queue = QueueFactory.getQueue(QueueEnum.SEARCH_INDEX.getName());
//    TaskHandle taskHandle = queue.add(taskOptions);
//    logger.info("Enqueued searchIndexTask " + " with TaskHandle : " + taskHandle);
    logger.info("Ignoring adding to queue");
  }

  @Override
  public void enqueueSearchIndexGSSTask(Objectify ofy) {
    checkTxnIsRunning(ofy);

    PersonManager personManager = GuiceUtils.getInstance(PersonManager.class);
    PersonEntity lightBot = personManager.findByEmail(LightConstants.LIGHT_BOT_EMAIL);
    checkNotNull(lightBot, "lightBot is missing");
    
    RetryOptions retryOptions = QueueEnum.SEARCH_INDEX_GSS.getRetryOptions();
    TaskOptions taskOptions = getTaskOptions(lightBot.getPersonId(),
        retryOptions, TaskOptions.Method.GET, ContentTypeConstants.TEXT_PLAIN, null,
        getURI(JerseyConstants.URI_SEARCH_INDEX_UPDATE_INDICES_GSS));

    Queue queue = QueueFactory.getQueue(QueueEnum.SEARCH_INDEX.getName());
    TaskHandle taskHandle = queue.add(taskOptions);
    logger.info("Enqueued searchIndexGSSTask " + " with TaskHandle : " + taskHandle);
    
  }
}
