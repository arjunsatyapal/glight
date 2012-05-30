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
import static com.google.light.server.constants.LightConstants.GDATA_GDOC_MAX_RESULTS;
import static com.google.light.server.httpclient.LightHttpClient.getHeaderValueFromResponse;
import static com.google.light.server.utils.LightUtils.createExternalIdTreeNode;
import static com.google.light.server.utils.LightUtils.getURI;

import com.google.api.client.http.HttpResponse;
import com.google.light.server.constants.HttpHeaderEnum;
import com.google.light.server.dto.module.ModuleType;
import com.google.light.server.dto.pojo.tree.AbstractTreeNode.TreeNodeType;
import com.google.light.server.dto.pojo.tree.externaltree.ExternalIdTreeNodeDto;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.ModuleId;
import com.google.light.server.dto.pojo.typewrapper.stringwrapper.ExternalId;
import com.google.light.server.dto.thirdparty.google.gdoc.GoogleDocInfoDto;
import com.google.light.server.dto.thirdparty.google.gdoc.GoogleDocResourceId;
import com.google.light.server.dto.thirdparty.google.youtube.ContentLicense;
import com.google.light.server.dto.thirdparty.google.youtube.YouTubePlaylistInfo;
import com.google.light.server.dto.thirdparty.google.youtube.YouTubeVideoInfo;
import com.google.light.server.exception.unchecked.InvalidExternalIdException;
import com.google.light.server.httpclient.LightHttpClient;
import com.google.light.server.manager.interfaces.ModuleManager;
import com.google.light.server.persistence.entity.module.ModuleEntity;
import com.google.light.server.servlets.thirdparty.google.gdoc.DocsServiceWrapper;
import com.google.light.server.servlets.thirdparty.google.youtube.YouTubeServiceWrapper;
import com.google.light.server.urls.LightUrl;
import com.google.light.server.urls.YouTubeUrl;
import java.net.URI;
import java.util.List;

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
    ModuleType moduleType = externalId.getModuleType();

    switch (moduleType) {
      case GOOGLE_COLLECTION:
      case GOOGLE_DOCUMENT:
        return getTitleForGDocResource(externalId);

      case LIGHT_HOSTED_MODULE:
        return lihtModuleTitle(externalId);

      case LIGHT_SYNTHETIC_MODULE:
        return getTitleForSyntheticModule(externalId);

      case YOU_TUBE_VIDEO:
      case YOU_TUBE_PLAYLIST:
        return getTitleForYouTubeResource(externalId);

      default:
        throw new InvalidExternalIdException(externalId);

    }
  }

  /**
   * @param externalId
   * @return
   */
  private static String getTitleForYouTubeResource(ExternalId externalId) {
    YouTubeUrl ytUrl = new YouTubeUrl(externalId);
    YouTubeServiceWrapper ytService = GuiceUtils.getInstance(YouTubeServiceWrapper.class);

    ModuleType moduleType = ytUrl.getModuleType();
    switch (moduleType) {
      case YOU_TUBE_VIDEO:
        YouTubeVideoInfo videoInfo = ytService.getYouTubeVideoInfo(ytUrl);
        return videoInfo.getTitle();

      case YOU_TUBE_PLAYLIST:
        YouTubePlaylistInfo playlistInfo = ytService.getYouTubePlayListInfo(ytUrl);
        return playlistInfo.getTitle();

      default:
        throw new IllegalStateException("Unsupported moduleType : " + moduleType
            + "for " + externalId);
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
      GoogleDocInfoDto info = docsService.getGoogleDocInfo(resourceId,
          GoogleDocInfoDto.Configuration.DTO_FOR_IMPORT);
      return info.getTitle();
    } catch (Exception e) {
      throw new InvalidExternalIdException(externalId);
    }
  }

  public static ExternalIdTreeNodeDto createExternalIdTree(ExternalId externalId) {
    ModuleType moduleType = externalId.getModuleType();
    switch (moduleType) {
      case GOOGLE_COLLECTION:
      case GOOGLE_DOCUMENT:
        return createExternalIdTreeForGDocResource(externalId);

      case YOU_TUBE_VIDEO:
      case YOU_TUBE_PLAYLIST:
        return createExternalIdTreeForYouTubeResource(externalId);

      case LIGHT_HOSTED_MODULE:
        return createExternalIdTreeForLightModule(externalId);
      case LIGHT_SYNTHETIC_MODULE:
        return createExternalIdTreeForSyntheticModule(externalId);

      default:
        throw new InvalidExternalIdException(externalId);

    }
  }

  /**
   * @param externalId
   * @return
   */
  private static ExternalIdTreeNodeDto
      createExternalIdTreeForSyntheticModule(ExternalId externalId) {
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
      return createExternalIdTreeNode(externalId, title,
          TreeNodeType.LEAF_NODE, ContentLicense.DEFAULT_UNKNOWN_LICENSES);
    } catch (Exception e) {
      throw new InvalidExternalIdException(externalId);
    }
  }

  /**
   * @param externalId
   * @return
   */
  private static ExternalIdTreeNodeDto createExternalIdTreeForLightModule(ExternalId externalId) {
    ModuleManager moduleManager = GuiceUtils.getInstance(ModuleManager.class);
    ModuleId moduleId = moduleManager.findModuleIdByExternalId(null, externalId);

    if (moduleId == null) {
      throw new InvalidExternalIdException(externalId);
    }

    ModuleEntity moduleEntity = moduleManager.get(null, moduleId);

    return createExternalIdTreeNode(externalId, moduleEntity.getTitle(),
        TreeNodeType.LEAF_NODE, moduleEntity.getContentLicenses());
  }

  private static ExternalIdTreeNodeDto createExternalIdTreeForGDocResource(ExternalId externalId) {
    DocsServiceWrapper docsService = GuiceUtils.getInstance(DocsServiceWrapper.class);
    GoogleDocResourceId gdocResourceId = new GoogleDocResourceId(externalId);

    GoogleDocInfoDto gdocInfo = docsService.getGoogleDocInfo(gdocResourceId,
        GoogleDocInfoDto.Configuration.DTO_FOR_DEBUGGING);

    ModuleType moduleType = externalId.getModuleType();
    if (moduleType == ModuleType.GOOGLE_COLLECTION) {
      return createExternalIdTreeForGoogleCollection(externalId,
          gdocInfo, docsService);
    } else if (moduleType == ModuleType.GOOGLE_DOCUMENT) {
      return LightUtils.createExternalIdTreeNode(externalId, gdocInfo.getTitle(),
          TreeNodeType.LEAF_NODE, ContentLicense.DEFAULT_LIGHT_CONTENT_LICENSES);
    } else {
      throw new IllegalArgumentException("Should not be called for : " + moduleType
          + " but was called for " + externalId);
    }
  }

  private static ExternalIdTreeNodeDto createExternalIdTreeForGoogleCollection(
      ExternalId externalId, GoogleDocInfoDto gdocInfo, DocsServiceWrapper docsService) {
    GoogleDocResourceId gdocResourceId = new GoogleDocResourceId(externalId);

    ExternalIdTreeNodeDto externalIdTreeNode = createExternalIdTreeNode(externalId,
        gdocInfo.getTitle(), gdocResourceId.getModuleType().getNodeType(), null /* license */);

    List<GoogleDocInfoDto> listOfChilds =
        docsService.getFolderContentWhichAreSupportedInAlphabeticalOrder(gdocResourceId,
            GDATA_GDOC_MAX_RESULTS, GoogleDocInfoDto.Configuration.DTO_FOR_DEBUGGING);

    for (GoogleDocInfoDto currChild : listOfChilds) {
      ExternalIdTreeNodeDto childTreeNode = null;
      if (currChild.getModuleType() == ModuleType.GOOGLE_COLLECTION) {
        childTreeNode = createExternalIdTreeForGoogleCollection(
            currChild.getExternalId(), currChild, docsService);
      } else if (currChild.getModuleType() == ModuleType.GOOGLE_DOCUMENT) {
        childTreeNode = createExternalIdTreeNode(currChild.getExternalId(),
            currChild.getTitle(), TreeNodeType.LEAF_NODE,
            ContentLicense.DEFAULT_LIGHT_CONTENT_LICENSES);
      } else {
        throw new IllegalStateException("Code should not reach here but reached for : "
            + currChild.getExternalId() + " and for parent : " + externalId);
      }

      externalIdTreeNode.addChildren(childTreeNode);
    }

    return externalIdTreeNode;
  }

  private static ExternalIdTreeNodeDto
      createExternalIdTreeForYouTubeResource(ExternalId externalId) {
    YouTubeServiceWrapper ytService = GuiceUtils.getInstance(YouTubeServiceWrapper.class);
    YouTubeUrl ytUrl = new YouTubeUrl(externalId);

    ModuleType moduleType = externalId.getModuleType();

    if (moduleType == ModuleType.YOU_TUBE_VIDEO) {
      YouTubeVideoInfo videoInfo = ytService.getYouTubeVideoInfo(ytUrl);
      return LightUtils.createExternalIdTreeNode(externalId, videoInfo.getTitle(),
          TreeNodeType.LEAF_NODE, videoInfo.getContentLicenses());
    } else if (moduleType == ModuleType.YOU_TUBE_PLAYLIST) {
      return createExternalIdTreeForYouTubePlaylist(externalId, ytUrl, ytService);
    } else {
      throw new IllegalArgumentException("Should not be called for : " + moduleType
          + " but was called for " + externalId);
    }
  }

  private static ExternalIdTreeNodeDto createExternalIdTreeForYouTubePlaylist(
      ExternalId externalId, YouTubeUrl ytUrl, YouTubeServiceWrapper ytService) {
    YouTubePlaylistInfo ytPlInfo = ytService.getYouTubePlayListDetailedInfo(ytUrl);

    ExternalIdTreeNodeDto ytPlTreeNode = createExternalIdTreeNode(externalId,
        ytPlInfo.getTitle(), TreeNodeType.INTERMEDIATE_NODE, ytPlInfo.getContentLicenses());

    for (YouTubeVideoInfo currChild : ytPlInfo.getListOfVideos()) {
      ExternalIdTreeNodeDto childTreeNode = createExternalIdTreeNode(currChild.getExternalId(),
          currChild.getTitle(), TreeNodeType.LEAF_NODE, currChild.getContentLicenses());
      ytPlTreeNode.addChildren(childTreeNode);
    }

    return ytPlTreeNode;
  }
}
