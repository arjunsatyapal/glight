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
package com.google.light.server.dto;

import com.google.light.server.utils.JsonUtils;
import com.google.light.server.utils.XmlUtils;
import org.codehaus.jackson.annotate.JsonCreator;
import org.joda.time.Instant;

/**
 * Abstract DTO to accomodate all the common variables and methods for DTOs.
 * 
 * TODO(arjuns) : Make other DTOs to use this.
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * D : DTO type.
 * P : Persistence for D.
 * I : Id for P.
 * 
 * @author Arjun @SuppressWarnings("serial")
 *         Satyapal
 */
@SuppressWarnings("serial")
public abstract class AbstractDto<D> extends AbstractPojo<D>  {
  protected final Long creationTimeInMillis = null;
  protected Long lastUpdateTimeInMillis;

  /**
   *  Convert DTO to JSON String.
   */
  public String toJson() {
    return JsonUtils.toJson(this);
  }

  /**
   * Convert DTO to XML String.
   */
  public String toXml() {
    return XmlUtils.toXml(this);
  }

  public Long getLastUpdateTimeInMillis() {
    return lastUpdateTimeInMillis;
  }

  public Long getCreationTimeInMillis() {
    return creationTimeInMillis;
  }

  @SuppressWarnings("rawtypes")
  public static class BaseBuilder<T extends BaseBuilder> {
    @SuppressWarnings("unused")
    private Long creationTimeInMillis;
    private Long lastUpdateTimeInMillis;

    @SuppressWarnings("unchecked")
    protected T creationTime(Instant creationTime) {
      this.creationTimeInMillis = creationTime.getMillis();
      return ((T) this);
    }

    @SuppressWarnings("unchecked")
    protected T lastUpdateTime(Instant lastUpdateTime) {
      this.lastUpdateTimeInMillis = lastUpdateTime.getMillis();
      return ((T) this);
    }
  }

  @SuppressWarnings("synthetic-access")
  protected AbstractDto(@SuppressWarnings("rawtypes") BaseBuilder builder) {
    if (builder == null) {
      return;
    }
//    this.creationTime = builder.creationTime;
    this.lastUpdateTimeInMillis = builder.lastUpdateTimeInMillis;
  }
  
  @SuppressWarnings("unused")
  @JsonCreator
  private AbstractDto() {
  }
}
