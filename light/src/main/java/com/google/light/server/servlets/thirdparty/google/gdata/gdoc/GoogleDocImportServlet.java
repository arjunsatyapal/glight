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
package com.google.light.server.servlets.thirdparty.google.gdata.gdoc;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.utils.GuiceUtils.getInstance;
import static com.google.light.server.utils.LightPreconditions.checkValidSession;
import static com.google.light.server.utils.ServletUtils.getRequestParameterValue;

import com.google.appengine.api.log.InvalidRequestException;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.light.server.constants.HttpHeaderEnum;
import com.google.light.server.constants.RequestParamKeyEnum;
import com.google.light.server.constants.http.ContentTypeEnum;
import com.google.light.server.constants.http.HttpStatusCodesEnum;
import com.google.light.server.dto.pojo.ChangeLogEntryPojo;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocInfoDto;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocResourceId;
import com.google.light.server.guice.providers.InstantProvider;
import com.google.light.server.manager.implementation.JobManagerImpl;
import com.google.light.server.manager.interfaces.ImportManager;
import com.google.light.server.persistence.entity.jobs.JobEntity;
import com.google.light.server.persistence.entity.queue.importflow.ImportJobEntity;
import com.google.light.server.servlets.AbstractLightServlet;
import com.google.light.server.servlets.SessionManager;
import com.google.light.server.thirdparty.clients.google.gdata.gdoc.DocsServiceWrapper;
import com.google.light.server.utils.LightUtils;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.joda.time.Instant;

/**
 * Servlet to show information about a Google Docs ResourceId.
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class GoogleDocImportServlet extends AbstractLightServlet {
  private static final Logger logger = Logger.getLogger(GoogleDocImportServlet.class.getName());

  private DocsServiceWrapper docsService;
  private ImportManager importManager;
  private InstantProvider dateTimeProvider;
  private JobManagerImpl jobManager;
  private SessionManager sessionManager;

  @Inject
  public GoogleDocImportServlet(Injector injector) {
    checkNotNull(injector);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void service(HttpServletRequest request, HttpServletResponse response) {
    docsService = getInstance(DocsServiceWrapper.class);
    importManager = getInstance(ImportManager.class);
    dateTimeProvider = getInstance(InstantProvider.class);
    sessionManager = getInstance(SessionManager.class);
    jobManager = getInstance(JobManagerImpl.class);
    checkValidSession(sessionManager);

    super.service(request, response);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response) {
    // TODO(arjuns): Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) {
    // TODO(arjuns): Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doHead(HttpServletRequest request, HttpServletResponse response) {
    // TODO(arjuns): Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void doOptions(HttpServletRequest request, HttpServletResponse response) {
    // TODO(arjuns): Auto-generated method stub
    throw new UnsupportedOperationException();
  }

  /**
   * {@inheritDoc} Since HTML forms dont allow PUT, so piggybacking on post.
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) {
    doPut(request, response);
  }

  /**
   * {@inheritDoc}
   * 
   */
  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) {
    try {
      String resourceIdStr =
          getRequestParameterValue(request, RequestParamKeyEnum.GOOGLE_DOC_RESOURCE_ID);
      String docUrl = getRequestParameterValue(request, RequestParamKeyEnum.GOOGLE_DOC_URL);

      if (!StringUtils.isBlank(resourceIdStr) && !StringUtils.isBlank(docUrl)) {
        throw new InvalidRequestException(
            "Both googleDocResourceId and googleDocUrl should not be set.");
      }

      GoogleDocResourceId resourceId = new GoogleDocResourceId(resourceIdStr);
      GoogleDocInfoDto docInfoDto = docsService.getDocumentEntryWithAcl(resourceId);
      checkNotNull(docInfoDto, "DocInfoDto for ResourceId[" + resourceId + "] was null.");

      // TODO(arjuns): Add more logic to filter out what docs can be imported.
      logger.info(docInfoDto.toJson());

      Instant now = dateTimeProvider.get();
      ImportJobEntity entity = new ImportJobEntity.Builder()
          .moduleType(resourceId.getModuleType())
          .resourceId(resourceId.getTypedResourceId())
          .personId(sessionManager.getPersonId())
          .additionalJsonInfo(docInfoDto.toJson())
          .creationTime(now)
          .lastUpdateTime(now)
          .build();
      ImportJobEntity persistedEntity = importManager.put(entity, 
          new ChangeLogEntryPojo("Enqueuing for Import."));

      JobEntity jobEntity = jobManager.enqueueImportJob(persistedEntity);
      response.setStatus(HttpStatusCodesEnum.OK.getStatusCode());
      response.setHeader(HttpHeaderEnum.LOCATION.get(), jobEntity.getLocation());
      response.setContentType(ContentTypeEnum.TEXT_HTML.get());

      String url = LightUtils.getHref(jobEntity.getLocation(), "Check status");
      response.getWriter().println(url);
    } catch (Exception e) {
      throw new RuntimeException(e);
    } 
  }
}
