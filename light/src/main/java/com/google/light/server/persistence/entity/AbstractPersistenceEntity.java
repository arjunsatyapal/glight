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

import com.google.light.server.persistence.PersistenceToDtoInterface;
import com.googlecode.objectify.Key;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
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
public abstract class AbstractPersistenceEntity<P, D> implements PersistenceToDtoInterface<P, D> {
  protected Instant creationTime;
  protected Instant lastUpdateTime;

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  public Instant getLastUpdateTime() {
    return lastUpdateTime;
  }

  public void setLastUpdateTime(Instant lastUpdateTime) {
    this.lastUpdateTime = lastUpdateTime;
  }

  public Instant getCreationTime() {
    return creationTime;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public abstract Key<P> getKey();

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
      return ((T)this);
    }

    @SuppressWarnings("unchecked")
    public T lastUpdateTime(Instant lastUpdateTime) {
      this.lastUpdateTime = lastUpdateTime;
      return ((T)this);
    }
  }

  @SuppressWarnings("synthetic-access")
  protected AbstractPersistenceEntity(@SuppressWarnings("rawtypes") BaseBuilder builder) {
    this.creationTime = checkNotNull(builder.creationTime, "creationTime");
    this.lastUpdateTime = checkNotNull(builder.lastUpdateTime, "lastUpdateTime");
  }

  // For Objectify.
  protected AbstractPersistenceEntity() {
  }
}
