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

import static com.google.light.server.constants.LightConstants.GDATA_GDOC_MAX_RESULTS;
import static com.google.light.server.exception.ExceptionType.SERVER_GUICE_INJECTION;
import static com.google.light.server.servlets.thirdparty.google.gdoc.GoogleDocUtils.getDocumentFeedWithFolderUrl;
import static com.google.light.server.servlets.thirdparty.google.gdoc.GoogleDocUtils.getDocumentFeedWithFolderUrlAndFilter;
import static com.google.light.server.utils.LightPreconditions.checkIntegerIsInRage;
import static com.google.light.server.utils.LightPreconditions.checkNotNull;
import static com.google.light.server.utils.LightUtils.encodeToUrlEncodedString;
import static com.google.light.server.utils.LightUtils.getURL;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.light.server.constants.JerseyConstants;
import com.google.light.server.constants.LightConstants;
import com.google.light.server.constants.LightStringConstants;
import com.google.light.server.constants.http.ContentTypeConstants;
import com.google.light.server.dto.module.ModuleType;
import com.google.light.server.dto.pages.PageDto;
import com.google.light.server.dto.pojo.typewrapper.stringwrapper.ExternalId;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocInfoDto;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocResourceId;
import com.google.light.server.jersey.resources.AbstractJerseyResource;
import com.google.light.server.thirdparty.clients.google.gdata.gdoc.DocsServiceWrapper;
import com.google.light.server.utils.JsonUtils;
import com.google.light.server.utils.LightUtils;
import java.net.URL;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
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
  @Inject
  public GoogleDocIntegration(Injector injector, @Context HttpServletRequest request,
      @Context HttpServletResponse response, Provider<DocsServiceWrapper> docsServiceProvider) {
    super(injector, request, response);
    // Since Doc Service depends on currently logged in person, so its required that parent class
    // is constructed first. Therefore, instead of injecting DocsService directly,
    this.docsServiceProvider = checkNotNull(docsServiceProvider,
        SERVER_GUICE_INJECTION, "docsServiceProvider");
  }

  @GET
  @Path(JerseyConstants.PATH_GOOGLE_DOC_LIST)
  @Produces({ ContentTypeConstants.APPLICATION_JSON, ContentTypeConstants.APPLICATION_XML })
  public PageDto getDocList(
      @QueryParam(LightStringConstants.START_INDEX_STR) String startIndexStr,
      @QueryParam(LightStringConstants.MAX_RESULTS_STR) String maxResultsStr,
      @QueryParam(LightStringConstants.FILTER) String queryStr) {
    // TODO(arjuns): Test maxResult.
    int maxResults = initializeMaxResults(maxResultsStr);
    String query = initializeQueryStr(queryStr);

    URL url = null;
    if (!StringUtils.isBlank(startIndexStr)) {
      // Get values for next page.
      url = LightUtils.getURL(startIndexStr);
    } else {
      // Start from page1.
      if (query == null) {
        url = getDocumentFeedWithFolderUrl(maxResults);
      } else {
        url = getDocumentFeedWithFolderUrlAndFilter(maxResults, query);
      }
    }

    PageDto pageDto = docsServiceProvider.get().getDocumentFeedWithFolders(
        url, JerseyConstants.URI_GOOGLE_DOC_LIST);

    return pageDto;
  }

  /**
   * @param queryStr
   * @return
   */
  private String initializeQueryStr(String queryStr) {
    if (StringUtils.isBlank(queryStr)) {
      return null;
    }

    return encodeToUrlEncodedString(queryStr);
  }

  /**
   * @param maxResultsStr
   * @return
   */
  private int initializeMaxResults(String maxResultsStr) {
    int maxResult = LightConstants.MAX_RESULTS_DEFAULT;
    if (maxResultsStr != null) {
      maxResult = Integer.parseInt(maxResultsStr);
      checkIntegerIsInRage(maxResult, LightConstants.MAX_RESULTS_MIN,
          LightConstants.MAX_RESULTS_MAX, "Invalid value for maxResult[" + maxResult + "].");
    }
    return maxResult;
  }

  @SuppressWarnings("unchecked")
  @POST
  @Path(JerseyConstants.PATH_GOOGLE_DOC_INFO)
  @Produces({ ContentTypeConstants.TEXT_PLAIN })
  public Response getDocInfo(
      @FormParam(JerseyConstants.PATH_PARAM_EXTERNAL_ID) String externalIdStr) {
    ExternalId externalId = new ExternalId(externalIdStr);
    GoogleDocResourceId resourceId = new GoogleDocResourceId(externalId);
    GoogleDocInfoDto dto = docsServiceProvider.get().getGoogleDocInfo(resourceId);

    StringBuilder builder = new StringBuilder();
    builder.append("Info for : " + externalIdStr + "\n").append(dto.toJson());
    
    
    if (dto.getModuleType() == ModuleType.GOOGLE_COLLECTION) {
      PageDto pageDto = getFolderContents(externalIdStr, null, 
          Long.toString(LightConstants.MAX_RESULTS_MAX));

      List<GoogleDocInfoDto> list = ((List<GoogleDocInfoDto>) pageDto.getList());
      java.util.Collections.sort(list);
      
      builder.append("\n Folder Contents : \n").append(JsonUtils.toJson(list));
    }
    return Response.ok(builder.toString()).build();
  }

  @GET
  @Path(JerseyConstants.PATH_GOOGLE_FOLDER_INFO)
  @Produces({ ContentTypeConstants.APPLICATION_JSON, ContentTypeConstants.APPLICATION_XML })
  public PageDto getFolderContents(
      @PathParam(JerseyConstants.PATH_PARAM_EXTERNAL_ID) String externalIdStr,
      @QueryParam(LightStringConstants.START_INDEX_STR) String startIndex,
      @QueryParam(LightStringConstants.MAX_RESULTS_STR) String maxResultsStr) {
    int maxResult = initializeMaxResults(maxResultsStr);

    try {
      String handlerUri = request.getRequestURI();

      // TODO(arjuns): Here client could have modified the start-index.
      if (!Strings.isNullOrEmpty(startIndex)) {
        return docsServiceProvider.get().getFolderContentWithStartIndex(getURL(startIndex),
            handlerUri);
      }
      
      ExternalId externalId = new ExternalId(externalIdStr);
      GoogleDocResourceId resourceId = new GoogleDocResourceId(externalId);
      PageDto pageDto = docsServiceProvider.get().getFolderContentPageWise(
          resourceId, handlerUri, maxResult);
      return pageDto;
    } catch (Exception e) {
      // TODO(arjuns): Add exception Handling.
      throw new RuntimeException(e);
    }
  }

//  @POST
//  @Consumes(ContentTypeConstants.APPLICATION_URL_ENCODED)
//  @Path(JerseyConstants.PATH_GOOGLE_DOC_IMPORT_POST)
//  public Response importGoogleDocBatchFormPost(
//      @FormParam("json_body") String body) {
//    return importGoogleDocBatchPost(body);
//  }
//
//  @POST
//  @Consumes({ ContentTypeConstants.APPLICATION_JSON, ContentTypeConstants.APPLICATION_XML })
//  @Path(JerseyConstants.PATH_GOOGLE_DOC_IMPORT_POST)
//  public Response importGoogleDocBatchPost(String body) {
//    ContentTypeEnum contentType = ContentTypeEnum.getContentTypeByString(
//        request.getContentType());
//
//    ImportBatchWrapper externalIdDtoListWrapper = null;
//    if (contentType == ContentTypeEnum.APPLICATION_JSON
//        || contentType == ContentTypeEnum.APPLICATION_URL_ENCODED) {
//      externalIdDtoListWrapper = JsonUtils.getDto(body, ImportBatchWrapper.class);
//    } else if (contentType == ContentTypeEnum.APPLICATION_XML) {
//      externalIdDtoListWrapper = XmlUtils.getDto(body);
//    } else {
//      throw new IllegalArgumentException("Invalid contentType : " + contentType);
//    }
//
////    checkNonEmptyList(externalIdDtoListWrapper.getList(),
////        "List should not be empty at this point.");
////    checkIntegerIsInRage(externalIdDtoListWrapper.getList().size(),
////        1, GOOGLE_DOC_IMPORT_BATCH_SIZE_MAX,
////        "Number of docuements that can be downloaded in a batch should be between 1 & " +
////            GOOGLE_DOC_IMPORT_BATCH_SIZE_MAX + ".");
//
//    ImportBatchType type = externalIdDtoListWrapper.getImportBatchType();
//
//    // First ensure that all ExternalIds are for google.
//    List<GoogleDocResourceId> listOfGDocResourceIds = Lists.newArrayList();
//    for (ImportModuleDto currExternalIdDto : externalIdDtoListWrapper.getList()) {
//      ModuleTypeProvider provider = currExternalIdDto.getExternalId().getModuleType().getModuleTypeProvider();
//      checkArgument(provider == ModuleTypeProvider.GOOGLE_DOC, "Invalid Provider[" + provider
//          + "] for " + currExternalIdDto.getExternalId());
//      GoogleDocResourceId gdocResourceId = new GoogleDocResourceId(currExternalIdDto.getExternalId());
//      listOfGDocResourceIds.add(gdocResourceId);
//    }
//
//    // TODO(arjuns) : See this needs max-result or any underlying layer.
//    List<GoogleDocInfoDto> docInfoList = docsServiceProvider.get().getGoogleDocInfoInBatch(
//        listOfGDocResourceIds);
//
//    GoogleDocImportBatchJobContext gdocImportBatchJobContext =
//        new GoogleDocImportBatchJobContext.Builder()
//            .ownerId(GuiceUtils.getOwnerId())
//            .state(GoogleDocImportBatchJobContext.GoogleDocImportBatchJobState.BUILDING)
//            .type(type)
//            .build();
//
//    for (GoogleDocInfoDto currInfo : docInfoList) {
//      if (currInfo.getModuleType().isSupported()) {
//        gdocImportBatchJobContext.addGoogleDocResource(currInfo);
//      }
//    }
//
//    if (isListEmpty(gdocImportBatchJobContext.get())) {
//      throw new BadRequestException("Did not find any supported GDoc to import");
//    }
//
//    gdocImportBatchJobContext.setState(
//        GoogleDocImportBatchJobContext.GoogleDocImportBatchJobState.ENQUEUED);
//    gdocImportBatchJobContext.setCollectionTitle(externalIdDtoListWrapper.getCollectionTitle());
//    gdocImportBatchJobContext.setCollectionId(externalIdDtoListWrapper.getCollectionId());
//
//    JobEntity jobEntity = jobManager.enqueueGoogleDocImportBatchJob(gdocImportBatchJobContext);
//
//    URI jobLocationUri = LocationHeaderUtils.getJobLocation(jobEntity.getJobId());
//    /*
//     * HtmlBuilder htmlBuilder = new HtmlBuilder();
//     * htmlBuilder.appendHref(null/* id * /, jobLocationUri, "Check Status for : " +
//     * jobEntity.getId());
//     */
//    // TODO(waltercacau): allow JSON responses
//    // TODO(arjuns): Fix Jersey's relative path bug with locationUris.
//    return Response.created(jobLocationUri).entity(JsonUtils.toJson(jobEntity.getJobId()))
//        .type(ContentTypeConstants.APPLICATION_JSON).build();
//  }
}
