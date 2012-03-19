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
package com.google.light.server.servlets.pojo;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;

import com.google.light.server.constants.ContentTypeEnum;
import com.google.light.server.dto.DtoInterface;
import com.google.light.server.exception.unchecked.httpexception.UnsupportedMediaTypeException;
import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletResponse;

/**
 * Request POJO for HTTP Requests. This class wraps the Content-Type, requestString and converted
 * DTO.
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * D : DTO <br>
 * P : Persistence <br>
 * I : Id Type (String/Long).<br>
 * 
 * @author Arjun Satyapal
 */
public class ServletResponsePojo<D extends DtoInterface<D>> {
  private static final Logger logger = Logger.getLogger(ServletResponsePojo.class.getName());
  
  private HttpServletResponse response;
  private ContentTypeEnum contentType;
  private String responseString;
  private D dto;

  public ServletResponsePojo(HttpServletResponse response, ContentTypeEnum contentType, D dto) {
    this.response = checkNotNull(response);
    this.contentType = checkNotNull(contentType);
    this.dto = checkNotNull(dto);

    switch (contentType) {
      case APPLICATION_JSON:
        responseString = dto.toJson();
        break;

      case APPLICATION_XML:
        responseString = dto.toXml();
        break;

      default:
        throw new UnsupportedMediaTypeException("Invalid Content-Type : " + contentType);
    }

    checkNotBlank(responseString);
  }

  public HttpServletResponse getResponse() {
    return response;
  }

  public ContentTypeEnum getContentType() {
    return contentType;
  }

  public String getResponseString() {
    return responseString;
  }

  public D getDto() {
    return dto;
  }
  
  public void logAndReturn() throws IOException {
    logger.info(responseString);
    response.setContentType(contentType.get());
    response.getWriter().print(responseString);
  }
}
