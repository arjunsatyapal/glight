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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.servlets.thirdparty.google.gdata.gdoc.GoogleDocUtils.getDocumentFeedWithFolderUrl;
import static com.google.light.server.utils.GuiceUtils.getInstance;

import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.log.InvalidRequestException;
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
import com.google.light.server.dto.pojo.ChangeLogEntryPojo;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocInfoDto;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocResourceId;
import com.google.light.server.jersey.resources.AbstractJerseyResource;
import com.google.light.server.manager.interfaces.ImportManager;
import com.google.light.server.manager.interfaces.JobManager;
import com.google.light.server.persistence.entity.jobs.JobEntity;
import com.google.light.server.persistence.entity.queue.importflow.ImportJobEntity;
import com.google.light.server.thirdparty.clients.google.gdata.gdoc.DocsServiceWrapper;
import com.google.light.server.utils.GuiceUtils;
import com.google.light.server.utils.LightPreconditions;
import com.google.light.server.utils.LightUtils;
import java.net.URL;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
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
import org.joda.time.Instant;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@Path(JerseyConstants.RESOURCE_PATH_THIRD_PARTY_GOOGLE_DOC)
public class GoogleDocIntegration extends AbstractJerseyResource {
  private static final Logger logger = Logger.getLogger(GoogleDocIntegration.class.getName());

  private Provider<Instant> instantProvider;
  private DocsServiceWrapper docsService;
  private ImportManager importManager;
  private JobManager jobManager;

  @Inject
  public GoogleDocIntegration(Injector injector, @Context HttpServletRequest request,
      @Context HttpServletResponse response) {
    super(injector, request, response);
    this.docsService = getInstance(DocsServiceWrapper.class);
    this.instantProvider = GuiceUtils.getProvider(Instant.class);
    this.importManager = getInstance(ImportManager.class);
    this.jobManager = getInstance(JobManager.class);
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

    PageDto pageDto = docsService.getDocumentFeedWithFolders(url);
    return pageDto;
  }

  @POST
  @Path(JerseyConstants.PATH_GOOGLE_DOC_IMPORT)
  public Response importGoogleDocPost(
      @PathParam(JerseyConstants.PATH_PARAM_EXTERNAL_KEY) String externalKeyStr) {
    return importGoogleDoc(externalKeyStr);
  }

  @PUT
  @Path(JerseyConstants.PATH_GOOGLE_DOC_IMPORT)
  public Response importGoogleDoc(
      @PathParam(JerseyConstants.PATH_PARAM_EXTERNAL_KEY) String externalKeyStr) {
    externalKeyStr = "document:1tJZGzv_2sjMpvs4jtwxg18PGuSG-6nlfmx8Hlqa-_58";

    if (StringUtils.isBlank(externalKeyStr)) {
      throw new InvalidRequestException("ExternalKeyStr should not be set.");
    }

    GoogleDocResourceId resourceId = new GoogleDocResourceId(externalKeyStr);
    GoogleDocInfoDto docInfoDto = docsService.getDocumentEntryWithAcl(resourceId);
    checkNotNull(docInfoDto, "DocInfoDto for ResourceId[" + resourceId + "] was null.");

    // TODO(arjuns): Add more logic to filter out what docs can be imported.
    logger.info(docInfoDto.toJson());

    Instant now = instantProvider.get();
    ImportJobEntity entity = new ImportJobEntity.Builder()
        .moduleType(resourceId.getModuleType())
        .resourceId(resourceId.getTypedResourceId())
        .personId(GuiceUtils.getOwnerId())
        .additionalJsonInfo(docInfoDto.toJson())
        .creationTime(now)
        .lastUpdateTime(now)
        .build();
    ImportJobEntity persistedEntity = importManager.put(entity,
        new ChangeLogEntryPojo("Enqueuing for Import."));

    JobEntity jobEntity = jobManager.enqueueImportJob(persistedEntity);

    ResponseBuilder responseBuilder = Response.ok();
    responseBuilder.status(HttpStatusCodesEnum.CREATED.getStatusCode());
    responseBuilder.location(LightUtils.getURI(jobEntity.getLocation()));
    responseBuilder.type(ContentTypeEnum.TEXT_HTML.get());

    String url = LightUtils.getHref(jobEntity.getLocation(), "Check status");

    responseBuilder.entity(url);
    return responseBuilder.build();
  }
}
