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

import static com.google.light.server.jersey.resources.thirdparty.google.GoogleDocIntegrationUtils.importGDocResource;
import static com.google.light.server.servlets.thirdparty.google.gdoc.GoogleDocUtils.getDocumentFeedWithFolderUrl;
import static com.google.light.server.utils.GuiceUtils.getInstance;
import static com.google.light.server.utils.LightUtils.getURL;

import com.google.appengine.api.log.InvalidRequestException;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.light.server.constants.JerseyConstants;
import com.google.light.server.constants.LightConstants;
import com.google.light.server.constants.LightStringConstants;
import com.google.light.server.constants.http.ContentTypeConstants;
import com.google.light.server.constants.http.ContentTypeEnum;
import com.google.light.server.constants.http.HttpStatusCodesEnum;
import com.google.light.server.dto.pages.PageDto;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocInfoDto;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocResourceId;
import com.google.light.server.jersey.resources.AbstractJerseyResource;
import com.google.light.server.persistence.entity.jobs.JobEntity;
import com.google.light.server.thirdparty.clients.google.gdata.gdoc.DocsServiceWrapper;
import com.google.light.server.utils.LightPreconditions;
import com.google.light.server.utils.LightUtils;
import java.net.URL;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
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
  public GoogleDocIntegration(Injector injector, @Context HttpServletRequest request,
      @Context HttpServletResponse response) {
    super(injector, request, response);
    this.docsService = getInstance(DocsServiceWrapper.class);
  }

  @GET
  @Path(JerseyConstants.PATH_GOOGLE_DOC_LIST)
  @Produces({ ContentTypeConstants.APPLICATION_JSON, ContentTypeConstants.TEXT_XML })
  public PageDto getDocList(
      @QueryParam(LightStringConstants.START_INDEX_STR) String startIndexStr,
      @QueryParam(LightStringConstants.MAX_RESULTS_STR) String maxResultsStr) {
    // TODO(arjuns): Test maxResult.
    int maxResult = LightConstants.MAX_RESULTS_DEFAULT;
    if (maxResultsStr != null) {
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

    PageDto pageDto = docsService.getDocumentFeedWithFolders(
        url, JerseyConstants.URI_GOOGLE_DOC_LIST);
    return pageDto;
  }

  @GET
  @Path(JerseyConstants.PATH_GOOGLE_DOC_INFO)
  @Produces({ ContentTypeConstants.APPLICATION_JSON, ContentTypeConstants.TEXT_XML })
  public GoogleDocInfoDto getDocInfo(
      @PathParam(JerseyConstants.PATH_PARAM_EXTERNAL_KEY) String externalKey) {
    GoogleDocResourceId resourceId = new GoogleDocResourceId(externalKey);
    GoogleDocInfoDto dto = docsService.getDocumentEntryWithAcl(resourceId);
    return dto;
  }

  @GET
  @Path(JerseyConstants.PATH_GOOGLE_FOLDER_INFO)
  @Produces({ ContentTypeConstants.APPLICATION_JSON, ContentTypeConstants.TEXT_XML })
  public PageDto getFolderContents(
      @PathParam(JerseyConstants.PATH_PARAM_EXTERNAL_KEY) String externalKey,
      @QueryParam(LightStringConstants.START_INDEX_STR) String startIndex) {
    try {
      String handlerUri = request.getRequestURI();

      if (!Strings.isNullOrEmpty(startIndex)) {
        return docsService.getFolderContentWithStartIndex(getURL(startIndex), handlerUri);
      }
      GoogleDocResourceId resourceId = new GoogleDocResourceId(externalKey);
      PageDto pageDto = docsService.getFolderContentPageWise(resourceId, handlerUri);
      return pageDto;
    } catch (Exception e) {
      // TODO(arjuns): Add exception Handling.
      throw new RuntimeException(e);
    }
  }

  @POST
  @Path(JerseyConstants.PATH_GOOGLE_DOC_IMPORT_POST)
  public Response importGoogleDocPost(
      @FormParam(JerseyConstants.PATH_PARAM_EXTERNAL_KEY) String externalKeyStr) {
    return importGoogleDocPut(externalKeyStr);
  }

  @PUT
  @Path(JerseyConstants.PATH_GOOGLE_DOC_IMPORT_PUT)
  public Response importGoogleDocPut(
      @PathParam(JerseyConstants.PATH_PARAM_EXTERNAL_KEY) String externalKeyStr) {
    if (StringUtils.isBlank(externalKeyStr)) {
      throw new InvalidRequestException("ExternalKeyStr should not be set.");
    }

    GoogleDocResourceId resourceId = new GoogleDocResourceId(externalKeyStr);
    JobEntity jobEntity = importGDocResource(resourceId, docsService, null /* parentJobId */, 
        null /* rootJobId */, null /*promiseHandle*/);

    ResponseBuilder responseBuilder = Response.ok();
    responseBuilder.status(HttpStatusCodesEnum.CREATED.getStatusCode());
    responseBuilder.location(LightUtils.getURI(jobEntity.getLocation()));
    responseBuilder.type(ContentTypeEnum.TEXT_HTML.get());

    String url = LightUtils.getHref(jobEntity.getLocation(), "Check status");

    responseBuilder.entity(url);
    return responseBuilder.build();
  }
}
