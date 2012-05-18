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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;

import com.google.light.server.constants.http.ContentTypeEnum;

/**
 *
 * 
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */
public enum FileExtensions {
  HTML("html", ContentTypeEnum.TEXT_HTML),
  JPG("jpg", ContentTypeEnum.IMAGE_JPEG),
  JPEG("jpeg", ContentTypeEnum.IMAGE_JPEG),
  PNG("png", ContentTypeEnum.IMAGE_PNG),
  ZIP("zip", ContentTypeEnum.APPLICATION_ZIP);
  
  private String extension;
  private ContentTypeEnum contentType;
  
  private FileExtensions(String extension, ContentTypeEnum contentType) {
    this.extension = checkNotBlank(extension, "extension");
    this.contentType = checkNotNull(contentType, "contentType");
  }
  
  public String get() {
    return extension;
  }
  
  public ContentTypeEnum getContentType() {
    return contentType;
  }
  
  public static FileExtensions getFileExtension(String fileName) {
    for (FileExtensions currFileExtension : FileExtensions.values()) {
      if (fileName.endsWith(currFileExtension.get())) {
        return currFileExtension;
      }
    }
    
    throw new IllegalArgumentException("Unsupported fileType : " + fileName);
  }
  
  public static String appendExtensionToFileName(String fileName, FileExtensions requiredExt) {
    String requiredSuffix = "." + requiredExt.get();
    if (fileName.endsWith(requiredSuffix)) {
      return fileName;
    } else {
      return fileName + requiredSuffix;
    }
      
    
    
  }
}
