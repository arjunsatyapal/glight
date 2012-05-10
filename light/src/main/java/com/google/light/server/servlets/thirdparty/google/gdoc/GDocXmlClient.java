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
package com.google.light.server.servlets.thirdparty.google.gdoc;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.constants.OAuth2ProviderService.GOOGLE_DOC;
import static com.google.light.server.servlets.thirdparty.google.gdoc.GoogleDocUtils.getResourceEntryWithFoldersUrl;
import static com.google.light.server.utils.GuiceUtils.getInstance;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.inject.Inject;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocResourceId;
import com.google.light.server.manager.implementation.oauth2.owner.OAuth2OwnerTokenManagerFactory;
import com.google.light.server.manager.interfaces.OAuth2OwnerTokenManager;
import com.google.light.server.persistence.entity.oauth2.owner.OAuth2OwnerTokenEntity;
import com.google.light.server.utils.LightUtils;
import com.google.light.server.utils.XmlUtils;
import java.net.URL;

/**
 * A simple helper client that will send requests and fetch AtomFeeds directly from Google Doc.
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class GDocXmlClient {
  private HttpTransport httpTransport;
  private OAuth2OwnerTokenManager googDocTokenManager;

  @Inject
  public GDocXmlClient(HttpTransport httpTransport) {
    this.httpTransport = checkNotNull(httpTransport, "httpTransport");

    OAuth2OwnerTokenManagerFactory tokenManagerFactory = getInstance(
        OAuth2OwnerTokenManagerFactory.class);
    googDocTokenManager = tokenManagerFactory.create(GOOGLE_DOC);
  }

  private HttpHeaders getDefaultHeaders() {
    OAuth2OwnerTokenEntity token = googDocTokenManager.get();
    String bearerToken = token.getTokenType() + " " + token.getAccessToken();
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.set("Authorization", bearerToken);
    httpHeaders.set("GData-Version", "3.0");

    return httpHeaders;

  }

  public void getFullDocumentFeed() {
    String url = "https://docs.google.com/feeds/default/private/full";

    fetchFeedFromGoogle(url);
  }

  public String getDocumentListUserMetadata() {
    String url = "https://docs.google.com/feeds/metadata/default";
    return fetchFeedFromGoogle(url);
  }

  public String getFolderFeed(GoogleDocResourceId resourceId, int maxResult) {
    URL url = GoogleDocUtils.getFolderContentUrl(resourceId, maxResult);
    return fetchFeedFromGoogle(url.toString());
  }

  public String getDocumentPermissions(GoogleDocResourceId resourceId) {
    URL url = getResourceEntryWithFoldersUrl(resourceId);
    return fetchFeedFromGoogle(url.toString());
  }

  /**
   * @param url
   * @throws IOException
   * @throws JDOMException
   */
  private String fetchFeedFromGoogle(String url) {
    try {
      HttpRequest request =
          httpTransport.createRequestFactory().buildGetRequest(new GenericUrl(url))
              .setHeaders(getDefaultHeaders());

      HttpResponse response = request.execute();
      String xml = LightUtils.getInputStreamAsString(response.getContent());
      String prettyXml = XmlUtils.pretyfyXml(xml); 
      return prettyXml;
    } catch (Exception e) {
      // TODO(arjuns): Add exception handling.
      throw new RuntimeException(e);
    }
  }
}
