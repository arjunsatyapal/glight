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
package com.google.light.server.persistence.entity.jobs;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.light.server.constants.PlacementOrder;
import javax.xml.bind.annotation.XmlEnumValue;

/**
 * Enum for Encapsulating different JobStates.
 * 
 * TODO(arjuns): Add test for this class.
 * TODO(arjuns): Ensure enum values match.
 * 
 * @author Arjun Satyapal
 */
public enum JobState {
  @XmlEnumValue(value = "PRE_START")
  PRE_START(10, JobStateCategory.RUNNING),

  @XmlEnumValue(value = "ENQUEUED")
  ENQUEUED(100, JobStateCategory.RUNNING),

  @XmlEnumValue(value = "CREATING_CHILDS")
  CREATING_CHILDS(200, JobStateCategory.RUNNING),

  @XmlEnumValue(value = "WAITING_FOR_CHILD_COMPLETE_NOTIFICATION")
  WAITING_FOR_CHILD_COMPLETE_NOTIFICATION(300, JobStateCategory.RUNNING),

  @XmlEnumValue(value = "POLLING_FOR_CHILDS")
  POLLING_FOR_CHILDS(400, JobStateCategory.RUNNING),

  @XmlEnumValue(value = "ALL_CHILDS_COMPLETED")
  ALL_CHILDS_COMPLETED(500, JobStateCategory.RUNNING),

  @XmlEnumValue(value = "COMPLETE")
  COMPLETE(950, JobStateCategory.COMPLETE_SUCCESS),

  @XmlEnumValue(value = "STOPPED_BY_ERROR")
  STOPPED_BY_ERROR(1000, JobStateCategory.STOPPED),

  @XmlEnumValue(value = "STOPPED_BY_REQUEST")
  STOPPED_BY_REQUEST(1100, JobStateCategory.STOPPED),

  @XmlEnumValue(value = "READY_FOR_CLEANUP")
  READY_FOR_CLEANUP(2000, JobStateCategory.CLEANUP);

  private int level;
  private JobStateCategory category;

  private JobState(int level, JobStateCategory category) {
    this.level = level;
    this.category = checkNotNull(category, "category");
  }

  public int getLevel() {
    return level;
  }
  
  public JobStateCategory getCategory() {
    return category;
  }
  
  public PlacementOrder getPlacement(JobState jobState) {
    if (this == jobState) {
      return PlacementOrder.EQUAL;
    } else if (this.getLevel() < jobState.getLevel()) {
      return PlacementOrder.BEFORE;
    } else {
      return PlacementOrder.AFTER;
    }
  }
}
