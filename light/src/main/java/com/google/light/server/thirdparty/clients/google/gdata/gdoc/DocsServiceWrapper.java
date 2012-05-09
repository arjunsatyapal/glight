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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.constants.LightConstants.HTTP_CONNECTION_TIMEOUT_IN_MILLIS;
import static com.google.light.server.constants.OAuth2ProviderService.GOOGLE_DOC;
import static com.google.light.server.constants.google.cloudstorage.GoogleCloudStorageBuckets.WORKSPACE;
import static com.google.light.server.utils.GoogleCloudStorageUtils.getAbsolutePathOnBucket;
import static com.google.light.server.utils.GoogleCloudStorageUtils.writeFileOnGCS;
import static com.google.light.server.utils.GuiceUtils.getInstance;
import static com.google.light.server.utils.LightUtils.getURL;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.appengine.api.files.GSFileOptions;
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
import com.google.light.server.constants.HttpHeaderEnum;
import com.google.light.server.constants.google.cloudstorage.GoogleCloudStorageBuckets;
import com.google.light.server.constants.http.ContentTypeEnum;
import com.google.light.server.dto.AbstractDto;
import com.google.light.server.dto.pages.PageDto;
import com.google.light.server.dto.pojo.GoogleDocArchivePojo;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocInfoDto;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocResourceId;
import com.google.light.server.exception.unchecked.GoogleDocException;
import com.google.light.server.exception.unchecked.httpexception.NotFoundException;
import com.google.light.server.manager.implementation.oauth2.owner.OAuth2OwnerTokenManagerFactory;
import com.google.light.server.manager.interfaces.OAuth2OwnerTokenManager;
import com.google.light.server.persistence.entity.oauth2.owner.OAuth2OwnerTokenEntity;
import com.google.light.server.servlets.thirdparty.google.gdoc.GoogleDocUtils;
import com.google.light.server.utils.GoogleCloudStorageUtils;
import com.google.light.server.utils.LightUtils;
import com.google.light.server.utils.XmlUtils;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

/**
 * Wrapper for {@link DocsService}.
 * This class depends on some request scoped parameters and therefore it is required that those
 * values are injected before an attempt is made to construct instance for this class.
 * Therefore this can be injected in two methods :
 * 1. User Provider.
 * 2. User {@link com.google.light.server.utils.GuiceUtils#getInstance(DocsServiceWrapper)} instead
 * of
 * injecting this class directly in Constructor.
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class DocsServiceWrapper extends DocsService {
  private String authorizationToken;

  @Inject
  public DocsServiceWrapper(OAuth2OwnerTokenManagerFactory ownerTokenManagerFactory) {
    // TODO(arjuns): Inject application Name.
    super("light");
    OAuth2OwnerTokenManagerFactory tokenManagerFactory = checkNotNull(
        ownerTokenManagerFactory, "ownerTokenManagerFactory");
    OAuth2OwnerTokenManager googDocTokenManager = tokenManagerFactory.create(GOOGLE_DOC);
    OAuth2OwnerTokenEntity token = googDocTokenManager.get();

    authorizationToken = token.getAuthorizationToken();
    setUserToken(token.getAuthorizationToken());
    getRequestFactory().setHeader("Authorization", authorizationToken);
    getRequestFactory().setHeader("GData-Version", "3.0");
    setConnectTimeout(HTTP_CONNECTION_TIMEOUT_IN_MILLIS);
  }

  /**
   * Get DocumentFeed for a user.
   * 
   * @param feedUrl
   * @return
   */
  public PageDto getDocumentFeedWithFolders(URL feedUrl, String handlerUri) {
    DocumentListFeed docListFeed = null;
    try {
      docListFeed = getFeed(feedUrl, DocumentListFeed.class);

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

      String startIndex = getUriFromLink(docListFeed.getNextLink());
      PageDto pageDto = new PageDto.Builder()
          .handlerUri(handlerUri)
          .startIndex(startIndex)
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

  /**
   * Get Document Entry for a Google Doc.
   * 
   * @param resourceId
   * @return
   */
  public GoogleDocInfoDto getGoogleDocInfo(GoogleDocResourceId resourceId) {
    DocumentListEntry entry;
    try {
      entry = getEntry(GoogleDocUtils.getResourceEntryWithAclFeedUrl(resourceId),
          DocumentListEntry.class);
    } catch (ResourceNotFoundException e) {
      throw new NotFoundException("GoogleDoc resource[" + resourceId + "] was not found.");
    } catch (Exception e) {
      throw new GoogleDocException(e);
    }
    GoogleDocInfoDto dto =
        new GoogleDocInfoDto.Builder(GoogleDocInfoDto.Configuration.DTO_FOR_DEBUGGING)
            .withDocumentListEntry(entry)
            .build();
    return dto;
  }

  /**
   * Get Google Document Info for List of Google Doc ResourceIds;
   * 
   * @param resourceId
   * @return
   */
  public List<GoogleDocInfoDto> getGoogleDocInfoInBatch(List<GoogleDocResourceId> resourceIdList) {
    List<GoogleDocInfoDto> listOfInfo = Lists.newArrayList();
    for (GoogleDocResourceId currResourceId : resourceIdList) {
      GoogleDocInfoDto docInfo = getGoogleDocInfo(currResourceId);
      listOfInfo.add(docInfo);
    }

    return listOfInfo;
  }

  @SuppressWarnings("rawtypes")
  public List<GoogleDocInfoDto> getFolderContentWhichAreSupported(GoogleDocResourceId resourceId) {
    String randomString = LightUtils.getUUIDString();

    List<GoogleDocInfoDto> list = Lists.newArrayList();
    PageDto pageDto = null;

    do {
      if (pageDto == null) {
        pageDto = getFolderContentPageWise(resourceId, randomString);
      } else {
        String decodedStartIndex = LightUtils.decodeFromUrlEncodedString(pageDto.getStartIndex());
        pageDto = getFolderContentWithStartIndex(getURL(decodedStartIndex), randomString);
      }

      for (AbstractDto currDto : pageDto.getList()) {
        checkArgument(currDto instanceof GoogleDocInfoDto);
        GoogleDocInfoDto currDocInfo = (GoogleDocInfoDto) currDto;
        
        if (currDocInfo.getGoogleDocsResourceId().getModuleType().isSupported()) {
          list.add(currDocInfo);
        }
      }
    } while (pageDto.getStartIndex() != null);

    return list;
  }

  public PageDto getFolderContentPageWise(GoogleDocResourceId resourceId, String handlerUri) {
    return getFolderContentWithStartIndex(GoogleDocUtils.getFolderContentUrl(resourceId),
        handlerUri);
  }

  public PageDto getFolderContentWithStartIndex(URL startIndex, String handlerUri) {
    return getDocumentFeedWithFolders(startIndex, handlerUri);
  }

  public GoogleDocArchivePojo archiveResource(GoogleDocResourceId resourceId)
      throws GoogleDocException {
    return archiveResources(Lists.newArrayList(resourceId));
  }

  public GoogleDocArchivePojo archiveResources(List<GoogleDocResourceId> listOfResourcesToArchive)
      throws GoogleDocException {
    try {
      ArchiveEntry archiveEntry = new ArchiveEntry();

      for (GoogleDocResourceId currResourceId : listOfResourcesToArchive) {
        ArchiveResourceId archiveResourceId = new ArchiveResourceId(
            currResourceId.getTypedResourceId());
        archiveEntry.addArchiveResourceId(archiveResourceId);
      }

      // TODO(arjuns) : Update this email.
      ArchiveNotify archiveNotify = new ArchiveNotify("unit-test1@myopenedu.com");
      archiveEntry.setArchiveNotify(archiveNotify);

      updateArchiveConversion(archiveEntry);

      ArchiveEntry archiveResponseEntry = insert(GoogleDocUtils.getArchiveUrl(), archiveEntry);

      GoogleDocArchivePojo pojo = new GoogleDocArchivePojo.Builder().withArchiveEntry(
          archiveResponseEntry).build();

      return pojo;
    } catch (Exception e) {
      throw new GoogleDocException(e);
    }
  }

  private void updateArchiveConversion(ArchiveEntry archiveEntry) {
    ArchiveConversion docConversion = new ArchiveConversion(
        ContentTypeEnum.GOOGLE_DOC.get(),
        ContentTypeEnum.TEXT_HTML.get());
    archiveEntry.addArchiveConversion(docConversion);
  }

  /**
   * Get status of an archive.
   */
  public GoogleDocArchivePojo getArchiveStatus(String archiveId) {
    try {
      ArchiveEntry statusEntry =
          getEntry(GoogleDocUtils.getArchiveStatusUrl(archiveId), ArchiveEntry.class);

      GoogleDocArchivePojo pojo = new GoogleDocArchivePojo.Builder()
          .withArchiveEntry(statusEntry).build();

      return pojo;
    } catch (Exception e) {
      throw new GoogleDocException(e);
    }
  }

  public String downloadArchive(String archiveLocation, String destinationFileName) {
    try {
      // Download Archive to Google Cloud Storage.
      HttpTransport transport = getInstance(HttpTransport.class);
      HttpRequest request = transport.createRequestFactory().buildGetRequest(
          new GenericUrl(archiveLocation));

      HttpHeaders httpHeaders = new HttpHeaders();
      httpHeaders.set(HttpHeaderEnum.AUTHORIZATION.get(), authorizationToken);
      request.setHeaders(httpHeaders);

      HttpResponse response = request.execute();

      GSFileOptions gsFileOptions = GoogleCloudStorageUtils.getGCSFileOptionsForCreate(
          GoogleCloudStorageBuckets.WORKSPACE,
          ContentTypeEnum.APPLICATION_ZIP, destinationFileName);

      // TODO(arjuns): Ensure that file does not exist.
      writeFileOnGCS(response.getContent(), gsFileOptions);

      String gcsFilePath = getAbsolutePathOnBucket(WORKSPACE, destinationFileName);
      return gcsFilePath;
    } catch (Exception e) {
      throw new GoogleDocException(e);
    }
  }

  public void getUserAccountInformation() {
    try {
      Entry entry = getEntry(GoogleDocUtils.getUserAccountInfoUrl(), Entry.class);
      System.out.println(XmlUtils.getXmlEntry(entry));
    } catch (Exception e) {
      throw new GoogleDocException(e);
    }
  }
}
