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
package com.google.light.server.dto.notifications;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.light.server.dto.pojo.typewrapper.longwrapper.JobId;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonTypeName;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
@JsonTypeName(value = "childJobCompletionNotification")
@XmlRootElement(name = "childJobCompletionNotification")
@XmlAccessorType(XmlAccessType.FIELD)
public class ChildJobCompletionNotification extends
    AbstractNotification<ChildJobCompletionNotification> {
  @XmlElement(name = "childJobId")
  @JsonProperty(value = "childJobId")
  private JobId childJobId;
  
  @XmlElement(name = "parentJobId")
  @JsonProperty(value = "parentJobId")
  private JobId parentJobId;
  
  @XmlElement(name = "jsonPayLoad")
  @JsonProperty(value = "jsonPayLoad")
  private String jsonPayLoad;

  /**
   * {@inheritDoc}
   */
  @Override
  public ChildJobCompletionNotification validate() {
    super.validate();
    checkNotNull(childJobId, "childJobId");
    checkNotNull(parentJobId, "parentJobId");

    // TODO(arjuns): Add more validations for JsonPayLoad.

    return this;
  }

  public JobId getChildJobId() {
    return childJobId;
  }

  public JobId getParentJobId() {
    return parentJobId;
  }

  public String getJsonPayLoad() {
    return jsonPayLoad;
  }

  public static class Builder extends AbstractNotification.Builder<Builder> {
    private JobId childJobId;
    private JobId parentJobId;
    private String jsonPayLoad;

    public Builder childJobId(JobId childJobId) {
      this.childJobId = childJobId;
      return this;
    }

    public Builder parentJobId(JobId parentJobId) {
      this.parentJobId = parentJobId;
      return this;
    }

    public Builder jsonPayLoad(String jsonPayLoad) {
      this.jsonPayLoad = jsonPayLoad;
      return this;
    }

    @SuppressWarnings("synthetic-access")
    public ChildJobCompletionNotification build() {
      return new ChildJobCompletionNotification(this);
    }
  }

  @SuppressWarnings("synthetic-access")
  private ChildJobCompletionNotification(Builder builder) {
    super(builder);
    this.childJobId = builder.childJobId;
    this.parentJobId = builder.parentJobId;
    this.jsonPayLoad = builder.jsonPayLoad;
  }

  // For JAXB
  private ChildJobCompletionNotification() {
    super(null);
  }
}
