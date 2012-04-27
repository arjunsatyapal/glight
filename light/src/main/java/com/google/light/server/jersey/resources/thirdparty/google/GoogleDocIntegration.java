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
package com.google.light.server.jersey.resources.thirdparty.google;

import static com.google.light.server.servlets.thirdparty.google.gdata.gdoc.GoogleDocUtils.getDocumentFeedWithFolderUrl;
import static com.google.light.server.utils.GuiceUtils.getInstance;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.light.server.constants.JerseyConstants;
import com.google.light.server.constants.LightConstants;
import com.google.light.server.constants.LightStringConstants;
import com.google.light.server.constants.http.ContentTypeConstants;
import com.google.light.server.dto.pages.PageDto;
import com.google.light.server.jersey.resources.AbstractJerseyResource;
import com.google.light.server.thirdparty.clients.google.gdata.gdoc.DocsServiceWrapper;
import com.google.light.server.utils.LightPreconditions;
import com.google.light.server.utils.LightUtils;
import java.net.URL;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import org.apache.commons.lang.StringUtils;

/**
 *
 * 
 * TODO(arjuns): Add test for this class.
 *
 * @author Arjun Satyapal
 */
@Path(JerseyConstants.RESOURCE_PATH_THIRD_PARTY_GOOGLE_DOC)
public class GoogleDocIntegration extends AbstractJerseyResource {
  private DocsServiceWrapper docsService;
  
  @Inject
  public GoogleDocIntegration(Injector injector, @Context HttpServletRequest request) {
    super(injector, request);
    this.docsService = getInstance(DocsServiceWrapper.class);
  }

  @GET
  @Path(JerseyConstants.PATH_GOOGLE_DOC_LIST)
  @Produces({ContentTypeConstants.APPLICATION_JSON, ContentTypeConstants.TEXT_XML})
  public PageDto getDocList(
      @QueryParam(LightStringConstants.START_INDEX_STR) String startIndexStr,
      @QueryParam(LightStringConstants.MAX_RESULTS_STR) String maxResultsStr) {
    int maxResult = LightConstants.MAX_RESULTS_DEFAULT;
    if(maxResultsStr != null) {
      maxResult = Integer.parseInt(maxResultsStr);
      LightPreconditions.checkIntegerIsInRage(maxResult, LightConstants.MAX_RESULTS_MIN,
          LightConstants.MAX_RESULTS_MAX, "Invalid value for maxResult[" + maxResult + "].");
    }
    
    URL url = null;
    if (!StringUtils.isBlank(startIndexStr)) {
      // Get values for next page.
      url = LightUtils.getURL(startIndexStr);
    } else {
      // Start from page1.
      url = getDocumentFeedWithFolderUrl();
    }
    
    PageDto pageDto = docsService.getDocumentFeedWithFolders(url);
    return pageDto;
  }
}
