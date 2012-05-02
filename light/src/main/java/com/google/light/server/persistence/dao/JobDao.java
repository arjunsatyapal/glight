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
package com.google.light.server.persistence.dao;

import static com.google.light.server.utils.ObjectifyUtils.assertAndReturnUniqueEntity;

import com.google.light.server.dto.pojo.JobHandlerId;

import com.google.inject.Inject;
import com.google.light.server.persistence.entity.jobs.JobEntity;
import com.google.light.server.persistence.entity.jobs.JobEntity.JobHandlerType;
import com.google.light.server.utils.ObjectifyUtils;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Query;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class JobDao extends AbstractBasicDao<Object, JobEntity> {
  static {
    ObjectifyService.register(JobEntity.class);
  }

  /**
   * @param entityClazz
   */
  @Inject
  public JobDao() {
    super(JobEntity.class);
  }

  public JobEntity get(Objectify ofy, Long id) {
    Key<JobEntity> key = JobEntity.generateKey(id);
    return super.get(ofy, key);
  }

  public JobEntity findByJobHandlerId(JobHandlerType jobHandlerType, JobHandlerId jobHandlerId) {
    Objectify ofy = ObjectifyUtils.nonTransaction();

    Query<JobEntity> query = ofy.query(JobEntity.class)
        .filter(JobEntity.OFY_JOB_HANDLER_ID, jobHandlerId.get())
        .filter(JobEntity.OFY_JOB_HANDLER_TYPE, jobHandlerType);

    String errMessage =
        "For JobHandlerType[" + jobHandlerType + "], JobHandlerId[" + jobHandlerId
            + "], found more then one record.";
    return assertAndReturnUniqueEntity(query, errMessage);
  }
}
