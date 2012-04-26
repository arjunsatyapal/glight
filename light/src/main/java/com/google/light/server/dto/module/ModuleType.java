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
package com.google.light.server.dto.module;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;

import com.google.light.server.constants.OAuth2ProviderService;
import com.google.light.server.constants.http.ContentTypeEnum;

/**
 * Types of Modules supported by Light.
 * 
 * TODO(arjuns): Add test for this class.
 * TODO(arjuns): Add integration test to verify the category.
 * TODO(arjuns): Add tests for uniqueness of combination of ProviderService and category.
 * 
 * @author Arjun Satyapal
 */
public enum ModuleType {
  GOOGLE_COLLECTION(
                    OAuth2ProviderService.GOOGLE_DOC,
                    "folder",
                    // TODO(arjuns):See what should be the content type enum for collections.
                    ContentTypeEnum.GOOGLE_DOC,
                    true),
  GOOGLE_DOC(
             OAuth2ProviderService.GOOGLE_DOC,
             "document",
             ContentTypeEnum.GOOGLE_DOC,
             true),
  GOOGLE_SPREADSHEET(
                     OAuth2ProviderService.GOOGLE_DOC,
                     "spreadsheet",
                     ContentTypeEnum.GOOGLE_SPREADSHEET,
                     false),
  GOOGLE_FORM(
              OAuth2ProviderService.GOOGLE_DOC,
              "form",
              ContentTypeEnum.GOOGLE_FORM,
              false),
  GOOGLE_PRESENTATION(
                      OAuth2ProviderService.GOOGLE_DOC,
                      "presentation",
                      ContentTypeEnum.GOOGLE_PRESENTATION,
                      false),
  GOOGLE_DRAWING(
                 OAuth2ProviderService.GOOGLE_DOC,
                 "drawing",
                 ContentTypeEnum.GOOGLE_DRAWING,
                 false),
  GOOGLE_FILE(
              OAuth2ProviderService.GOOGLE_DOC,
              "file",
              // TODO(arjuns): See what should be the content type.
              ContentTypeEnum.GOOGLE_DOC,
              false);

  private OAuth2ProviderService providerService;
  /*
   * Category of Document as provided by ProviderService. e.g. For Spreadsheets, Google Doc
   * identifies it as a spreadsheet.
   */
  private String category;
  private ContentTypeEnum contentType;
  private boolean supported;

  private ModuleType(OAuth2ProviderService providerService, String category,
      ContentTypeEnum contentType, boolean supported) {
    this.providerService = checkNotNull(providerService, "providerService");
    this.category = checkNotBlank(category, "category");
    this.contentType = checkNotNull(contentType, "contentType");
    this.supported = supported;
  }

  public OAuth2ProviderService getProviderService() {
    return providerService;
  }

  public String getCategory() {
    return category;
  }

  public ContentTypeEnum getContentType() {
    return contentType;
  }

  public boolean isSupported() {
    return supported;
  }

  public static ModuleType getByProviderServiceAndCategory(OAuth2ProviderService providerService,
      String category) {
    for (ModuleType curr : ModuleType.values()) {
      if (curr.providerService == providerService) {
        if (curr.getCategory().equals(category)) {
          return curr;
        }
      }
    }

    throw new EnumConstantNotPresentException(ModuleType.class, providerService + ":" + category);
  }
}
