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
package com.google.light.server.dto.pojo.tree.collection;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.light.server.dto.pojo.typewrapper.longwrapper.JobId;
import com.google.light.server.manager.interfaces.JobManager;
import com.google.light.server.persistence.entity.jobs.JobEntity;
import com.google.light.server.persistence.entity.jobs.JobState;

/**
 *
 * 
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */
public class CollectionTreeUtils {
  public static CollectionTreeNodeDto generateCollectionNodeFromChildJob(
      JobManager jobManager, JobId childJobId) {
    JobEntity childJobEntity = jobManager.get(null, childJobId);
    checkArgument(childJobEntity.getJobState() == JobState.COMPLETE,
        "Child job should be complete by now but was not for : " + childJobId);
    checkNotNull(childJobEntity.getResponse(),
        "Response should not be null for child Job : " + childJobId);
    CollectionTreeNodeDto child = childJobEntity.getResponse(CollectionTreeNodeDto.class);
    return child;
  }
}
