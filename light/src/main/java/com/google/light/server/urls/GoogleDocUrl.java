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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.light.server.dto.module.ModuleType.getByProviderServiceAndCategory;

import com.google.light.server.constants.OAuth2ProviderService;
import com.google.light.server.dto.module.ModuleType;
import java.net.URL;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class GoogleDocUrl implements ExternalIdUrlInterface {
  private String scheme;
  private int port;
  private String host;
  private String domain;
  private ModuleType moduleType;
  private String typedResourceId;

  public GoogleDocUrl(URL url) {
    this.scheme = url.getProtocol();
    this.port = url.getPort();
    this.host = url.getHost();
    initrest(url.getFile(), url);
  }

  /**
   * @param path
   */
  private void initrest(String file, URL url) {
    if (file.startsWith("/a")) {
      initUsingDasher(file, url);
    } else {
      initUsingPublic(file, url);
    }
  }

  /**
   * @param path
   */
  private void initUsingPublic(String file, URL url) {
    String parts[] = file.split("/");
    checkArgument(parts.length >= 1, "Invalid URL " + url);
    domain = null;
    initRest(parts, 1, url);
  }

  /**
   * @param path
   */
  private void initUsingDasher(String file, URL url) {
    String parts[] = file.split("/");
    checkArgument(parts.length >= 2, "Invalid URL " + url);

    domain = parts[1];
    initRest(parts, 3, url);
  }

  /**
   * @param string
   */
  private void initRest(String[] parts, int startIndex, URL url) {
    String ref = url.getRef();
    if (ref != null && ref.startsWith(ModuleType.GOOGLE_COLLECTION.getCategory())) {
      this.moduleType = ModuleType.GOOGLE_COLLECTION;
    } else if (ref != null && ref.startsWith("advanced-search")) {
      throw new IllegalArgumentException("Urls with Search are not allowed. " + url);
    } else {

      if (parts[startIndex].startsWith(ModuleType.GOOGLE_DRAWING.getCategory())) {
        this.moduleType = ModuleType.GOOGLE_DRAWING;
      } else {
        this.moduleType = getByProviderServiceAndCategory(
            OAuth2ProviderService.GOOGLE_DOC, parts[startIndex]);
      }
    }

    switch (moduleType) {
      case GOOGLE_COLLECTION:
        String folderKey = getFolderKey(ref, url);
        this.typedResourceId = moduleType.getCategory() + ":" + folderKey;
        break;

      case GOOGLE_SPREADSHEET:
        String spreadSheetKey = getSpreadSheetKey(url);
        this.typedResourceId = moduleType.getCategory() + ":" + spreadSheetKey;
        break;

      case GOOGLE_DOCUMENT:
        //$FALL-THROUGH$
      case GOOGLE_DRAWING:
        //$FALL-THROUGH$
        
      case GOOGLE_FILE:
        //$FALL-THROUGH$
      case GOOGLE_PRESENTATION:
        this.typedResourceId = moduleType.getCategory() + ":" + parts[startIndex + 2];
        break;
      default:
        throw new IllegalArgumentException("Unsupported " + moduleType + " for URL : " + url);
    }
  }

  /**
   * @param ref
   * @return
   */
  private String getFolderKey(String ref, URL url) {
    String parts[] = ref.split("/");
    checkArgument(parts.length >= 2, "Invalid Folder URL : " + url);

    if (parts[1].contains(".")) {
      String subParts[] = parts[1].split("\\.");
      checkArgument(subParts.length >= 3, "Invalid Folder URL : " + url);
      return subParts[2];
    } else {
      return parts[1];
    }
  }

  /**
   * @param url
   * @return
   */
  private String getSpreadSheetKey(URL url) {
    String query = url.getQuery();
    String[] parts = query.split("&");
    checkArgument(parts.length >= 1, "Invalid SpreadSheet URL : " + url);
    checkArgument(parts[0].contains("key="), "Invalid SpreadSheet URL : " + url);
    String[] subParts = parts[0].split("=");
    checkArgument(subParts.length >= 2, "Invalid SpreadSheet URL : " + url);
    return subParts[1];
  }

  public String getScheme() {
    return scheme;
  }

  public int getPort() {
    return port;
  }

  public String getHost() {
    return host;
  }

  public String getDomain() {
    return domain;
  }

  @Override
  public ModuleType getModuleType() {
    return moduleType;
  }

  public String getTypedResourceId() {
    return typedResourceId;
  }
}
