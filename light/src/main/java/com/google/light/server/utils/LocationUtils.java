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

import static com.google.light.server.utils.LightUtils.getWrapperValue;

import com.google.light.server.constants.JerseyConstants;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.JobId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.ModuleId;
import java.net.URI;

/**
 *
 * 
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */
public class LocationUtils {
  public static URI getJobLocation(JobId jobId) {
    String location = JerseyConstants.URI_RESOURCE_JOB + "/" + jobId.getValue();
    return LightUtils.getURI(location);
  }
  
  public static URI getModuleLocation(ModuleId moduleId) {
    // Find a better way to figure out the name.
    
    URIBuilder uriBuilder = new URIBuilder();
    uriBuilder.withScheme("http");
    
    if (GaeUtils.isDevServer()) {
      uriBuilder.withServer("localhost");
      uriBuilder.withPort("8080");
    } else {
      String appId = GaeUtils.getAppId();
      String appSpot = appId + ".appspot.com";
      uriBuilder.withServer(appSpot);
    }
    
    uriBuilder.append("/rest/content/general/module").append("" + getWrapperValue(moduleId)).append("/latest");
    
    return LightUtils.getURI(uriBuilder.toString());
  }
  
  public static enum LocationType {
    JOB;
  }
}
