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
package com.google.light.server.dto.pojo.wrappers;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.LightUtils.getWrapperValue;

import com.google.appengine.api.search.Document;
import com.google.light.server.constants.http.ContentTypeEnum;
import com.google.light.server.dto.pojo.typewrapper.stringwrapper.FTSDocumentId;
import com.google.light.server.utils.FTSUtils;
import org.joda.time.Instant;

/**
 * Wrapper class for {@link com.google.appengine.api.search.Document}
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class FTSDocumentWrapper {
  private FTSDocumentId ftsDocumentId;
  private String title;
  private String content;
  private ContentTypeEnum contentType;
  private Instant publishTime;
  private Document document;
  
  public Document getDocument() {
    return document;
  }

  public FTSDocumentId getFtsDocumentId() {
    return ftsDocumentId;
  }

  public String getTitle() {
    return title;
  }

  public String getContent() {
    return content;
  }

  public ContentTypeEnum getContentType() {
    return contentType;
  }

  public Instant getPublishTime() {
    return publishTime;
  }

  public static class Builder {
    private FTSDocumentId ftsDocumentId;
    private String title;
    private String content;
    private ContentTypeEnum contentType;
    private Instant publishTime;

    public Builder ftsDocumentId(FTSDocumentId ftsDocumentId) {
      this.ftsDocumentId = ftsDocumentId;
      return this;
    }
    
    public Builder title(String title) {
      this.title = title;
      return this;
    }

    public Builder content(String content) {
      this.content = content;
      return this;
    }

    public Builder contentType(ContentTypeEnum contentType) {
      checkArgument(contentType == ContentTypeEnum.TEXT_PLAIN
          || contentType == ContentTypeEnum.TEXT_HTML,
          "Only Text and Html contentTypes are allowed.");

      this.contentType = contentType;
      return this;
    }

    public Builder publishTime(Instant publishTime) {
      this.publishTime = publishTime;
      return this;
    }

    public FTSDocumentWrapper build() {
      return new FTSDocumentWrapper(this);

    }
  }

  /**
   * @param builder
   */
  @SuppressWarnings({ "synthetic-access", "cast" })
  public FTSDocumentWrapper(Builder builder) {
    this.ftsDocumentId = checkNotNull(builder.ftsDocumentId, "ftsDocumentId");
    this.title = checkNotBlank(builder.title, "title");
    this.content = checkNotBlank(builder.content, "content");
    this.contentType = checkNotNull(builder.contentType, "contentType");
    this.publishTime = checkNotNull(builder.publishTime, "publishTime");
    
    // Now build.
    Document.Builder documentBuilder = Document.newBuilder();
    documentBuilder.addField(FTSUtils.createTitleField(title));
    
    
    switch(builder.contentType) {
      case TEXT_HTML:
        documentBuilder.addField(FTSUtils.createHtmlContentField(content));
        break;
      case TEXT_PLAIN :
        documentBuilder.addField(FTSUtils.createTextContentField(content));
        break;
        
        default :
          throw new IllegalStateException("Unsupported type : " + contentType);
    }
    
    
    documentBuilder.addField(FTSUtils.createPublishedField(publishTime));
    documentBuilder.setId(((String)getWrapperValue(ftsDocumentId)));

    this.document = documentBuilder.build();
  }
}
