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
package com.google.light.server.servlets.thirdparty.google.gdata.gdoc;

import static com.google.light.server.constants.LightConstants.GDATA_MAX_RESULT;
import static com.google.light.server.utils.LightUtils.getURL;

import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocResourceId;
import java.net.URL;
import java.util.logging.Logger;

/**
 * Utility class for Google Docs.
 * 
 * TODO(arjuns): Add a URL builder.
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class GoogleDocUtils {
  private static final Logger logger = Logger.getLogger(GoogleDocUtils.class.getName());

  private static final String DOCUMENT_FEED_BASE_URL =
      "https://docs.google.com/feeds/default/private/full";
  private static final String ACL_FEED_BASE_URL =
      "https://docs.google.com/feeds/default/private/full";
  private static final String ARCHIVE_URL = "https://docs.google.com/feeds/default/private/archive";

  private static final String USER_ACCOUNT_INFO_URL =
      "https://docs.google.com/feeds/metadata/default";

  private static String SHOW_FOLDERS = "showfolders=true";
  private static String SHOW_ROOTS = "showroots=true";
  private static String EXPAND_ACL = "expand-acl=true";
  private static String MAX_RESULTS = "max-results=" + GDATA_MAX_RESULT;

  public static URL getUserAccountInfoUrl() {
    return getURL(USER_ACCOUNT_INFO_URL);
  }

  public static URL getDocumentFeedWithFolderUrl() {
    return getURL(DOCUMENT_FEED_BASE_URL + "?" + SHOW_FOLDERS + "&" + SHOW_ROOTS + "&"
        + MAX_RESULTS);
  }

  public static URL getArchiveUrl() {
    return getURL(ARCHIVE_URL);
  }
  
  public static URL getArchiveStatusUrl(String archiveId) {
    return getURL(ARCHIVE_URL + "/" + archiveId);
  }

  public static URL getResourceEntryUrl(GoogleDocResourceId resourceId) {
    return getURL(DOCUMENT_FEED_BASE_URL + "/" + resourceId.getTypedResourceId() + "?" + SHOW_ROOTS);
  }

  public static URL getResourceEntryWithAclFeedUrl(GoogleDocResourceId resourceId) {
    URL url = getResourceEntryUrl(resourceId);
    return getURL(url.toString() + "&" + EXPAND_ACL);
  }

  public static URL getResourceAclFeedUrl(GoogleDocResourceId resourceId) {
    logger.warning("Ideally, ACL Feed link should be fetched from the ResourceEntry. " +
        "This can break if Google Changes some logic.");
    return getURL(ACL_FEED_BASE_URL + "/" + resourceId.getTypedResourceId() + "/acl");
  }
}
