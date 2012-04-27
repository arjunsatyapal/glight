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
package com.google.light.server.servlets.thirdparty.google.gdoc;

import static org.junit.Assert.assertEquals;

import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocResourceId;
import java.net.URL;
import org.junit.Test;

/**
 * Test for {@link GoogleDocUtils}
 * 
 * @author Arjun Satyapal
 */
public class GoogleDocUtilsTest {
  /**
   * Test for {@link GoogleDocUtils#getArchiveStatusUrl(String)}.
   * 
   * @throws Exception
   */
  @Test
  public void test_getArchiveStatusUrl() throws Exception {
    String archiveId = "1234";
    String expected = "https://docs.google.com/feeds/default/private/archive/1234";
    URL expectedUrl = new URL(expected);
    assertEquals(expectedUrl, GoogleDocUtils.getArchiveStatusUrl(archiveId));
  }

  /**
   * Test for {@link GoogleDocUtils#getArchiveUrl()}.
   * 
   * @throws Exception
   */
  @Test
  public void test_getArchiveUrl() throws Exception {
    String expected = "https://docs.google.com/feeds/default/private/archive";
    URL expectedUrl = new URL(expected);
    assertEquals(expectedUrl, GoogleDocUtils.getArchiveUrl());
  }

  /**
   * Test for {@link GoogleDocUtils#getDocumentFeedWithFolderUrl()}.
   * 
   * @throws Exception
   */
  @Test
  public void test_getDocumentFeedWithFolderUrl() throws Exception {
    String expected = "https://docs.google.com/feeds/default/private/full" + 
        "?showroots=true&showfolders=true&max-results=3";
    URL expectedUrl = new URL(expected);
    assertEquals(expectedUrl, GoogleDocUtils.getDocumentFeedWithFolderUrl());
  }
  
  /**
   * Test for {@link GoogleDocUtils#getResourceAclFeedUrl(GoogleDocResourceId))}.
   */
  @Test
  public void test_getResourceAclFeedUrl() throws Exception {
    GoogleDocResourceId resourceId = new GoogleDocResourceId("document:1234");
    String expected = "https://docs.google.com/feeds/default/private/full/document:1234/acl";
    URL expectedUrl = new URL(expected);
    assertEquals(expectedUrl, GoogleDocUtils.getResourceAclFeedUrl(resourceId));
  }
  
  /**
   * Test for {@link GoogleDocUtils#getResourceEntryWithFoldersUrl(GoogleDocResourceId))}.
   */
  @Test
  public void test_getResourceEntryWithFoldersUrl() throws Exception {
    GoogleDocResourceId resourceId = new GoogleDocResourceId("document:1234");
    String expected = "https://docs.google.com/feeds/default/private/full/document:1234"
          + "?showroots=true&showfolders=true";
    URL expectedUrl = new URL(expected);
    assertEquals(expectedUrl, GoogleDocUtils.getResourceEntryWithFoldersUrl(resourceId));
  }
  
  /**
   * Test for {@link GoogleDocUtils#getResourceEntryWithAclFeedUrl(GoogleDocResourceId))}.
   */
  @Test
  public void test_getResourceEntryWithAclFeedUrl() throws Exception {
    GoogleDocResourceId resourceId = new GoogleDocResourceId("document:1234");
    String expected = "https://docs.google.com/feeds/default/private/full/document:1234"
          + "?showroots=true&showfolders=true&expand-acl=true";
    URL expectedUrl = new URL(expected);
    assertEquals(expectedUrl, GoogleDocUtils.getResourceEntryWithAclFeedUrl(resourceId));
  }
  
  /**
   * Test for {@link GoogleDocUtils#getResourceEntryUrl(GoogleDocResourceId)}.
   */
  @Test
  public void test_getResourceEntryUrl() throws Exception {
    GoogleDocResourceId resourceId = new GoogleDocResourceId("document:1234");
    String expected = "https://docs.google.com/feeds/default/private/full/document:1234";
    URL expectedUrl = new URL(expected);
    assertEquals(expectedUrl, GoogleDocUtils.getResourceEntryUrl(resourceId));
  }
  
  /**
   * Test for {@link GoogleDocUtils#getUserAccountInfoUrl())}.
   */
  @Test
  public void test_getUserAccountInfoUrl() throws Exception {
    String expected = "https://docs.google.com/feeds/metadata/default";
    URL expectedUrl = new URL(expected);
    assertEquals(expectedUrl, GoogleDocUtils.getUserAccountInfoUrl());
  }
  
  /**
   * Test for {@link GoogleDocUtils#getFolderContentUrl(GoogleDocResourceId)))}.
   */
  @Test
  public void test_getFolderContentUrl() throws Exception {
    GoogleDocResourceId resourceId = new GoogleDocResourceId("folder:1234");
    String expected = "https://docs.google.com/feeds/default/private/full/folder:1234/contents?max-results=3";
    URL expectedUrl = new URL(expected);
    assertEquals(expectedUrl, GoogleDocUtils.getFolderContentUrl(resourceId));
  }
}
