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
package com.google.light.server.jersey.resources;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.constants.LightConstants.MAX_RESULTS_MAX;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;
import static com.google.light.server.utils.LightPreconditions.checkPersonLoggedIn;
import static com.google.light.server.utils.LightUtils.isListEmpty;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.light.server.constants.JerseyConstants;
import com.google.light.server.constants.LightConstants;
import com.google.light.server.constants.LightStringConstants;
import com.google.light.server.constants.http.ContentTypeConstants;
import com.google.light.server.constants.http.ContentTypeEnum;
import com.google.light.server.dto.collection.CollectionDto;
import com.google.light.server.dto.collection.CollectionVersionDto;
import com.google.light.server.dto.pages.PageDto;
import com.google.light.server.dto.pojo.tree.CollectionTreeNodeDto;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.CollectionId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.Version;
import com.google.light.server.dto.thirdparty.google.gdata.gdoc.ExternalIdListWrapperDto;
import com.google.light.server.exception.unchecked.httpexception.NotFoundException;
import com.google.light.server.manager.interfaces.CollectionManager;
import com.google.light.server.persistence.entity.collection.CollectionEntity;
import com.google.light.server.persistence.entity.collection.CollectionVersionEntity;
import com.google.light.server.servlets.SessionManager;
import com.google.light.server.utils.GuiceUtils;
import com.google.light.server.utils.JsonUtils;
import com.google.light.server.utils.XmlUtils;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import org.apache.commons.lang.StringUtils;

/**
 * 
 * 
 * TODO(arjuns): Add test for this class.
 * 
 * @author Arjun Satyapal
 */
@Path(JerseyConstants.RESOURCE_PATH_COLLECTION)
public class CollectionResource extends AbstractJerseyResource {
  private final CollectionManager collectionManager;

  @Inject
  public CollectionResource(Injector injector, @Context HttpServletRequest request,
      @Context HttpServletResponse response, CollectionManager collectionManager) {
    super(injector, request, response);
    this.collectionManager = checkNotNull(collectionManager, "collectionManager");
  }

  /**
   * Get on a CollectionId will redirect to Latest Version for that CollectionId.
   */
  @GET
  @Path(JerseyConstants.PATH_COLLECTION_ID)
  @Produces({ ContentTypeConstants.APPLICATION_JSON, ContentTypeConstants.APPLICATION_XML })
  public CollectionDto getCollection(
      @PathParam(JerseyConstants.PATH_PARAM_COLLECTION_ID) String collectionIdStr) {
    CollectionId collectionId = new CollectionId(collectionIdStr);
    CollectionEntity collectionEntity = collectionManager.get(null, collectionId);
    
    if (collectionEntity == null) {
      throw new NotFoundException("Collection[" + collectionId + "] was not found.");
    }
    
    return collectionEntity.toDto();
  }

  @GET
  @Path(JerseyConstants.PATH_COLLECTION_VERSION)
  @Produces({ ContentTypeConstants.APPLICATION_JSON, ContentTypeConstants.APPLICATION_XML })
  public CollectionVersionDto getCollectionVersion(
      @PathParam(JerseyConstants.PATH_PARAM_COLLECTION_ID) String collectionIdStr,
      @PathParam(JerseyConstants.PATH_PARAM_VERSION) String versionStr) {
    CollectionVersionDto collectionVersionDto = getCollectionVersionDto(
        collectionIdStr, versionStr);
    return collectionVersionDto;
  }
  
  @PUT
  @Path(JerseyConstants.PATH_COLLECTION_VERSION)
  @Consumes({ ContentTypeConstants.APPLICATION_JSON, ContentTypeConstants.APPLICATION_XML })
  @Produces({ ContentTypeConstants.APPLICATION_JSON, ContentTypeConstants.APPLICATION_XML })
  public CollectionTreeNodeDto putCollectionVersion(
      @PathParam(JerseyConstants.PATH_PARAM_COLLECTION_ID) String collectionIdStr,
      @PathParam(JerseyConstants.PATH_PARAM_VERSION) String versionStr,
      String body) {
    ContentTypeEnum contentType = ContentTypeEnum.getContentTypeByString(
        request.getContentType());
    
    ExternalIdListWrapperDto resourceIdListWrapper = null;
    if (contentType == ContentTypeEnum.APPLICATION_JSON
        || contentType == ContentTypeEnum.APPLICATION_URL_ENCODED) {
      resourceIdListWrapper = JsonUtils.getDto(body, ExternalIdListWrapperDto.class);
    } else if (contentType == ContentTypeEnum.APPLICATION_XML) {
      resourceIdListWrapper = XmlUtils.getDto(body);
    } else {
      throw new IllegalArgumentException("Invalid contentType : " + contentType);
    }
    System.out.println(body);
    return null;
  }

  /**
   * @param tree
   * @param builder
   */
  private void appendCurrNode(CollectionTreeNodeDto node, StringBuilder builder, int space) {
    builder.append("<br>");
    
    builder.append("<pre>");
    for (int i = 0; i < space; i++) {
      builder.append(" ");
    }

    if (node.isLeafNode()) {
      String uri = "/rest/module/" + node.getModuleId().getValue() + "/latest/content";
      builder.append("<a href=").append(uri).append(">").append(node.getTitle()).append("</a>");
    } else {
      builder.append("<b>").append(node.getTitle()).append("</b>");
      if (node.hasChildren()) {
        for (CollectionTreeNodeDto currChild : node.getChildren()) {
          appendCurrNode(currChild, builder, space * 2);
        }
      }
    }
    builder.append("</pre>");
  }

  protected CollectionVersionDto getCollectionVersionDto(String collectionIdStr,
      String versionStr) {
    checkNotBlank(collectionIdStr, "Invalid CollectionId.");
    checkNotBlank(versionStr, "Invalid version.");
    CollectionId collectionId = new CollectionId(collectionIdStr);
    Version version = new Version(versionStr);

    CollectionVersionEntity cvEntity = collectionManager.getCollectionVersion(
        null, collectionId, version);

    if (cvEntity == null) {
      throw new NotFoundException("Could not find " + collectionId + ":" + version);
    }

    return cvEntity.toDto();
  }
  
  @GET
  @Path(JerseyConstants.PATH_ME)
  @Produces({ ContentTypeConstants.APPLICATION_JSON, ContentTypeConstants.APPLICATION_XML })
  public PageDto getCollectionsPublishedByMe(
      @QueryParam(LightStringConstants.START_INDEX_STR) String startIndex,
      @QueryParam(LightStringConstants.MAX_RESULTS_STR) String maxResultStr) {
    SessionManager sessionManager = GuiceUtils.getInstance(SessionManager.class);
    checkPersonLoggedIn(sessionManager);
    
    int maxResult = LightConstants.MAX_RESULTS_DEFAULT;
    
    if (StringUtils.isNotBlank(maxResultStr)) {
      maxResult = Integer.parseInt(maxResultStr);
    }
    
    checkArgument(maxResult <= MAX_RESULTS_MAX, "Max results allowed = " + MAX_RESULTS_MAX);
    
return collectionManager.findCollectionsByOwnerId(GuiceUtils.getOwnerId(), startIndex, maxResult);
    
  }
  
  @SuppressWarnings("unchecked")
  @GET
  @Path(JerseyConstants.PATH_ME_HTML)
  @Produces( ContentTypeConstants.TEXT_HTML )
  public String getCollectionsPublishedByMeHtml() {
    SessionManager sessionManager = GuiceUtils.getInstance(SessionManager.class);
    checkPersonLoggedIn(sessionManager);
    
    PageDto pageDto = collectionManager.findCollectionsByOwnerId(GuiceUtils.getOwnerId(), null, 5000);
    
    StringBuilder htmlBuilder = new StringBuilder("Collections created by me : <br>");
    
    List<CollectionDto> list = ((List<CollectionDto>) pageDto.getList());
    if (!isListEmpty(list)) {
      int counter = 1;
      for (CollectionDto currDto : list) {
        htmlBuilder.append(counter++)
        .append(".&nbsp")
        .append("<a href=" + "/rest/collection/")
        .append(currDto.getCollectionId().getValue())
        .append("/latest/content")
        .append(">")
        .append(currDto.getTitle())
        .append("</a><br>\n");
      }
    }
    
    return htmlBuilder.toString();
  }
}
