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
package com.google.light.server.utils;

import com.google.light.server.constants.JerseyConstants;
import com.google.light.server.dto.pojo.longwrapper.JobId;
import java.net.URI;

/**
 *
 * 
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */
public class LocationHeaderUtils {
  public static URI getJobLocation(JobId jobId) {
    String location = JerseyConstants.URI_RESOURCE_JOB + "/" + jobId.getValue();
    return LightUtils.getURI(location);
  }
  
  public static enum LocationType {
    JOB;
  }
}
