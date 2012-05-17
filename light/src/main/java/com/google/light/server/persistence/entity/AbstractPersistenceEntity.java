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
package com.google.light.server.persistence.entity;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.light.server.dto.AbstractPojo;
import com.google.light.server.persistence.PersistenceToDtoInterface;
import com.google.light.server.utils.LightUtils;
import javax.persistence.PrePersist;
import javax.persistence.Transient;
import org.joda.time.Instant;

/**
 * Abstract Persistence entity to take care of all the fields and methods required by other
 * Persistence entities.
 * 
 * TODO(arjuns) : Move other entities to use this.
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public abstract class AbstractPersistenceEntity<P, D> extends AbstractPojo<P> implements
    PersistenceToDtoInterface<P, D> {
  // This value will not be persisted in dataStore.
  @Transient
  protected Boolean needsCreationTime;

  // Transient avoids comparison for equals.
  protected transient Long creationTimeInMillis;
  protected transient Long lastUpdateTimeInMillis;

  public Instant getCreationTime() {
    if (creationTimeInMillis != null) {
      return new Instant(creationTimeInMillis);
    }

    return null;
  }

  public Instant getLastUpdateTime() {
    if (lastUpdateTimeInMillis != null) {
      return new Instant(lastUpdateTimeInMillis);
    }
    
    return null;
  }

  public boolean needsCreationTime() {
    return needsCreationTime();
  }

  // Before persisiting any entity, update its lastUpdateTime.
  @PrePersist
  protected void prePersist() {
    Instant now = LightUtils.getNow();
    this.lastUpdateTimeInMillis = now.getMillis();

//    // TODO(arjuns): Fix creationTime.
//    this.creationTimeInMillis = null;

  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings("unchecked")
  @Override
  public P validate() {
    return (P) this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public abstract D toDto();

  @SuppressWarnings("rawtypes")
  public abstract static class BaseBuilder<T extends BaseBuilder> {
    private Instant creationTime;
    private Instant lastUpdateTime;

    @SuppressWarnings("unchecked")
    public T creationTime(Instant creationTime) {
      this.creationTime = creationTime;
      return ((T) this);
    }

    @SuppressWarnings("unchecked")
    public T lastUpdateTime(Instant lastUpdateTime) {
      this.lastUpdateTime = lastUpdateTime;
      return ((T) this);
    }
  }

  @SuppressWarnings("synthetic-access")
  protected AbstractPersistenceEntity(@SuppressWarnings("rawtypes") BaseBuilder builder,
      Boolean shouldHaveCreationTime) {
    checkNotNull(shouldHaveCreationTime, "shouldHaveCreationTime");
    this.needsCreationTime = shouldHaveCreationTime;
    if (builder == null) {
      return;
    }

    if (builder.creationTime != null) {
      this.creationTimeInMillis = builder.creationTime.getMillis();
    }

    if (builder.lastUpdateTime != null) {
      this.lastUpdateTimeInMillis = builder.lastUpdateTime.getMillis();
    }
  }
}
