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
package com.google.light.server.thirdparty.clients.google.gdata.gdoc;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.constants.LightConstants.HTTP_CONNECTION_TIMEOUT_IN_MILLIS;
import static com.google.light.server.constants.OAuth2ProviderService.GOOGLE_DOC;

import com.google.light.server.constants.JerseyConstants;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.gdata.client.docs.DocsService;
import com.google.gdata.data.Entry;
import com.google.gdata.data.Link;
import com.google.gdata.data.docs.ArchiveConversion;
import com.google.gdata.data.docs.ArchiveEntry;
import com.google.gdata.data.docs.ArchiveNotify;
import com.google.gdata.data.docs.ArchiveResourceId;
import com.google.gdata.data.docs.DocumentListEntry;
import com.google.gdata.data.docs.DocumentListFeed;
import com.google.gdata.util.ResourceNotFoundException;
import com.google.inject.Inject;
import com.google.light.server.constants.http.ContentTypeEnum;
import com.google.light.server.dto.pages.PageDto;
import com.google.light.server.dto.pojo.GoogleDocArchivePojo;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocInfoDto;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocResourceId;
import com.google.light.server.exception.unchecked.GoogleDocException;
import com.google.light.server.exception.unchecked.GoogleDocsException;
import com.google.light.server.exception.unchecked.httpexception.NotFoundException;
import com.google.light.server.manager.implementation.oauth2.owner.OAuth2OwnerTokenManagerFactory;
import com.google.light.server.manager.interfaces.OAuth2OwnerTokenManager;
import com.google.light.server.persistence.entity.oauth2.owner.OAuth2OwnerTokenEntity;
import com.google.light.server.servlets.thirdparty.google.gdata.gdoc.GoogleDocUtils;
import com.google.light.server.utils.XmlUtils;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

/**
 * Wrapper for {@link DocsService}.
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class DocsServiceWrapper extends DocsService {
  @Inject
  public DocsServiceWrapper(OAuth2OwnerTokenManagerFactory ownerTokenManagerFactory) {
    // TODO(arjuns): Inject application Name.
    super("light");
    OAuth2OwnerTokenManagerFactory tokenManagerFactory = checkNotNull(
        ownerTokenManagerFactory, "ownerTokenManagerFactory");
    OAuth2OwnerTokenManager googDocTokenManager = tokenManagerFactory.create(GOOGLE_DOC);
    OAuth2OwnerTokenEntity token = googDocTokenManager.get();
    checkNotNull(token, "token not found.");

    setUserToken(token.getAuthorizationToken());
    getRequestFactory().setHeader("Authorization", token.getAuthorizationToken());
    getRequestFactory().setHeader("GData-Version", "3.0");
    setConnectTimeout(HTTP_CONNECTION_TIMEOUT_IN_MILLIS);
  }

  public PageDto getDocumentFeedWithFolders(URL feedUrl) {
    DocumentListFeed docListFeed = null;
    try {
      docListFeed = getFeed(feedUrl, DocumentListFeed.class);
      System.out.println(XmlUtils.getXmlFeed(docListFeed));

      // TODO(arjuns): Test this on a person who has no document.
      checkNotNull(docListFeed, "docList should not be null.");

      List<GoogleDocInfoDto> listOfDocuments = Lists.newArrayList();

      for (DocumentListEntry currEntry : docListFeed.getEntries()) {
        GoogleDocInfoDto dto =
            new GoogleDocInfoDto.Builder(GoogleDocInfoDto.Configuration.DTO_FOR_IMPORT)
                .withDocumentListEntry(currEntry)
                .build();
        listOfDocuments.add(dto);
      }

      String previous = getUriFromLink(docListFeed.getPreviousLink());
      String next = getUriFromLink(docListFeed.getNextLink());

      PageDto pageDto = new PageDto.Builder()
          .lightUri(JerseyConstants.URI_GOOGLE_DOC_LIST)
          .previous(previous)
          .next(next)
          .list(listOfDocuments)
          .build();

      return pageDto;
    } catch (Exception e) {
      throw new GoogleDocException(e);
    }
  }

  private String getUriFromLink(Link link) throws UnsupportedEncodingException {
    if (link == null) {
      return null;
    }

    String encodedString = URLEncoder.encode(link.getHref(), Charsets.UTF_8.displayName());
    return encodedString;
  }

  public GoogleDocInfoDto getDocumentEntryWithAcl(GoogleDocResourceId resourceId) {
    DocumentListEntry entry;
    try {
      entry =
          getEntry(GoogleDocUtils.getResourceEntryWithAclFeedUrl(resourceId),
              DocumentListEntry.class);
    } catch (ResourceNotFoundException e) {
      throw new NotFoundException("GoogleDoc resource[" + resourceId + "] not found.");
    } catch (Exception e) {
      throw new GoogleDocException(e);
    }
    GoogleDocInfoDto dto =
        new GoogleDocInfoDto.Builder(GoogleDocInfoDto.Configuration.DTO_FOR_DEBUGGING)
            .withDocumentListEntry(entry)
            .build();
    return dto;
  }

  public GoogleDocArchivePojo archiveResource(GoogleDocResourceId resourceId) throws GoogleDocsException {
    try {
      ArchiveEntry archiveEntry = new ArchiveEntry();

      ArchiveResourceId archiveResourceId = new ArchiveResourceId(resourceId.getTypedResourceId());
      archiveEntry.addArchiveResourceId(archiveResourceId);
      //
      // ArchiveResourceId docResourceId = new
      // ArchiveResourceId("document%3A1tJZGzv_2sjMpvs4jtwxg18PGuSG-6nlfmx8Hlqa-_58");
      // archiveEntry.addArchiveResourceId(docResourceId);
      //
      // ArchiveResourceId drawingResrouceId = new
      // ArchiveResourceId("drawing:1tw4KMrNIv75260iR90i6wuGd31OabSAGYiwmqIkRQoY");
      // archiveEntry.addArchiveResourceId(drawingResrouceId);
      //
      // ArchiveResourceId formResourceId = new
      // ArchiveResourceId("form:0Al5KDir5QLAcdDhoSmVlV2JoV20yNXRIY0VfTGhDSnc");
      // archiveEntry.addArchiveResourceId(formResourceId);
      //
      // ArchiveResourceId presentationResourceId = new
      // ArchiveResourceId("presentation:1SII0J1EBCA-4PUdtZ9ZVi4Xp3JOgVN6pQPRecZCpyds");
      // archiveEntry.addArchiveResourceId(presentationResourceId);
      //
      // ArchiveResourceId spreadsheetResourceId = new
      // ArchiveResourceId("spreadsheet:0Al5KDir5QLAcdGE1Q0g1NUhndXRDSmRxdXBuVW0yYWc");
      // archiveEntry.addArchiveResourceId(spreadsheetResourceId);

      // ArchiveResourceId collectionResourceId = new
      // ArchiveResourceId("folder:0B15KDir5QLAcQlpiM1hVS25RUUdxcVAwQlNYcXZDQQ");
      // archiveEntry.addArchiveResourceId(collectionResourceId);

      // TODO(arjuns) : Update this email.
      ArchiveNotify archiveNotify = new ArchiveNotify("unit-test1@myopenedu.com");
      archiveEntry.setArchiveNotify(archiveNotify);

      updateArchiveConversion(archiveEntry);

      ArchiveEntry archiveResponseEntry = insert(GoogleDocUtils.getArchiveUrl(), archiveEntry);

      GoogleDocArchivePojo pojo =
          new GoogleDocArchivePojo.Builder().withArchiveEntry(archiveResponseEntry).build();

      return pojo;
    } catch (Exception e) {
      throw new GoogleDocsException(e);
    }
  }

  private void updateArchiveConversion(ArchiveEntry archiveEntry) {
    ArchiveConversion docConversion = new ArchiveConversion(
        ContentTypeEnum.GOOGLE_DOC.get(),
        ContentTypeEnum.TEXT_HTML.get());
    archiveEntry.addArchiveConversion(docConversion);
  }

  public GoogleDocArchivePojo getArchiveStatus(String archiveId) {
    try {
      ArchiveEntry statusEntry =
          getEntry(GoogleDocUtils.getArchiveStatusUrl(archiveId), ArchiveEntry.class);
      
      System.out.println(XmlUtils.getXmlEntry(statusEntry));
      GoogleDocArchivePojo pojo =
          new GoogleDocArchivePojo.Builder().withArchiveEntry(statusEntry).build();

      return pojo;
    } catch (Exception e) {
      throw new GoogleDocsException(e);
    }
  }

  public void getUserAccountInformation() {
    try {
      // HttpRequest request = httpTransport.createRequestFactory().buildGetRequest(
      // new GenericUrl(GoogleDocUtils.getUserAccountInfoUrl().toString()));
      //
      // HttpResponse response = request.execute();
      //
      // System.out.println(LightUtils.getInputStreamAsString(response.getContent()));
      Entry entry = getEntry(GoogleDocUtils.getUserAccountInfoUrl(), Entry.class);
      System.out.println(XmlUtils.getXmlEntry(entry));
    } catch (Exception e) {
      throw new GoogleDocException(e);
    }
  }
}
