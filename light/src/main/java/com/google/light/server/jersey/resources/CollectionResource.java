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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.light.server.constants.LightStringConstants.VERSION_LATEST_STR;
import static com.google.light.server.utils.LightPreconditions.checkNotBlank;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.light.server.constants.JerseyConstants;
import com.google.light.server.constants.http.ContentTypeConstants;
import com.google.light.server.dto.pojo.longwrapper.CollectionId;
import com.google.light.server.dto.pojo.longwrapper.Version;
import com.google.light.server.dto.pojo.tree.CollectionTreeNode;
import com.google.light.server.exception.unchecked.httpexception.NotFoundException;
import com.google.light.server.manager.interfaces.CollectionManager;
import com.google.light.server.persistence.entity.collection.CollectionVersionEntity;
import com.google.light.server.utils.LightUtils;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

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
  public Response getCollection() {
    String uri = request.getRequestURI() + "/" + VERSION_LATEST_STR;
    return Response.seeOther(LightUtils.getURI(uri)).build();
  }

  @GET
  @Path(JerseyConstants.PATH_COLLECTION_VERSION)
  @Produces({ ContentTypeConstants.APPLICATION_JSON, ContentTypeConstants.TEXT_XML })
  public CollectionTreeNode getCollectionVersion(
      @PathParam(JerseyConstants.PATH_PARAM_COLLECTION_ID) String collectionIdStr,
      @PathParam(JerseyConstants.PATH_PARAM_VERSION) String versionStr) {
    CollectionVersionEntity collectionVersionEntity = getCollectionVersionEntity(
        collectionIdStr, versionStr);
    return collectionVersionEntity.getCollectionTree();
  }

  @GET
  @Path(JerseyConstants.PATH_COLLECTION_VERSION_CONTENT)
  @Produces(ContentTypeConstants.TEXT_HTML)
  public Response getCollectionVersionContent(
      @PathParam(JerseyConstants.PATH_PARAM_COLLECTION_ID) String collectionIdStr,
      @PathParam(JerseyConstants.PATH_PARAM_VERSION) String versionStr) {
    CollectionVersionEntity collectionVersionEntity = getCollectionVersionEntity(
        collectionIdStr, versionStr);
    
    CollectionTreeNode tree = collectionVersionEntity.getCollectionTree();
    StringBuilder builder = new StringBuilder();
    
    appendCurrNode(tree, builder, 2);
    
    return Response.ok(builder.toString()).build();
  }

  /**
   * @param tree
   * @param builder
   */
  private void appendCurrNode(CollectionTreeNode node, StringBuilder builder, int space) {
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
        for (CollectionTreeNode currChild : node.getChildren()) {
          appendCurrNode(currChild, builder, space * 2);
        }
      }
    }
    builder.append("</pre>");
  }

  protected CollectionVersionEntity getCollectionVersionEntity(String collectionIdStr,
      String versionStr) {
    checkNotBlank(collectionIdStr, "Invalid CollectionId.");
    checkNotBlank(versionStr, "Invalid version.");
    CollectionId collectionId = new CollectionId(collectionIdStr);
    Version version = Version.createVersion(versionStr);

    CollectionVersionEntity moduleEntity =
        collectionManager.getCollectionVersion(collectionId, version);

    if (moduleEntity == null) {
      throw new NotFoundException("Could not find " + collectionId + ":" + version);
    }

    return moduleEntity;
  }
}
