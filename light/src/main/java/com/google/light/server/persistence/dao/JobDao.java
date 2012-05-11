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

import com.google.light.server.dto.pojo.typewrapper.longwrapper.JobId;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.light.server.persistence.entity.jobs.JobEntity;
import com.google.light.server.utils.ObjectifyUtils;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import java.util.List;
import java.util.Map;

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

  public JobEntity get(Objectify ofy, JobId id) {
    Key<JobEntity> key = JobEntity.generateKey(id);
    return super.get(ofy, key);
  }

  public Map<JobId, JobEntity> findListOfJobs(List<JobId> listOfJobIds) {
    List<Key<JobEntity>> listOfKeys = Lists.newArrayListWithCapacity(listOfJobIds.size());
    
    for (JobId currJobId : listOfJobIds) {
      listOfKeys.add(JobEntity.generateKey(currJobId));
    }
    
    Objectify ofy = ObjectifyUtils.nonTransaction();
    Map<Key<JobEntity>, JobEntity> fetchedMap = ofy.get(listOfKeys);
    
    Map<JobId, JobEntity> requiredMap = Maps.newHashMap();
    
    for (Key<JobEntity> currKey : fetchedMap.keySet()) {
      JobEntity currEntity = fetchedMap.get(currKey);
      requiredMap.put(currEntity.getJobId(), currEntity);
    }
    
    return requiredMap;
  }
}
