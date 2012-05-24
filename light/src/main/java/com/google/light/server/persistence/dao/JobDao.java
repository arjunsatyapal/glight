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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.persistence.entity.jobs.JobEntity.OFY_JOB_OWNER_QUERY_STRING;
import static com.google.light.server.utils.LightPreconditions.checkNotNull;
import static com.google.light.server.utils.LightUtils.getWrapperValue;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.JobId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.PersonId;
import com.google.light.server.exception.ExceptionType;
import com.google.light.server.persistence.entity.jobs.JobEntity;
import com.google.light.server.persistence.entity.jobs.JobEntity.JobType;
import com.google.light.server.serveronlypojos.GAEQueryWrapper;
import com.google.light.server.utils.ObjectifyUtils;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class JobDao extends AbstractBasicDao<Object, JobEntity> {
  private static final Logger logger = Logger.getLogger(JobDao.class.getName());
  
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

  public Map<JobId, JobEntity> findListOfJobs(Collection<JobId> listOfJobIds) {
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
  
  @Override
  public JobEntity put(Objectify ofy, JobEntity entity) {
    checkNotNull(entity.getCreationTime(), "creationTime cannot be null. Happened for " + entity.getJobId());
    JobEntity temp = super.put(ofy, entity);
    logger.info("Successfully created/updated : " + temp.getJobId());
    return temp;
  }
  
  public GAEQueryWrapper<JobEntity> findRootJobsByOwnerId(PersonId ownerId, 
      String startIndex, int maxResults) {
    checkNotNull(ownerId, ExceptionType.SERVER,
        "OwnerId should not be null. Callers of this method should ensure that.");

    Map<String, Object> mapOfFilterKeyValues = new ImmutableMap.Builder<String, Object>()
        .put(OFY_JOB_OWNER_QUERY_STRING, getWrapperValue(ownerId))
        .put(JobEntity.OFY_JOB_TYPE_QUERY_STRING, JobType.ROOT_JOB)
        .build();
    
    Objectify ofy = ObjectifyUtils.nonTransaction();
    GAEQueryWrapper<JobEntity> listOfModules = ObjectifyUtils.findQueryResults(
        ofy, JobEntity.class, mapOfFilterKeyValues, null, startIndex, maxResults);
    return listOfModules;
  }
}
