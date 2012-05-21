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
package com.google.light.server.utils;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.httpclient.LightHttpClient.getHeaderValueFromResponse;
import static com.google.light.server.utils.LightUtils.getURI;

import com.google.light.server.dto.thirdparty.google.gdoc.GoogleDocInfoDto;
import com.google.light.server.dto.thirdparty.google.gdoc.GoogleDocResourceId;

import com.google.light.server.thirdparty.clients.google.gdoc.DocsServiceWrapper;

import com.google.api.client.http.HttpResponse;
import com.google.light.server.constants.HttpHeaderEnum;
import com.google.light.server.dto.pojo.typewrapper.stringwrapper.ExternalId;
import com.google.light.server.exception.unchecked.InvalidExternalIdException;
import com.google.light.server.httpclient.LightHttpClient;
import com.google.light.server.manager.interfaces.ModuleManager;
import com.google.light.server.persistence.entity.module.ModuleEntity;
import com.google.light.server.urls.LightUrl;
import java.net.URI;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public class ModuleUtils {
  public static String getTitleForExternalId(ExternalId externalId) {
    checkNotNull(externalId, "externalId");
    switch (externalId.getModuleType()) {
      case GOOGLE_COLLECTION:
      case GOOGLE_DOCUMENT:
        return getTitleForGDocResource(externalId);
        
      case LIGHT_HOSTED_MODULE:
        return lihtModuleTitle(externalId);

      case LIGHT_SYNTHETIC_MODULE:
        return getTitleForSyntheticModule(externalId);

      default:
        throw new InvalidExternalIdException(externalId);

    }
  }

  /**
   * @param externalId
   * @return
   */
  private static String lihtModuleTitle(ExternalId externalId) {
    LightUrl lightUrl = externalId.getLightUrl();
    ModuleManager moduleManager = GuiceUtils.getInstance(ModuleManager.class);
    ModuleEntity module = moduleManager.get(null, lightUrl.getModuleId());
    
    if (module == null) {
      throw new InvalidExternalIdException(externalId);
    }
    
    return module.getTitle();
  }

  /**
   * @param externalId
   * @return
   */
  private static String getTitleForSyntheticModule(ExternalId externalId) {
    // First see if this ExternalId is suitable for Synthetic Module.
    try {
      LightHttpClient httpClient = GuiceUtils.getInstance(LightHttpClient.class);
      URI uri = getURI(externalId.getValue());
      HttpResponse response = httpClient.get(uri);

      if (!response.isSuccessStatusCode()) {
        throw new InvalidExternalIdException(externalId);
      }

      String xFrameHeader = getHeaderValueFromResponse(response, HttpHeaderEnum.X_FRAME_OPTIONS);
      if (xFrameHeader != null) {
        // This module cannot be embedded inside iFrame so it is not allowed for synthetic module.
        throw new InvalidExternalIdException(externalId);
      }

      // Current ExternalId is suitable for Synthetic Module.
      String title = null;
      title = httpClient.getTitle(response, externalId);
      return title;
    } catch (Exception e) {
      throw new InvalidExternalIdException(externalId);
    }
  }

  /**
   * @param externalId
   * @return
   */
  private static String getTitleForGDocResource(ExternalId externalId) {
    DocsServiceWrapper docsService = GuiceUtils.getInstance(DocsServiceWrapper.class);
    GoogleDocResourceId resourceId = new GoogleDocResourceId(externalId);
    try {
      GoogleDocInfoDto info = docsService.getGoogleDocInfo(resourceId);
      return info.getTitle();
    } catch (Exception e) {
      throw new InvalidExternalIdException(externalId);
    }
  }
}
