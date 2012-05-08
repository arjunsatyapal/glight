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

import static com.google.light.server.constants.LightConstants.GOOGLE_DOC_IMPORT_BATCH_SIZE_MAX;
import static com.google.light.server.jersey.resources.thirdparty.google.GoogleDocIntegrationUtils.importGDocResource;
import static com.google.light.server.servlets.thirdparty.google.gdoc.GoogleDocUtils.getDocumentFeedWithFolderUrl;
import static com.google.light.server.utils.LightPreconditions.checkIntegerIsInRage;
import static com.google.light.server.utils.LightPreconditions.checkNonEmptyList;
import static com.google.light.server.utils.LightPreconditions.checkNotNull;
import static com.google.light.server.utils.LightUtils.getURL;

import com.google.light.server.utils.GuiceUtils;

import com.google.appengine.api.log.InvalidRequestException;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.light.server.constants.JerseyConstants;
import com.google.light.server.constants.LightConstants;
import com.google.light.server.constants.LightStringConstants;
import com.google.light.server.constants.http.ContentTypeConstants;
import com.google.light.server.constants.http.ContentTypeEnum;
import com.google.light.server.constants.http.HttpStatusCodesEnum;
import com.google.light.server.dto.pages.PageDto;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocImportBatchJobContext;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocInfoDto;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocResourceId;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocResourceIdListWrapperDto;
import com.google.light.server.exception.ExceptionType;
import com.google.light.server.jersey.resources.AbstractJerseyResource;
import com.google.light.server.manager.interfaces.JobManager;
import com.google.light.server.persistence.entity.jobs.JobEntity;
import com.google.light.server.thirdparty.clients.google.gdata.gdoc.DocsServiceWrapper;
import com.google.light.server.utils.HtmlBuilder;
import com.google.light.server.utils.JsonUtils;
import com.google.light.server.utils.LightPreconditions;
import com.google.light.server.utils.LightUtils;
import com.google.light.server.utils.LocationHeaderUtils;
import com.google.light.server.utils.XmlUtils;
import java.net.URI;
import java.net.URL;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
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
  private Provider<DocsServiceWrapper> docsServiceProvider;
  private JobManager jobManager;

  @Inject
  public GoogleDocIntegration(Injector injector, @Context HttpServletRequest request,
      @Context HttpServletResponse response, Provider<DocsServiceWrapper> docsServiceProvider,
      JobManager jobManager) {
    super(injector, request, response);
    // Since Doc Service depends on currently logged in person, so its required that parent class
    // is constructed first. Therefore, instead of injecting DocsService directly,
    this.docsServiceProvider = checkNotNull(docsServiceProvider,
        ExceptionType.SERVER_GUICE_INJECTION, "docsServiceProvider");
    this.jobManager = checkNotNull(jobManager, ExceptionType.SERVER_GUICE_INJECTION, "jobManager");
  }

  @GET
  @Path(JerseyConstants.PATH_GOOGLE_DOC_LIST)
  @Produces({ ContentTypeConstants.APPLICATION_JSON, ContentTypeConstants.APPLICATION_XML })
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

    PageDto pageDto = docsServiceProvider.get().getDocumentFeedWithFolders(
        url, JerseyConstants.URI_GOOGLE_DOC_LIST);

    System.out.println(XmlUtils.toXml(pageDto));

    return pageDto;
  }

  @GET
  @Path(JerseyConstants.PATH_GOOGLE_DOC_INFO)
  @Produces({ ContentTypeConstants.APPLICATION_JSON, ContentTypeConstants.APPLICATION_XML })
  public GoogleDocInfoDto getDocInfo(
      @PathParam(JerseyConstants.PATH_PARAM_EXTERNAL_KEY) String externalKey) {
    GoogleDocResourceId resourceId = new GoogleDocResourceId(externalKey);
    GoogleDocInfoDto dto = docsServiceProvider.get().getGoogleDocInfo(resourceId);
    return dto;
  }

  @GET
  @Path(JerseyConstants.PATH_GOOGLE_FOLDER_INFO)
  @Produces({ ContentTypeConstants.APPLICATION_JSON, ContentTypeConstants.APPLICATION_XML })
  public PageDto getFolderContents(
      @PathParam(JerseyConstants.PATH_PARAM_EXTERNAL_KEY) String externalKey,
      @QueryParam(LightStringConstants.START_INDEX_STR) String startIndex) {
    try {
      String handlerUri = request.getRequestURI();

      if (!Strings.isNullOrEmpty(startIndex)) {
        return docsServiceProvider.get().getFolderContentWithStartIndex(getURL(startIndex),
            handlerUri);
      }
      GoogleDocResourceId resourceId = new GoogleDocResourceId(externalKey);
      PageDto pageDto = docsServiceProvider.get().getFolderContentPageWise(resourceId, handlerUri);
      return pageDto;
    } catch (Exception e) {
      // TODO(arjuns): Add exception Handling.
      throw new RuntimeException(e);
    }
  }

  @POST
  @Consumes(ContentTypeConstants.APPLICATION_URL_ENCODED)
  @Path(JerseyConstants.PATH_GOOGLE_DOC_IMPORT_POST)
  public Response importGoogleDocBatchFormPost(
      @FormParam("json_body") String body) {
    return importGoogleDocBatchPost(body);
  }

  @POST
  @Consumes({ ContentTypeConstants.APPLICATION_JSON, ContentTypeConstants.APPLICATION_XML })
  @Path(JerseyConstants.PATH_GOOGLE_DOC_IMPORT_POST)
  public Response importGoogleDocBatchPost(String body) {
    ContentTypeEnum contentType = ContentTypeEnum.getContentTypeByString(
        request.getContentType());

    GoogleDocResourceIdListWrapperDto resourceIdListWrapper = null;
    if (contentType == ContentTypeEnum.APPLICATION_JSON
        || contentType == ContentTypeEnum.APPLICATION_URL_ENCODED) {
      resourceIdListWrapper = JsonUtils.getDto(body, GoogleDocResourceIdListWrapperDto.class);
    } else if (contentType == ContentTypeEnum.APPLICATION_XML) {
      resourceIdListWrapper = XmlUtils.getDto(body);
    } else {
      throw new IllegalArgumentException("Invalid contentType : " + contentType);
    }

    checkNonEmptyList(resourceIdListWrapper.get(),
        "List should not be empty at this point.");
    checkIntegerIsInRage(resourceIdListWrapper.get().size(),
        1, GOOGLE_DOC_IMPORT_BATCH_SIZE_MAX,
        "Number of docuements that can be downloaded in a batch should be between 1 & " +
            GOOGLE_DOC_IMPORT_BATCH_SIZE_MAX + ".");

    List<GoogleDocInfoDto> docInfoList = docsServiceProvider.get().getGoogleDocInfoInBatch(
        resourceIdListWrapper.get());
    System.out.println("etag = " + docInfoList.get(0).getEtag());
    GoogleDocImportBatchJobContext docInfoListWrapper = new GoogleDocImportBatchJobContext.Builder()
        .ownerId(GuiceUtils.getOwnerId())
        .state(GoogleDocImportBatchJobContext.GoogleDocImportBatchJobState.BUILDING)
        .build();

    for (GoogleDocInfoDto currInfo : docInfoList) {
      docInfoListWrapper.addGoogleDocResource(currInfo);
    }
    
    docInfoListWrapper.setState(GoogleDocImportBatchJobContext.GoogleDocImportBatchJobState.ENQUEUED);

    JobEntity jobEntity = jobManager.enqueueGoogleDocImportBatchJob(docInfoListWrapper);

    URI jobLocationUri = LocationHeaderUtils.getJobLocation(jobEntity.getId());
    HtmlBuilder htmlBuilder = new HtmlBuilder();
    htmlBuilder.appendHref(null/* id */, jobLocationUri, "Check Status for : " + jobEntity.getId());

    return Response.created(jobLocationUri).entity(htmlBuilder.toString())
        .type(ContentTypeConstants.TEXT_HTML).build();
  }

  @Deprecated
  @PUT
  @Path(JerseyConstants.PATH_GOOGLE_DOC_IMPORT_PUT)
  public Response importGoogleDocBatchPut(
      @PathParam(JerseyConstants.PATH_PARAM_EXTERNAL_KEY) String externalKeyStr) {
    if (StringUtils.isBlank(externalKeyStr)) {
      throw new InvalidRequestException("ExternalKeyStr should be set.");
    }

    GoogleDocResourceId resourceId = new GoogleDocResourceId(externalKeyStr);
    JobEntity jobEntity = importGDocResource(resourceId, docsServiceProvider.get(),
        null /* parentJobId */, null /* rootJobId */, null /* promiseHandle */);

    ResponseBuilder responseBuilder = Response.ok();
    responseBuilder.status(HttpStatusCodesEnum.CREATED.getStatusCode());
    responseBuilder.location(LightUtils.getURI(jobEntity.getLocation()));
    responseBuilder.type(ContentTypeEnum.TEXT_HTML.get());

    String url = LightUtils.getHref(jobEntity.getLocation(), "Check status");

    responseBuilder.entity(url);
    return responseBuilder.build();
  }
}
