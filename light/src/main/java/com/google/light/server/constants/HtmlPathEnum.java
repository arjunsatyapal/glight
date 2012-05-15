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

import static com.google.light.server.utils.LightPreconditions.checkNotBlank;

import com.google.common.annotations.VisibleForTesting;

import com.google.common.base.Preconditions;

/**
 * Path to HTML files.
 * 
 * @author Arjun Satyapal
 */
public enum HtmlPathEnum {
  GOOGLE_DOC_IMPORT_BATCH("/html/test/thirdparty/google/gdoc/import_gdoc_batch.html"),
  GOOGLE_DOC_INFORMATION("/html/test/thirdparty/google/gdoc/information.html"),
  IMPORT_BATCH("/html/test/thirdparty/import/batch.html"),
  PUT_OAUTH2_CONSUMER_CREDENTIAL("/html/admin/put_oauth2_consumer_credential.html"),
  REST_CLIENT("/html/test/rest.html"),
  TEST("/test.html");

  private String path;

  private HtmlPathEnum(String path) {
    Preconditions.checkArgument(path.startsWith("/"), "Path should start with /.");
    this.path = checkNotBlank(path, "path");
  }

  public String get() {
    return path;
  }
  
  @VisibleForTesting
  static boolean contains(String path) {
    for (HtmlPathEnum curr : HtmlPathEnum.values()) {
      if (curr.path.equals(path)) {
        return true;
      }
    }
    
    return false;
  }
}
