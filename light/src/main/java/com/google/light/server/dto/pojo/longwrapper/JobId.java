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
package com.google.light.server.dto.pojo.longwrapper;

import static com.google.light.server.utils.LightPreconditions.checkPositiveLong;

/**
 * Wrapper for JobId.
 * 
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class JobId extends AbstractTypeWrapper<Long, JobId>{

  public JobId(String value) {
    this(Long.parseLong(value));
  }
  
  /**
   * @param value
   */
  public JobId(Long value) {
    super(value);
    validate();
  }

  /** 
   * {@inheritDoc}
   */
  @Override
  public JobId validate() {
    checkPositiveLong(getValue(), "InvalidJobId");
    return this;
  }

  // For Objectify and JAXB.
  private JobId() {
    super(null);
  }
}