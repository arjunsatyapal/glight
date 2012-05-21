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

import static org.junit.Assert.assertEquals;

import com.google.light.server.dto.module.ModuleType;

import com.google.common.collect.Lists;

import java.util.List;

import com.google.light.server.dto.pojo.typewrapper.longwrapper.ModuleId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.Version;
import com.google.light.server.utils.LightUtils;
import org.junit.Test;

/**
 * Test for {@link LightUrl}
 *
 * @author Arjun Satyapal
 */
public class LightUrlTest {
  @Test
  public void test_moduleUrls() throws Exception {
    List<String> listOfLatest = Lists.newArrayList(
        "http://localhost:8080/rest/content/general/module/1234/latest",
        "https://localhost:8080/rest/content/general/module/1234/latest/",
        
        "http://light-qa.appspot.com/rest/content/general/module/1234/latest",
        "https://light-qa.appspot.com/rest/content/general/module/1234/latest/",
        
        "http://light-qa1.appspot.com/rest/content/general/module/1234/latest",
        "https://light-qa1.appspot.com/rest/content/general/module/1234/latest/",
        
        "http://light-demo.appspot.com/rest/content/general/module/1234/latest",
        "https://light-demo.appspot.com/rest/content/general/module/1234/latest/",
        
        "http://light-prod.appspot.com/rest/content/general/module/1234/latest",
        "https://light-prod.appspot.com/rest/content/general/module/1234/latest/");

    for (String curr : listOfLatest) {
      doTestModuleUrl(curr, "1234", "latest");
    }
    
    List<String> listOfSpecificVersion = Lists.newArrayList(
        "http://localhost:8080/rest/content/general/module/1234/5678",
        "https://localhost:8080/rest/content/general/module/1234/5678/",
        
        "http://light-qa.appspot.com/rest/content/general/module/1234/5678",
        "https://light-qa.appspot.com/rest/content/general/module/1234/5678/",
        
        "http://light-qa1.appspot.com/rest/content/general/module/1234/5678",
        "https://light-qa1.appspot.com/rest/content/general/module/1234/5678/",
        
        "http://light-demo.appspot.com/rest/content/general/module/1234/5678",
        "https://light-demo.appspot.com/rest/content/general/module/1234/5678/",
        
        "http://light-prod.appspot.com/rest/content/general/module/1234/5678",
        "https://light-prod.appspot.com/rest/content/general/module/1234/5678/");
    
    for (String curr : listOfSpecificVersion) {
      doTestModuleUrl(curr, "1234", "5678");
    }
  }

  @Test
  public void test_collectionUrl() {
    List<String> listOfSpecificVersion = Lists.newArrayList(
        "http://localhost:8080/rest/content/general/collection/4321/latest/1234",
        "https://localhost:8080/rest/content/general/collection/4321/latest/1234/",

        "http://light-qa.appspot.com/rest/content/general/collection/4321/latest/1234",
        "https://light-qa.appspot.com/rest/content/general/collection/4321/latest/1234/",

        "http://light-qa1.appspot.com/rest/content/general/collection/4321/latest/1234",
        "https://light-qa1.appspot.com/rest/content/general/collection/4321/latest/1234/",
        
        "http://light-demo.appspot.com/rest/content/general/collection/4321/latest/1234",
        "https://light-demo.appspot.com/rest/content/general/collection/4321/latest/1234/",
        
        "http://light-prod.appspot.com/rest/content/general/collection/4321/latest/1234",
        "https://light-prod.appspot.com/rest/content/general/collection/4321/latest/1234/");
    
    for (String curr : listOfSpecificVersion) {
      doTestModuleUrl(curr, "1234", "latest");
    }
  }
  
  private void doTestModuleUrl(String url, String moduleIdStr, String versionStr) {
    ModuleId moduleId = new ModuleId(moduleIdStr);
    Version version = new Version(versionStr);
    
    LightUrl lightUrl = new LightUrl(LightUtils.getURL(url));
    assertEquals(moduleId, lightUrl.getModuleId());
    assertEquals(version, lightUrl.getVersion());
    assertEquals(ModuleType.LIGHT_HOSTED_MODULE, lightUrl.getModuleType());
  }
  
  
}
