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
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.sites.SiteEntry;
import com.google.gdata.util.ServiceException;
import com.google.inject.ProvisionException;
import com.google.light.server.manager.interfaces.PersonManager;
import com.google.light.server.persistence.entity.person.PersonEntity;
import com.google.light.server.servlets.thirdparty.google.gdoc.SitesServiceWrapper;
import com.google.light.server.utils.GuiceUtils;
import com.google.light.server.utils.LightUtils;
import com.google.light.server.utils.ServletUtils;

@SuppressWarnings("serial")
public class GSitesTestServlet extends HttpServlet {

  private boolean hasGoogleDocCredentials() {
    try {
      GuiceUtils.getInstance(SitesServiceWrapper.class);
    } catch (ProvisionException e) {
      return false;
    }
    return true;
  }

  private SiteEntry createGSites() {
    SitesServiceWrapper sitesService = GuiceUtils.getInstance(SitesServiceWrapper.class);
    SiteEntry entry = new SiteEntry();
    entry.setTitle(new PlainTextConstruct("Sample Test Site"));
    entry.setSummary(new PlainTextConstruct("No summary"));
    try {
      return sitesService.insert(new URL("https://sites.google.com/feeds/site/myopenedu.com/"), entry);
    } catch (Exception e) {
      LightUtils.wrapIntoRuntimeExceptionAndThrow(e);
      return null;
    }
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
      SiteEntry gsite = createGSites();

      request.setAttribute("editorUrl", gsite.getSelfLink().getHref());
      request.setAttribute("embeddedUrl", gsite.getSelfLink().getHref());
      ServletUtils.forward(request, response, "/WEB-INF/pages/gformorsitespopup.jsp");
    } else {
      ServletUtils.forward(request, response, "/WEB-INF/pages/gsitesdemo.jsp");
    }

  }
}
