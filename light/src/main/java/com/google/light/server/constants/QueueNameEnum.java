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
 * Enum to encapsulate AppEngine queues.
 * 
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */
public enum QueueNameEnum {
  GOOGLE_DOC_IMPORT("google-doc-import");
  
  private String name;

  // TODO(arjuns): Add regex check : [a-zA-Z\d-]{1,100}
  private QueueNameEnum(String name) {
    this.name = checkNotBlank(name, "queueName");
  }
  
  public String getName() {
    return name;
  }
  
  public static QueueNameEnum getByName(String queueName) {
    for (QueueNameEnum curr : QueueNameEnum.values()) {
      if (curr.getName().equals(queueName)) {
        return curr;
      }
    }
    
    throw new EnumConstantNotPresentException(QueueNameEnum.class, queueName);
  }
}
