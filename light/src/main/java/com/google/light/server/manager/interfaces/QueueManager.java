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
package com.google.light.server.manager.interfaces;

import com.google.light.server.dto.notifications.AbstractNotification;

import com.google.light.server.dto.pojo.longwrapper.JobId;
import com.googlecode.objectify.Objectify;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public interface QueueManager {
  /**
   * Method to enqueue a Job inside a transaction.
   * <b> Note that we want to enqueue all tasks within a transaction. So even when this
   * method does not consume Objectify Transaciton directly, it is required to ensure that it was
   * called inside a transaction.
   * 
   * @param ofy
   * @param jobId
   */
  public void enqueueGoogleDocInteractionJob(Objectify ofy, JobId jobId);
  
  public void enqueuePollingJob(Objectify ofy, JobId jobId);
  
  public void enqueueLightJob(Objectify ofy, JobId jobId);
  
  public void enqueueLightJobWithoutTxn(JobId jobId);
  
  public <T extends AbstractNotification<T>> void enqueueNotification(
      Objectify ofy, T notification);
}
