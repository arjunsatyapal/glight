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

import static com.google.light.server.utils.LightPreconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkTxnIsRunning;

import com.google.light.server.dto.pojo.typewrapper.longwrapper.JobId;

import java.util.logging.Logger;

import com.google.inject.Inject;
import com.google.light.server.constants.NotificationType;
import com.google.light.server.dto.notifications.ChildJobCompletionNotification;
import com.google.light.server.exception.ExceptionType;
import com.google.light.server.manager.interfaces.NotificationManager;
import com.google.light.server.manager.interfaces.QueueManager;
import com.googlecode.objectify.Objectify;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class NotificationManagerImpl implements NotificationManager {
  private static final Logger logger = Logger.getLogger(NotificationManagerImpl.class.getName());

  private QueueManager queueManager;

  @Inject
  public NotificationManagerImpl(QueueManager queueManager) {
    this.queueManager = checkNotNull(queueManager,
        ExceptionType.SERVER_GUICE_INJECTION, "queueManager");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void notifyChildCompletionEvent(Objectify ofy, JobId parentJobId, JobId childJobId) {
    checkTxnIsRunning(ofy);

    ChildJobCompletionNotification notification = new ChildJobCompletionNotification.Builder()
        .childJobId(childJobId)
        .parentJobId(parentJobId)
        .type(NotificationType.CHILD_JOB_COMPLETION)
        .build();

    queueManager.enqueueNotification(ofy, notification);
    logger.info("Successfully enqueued " + NotificationType.CHILD_JOB_COMPLETION + " from "
        + childJobId + " to " + parentJobId);
  }
}
