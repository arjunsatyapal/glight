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
package com.google.light.server.jersey.resources;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.GuiceUtils.getInstance;
import static com.google.light.server.utils.GuiceUtils.seedEntityInRequestScope;

import com.google.light.server.dto.pojo.longwrapper.PersonId;

import com.google.common.base.Preconditions;

import com.google.light.server.annotations.AnotOwner;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.light.server.annotations.AnotHttpSession;
import com.google.light.server.servlets.SessionManager;
import com.google.light.server.utils.GuiceUtils;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * 
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */
public abstract class AbstractJerseyResource {
  @Inject Injector injector;
  protected HttpServletRequest request;
  protected HttpServletResponse response;
  
  @Inject
  public AbstractJerseyResource(Injector injector, HttpServletRequest request, 
      HttpServletResponse response) {
    this.injector = checkNotNull(injector, "injector");
    GuiceUtils.setInjector(injector);
    this.request = checkNotNull(request, "request");
    checkNotNull(request, "request");
    
    this.response = checkNotNull(response);
    
    HttpSession session = request.getSession();
    seedEntityInRequestScope(request, HttpSession.class, AnotHttpSession.class, session);
    
    SessionManager sessionManager = GuiceUtils.getInstance(SessionManager.class);
    sessionManager.seedPersonIds(request);

    PersonId foo = getInstance(PersonId.class, AnotOwner.class);
    Preconditions.checkNotNull(foo);

  }
}
