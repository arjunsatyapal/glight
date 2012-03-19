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
import com.google.light.server.utils.JsonUtils;
import com.google.light.server.utils.XmlUtils;
import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;

/**
 * Request POJO for HTTP Requests. This class wraps the Content-Type, requestString and converted
 * DTO.
 * 
 * D : DTO.
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class ServletRequestPojo<D extends DtoInterface<D>> {
  private static final Logger logger = Logger.getLogger(ServletRequestPojo.class.getName());
  
  private HttpServletRequest request;
  private ContentTypeEnum contentType;
  private String requestString;
  private D dto;

  public ServletRequestPojo(HttpServletRequest request, ContentTypeEnum contentType,
      String requestString, Class<D> clazz) throws JsonParseException, JsonMappingException,
      IOException, JAXBException {
    this.request = checkNotNull(request);
    this.contentType = checkNotNull(contentType);
    logger.info("Content-Type[" + contentType + "], RequestBody = [" + requestString + "].");
    this.requestString = checkNotBlank(requestString);
    
    switch (contentType) {
      case APPLICATION_JSON:
        dto = JsonUtils.getDto(requestString, clazz);
        break;

      case APPLICATION_XML:
        dto = XmlUtils.getDto(requestString);
        break;

      default:
        throw new UnsupportedMediaTypeException("Invalid Content Type : " + contentType);
    }
  }

  public HttpServletRequest getRequest() {
    return request;
  }

  public ContentTypeEnum getContentType() {
    return contentType;
  }

  public String getRequestString() {
    return requestString;
  }

  public D getValidDto() {
    return dto.validate();
  }
}
