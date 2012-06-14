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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.docs.DocumentEntry;
import com.google.inject.ProvisionException;
import com.google.light.server.manager.interfaces.PersonManager;
import com.google.light.server.persistence.entity.person.PersonEntity;
import com.google.light.server.servlets.thirdparty.google.gdoc.DocsServiceWrapper;
import com.google.light.server.urls.GoogleDocUrl;
import com.google.light.server.utils.GuiceUtils;
import com.google.light.server.utils.LightUtils;
import com.google.light.server.utils.ServletUtils;

@SuppressWarnings("serial")
public class GFormTestServlet extends HttpServlet {


  private boolean hasGoogleDocCredentials() {
    try {
      GuiceUtils.getInstance(DocsServiceWrapper.class);
    } catch (ProvisionException e) {
      return false;
    }
    return true;
  }

  /**
   * Creates a new Google Spreadsheet based on a public template
   * which should contain a Google Form.
   * 
   * @return The DocumentEntry corresponding to the created spreadsheet
   */
  DocumentEntry createGoogleForm() {

    DocsServiceWrapper docsService = GuiceUtils.getInstance(DocsServiceWrapper.class);
    GoogleDocUrl googleDocUrl =
        new GoogleDocUrl(
            LightUtils
                .getURL(
                "https://docs.google.com/spreadsheet/ccc?key=0AjzU4URnWq36dDZqTjZlSTNzZjN0aXMxWnhTdHhMRFE#gid=0"
                ));

    DocumentEntry entry = new DocumentEntry();
    entry.setTitle(new PlainTextConstruct("Sample GDoc Form"));
    entry.setId(googleDocUrl.getTypedResourceId());
    DocumentEntry inserted = null;
    docsService.setConnectTimeout(20*1000);
    try {
      inserted =
          docsService.insert(
              LightUtils.getURL("https://docs.google.com/feeds/default/private/full"),
              entry);
    } catch (Exception e) {
      LightUtils.wrapIntoRuntimeExceptionAndThrow(e);
      return null;
    }
    return inserted;
  }

  String getFormUrlFromSpreadsheetId(String docId) {
    return "https://docs.google.com/spreadsheet/embeddedform?formkey=" + docId.substring(13)
        + "6MQ";
  }

  String getFormEditorUrlFromSpreadsheetId(String docId) {
    return "https://docs.google.com/spreadsheet/gform?key=" + docId + "&gridId=0#edit";
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException,
      IOException {

    PersonEntity person = null;
    try {
      person = GuiceUtils.getInstance(PersonManager.class).getCurrent();
    } catch (Exception e) {
      System.out.println();
    }

    if (person != null) {
      request.setAttribute("name", person.getFirstName() + " " + person.getLastName());
      request.setAttribute("isLogged", "true");
      request.setAttribute("hasCredentials", Boolean.toString(hasGoogleDocCredentials()));
    } else {
      request.setAttribute("isLogged", "false");
    }

    if ("create".equals(request.getParameter("action"))) {
      String docId = createGoogleForm().getDocId();
      request.setAttribute("editorUrl", getFormEditorUrlFromSpreadsheetId(docId));
      request.setAttribute("embeddedUrl", getFormUrlFromSpreadsheetId(docId));
      ServletUtils.forward(request, response, "/WEB-INF/pages/gformorsitespopup.jsp");
    } else {
      ServletUtils.forward(request, response, "/WEB-INF/pages/gformdemo.jsp");
    }

  }
}
