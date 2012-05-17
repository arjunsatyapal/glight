/*
 * Copyright (C) Google Inc.
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
package com.google.light.server.exception.unchecked.taskqueue;

import java.util.Set;

import com.google.common.collect.Iterables;

import com.google.light.server.dto.pojo.typewrapper.longwrapper.JobId;





/**
 * This happens when incorrect KeyType is passed. e.g. Instead of String Id, a Long Id was passed.
 * Most probably this is going to be a server side issue.
 *  
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class WaitForChildsToComplete extends TaskQueueRetriableException {
  private JobId jobId;
  
  /**
   * {@inheritDoc}
   */
  public WaitForChildsToComplete(Set<JobId> jobIds) {
      super("Waiting for : " + Iterables.toString(jobIds));
  }
  
  public JobId getJobId() {
    return jobId;
  }
}
