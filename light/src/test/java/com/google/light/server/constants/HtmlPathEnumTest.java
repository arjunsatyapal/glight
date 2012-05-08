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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Sets;
import com.google.light.testingutils.TestingUtils;
import java.util.Set;
import org.junit.Test;

/**
 * Test for {@link HtmlPathEnum}.
 *
 * @author Arjun Satyapal
 */
public class HtmlPathEnumTest implements EnumTestInterface {
  private String WEBAPP = "webapp";
  /** 
   * {@inheritDoc}
   */
  @Test
  @Override
  public void test_count() {
    Set<String> requiredFiles = findLightHtmlFiles();
    assertEquals("Add more tests as required.", 5, HtmlPathEnum.values().length);
    
    for (String currFile : requiredFiles) {
      int len = currFile.indexOf(WEBAPP) + WEBAPP.length();
      String path = currFile.substring(len);
      assertTrue(path + " is missing from " + HtmlPathEnum.class.getSimpleName(),
          HtmlPathEnum.contains(path));
    }
  }
  
  private Set<String> findLightHtmlFiles() {
    Set<String> setOfFiles = TestingUtils.findAllFilesUnderLight();
    
    Set<String> requiredFiles = Sets.newHashSet();
    for (String currFile : setOfFiles) {
      if(currFile.contains(WEBAPP) && currFile.endsWith(".html")) {
        requiredFiles.add(currFile);
      }
    }
    
    return requiredFiles;
  }
}
