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

import com.google.light.server.constants.http.ContentTypeEnum;
import com.google.light.server.dto.AbstractDto;

/**
 * DTO for Light Modules.
 * 
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class GSBlobInfo extends AbstractDto<GSBlobInfo> {
  private ContentTypeEnum contentType;
  private String fileName;
  private String gsKey;
  private Long sizeInBytes;

  /** 
   * {@inheritDoc}
   */
  @Override
  public GSBlobInfo validate() {
    checkNotNull(contentType, "contentType");
    checkNotBlank(fileName, "fileName");
    checkNotBlank(gsKey, "gsKey");
    // Getting from Zip. so not validating it as it may be incorrect.
//    checkPositiveLong(sizeInBytes, "sizeInBytes");
    return this;
  }

  public ContentTypeEnum getContentType() {
    return contentType;
  }

  public String getFileName() {
    return fileName;
  }

  public String getGsKey() {
    return gsKey;
  }

  public Long getSizeInBytes() {
    return sizeInBytes;
  }
  
  public static class Builder extends AbstractDto.BaseBuilder<Builder> {
    private ContentTypeEnum contentType;
    private String fileName;
    private String gsKey;
    @SuppressWarnings("unused")
    private String md5;
    private Long sizeInBytes;

    public Builder contentType(ContentTypeEnum contentType) {
      this.contentType = contentType;
      return this;
    }

    public Builder fileName(String fileName) {
      this.fileName = fileName;
      return this;
    }

    public Builder gsKey(String gsKey) {
      this.gsKey = gsKey;
      return this;
    }

    public Builder md5(String md5) {
      this.md5 = md5;
      return this;
    }

    public Builder sizeInBytes(Long sizeInBytes) {
      this.sizeInBytes = sizeInBytes;
      return this;
    }
    
    @SuppressWarnings("synthetic-access")
    public GSBlobInfo build() {
      return new GSBlobInfo(this).validate();
    }
  }

  @SuppressWarnings("synthetic-access")
  private GSBlobInfo(Builder builder) {
    super(builder);
    this.contentType = builder.contentType;
    this.fileName = builder.fileName;
    this.gsKey = builder.gsKey;
    this.sizeInBytes = builder.sizeInBytes;
  }
  
  // For JAXB
  private GSBlobInfo() {
    super(null);
  }
}
