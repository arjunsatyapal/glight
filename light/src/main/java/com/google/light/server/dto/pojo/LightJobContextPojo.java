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
package com.google.light.server.dto.pojo;

import com.google.light.server.dto.AbstractPojo;
import com.google.light.server.dto.pojo.longwrapper.JobId;
import com.google.light.server.dto.pojo.longwrapper.PersonId;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class LightJobContextPojo extends AbstractPojo<LightJobContextPojo> {
  private PersonId ownerId;
  private PersonId actorId;
  private JobId jobId;
  private JobId parentJobId;
  private JobId rootJobId;
  private String promiseHandle;

  public PersonId getOwnerId() {
    return ownerId;
  }

  public PersonId getActorId() {
    return actorId;
  }

  public JobId getJobId() {
    return jobId;
  }
  
  public JobId getParentJobId() {
    return parentJobId;
  }
  
  public JobId getRootJobId() {
    return rootJobId;
  }
  
  public String getPromiseHandle() {
    return promiseHandle;
  }
  
  public static class Builder {
    private PersonId ownerId;
    private PersonId actorId;
    private JobId jobId;
    private JobId parentJobId;
    private JobId rootJobId;
    private String promiseHandle;

    public Builder ownerId(PersonId ownerId) {
      this.ownerId = ownerId;
      return this;
    }

    public Builder actorId(PersonId actorId) {
      this.actorId = actorId;
      return this;
    }

    public Builder jobId(JobId jobId) {
      this.jobId = jobId;
      return this;
    }
    
    public Builder parentJobId(JobId parentJobId) {
      this.parentJobId = parentJobId;
      return this;
    }
    
    public Builder rootJobId(JobId rootJobId) {
      this.rootJobId = rootJobId;
      return this;
    }
    
    public Builder promiseHandle(String promiseHandle) {
      this.promiseHandle = promiseHandle;
      return this;
    }
    
    @SuppressWarnings("synthetic-access")
    public LightJobContextPojo build() {
      return new LightJobContextPojo(this).validate();
    }
  }

  @SuppressWarnings("synthetic-access")
  private LightJobContextPojo(Builder builder) {
    this.ownerId = builder.ownerId;
    this.actorId = builder.actorId;
    this.jobId = builder.jobId;
    this.parentJobId = builder.parentJobId;
    this.rootJobId = builder.rootJobId;
    this.promiseHandle = builder.promiseHandle;
  }

  /** 
   * {@inheritDoc}
   */
  @Override
  public LightJobContextPojo validate() {
    jobId.validate();
    rootJobId.validate();
    return this;
  }
}
