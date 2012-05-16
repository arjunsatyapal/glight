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
package com.google.light.server.constants;

import static com.google.light.server.utils.LightPreconditions.checkNotBlank;

/**
 *
 * 
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */
public enum QueueEnum {
  GDOC_INTERACTION("gdoc-interaction"),
  // TODO(waltercacau) : Fix the name.
//  GSS_PAGEMAP_UPDATE("gss_pagemap_update"),
  LIGHT("light"),
  LIGHT_NOTIFICATIONS("light-notification"),
  LIGHT_POLLING("light-polling");
  
  private String name;
  
  private QueueEnum(String name) {
    this.name = checkNotBlank(name, "queue name");
  }
  
  public String getName() {
    return name;
  }
  
  public static QueueEnum getByName(String name) {
    for (QueueEnum curr : QueueEnum.values()) {
      if (curr.getName().equals(name)) {
        return curr;
      }
    }
    
    throw new EnumConstantNotPresentException(QueueEnum.class, name);
  }
}
