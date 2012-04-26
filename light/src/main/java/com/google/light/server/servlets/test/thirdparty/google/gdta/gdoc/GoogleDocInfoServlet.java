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
package com.google.light.server.servlets.test.thirdparty.google.gdta.gdoc;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.constants.OAuth2ProviderService.GOOGLE_DOC;
import static com.google.light.server.constants.RequestParamKeyEnum.GOOGLE_DOC_RESOURCE_ID;
import static com.google.light.server.constants.RequestParamKeyEnum.MODULE_TYPE;
import static com.google.light.server.dto.module.ModuleType.getByProviderServiceAndCategory;
import static com.google.light.server.utils.GuiceUtils.getInstance;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.ServletUtils.getRequestParameterValue;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.common.base.Preconditions;
import com.google.gdata.data.docs.DocumentListEntry;
import com.google.inject.Inject;
import com.google.light.server.constants.OAuth2ProviderService;
import com.google.light.server.constants.http.ContentTypeEnum;
import com.google.light.server.dto.module.ModuleType;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocInfoDto;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocResourceId;
import com.google.light.server.manager.implementation.oauth2.owner.OAuth2OwnerTokenManagerFactory;
import com.google.light.server.manager.interfaces.OAuth2OwnerTokenManager;
import com.google.light.server.persistence.entity.oauth2.owner.OAuth2OwnerTokenEntity;
import com.google.light.server.servlets.AbstractLightServlet;
import com.google.light.server.servlets.thirdparty.google.gdata.gdoc.GoogleDocUtils;
import com.google.light.server.thirdparty.clients.google.gdata.gdoc.DocsServiceWrapper;
import com.google.light.server.utils.LightUtils;
import com.google.light.server.utils.XmlUtils;
import java.net.URL;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet to show information about a Google Docs ResourceId.
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@SuppressWarnings("serial")
public class GoogleDocInfoServlet extends AbstractLightServlet {
  private HttpTransport httpTransport;
  private DocsServiceWrapper docsService;

  @Inject
  public GoogleDocInfoServlet(HttpTransport httpTransport) {
    httpTransport = checkNotNull(httpTransport, "httpTransport");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void service(HttpServletRequest request, HttpServletResponse response) {
    docsService = getInstance(DocsServiceWrapper.class);
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
      StringBuilder builder = new StringBuilder();

      String moduleTypeString = getRequestParameterValue(request, MODULE_TYPE);
      ModuleType moduleType = getByProviderServiceAndCategory(
          OAuth2ProviderService.GOOGLE_DOC, moduleTypeString);
      checkNotNull(moduleType, "moduleType");
      String typedResourceId = getRequestParameterValue(request, GOOGLE_DOC_RESOURCE_ID);
      checkNotBlank(typedResourceId, "untypedResourceId");

      GoogleDocResourceId resourceId = new GoogleDocResourceId(typedResourceId);

      GoogleDocInfoDto dto = docsService.getDocumentEntryWithAcl(resourceId);
      builder.append("\n JSON = " + dto.toJson());
      
      
      
      OAuth2OwnerTokenManagerFactory tokenManagerFactory = getInstance(
          OAuth2OwnerTokenManagerFactory.class);
      OAuth2OwnerTokenManager googDocTokenManager = tokenManagerFactory.create(GOOGLE_DOC);
      OAuth2OwnerTokenEntity token = googDocTokenManager.get();
      Preconditions.checkNotNull(token, "token not found.");

      String bearerToken = token.getTokenType() + " " + token.getAccessToken();
      
      HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.set("Authorization", bearerToken);
      
      HttpRequestFactory reqFactory = httpTransport.createRequestFactory();
      HttpRequest getRequest = reqFactory.buildGetRequest(new GenericUrl(dto.getHtmlExportUrl()));
      
      getRequest.setHeaders(httpHeaders);
      
      HttpResponse downloadResp = getRequest.execute();
      LightUtils.getInputStreamAsString(downloadResp.getContent());
      

      // Now fetching values to put in xml format.
      URL url = GoogleDocUtils.getResourceEntryWithAclFeedUrl(resourceId);
      DocumentListEntry entry = docsService.getEntry(url, DocumentListEntry.class);
      String entryXml = XmlUtils.getXmlEntry(entry.getAdaptedEntry());
      builder.append("\n\n EntryXml = \n").append(entryXml);

      response.setContentType(ContentTypeEnum.TEXT_PLAIN.get());
      response.getWriter().println(builder.toString());
    } catch (Exception e) {
      // TODO(arjuns): Add exception handling.
      throw new RuntimeException(e);
    }
  }
}
