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
import static com.google.light.server.utils.LocationHeaderUtils.getJobLocation;

import java.util.logging.Logger;

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
import com.google.light.server.dto.pojo.longwrapper.JobId;
import com.google.light.server.dto.pojo.longwrapper.PersonId;
import com.google.light.server.manager.interfaces.QueueManager;
import com.google.light.server.utils.GuiceUtils;
import com.google.light.server.utils.JsonUtils;
import com.googlecode.objectify.Objectify;
import java.net.URI;

/**
 * 
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

    RetryOptions retryOptions = getRetryOptions();

    TaskOptions taskOptions = getTaskOptions(retryOptions,
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
  private TaskOptions getTaskOptions(RetryOptions retryOptions,
      TaskOptions.Method method, String contentType, String payLoad, URI uri) {
    PersonId ownerId = GuiceUtils.getOwnerId();
    checkNotNull(ownerId, "OwnerId cannot be null here.");

    TaskOptions taskOptions = TaskOptions.Builder.withDefaults()
        .countdownMillis(LightConstants.TASK_COUNTDOWN_MILLIS)
        .header(HttpHeaderEnum.LIGHT_OWNER_HEADER.get(), JsonUtils.toJson(ownerId, false))
        .header(HttpHeaderEnum.CONTENT_TYPE.get(), contentType)
        .method(method)
        .payload(payLoad.getBytes(UTF_8), UTF_8.displayName())
        .retryOptions(retryOptions)
        .url(uri.toString());
    return taskOptions;
  }

  /**
   * @return
   */
  private RetryOptions getRetryOptions() {
    RetryOptions retryOptions = RetryOptions.Builder.withDefaults();
    retryOptions.maxBackoffSeconds(LightConstants.TASK_MAX_BACKOFF_SEC);
    retryOptions.maxDoublings(LightConstants.TASK_MAX_DOUBLINGS);
    retryOptions.minBackoffSeconds(LightConstants.TASK_MIN_BACKOFF_SECONDS);
    retryOptions.taskAgeLimitSeconds(LightConstants.TASK_MAX_AGE);
    retryOptions.taskRetryLimit(LightConstants.TASK_RETRY_LIMIT);
    return retryOptions;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void enqueuePollingJob(Objectify ofy, JobId jobId) {
    checkTxnIsRunning(ofy);

    RetryOptions retryOptions = getRetryOptions();

    TaskOptions taskOptions = getTaskOptions(retryOptions,
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
    RetryOptions retryOptions = getRetryOptions();

    TaskOptions taskOptions = getTaskOptions(retryOptions,
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
    RetryOptions retryOptions = getRetryOptions();

    TaskOptions taskOptions = getTaskOptions(retryOptions,
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
    RetryOptions retryOptions = getRetryOptions();

    TaskOptions taskOptions = getTaskOptions(retryOptions,
        TaskOptions.Method.POST, ContentTypeConstants.APPLICATION_JSON,
        notification.toJson(),
        getURI(JerseyConstants.URI_RESOURCE_PATH_NOTIFICATION_JOB));

    taskOptions.header(HttpHeaderEnum.LIGHT_NOTIIFCATION_TYPE.get(), notification.getType().name());

    Queue queue = QueueFactory.getQueue(QueueEnum.LIGHT_NOTIFICATIONS.getName());
    TaskHandle taskHandle = queue.add(taskOptions);
    logger.info("Enqueued enqueueNotification " + notification.getType()
        + " with TaskHandle : " + taskHandle);
  }
}
