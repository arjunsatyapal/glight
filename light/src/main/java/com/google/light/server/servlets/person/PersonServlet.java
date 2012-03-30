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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.GuiceUtils.getInstance;
import static com.google.light.server.utils.ServletUtils.getRequestPojo;

import com.google.common.base.Throwables;
import com.google.light.server.dto.person.PersonDto;
import com.google.light.server.exception.unchecked.httpexception.MethodNotAllowedException;
import com.google.light.server.manager.interfaces.PersonManager;
import com.google.light.server.persistence.entity.person.PersonEntity;
import com.google.light.server.servlets.AbstractLightServlet;
import com.google.light.server.servlets.SessionManager;
import com.google.light.server.servlets.pojo.ServletRequestPojo;
import com.google.light.server.servlets.pojo.ServletResponsePojo;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet to handle requests related to a Person.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class PersonServlet extends AbstractLightServlet {
  private PersonManager personManager;
  private SessionManager sessionManager;
  /**
   * {@inheritDoc}
   */
  @Override
  public void service(HttpServletRequest request, HttpServletResponse response) {
    this.personManager = getInstance(getInjector(), PersonManager.class);
    this.sessionManager = getInstance(getInjector(), SessionManager.class);
    sessionManager.checkPersonLoggedIn();
    super.service(request, response);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response) {
    throw new MethodNotAllowedException("For Person, delete is not allowed.");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) {
    try {
      response.getWriter().println("hello : " + sessionManager.getEmail());
    } catch (IOException e) {
      // TODO(arjuns): Auto-generated catch block
      throw new RuntimeException(e);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doHead(HttpServletRequest request, HttpServletResponse response) {
    // TODO(arjuns): Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doOptions(HttpServletRequest request, HttpServletResponse response) {
    // TODO(arjuns): Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) {
    try {
      ServletRequestPojo<PersonDto> requestPojo = checkNotNull(getRequestPojo(
          request, PersonDto.class));
      PersonDto dto = requestPojo.getValidDto();

      PersonEntity personEntity = personManager.createPerson(
          dto.toPersistenceEntity(null/* personId */));

      ServletResponsePojo<PersonDto> responsePojo = new ServletResponsePojo<PersonDto>(
          response, requestPojo.getContentType(), personEntity.toDto());

      responsePojo.logAndReturn();
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
  public void doPut(HttpServletRequest request, HttpServletResponse response) {
    // TODO(arjuns): Auto-generated method stub
    throw new UnsupportedOperationException();
  }
}
