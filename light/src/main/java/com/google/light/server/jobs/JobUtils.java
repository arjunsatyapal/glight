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
package com.google.light.server.jobs;

import static com.google.light.server.utils.GuiceUtils.getInstance;

import com.google.appengine.api.datastore.Text;
import com.google.light.server.dto.AbstractDto;
import com.google.light.server.dto.pojo.ChangeLogEntryPojo;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.JobId;
import com.google.light.server.manager.interfaces.JobManager;
import com.google.light.server.persistence.entity.jobs.JobEntity;
import com.google.light.server.persistence.entity.jobs.JobState;
import com.googlecode.objectify.Objectify;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class JobUtils {
  public static void updateChangeLog(Objectify ofy, JobId jobId, String changeMessage) {
    JobManager jobManager = getInstance(JobManager.class);
    JobEntity jobEntity = jobManager.get(ofy, jobId);

    ChangeLogEntryPojo changeLog = new ChangeLogEntryPojo(changeMessage);
    jobManager.put(ofy, jobEntity, changeLog);
  }
  
  // TODO(arjuns) : Move this to a better place.
  public static <D extends AbstractDto<D>>void updateJobContext(Objectify ofy, JobManager jobManager,
      JobState jobState, D contextDto, JobEntity jobEntity, String changeLogMsg) {
    jobEntity.setJobState(jobState);
    jobEntity.setContext(new Text(contextDto.toJson()));
    jobManager.put(ofy, jobEntity, new ChangeLogEntryPojo(changeLogMsg));
  }
}
