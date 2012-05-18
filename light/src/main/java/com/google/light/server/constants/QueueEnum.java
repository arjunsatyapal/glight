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
package com.google.light.server.constants;

import static com.google.light.server.constants.LightConstants.SECOND;
import static com.google.light.server.constants.LightConstants.SECOND_FIVE;
import static com.google.light.server.constants.LightConstants.SECOND_TEN;
import static com.google.light.server.constants.LightConstants.TASK_AGE_LIMIT_MAX;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;

import com.google.appengine.api.taskqueue.RetryOptions;

/**
 *
 * 
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */
public enum QueueEnum {
  GDOC_INTERACTION("gdoc-interaction", 
      SECOND_TEN, 5, SECOND, TASK_AGE_LIMIT_MAX, 20),
  // TODO(waltercacau) : Fix the name.
//  GSS_PAGEMAP_UPDATE("gss_pagemap_update"),
  LIGHT("light", SECOND_TEN, 5, SECOND, TASK_AGE_LIMIT_MAX, 10),
  LIGHT_NOTIFICATIONS("light-notification", SECOND_TEN, 2, SECOND, TASK_AGE_LIMIT_MAX, 100),
  LIGHT_POLLING("light-polling", SECOND_FIVE, 2, SECOND, TASK_AGE_LIMIT_MAX, 100);
  
  private String name;
  private RetryOptions retryOptions;
  
  private QueueEnum(String name, double maxBackOffSec, int maxDoublings, double minBackOffSeconds,
      long taskAgeLimit, int taskRetryLimit) {
    this.name = checkNotBlank(name, "queue name");
    
    // Building Retry Optiosn.
    this.retryOptions = RetryOptions.Builder.withDefaults()
          .maxBackoffSeconds(maxBackOffSec)
          .maxDoublings(maxDoublings)
          .minBackoffSeconds(minBackOffSeconds)
          .taskAgeLimitSeconds(taskAgeLimit)
          .taskRetryLimit(taskRetryLimit);
  }
  
  public String getName() {
    return name;
  }
  
  public RetryOptions getRetryOptions() {
    return retryOptions;
  }
  
  public static QueueEnum getByName(String name) {
    for (QueueEnum curr : QueueEnum.values()) {
      if (curr.getName().equals(name)) {
        return curr;
      }
    }
    
    throw new EnumConstantNotPresentException(QueueEnum.class, name);
  }
}
