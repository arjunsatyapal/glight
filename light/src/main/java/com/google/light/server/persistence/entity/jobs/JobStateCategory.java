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
package com.google.light.server.persistence.entity.jobs;

import javax.xml.bind.annotation.XmlEnumValue;

/**
 *
 * 
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */
public enum JobStateCategory {
  @XmlEnumValue(value = "RUNNING")
  RUNNING,
  
  @XmlEnumValue(value = "STOPPED")
  STOPPED,
  
  @XmlEnumValue(value = "COMPLETE_SUCCESS")
  COMPLETE_SUCCESS,
  
  @XmlEnumValue(value = "FAILED")
  FAILED,
  
  @XmlEnumValue(value = "CLEANUP")
  CLEANUP,

}
