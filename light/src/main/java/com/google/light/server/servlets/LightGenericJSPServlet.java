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
package com.google.light.server.servlets;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.ProvisionException;
import com.google.light.server.constants.SupportedLanguagesEnum;
import com.google.light.server.constants.http.ContentTypeEnum;
import com.google.light.server.dto.JSVariablesPreloadDto;
import com.google.light.server.manager.interfaces.PersonManager;
import com.google.light.server.persistence.entity.person.PersonEntity;
import com.google.light.server.utils.ServletUtils;
import java.io.IOException;
import java.util.Collections;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet is responsible for generating the client side
 * HTML with server data necessary to properly show the page
 * embedded into it.
 * 
 * It basically generetes the {@link JSVariablesPreloadDto} and embeds it into a JSP page.
 * 
 * It automatically discovers the corresponding JSP for this
 * particular request using the following convention explained
 * in a form of an example: if you access /search, it will use
 * WEB-INF/pages/search.jsp .
 * 
 * TODO(waltercacau): add test for this class.
 * 
 * @author Walter Cacau
 */
@SuppressWarnings("serial")
public class LightGenericJSPServlet extends HttpServlet {

  private Provider<PersonManager> personManagerProvider;

  @Inject
  public LightGenericJSPServlet(Provider<PersonManager> personManagerProvider) {
    this.personManagerProvider = personManagerProvider;
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    // Making cache policy private
    resp.setHeader("Cache-Control", "private");

    JSVariablesPreloadDto.Builder dtoBuilder = new JSVariablesPreloadDto.Builder();

    PersonEntity personEntity = null;
    try {
      personEntity = personManagerProvider.get().getCurrent();
    } catch (ProvisionException e) {
      // expected when user is not logged in
    }
    if (personEntity != null)
      dtoBuilder.person(personEntity.toDto());

    // For now, just trusting the request headers
    // TODO(waltercacau): Add support for user defined language.
    dtoBuilder.locale(SupportedLanguagesEnum.getClosestPrefferedLanguage(
        Collections.list(req.getLocales())).getClientLanguageCode());

    resp.setContentType(ContentTypeEnum.TEXT_JAVASCRIPT.get());
    StringBuilder builder = new StringBuilder();
    builder.append("<script>\n");
    builder.append("var lightPreload = ");
    builder.append(dtoBuilder.build().toJson());
    builder.append(";\n</script>");
    req.setAttribute("preload", builder.toString());

    String path = req.getServletPath();
    checkArgument(path.startsWith("/"));
    ServletUtils.forward(req, resp, "/WEB-INF/pages" + path + ".jsp");
  }
}
