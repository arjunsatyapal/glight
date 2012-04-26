/*
 * Copyright (C) Google Inc.
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
package com.google.light.server.constants.http;

import static com.google.light.server.utils.LightPreconditions.checkNotBlank;

import com.google.light.server.exception.unchecked.httpexception.UnsupportedMediaTypeException;

/**
 * Enum for encapsulating Content Type.
 * 
 * TODO(arjuns): Add encoding here.
 * 
 * @author Arjun Satyapal
 */
public enum ContentTypeEnum {
  APPLICATION_JSON(ContentTypeConstants.APPLICATION_JSON),
  APPLICATION_PDF("application/pdf"),
  APPLICATION_XML("application/xml"),
  APPLICATION_ZIP("application/zip"),

  /*
   * Content type for MS Formats.
   */
  MS_EXCEL("application/vnd.ms-excel"),
  MS_WORD("application/msword"),
  MS_POWERPOINT("application/vnd.ms-powerpoint"),
  MS_DRAWING("application/x-msmetafile"),
  /*
   * Content type for OpenXml Formats
   */
  OPENXML_DOCUMENT("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
  OPENXML_PRESENTATION("application/vnd.openxmlformats-officedocument.presentationml.presentation"),
  OPENEXML_SPREADSHEET("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),

  /*
   * Content type for Google Doc Formats.
   * See MIME Types for native Google Document Formats at :
   * https://developers.google.com/google-apps/documents-list
   */
  GOOGLE_DOC("application/vnd.google-apps.document"),
  GOOGLE_SPREADSHEET("application/vnd.google-apps.spreadsheet"),
  GOOGLE_FORM("application/vnd.google-apps.form"),
  GOOGLE_PRESENTATION("application/vnd.google-apps.presentation"),
  GOOGLE_DRAWING("application/vnd.google-apps.drawing"),

  /*
   * Content type for OASIS formats.
   */
  OASIS_DOCUMENT("application/vnd.oasis.opendocument.text"),
  OASIS_SPREADSHEET("application/x-vnd.oasis.opendocument.spreadsheet"),
  /*
   * Content Type for Images.
   */
  IMAGE_GIF("image/gif"),
  IMAGE_JPEG("image/jpeg"),
  IMAGE_BMP("image/bmp"),
  IMAGE_PNG("image/png"),
  IMAGE_SVG_XML("image/svg+xml"),

  RTF("application/rtf"),

  TEXT_HTML("text/html"),
  TEXT_JAVASCRIPT("text/javascript"),
  TEXT_PLAIN(ContentTypeConstants.TEXT_PLAIN),
  TEXT_CSV("text/csv"),
  TEXT_TSV("text/tab-separated-values"),
  TEXT_XML(ContentTypeConstants.TEXT_XML),

  /*
   * Format for SUN.
   */
  SUN_XML_WRITE("application/vnd.sun.xml.writer");

  private String type;

  private ContentTypeEnum(String type) {
    this.type = checkNotBlank(type, "type");
  }

  public final String get() {
    return type;
  }

  public static ContentTypeEnum getContentTypeByString(String type) {
    if (type.contains(";"))
      type = type.split(";")[0];
    type = type.trim();
    
    for (ContentTypeEnum curr : ContentTypeEnum.values()) {
      if (curr.get().equals(type)) {
        return curr;
      }
    }

    throw new UnsupportedMediaTypeException("Invalid ContentType : " + type);
  }
}
