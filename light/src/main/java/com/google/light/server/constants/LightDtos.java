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
import com.google.light.server.dto.AbstractPojo;
import com.google.light.server.dto.JSVariablesPreloadDto;
import com.google.light.server.dto.LoginStateDto;
import com.google.light.server.dto.admin.OAuth2ConsumerCredentialDto;
import com.google.light.server.dto.collection.CollectionDto;
import com.google.light.server.dto.collection.CollectionVersionDto;
import com.google.light.server.dto.module.ModuleDto;
import com.google.light.server.dto.module.ModuleVersionDto;
import com.google.light.server.dto.oauth2.owner.OAuth2OwnerTokenDto;
import com.google.light.server.dto.pages.PageDto;
import com.google.light.server.dto.person.PersonDto;
import com.google.light.server.dto.pojo.tree.CollectionTreeNodeDto;
import com.google.light.server.dto.pojo.tree.TreeNode;
import com.google.light.server.dto.search.SearchRequestDto;
import com.google.light.server.dto.search.SearchResultDto;
import com.google.light.server.dto.search.SearchResultItemDto;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocInfoDto;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocImportBatchJobContext;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocResourceId;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.GoogleDocResourceIdListWrapperDto;
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
  COLLECTION_DTO(CollectionDto.class, "collection"),
  COLLECTION_VERSION_DTO(CollectionVersionDto.class, "collection_version"),
  COLLECTION_TREE_NODE_DTO(CollectionTreeNodeDto.class, "collection_tree"),
  JS_VARIABLES_PRELOAD_DTO(JSVariablesPreloadDto.class, "js_variables_preload"),
  GOOGLE_DOC_RESOURCE_ID(GoogleDocResourceId.class, "google_doc_resource_id"),
  GOOGLE_DOC_INFO_DTO(GoogleDocInfoDto.class, "google_doc_info"),
  GOOGLE_DOC_RESOURCE_ID_LIST_DTO(GoogleDocResourceIdListWrapperDto.class, "google_doc_resource_id_list"),
  GOOGLE_DOC_INFO_LIST_DTO(GoogleDocImportBatchJobContext.class, "google_doc_resource_info_list"),
  GOOGLE_USER_INFO(GoogleUserInfo.class, "google_user_info"),
  LOGIN_STATE_DTO(LoginStateDto.class, "login_state"),
  
  // TODO(arjuns): Get rid of this from DTO.
  OAUTH2_OWNER_CREDENTIAL_DTO(OAuth2OwnerTokenDto.class, "owner_token"),
  // TODO(arjuns): Get rid of this from DTO.
  OAUTH2_CONSUMER_CREDENTIAL_DTO(OAuth2ConsumerCredentialDto.class, "consumer_credential"),
  MODULE_DTO(ModuleDto.class, "module"),
  MODULE_VERSION_DTO(ModuleVersionDto.class, "module_version"),
  PAGE_DTO(PageDto.class, "page"),
  PERSON_DTO(PersonDto.class, "person"),
  SEARCH_REQUEST_DTO(SearchRequestDto.class, "search_request"),
  SEARCH_RESULT_DTO(SearchResultDto.class, "search_result"),
  SEARCH_RESULT_ITEM_DTO(SearchResultItemDto.class, "search_result_item"),
  TREE_NODE(TreeNode.class, "tree_node");
  
  @SuppressWarnings("rawtypes")
  private Class<? extends AbstractPojo> clazz;
  private String xmlRootElementName;
  
  @SuppressWarnings("rawtypes")
  private LightDtos(Class<? extends AbstractPojo> clazz, String xmlRootElementName) {
    this.clazz = checkNotNull(clazz, "class");
    this.xmlRootElementName = Preconditions.checkNotNull(xmlRootElementName);
  }
  
  @SuppressWarnings("rawtypes")
  public Class<? extends AbstractPojo> getClazz() {
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
    
    for (int index=0; index < LightDtos.values().length; index++) {
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
    for (LightDtos currDto: LightDtos.values()) {
      if (clazz == currDto.clazz) {
        return currDto;
      }
    }
    return null;
  }
}
