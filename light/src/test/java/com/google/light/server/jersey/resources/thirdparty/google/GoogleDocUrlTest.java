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
package com.google.light.server.jersey.resources.thirdparty.google;

import static com.google.light.server.utils.LightUtils.getURL;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.Lists;
import com.google.light.server.dto.module.ModuleType;
import java.net.URL;
import java.util.List;
import org.junit.Test;

/**
 * Test for {@link GoogleDocUrl}
 *
 * @author Arjun Satyapal
 */
public class GoogleDocUrlTest {
  /**
   * Test for {@link ModuleType#GOOGLE_FOLDER}.
   */
  @Test
  public void test_googleCollectionUrl() {
    List<URL> urls = Lists.newArrayList(
        getURL("https://drive.google.com/?authuser=0#folders/f1234"),
        getURL("https://drive.google.com/a/myopenedu.com/?pli=1#folders/f1234"),
        getURL("https://drive.google.com/a/foo.com/#folders/f1234"),
        getURL("https://docs.google.com/a/myopenedu.com/#folders/folder.0.f1234"));
    
    for (URL currUrl : urls) {
      GoogleDocUrl gdocUrl = new GoogleDocUrl(currUrl);
      assertEquals(currUrl.toString(), ModuleType.GOOGLE_COLLECTION, gdocUrl.getModuleType());
      assertEquals(currUrl.toString(), "folder:f1234", 
          gdocUrl.getResourceId().getTypedResourceId());
    }
  }
  
  @Test
  public void test_googleDocumentURL() {
    List<URL> urls = Lists.newArrayList(
        getURL("https://docs.google.com/document/d/d1234/edit?pli=1#x=3"),
        getURL("https://docs.google.com/a/myopenedu.com/document/d/d1234/edit"));
    
    for (URL currUrl : urls) {
      GoogleDocUrl gdocUrl = new GoogleDocUrl(currUrl);
      assertEquals(currUrl.toString(), ModuleType.GOOGLE_DOCUMENT, gdocUrl.getModuleType());
      assertEquals(currUrl.toString(), "document:d1234", 
          gdocUrl.getResourceId().getTypedResourceId());
    }
  }
  
  @Test
  public void test_googleDrawingURL() {
    List<URL> urls = Lists.newArrayList(
        getURL("https://docs.google.com/drawings/d/dr1234/edit"),
        getURL("https://docs.google.com/a/myopenedu.com/drawings/d/dr1234/edit"));
    
    for (URL currUrl : urls) {
      GoogleDocUrl gdocUrl = new GoogleDocUrl(currUrl);
      assertEquals(currUrl.toString(), ModuleType.GOOGLE_DRAWING, gdocUrl.getModuleType());
      assertEquals(currUrl.toString(), "drawing:dr1234", 
          gdocUrl.getResourceId().getTypedResourceId());
    }
  }
  
  @Test
  public void test_googleFileURL() {
    List<URL> urls = Lists.newArrayList(
        getURL("https://docs.google.com/file/d/fl1234/edit"),
        getURL("https://docs.google.com/a/myopenedu.com/file/d/fl1234/edit"));
    
    for (URL currUrl : urls) {
      GoogleDocUrl gdocUrl = new GoogleDocUrl(currUrl);
      assertEquals(currUrl.toString(), ModuleType.GOOGLE_FILE, gdocUrl.getModuleType());
      assertEquals(currUrl.toString(), "file:fl1234", 
          gdocUrl.getResourceId().getTypedResourceId());
    }
  }
  
//  @Test
//  public void test_googleFormURL() {
//    List<URL> urls = Lists.newArrayList(
//        checkValidURL("https://docs.google.com/spreadsheet/gform?key=fr1234&hl=en_US"),
//        checkValidURL("https://docs.google.com/a/myopenedu.com/spreadsheet/viewform?formkey=dDZPc3UwY3pWQ1dtemI4U1did3N0bWc6MQ"));
//    
//    for (URL currUrl : urls) {
//      GoogleDocUrl gdocUrl = new GoogleDocUrl(currUrl);
//      assertEquals(currUrl.toString(), ModuleType.GOOGLE_DRAWING, gdocUrl.getModuleType());
//      assertEquals(currUrl.toString(), "form:fr1234", 
//          gdocUrl.getResourceId().getTypedResourceId());
//    }
//  }
  
  @Test
  public void test_googlePresentationURL() {
    List<URL> urls = Lists.newArrayList(
        getURL("https://docs.google.com/presentation/d/p1234/edit#slide=id.g3a9dc55_0_22"),
        getURL("https://docs.google.com/a/myopenedu.com/presentation/d/p1234/edit#slide=id.gca62ceb_0_0"));
    
    for (URL currUrl : urls) {
      GoogleDocUrl gdocUrl = new GoogleDocUrl(currUrl);
      assertEquals(currUrl.toString(), ModuleType.GOOGLE_PRESENTATION, gdocUrl.getModuleType());
      assertEquals(currUrl.toString(), "presentation:p1234", 
          gdocUrl.getResourceId().getTypedResourceId());
    }
  }
  
  @Test
  public void test_googleSpreadsheetURL() {
    List<URL> urls = Lists.newArrayList(
        getURL("https://docs.google.com/spreadsheet/ccc?key=s1234#gid=2"),
        getURL("https://docs.google.com/a/myopenedu.com/spreadsheet/ccc?key=s1234#gid=0"));
    
    for (URL currUrl : urls) {
      GoogleDocUrl gdocUrl = new GoogleDocUrl(currUrl);
      assertEquals(currUrl.toString(), ModuleType.GOOGLE_SPREADSHEET, gdocUrl.getModuleType());
      assertEquals(currUrl.toString(), "spreadsheet:s1234", 
          gdocUrl.getResourceId().getTypedResourceId());
    }
  }
  

  
  
//  public void test_unsupportedUrlFormats() {
//    checkValidURL("https://drive.google.com/a/myopenedu.com/?pli=1#advanced-search/q=asdf&view=2&parent=0B15KDir5QLAcWlhUb2ZfdkttWjQ"));
//
//  }
}
