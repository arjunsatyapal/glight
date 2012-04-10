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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.light.server.constants.ContentTypeEnum;
import com.google.light.server.constants.SupportedLanguagesEnum;
import com.google.light.server.dto.JSVariablesPreloadDto;
import com.google.light.server.utils.ServletUtils;

@SuppressWarnings("serial")
/**
 * TODO(waltercacau): add test for this.
 * @author Walter Cacau
 *
 */
public class JSVariablesPreloadServlet extends HttpServlet {
  @SuppressWarnings("unchecked")
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {

    JSVariablesPreloadDto.Builder dtoBuilder = new JSVariablesPreloadDto.Builder();

    // For now, just trusting the request headers
    // TODO(waltercacau): Add support for user defined language.
    dtoBuilder.locale(SupportedLanguagesEnum.getClosestPrefferedLanguage(
        Collections.list(req.getLocales())).getClientLanguageCode());

    resp.setContentType(ContentTypeEnum.TEXT_JAVASCRIPT.get());
    PrintWriter writer = resp.getWriter();
    writer.print("var lightPreload = ");
    writer.print(dtoBuilder.build().toJson());
    writer.println(";");
  }

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    ServletUtils.avoidCaching(resp);

    super.service(req, resp);
  }

  @Override
  protected long getLastModified(HttpServletRequest req) {
    return -1;
  }
}
