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

import static com.google.light.server.utils.LightPreconditions.checkPositiveLong;

import com.google.light.server.dto.AbstractPojo;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class ModuleId extends AbstractPojo<ModuleId> {
  private Long id;
  
  public ModuleId(Long moduleId) {
    this.id = moduleId;
    validate();
  }
  
  public ModuleId(String moduleId) {
    this(Long.parseLong(moduleId));
  }
  
  public Long get() {
    return id;
  }

  @Override
  public String toString() {
    return "moduleId:" + id;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public ModuleId validate() {
    checkPositiveLong(id, "id");
    
    return this;
  }
  
  // For Objectify.
  @SuppressWarnings("unused")
  private ModuleId() {
  }
}
