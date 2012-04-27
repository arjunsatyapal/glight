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

import com.google.light.server.dto.pojo.JobHandlerId;
import com.google.light.server.persistence.entity.jobs.JobEntity;
import com.google.light.server.persistence.entity.jobs.JobEntity.JobHandlerType;
import com.google.light.server.persistence.entity.queue.importflow.ImportJobEntity;
import com.googlecode.objectify.Objectify;
import javax.annotation.Nullable;

/**
 *
 * 
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */
public interface JobManager {
  public JobEntity enqueueImportJob(ImportJobEntity importJobEntity);
  
  public JobEntity put(@Nullable Objectify ofy, JobEntity jobEntity);
  
  public JobEntity get(Long id);
  
  public JobEntity findByJobHandlerId(JobHandlerType jobHandlerType, JobHandlerId jobHandlerId);
}
