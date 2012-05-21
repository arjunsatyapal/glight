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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.light.server.constants.LightConstants.GDATA_GDOC_MAX_RESULTS;
import static com.google.light.server.utils.LightUtils.getURL;

import com.google.light.server.dto.thirdparty.google.gdoc.GoogleDocResourceId;

import com.google.common.base.Preconditions;
import com.google.light.server.constants.LightConstants;
import java.net.URL;
import java.util.logging.Logger;

/**
 * Utility class for Google Docs.
 * 
 * TODO(arjuns): Add a URL builder.
 * 
 * @author Arjun Satyapal
 */
public class GoogleDocUtils {
  private static final Logger logger = Logger.getLogger(GoogleDocUtils.class.getName());

  private static final String DOCUMENT_FEED_BASE_URL =
      "https://docs.google.com/feeds/default/private/full";
  private static final String ARCHIVE_URL = "https://docs.google.com/feeds/default/private/archive";

  private static final String USER_ACCOUNT_INFO_URL =
      "https://docs.google.com/feeds/metadata/default";

  private static String SHOW_FOLDERS = "showroots=true&showfolders=true";
  private static String EXPAND_ACL = "expand-acl=true";

  /**
   * Get URL for fetching User Account Information.
   */
  public static URL getUserAccountInfoUrl() {
    return getURL(USER_ACCOUNT_INFO_URL);
  }

  /**
   * Get Document Feed for a User with both RootFolder and Folders enabled.
   */
  public static URL getDocumentFeedWithFolderUrl(int maxResults) {
    return getURL(DOCUMENT_FEED_BASE_URL + "?" + SHOW_FOLDERS + "&" + getMaxResults(maxResults));
  }

  public static URL getDocumentFeedWithFolderUrlAndFilter(int maxResults, String query) {
    return getURL(getDocumentFeedWithFolderUrl(maxResults) + "&q=" + query);
  }

  /**
   * Get Archive URL.
   */
  public static URL getArchiveUrl() {
    return getURL(ARCHIVE_URL);
  }

  /**
   * Get URL to fetch Status for an Archive.
   */
  public static URL getArchiveStatusUrl(String archiveId) {
    return getURL(ARCHIVE_URL + "/" + archiveId);
  }

  /**
   * Get URL for a Resource.
   */
  protected static URL getResourceEntryUrl(GoogleDocResourceId resourceId) {
    return getURL(DOCUMENT_FEED_BASE_URL + "/" + resourceId.getTypedResourceId());
  }

  /**
   * Get URL to fetch GData Entry for a Resource.
   */
  public static URL getResourceEntryWithFoldersUrl(GoogleDocResourceId resourceId) {
    URL url = getResourceEntryUrl(resourceId);
    return getURL(url.toString() + "?" + SHOW_FOLDERS);
  }

  /**
   * Get URL to fetch GData Entry for a resource with ACLs.
   */
  public static URL getResourceEntryWithAclFeedUrl(GoogleDocResourceId resourceId) {
    URL url = getResourceEntryWithFoldersUrl(resourceId);
    return getURL(url.toString() + "&" + EXPAND_ACL);
  }

  /**
   * Get ACL feed for a Resource.
   */
  public static URL getResourceAclFeedUrl(GoogleDocResourceId resourceId) {
    logger.warning("Ideally, ACL Feed link should be fetched from the ResourceEntry. " +
        "This can break if Google Changes some logic.");
    URL url = getResourceEntryUrl(resourceId);
    return getURL(url.toString() + "/acl");
  }

  public static URL getFolderContentUrl(GoogleDocResourceId resourceId, int maxResult) {
    Preconditions.checkArgument(resourceId.getModuleType().mapsToCollection(),
        "Invalid GoogleResource[" + resourceId + "].");
    URL url = getResourceEntryUrl(resourceId);
    return getURL(url.toString() + "/contents" + "?" + getMaxResults(maxResult));
  }

  public static String getMaxResults(int maxResults) {
    checkArgument(maxResults > 0 && maxResults <= LightConstants.GDATA_GDOC_MAX_RESULTS,
        "GDATA API for Google Doc supports MaxResults between (1, " + GDATA_GDOC_MAX_RESULTS
            + "). Invalid MaxResults : " + maxResults);
    return "max-results=" + maxResults;

  }
}
