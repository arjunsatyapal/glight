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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.light.server.dto.AbstractPojo;
import com.google.light.server.persistence.entity.jobs.JobEntity;

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
  private JobEntity jobEntity;

  public PersonId getOwnerId() {
    return ownerId;
  }

  public PersonId getActorId() {
    return actorId;
  }

  public JobEntity getJobEntity() {
    return jobEntity;
  }
  
  public static class Builder {
    private PersonId ownerId;
    private PersonId actorId;
    private JobEntity jobEntity;

    public Builder ownerId(PersonId ownerId) {
      this.ownerId = ownerId;
      return this;
    }

    public Builder actorId(PersonId actorId) {
      this.actorId = actorId;
      return this;
    }

    public Builder jobEntity(JobEntity jobEntity) {
      this.jobEntity = jobEntity;
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
    this.jobEntity = builder.jobEntity;
  }

  /** 
   * {@inheritDoc}
   */
  @Override
  public LightJobContextPojo validate() {
    checkNotNull(jobEntity, "jobEntity");
    return this;
  }
}
