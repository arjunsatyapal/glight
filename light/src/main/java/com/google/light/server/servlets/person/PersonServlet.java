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
package com.google.light.server.servlets.person;

import static com.google.light.server.utils.LightPreconditions.checkNotBlank;

import com.google.light.server.utils.GaeUtils;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.io.CharStreams;
import com.google.light.server.constants.ContentTypeEnum;
import com.google.light.server.dto.person.PersonDto;
import com.google.light.server.exception.unchecked.httpexception.MethodNotAllowedException;
import com.google.light.server.exception.unchecked.httpexception.UnsupportedMediaTypeException;
import com.google.light.server.persistence.entity.person.PersonEntity;
import com.google.light.server.servlets.AbstractLightServlet;
import com.google.light.server.utils.JsonUtils;
import com.google.light.server.utils.XmlUtils;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet to handle requests related to a Person.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class PersonServlet extends AbstractLightServlet {
  private static final Logger logger = Logger.getLogger(PersonServlet.class.getName());

  /**
   * {@inheritDoc}
   */
  @Override
  protected void doDelete(HttpServletRequest request, HttpServletResponse response) {
    throw new MethodNotAllowedException("For Person, delete is not allowed.");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    // TODO(arjuns): Auto-generated method stub
    try {
      response.getWriter().println("hello : " + GaeUtils.getGaeUserEmail());
    } catch (IOException e) {
      // TODO(arjuns): Auto-generated catch block
      throw new RuntimeException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected long getLastModified(HttpServletRequest request) {
    /*
     * TODO(arjuns): Fix this by replacing from MemCache. At present defaulting to -1 so that
     * request can be executed.
     */
    return -1L;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void doHead(HttpServletRequest request, HttpServletResponse response) {
    // TODO(arjuns): Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void doOptions(HttpServletRequest request, HttpServletResponse response) {
    // TODO(arjuns): Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) {
    try {
      String reqContentType = request.getContentType();
      ContentTypeEnum contentType = ContentTypeEnum.getContentTypeByString(reqContentType);

      String reqEntityString =
          CharStreams.toString(new InputStreamReader(request.getInputStream(), Charsets.UTF_8));
      checkNotBlank(reqEntityString);
      logger.info("Content-Type[" + contentType + "], RequestBody = [" + reqEntityString + "].");

      PersonDto dto = null;

      switch (contentType) {
        case APPLICATION_JSON:
          dto = JsonUtils.getDto(reqEntityString, PersonDto.class);
          break;

        case APPLICATION_XML:
          dto = XmlUtils.getDto(reqEntityString);
          break;

        default:
          throw new UnsupportedMediaTypeException("Invalid Content Type : " + contentType.get());
      }

      dto.validate();

      PersonEntity personEntity = getInstanceProvider().getPersonManager().createPerson(
          dto.toPersistenceEntity(null/* personId */));

      String responseString = null;
      switch (contentType) {
        case APPLICATION_JSON:
          responseString = personEntity.toDto().toJson();
          break;

        case APPLICATION_XML:
          responseString = personEntity.toDto().toXml();
          break;

        default:
          throw new UnsupportedMediaTypeException("Invalid Content-Type : " + contentType.get());
      }

      checkNotBlank(responseString);
      response.setContentType(contentType.get());
      response.getWriter().print(responseString);
    } catch (Exception e) {
      // TODO(arjuns): Auto-generated catch block
      try {
        response.sendError(500, Throwables.getStackTraceAsString(e));
      } catch (IOException e1) {
        // TODO(arjuns): Auto-generated catch block
        e1.printStackTrace();
      }
      throw new RuntimeException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void doPut(HttpServletRequest request, HttpServletResponse response) {
    // TODO(arjuns): Auto-generated method stub
    throw new UnsupportedOperationException();
  }
}
