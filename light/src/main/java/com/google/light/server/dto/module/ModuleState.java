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
package com.google.light.server.dto.module;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.xml.bind.annotation.XmlEnumValue;

/**
 * Enum to encapsulate different possible states for Modules.
 * 
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */
public enum ModuleState {
  @XmlEnumValue(value = "FAILED")
  FAILED(ModuleStateCategory.RETRY),
  
  @XmlEnumValue(value = "RESERVED")
  RESERVED(ModuleStateCategory.HOSTED),
  
  @XmlEnumValue(value = "IMPORTING")
  IMPORTING(ModuleStateCategory.HOSTED),
  
  @XmlEnumValue(value = "REFRESHING")
  REFRESHING(ModuleStateCategory.HOSTED),
  
  @XmlEnumValue(value = "PUBLISHED")
  PUBLISHED(ModuleStateCategory.HOSTED),
  
//  @XmlEnumValue(value = "UNKNOWN")
//  UNKNOWN(ModuleStateCategory.RETRY),
  
  @XmlEnumValue(value = "NOT_SUPPORTED")
  NOT_SUPPORTED(ModuleStateCategory.NOT_HOSTED);
  
  private ModuleStateCategory category;
  
  private ModuleState(ModuleStateCategory category) {
    this.category = checkNotNull(category, "category");
  }
  
  public ModuleStateCategory getCategory() {
    return category;
  }
}
