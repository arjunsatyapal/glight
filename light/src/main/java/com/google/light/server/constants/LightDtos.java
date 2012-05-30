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
package com.google.light.server.constants;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.light.server.dto.AbstractDto;
import com.google.light.server.dto.AbstractDtoToPersistence;
import com.google.light.server.dto.JSVariablesPreloadDto;
import com.google.light.server.dto.NeedsDtoValidation;
import com.google.light.server.dto.RedirectDto;
import com.google.light.server.dto.admin.OAuth2ConsumerCredentialDto;
import com.google.light.server.dto.collection.CollectionDto;
import com.google.light.server.dto.collection.CollectionVersionDto;
import com.google.light.server.dto.importresource.ImportBatchWrapper;
import com.google.light.server.dto.importresource.ImportExternalIdDto;
import com.google.light.server.dto.module.ModuleDto;
import com.google.light.server.dto.module.ModuleVersionDto;
import com.google.light.server.dto.notifications.AbstractNotification;
import com.google.light.server.dto.notifications.ChildJobCompletionNotification;
import com.google.light.server.dto.oauth2.owner.OAuth2OwnerTokenDto;
import com.google.light.server.dto.pages.PageDto;
import com.google.light.server.dto.person.PersonDto;
import com.google.light.server.dto.pojo.tree.AbstractTreeNode;
import com.google.light.server.dto.pojo.tree.GoogleDocTree;
import com.google.light.server.dto.pojo.tree.collection.CollectionTreeNodeDto;
import com.google.light.server.dto.pojo.typewrapper.AbstractTypeWrapper;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.CollectionId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.JobId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.ModuleId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.PersonId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.Version;
import com.google.light.server.dto.pojo.typewrapper.stringwrapper.ExternalId;
import com.google.light.server.dto.pojo.typewrapper.stringwrapper.FTSDocumentId;
import com.google.light.server.dto.search.GSSClientLoginTokenInfoDto;
import com.google.light.server.dto.search.SearchRequestDto;
import com.google.light.server.dto.search.SearchResultDto;
import com.google.light.server.dto.search.SearchResultItemDto;
import com.google.light.server.dto.thirdparty.google.gdoc.GoogleDocImportBatchJobContext;
import com.google.light.server.dto.thirdparty.google.gdoc.GoogleDocInfoDto;
import com.google.light.server.dto.thirdparty.google.gdoc.GoogleDocResourceId;
import com.google.light.server.dto.thirdparty.google.youtube.ContentLicense;
import com.google.light.server.dto.thirdparty.google.youtube.YouTubePlaylistInfo;
import com.google.light.server.dto.thirdparty.google.youtube.YouTubeVideoInfo;
import com.google.light.server.jobs.handlers.collectionjobs.gdoccollection.ImportCollectionGoogleDocContext;
import com.google.light.server.jobs.handlers.collectionjobs.youtubeplaylist.ImportCollectionYouTubePlaylistContext;
import com.google.light.server.jobs.handlers.modulejobs.gdocument.ImportModuleGoogleDocJobContext;
import com.google.light.server.jobs.handlers.modulejobs.synthetic.ImportModuleSyntheticModuleJobContext;
import com.google.light.server.servlets.oauth2.google.pojo.GoogleUserInfo;
import java.util.List;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
public enum LightDtos {
  ABSTRACT_DTO(AbstractDto.class, ""),
  ABSTRACT_DTO_TO_PERSISTENCE(AbstractDtoToPersistence.class, ""),
  ABSTRACT_TREE_NODE(AbstractTreeNode.class, ""),
  ABSTRACT_NOTIFICATION(AbstractNotification.class, ""),
  ABSTRACT_TYPE_WRAPPER(AbstractTypeWrapper.class, ""),

  CHILD_JOB_COMPLETION_NOTIFICATION(ChildJobCompletionNotification.class,
                                    "childJobCompletionNotification"),
  COLLECTION_DTO(CollectionDto.class, "collection"),
  COLLECTION_ID(CollectionId.class, "collectionId"),
  COLLECTION_VERSION_DTO(CollectionVersionDto.class, "collectionVersion"),
  COLLECTION_TREE_NODE_DTO(CollectionTreeNodeDto.class, "collectionTree"),
  CONTENT_LICENSE_ENUM(ContentLicense.class, "contentLicense"),
  EXTERNAL_ID(ExternalId.class, "externalId"),
  
  FTS_DOCUMENT_ID(FTSDocumentId.class, "ftsDocumentId"),
  GOOGLE_DOC_RESOURCE_ID(GoogleDocResourceId.class, "googleDocResourceId"),
  GOOGLE_DOC_IMPORT_BATCH_JOB_CONTEXT(GoogleDocImportBatchJobContext.class,
                                      "googleDocImportBatchJobContext"),
  GOOGLE_DOC_IMPORT_JOB_CONTEXT(ImportModuleGoogleDocJobContext.class, "googleDocImportJobContext"),
  GOOGLE_DOC_INFO_DTO(GoogleDocInfoDto.class, "googleDocInfo"),
  GOOGLE_DOC_INFO_LIST_DTO(GoogleDocImportBatchJobContext.class, "googleDocResourceInfoList"),
  GOOGLE_DOC_TREE(GoogleDocTree.class, "googleDocTree"),
  GOOGLE_USER_INFO(GoogleUserInfo.class, "googleUserInfo"),
  GSS_CLIENT_LOGIN_TOKEN_INFO_DTO(GSSClientLoginTokenInfoDto.class, "gssClientLoginTokenInfo"),

  IMPORT_BATCH_WRAPPER(ImportBatchWrapper.class, "importBatch"),
  IMPORT_COLLECTION_GOOGLE_DOC_CONTEXT(ImportCollectionGoogleDocContext.class, "importCollectionGoogleDocContext"),
  IMPORT_COLLECTION_YOU_TUBE_PLAYLIST_CONTEXT(ImportCollectionYouTubePlaylistContext.class, "importCollectionYouTubePlaylistContext"),
  IMPORT_EXTERNAL_ID_DTO(ImportExternalIdDto.class, "importExternalId"),
  IMPORT_MODULE_SYNTHETIC_JOB_CONTEXT(ImportModuleSyntheticModuleJobContext.class, "importModuleSyntheticModuleJobContext"),
  
  JOB_ID(JobId.class, "jobId"),
  MODULE_ID(ModuleId.class, "moduleId"),
  PERSON_ID(PersonId.class, "personId"),
  
  JS_VARIABLES_PRELOAD_DTO(JSVariablesPreloadDto.class, "jsVariablesPreload"),

  // TODO(arjuns): Get rid of this from DTO.
  OAUTH2_OWNER_CREDENTIAL_DTO(OAuth2OwnerTokenDto.class, "oauth2OwnerToken"),
  // TODO(arjuns): Get rid of this from DTO.
  OAUTH2_CONSUMER_CREDENTIAL_DTO(OAuth2ConsumerCredentialDto.class, "oauth2ConsumerCredential"),
  
  MODULE_DTO(ModuleDto.class, "module"),
  MODULE_VERSION_DTO(ModuleVersionDto.class, "moduleVersion"),

  PAGE_DTO(PageDto.class, "page"),
  PERSON_DTO(PersonDto.class, "person"),

  REDIRECT_DTO(RedirectDto.class, "redirect"),

  SEARCH_REQUEST_DTO(SearchRequestDto.class, "searchRequest"),
  SEARCH_RESULT_DTO(SearchResultDto.class, "searchResult"),
  SEARCH_RESULT_ITEM_DTO(SearchResultItemDto.class, "searchResultItem"),
  VERSION(Version.class, "version"),
  YOU_TUBE_PLAYLIST_INFO(YouTubePlaylistInfo.class, "youTubePlaylistInfo"),
  YOU_TUBE_VIDEO_INFO(YouTubeVideoInfo.class, "youTubeVideoInfo");

  private Class<? extends NeedsDtoValidation> clazz;
  private String xmlRootElementName;

  private LightDtos(Class<? extends NeedsDtoValidation> clazz, String xmlRootElementName) {
    this.clazz = checkNotNull(clazz, "class");
    this.xmlRootElementName = Preconditions.checkNotNull(xmlRootElementName);
  }

  public Class<? extends NeedsDtoValidation> getClazz() {
    return clazz;
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public static List<Class<? extends AbstractDto>> getListOfDtoClasses() {
    List<Class<? extends AbstractDto>> list = Lists.newArrayList();

    for (LightDtos curr : LightDtos.values()) {
      list.add(((Class<? extends AbstractDto>) curr.getClazz()));
    }

    return list;
  }

  @SuppressWarnings("rawtypes")
  public static Class[] getArrayOfDtoClasses() {
    Class[] array = new Class[LightDtos.values().length];

    for (int index = 0; index < LightDtos.values().length; index++) {
      array[index] = LightDtos.values()[index].getClazz();
    }

    return array;
  }

  @VisibleForTesting
  String getXmlRootElementName() {
    return xmlRootElementName;
  }

  @SuppressWarnings("rawtypes")
  @VisibleForTesting
  static LightDtos findByDtoClass(Class clazz) {
    for (LightDtos currDto : LightDtos.values()) {
      if (clazz == currDto.clazz) {
        return currDto;
      }
    }
    return null;
  }
}
