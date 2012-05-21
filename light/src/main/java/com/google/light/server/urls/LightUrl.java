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
package com.google.light.server.urls;

import com.google.light.server.dto.module.ModuleType;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.ModuleId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.Version;
import com.google.light.server.utils.LightUtils;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class LightUrl implements ExternalIdUrlInterface {
  private static final String MODULE_IDENTIFIER = "/rest/content/general/module/";
  private static final String COLLECTION_IDENTIFIER = "/rest/content/general/collection/";

  private static String modulePatternRegex = "^" + MODULE_IDENTIFIER + "(\\d+)/((latest)|(\\d+))[/]*";
  private static Pattern modulePattern = Pattern.compile(modulePatternRegex);
  
  private static String collectionPatternRegex = "^" + COLLECTION_IDENTIFIER + "(\\d+)/(latest|\\d+)/((\\d+))[/]*";
  private static Pattern collectionPattern = Pattern.compile(collectionPatternRegex);

  private ModuleId moduleId;
  private Version version;

  public LightUrl(URL url) {
    initrest(url.getFile(), url);
  }

  @Override
  public ModuleType getModuleType() {
    return ModuleType.LIGHT_HOSTED_MODULE;
  }

  public ModuleId getModuleId() {
    return moduleId;
  }

  public Version getVersion() {
    return version;
  }
  
  /**
   * @param path
   */
  private void initrest(String file, URL url) {
    if (file.startsWith(MODULE_IDENTIFIER)) {
      initUsingModule(file, url);
    } else if (file.startsWith(COLLECTION_IDENTIFIER)) {
      initUsingCollection(file, url);
    } else {
      throw new IllegalArgumentException("Invalid classification");
    }
  }
  
  /**
   * Module url are of the format : <br>
   * /rest/content/general/module/{moduleId}/latest <br>
   * /rest/content/general/module/{moduleId}/{version}. <br>
   * So in both the cases, group(1) will be the moduleId and group(2) will be version.
   */
  private void initUsingModule(String file, URL url) {
    Matcher matcher = modulePattern.matcher(file);
    if (matcher.matches()) {
      moduleId = new ModuleId(matcher.group(1));
      version = new Version(matcher.group(2));
    } else {
      throw new IllegalArgumentException("Invalid Light URL : " + url);
    }
  }
  
  /**
   * @param file
   * @param url
   */
  private void initUsingCollection(String file, URL url) {
    Matcher matcher = collectionPattern.matcher(file);
    if (matcher.matches()) {
      moduleId = new ModuleId(matcher.group(3));
      // TODO(arjuns): Fix this hack once more information is available.
      version = LightUtils.LATEST_VERSION;
    } else {
      throw new IllegalArgumentException("Invalid Light URL : " + url);
    }
  }

}
