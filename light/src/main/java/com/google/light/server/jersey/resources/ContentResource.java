/*
 * Copyright (C) Google Inc.
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

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.light.server.constants.HttpHeaderEnum;
import com.google.light.server.constants.JerseyConstants;
import com.google.light.server.constants.ResourceTypes;
import com.google.light.server.constants.http.ContentTypeConstants;
import com.google.light.server.dto.pojo.tree.CollectionTreeNodeDto;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.CollectionId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.ModuleId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.Version;
import com.google.light.server.exception.unchecked.httpexception.InternalServerErrorException;
import com.google.light.server.exception.unchecked.httpexception.NotFoundException;
import com.google.light.server.manager.interfaces.CollectionManager;
import com.google.light.server.manager.interfaces.ModuleManager;
import com.google.light.server.persistence.entity.collection.CollectionVersionEntity;
import com.google.light.server.persistence.entity.module.ModuleVersionEntity;
import com.google.light.server.persistence.entity.module.ModuleVersionResourceEntity;
import com.google.light.server.utils.LightUtils;
import com.google.light.server.utils.ServletUtils;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * Jersey resource to serve content from collections and
 * modules.
 * 
 * @author Walter Cacau
 */
@SuppressWarnings("synthetic-access")
@Path(JerseyConstants.RESOURCE_PATH_CONTENT)
public class ContentResource extends AbstractJerseyResource {

  /**
   * Utility class to contain a CollectionVersionEntity, a
   * ModuleVersionEntity and the corresponding node in the CollectionVersionEntity tree.
   * 
   * While being constructed it will do all necessary validation.
   * 
   * @author Walter Cacau
   */
  private static class ModuleCollectionNodeTriple {

    private ModuleVersionEntity moduleVersionEntity;
    private CollectionVersionEntity collectionVersionEntity;
    private CollectionTreeNodeDto treeNode;

    /**
     * Constructs this class and do all the necessary validation related to the existence
     * of the module, the existence of the collection and the presence of module in the collection
     * tree.
     * 
     * @param resource
     * @param collectionIdStr
     * @param collectionVersionStr
     * @param moduleIdStr
     */
    private ModuleCollectionNodeTriple(ContentResource resource, String collectionIdStr,
        String collectionVersionStr, String moduleIdStr) {
      collectionVersionEntity = resource.getCollectionVersionEntity(
          collectionIdStr, collectionVersionStr);

      checkNotBlank(moduleIdStr, "Invalid ModuleId.");
      ModuleId moduleId = new ModuleId(moduleIdStr);
      treeNode = resource.findModule(moduleId, collectionVersionEntity.getCollectionTree());
      if (treeNode == null) {
        throw new NotFoundException("Could not find " + moduleIdStr + " in collection "
            + collectionIdStr + ":" + collectionVersionStr);
      }

      // TODO(waltercacau): Use the specified version when available
      moduleVersionEntity = resource.getModuleVersionEntity(moduleIdStr, VERSION_LATEST_STR);
    }

  }

  private CollectionManager collectionManager;
  private ModuleManager moduleManager;

  @Inject
  public ContentResource(Injector injector, @Context HttpServletRequest request,
      @Context HttpServletResponse response, CollectionManager collectionManager,
      ModuleManager moduleManager) {
    super(injector, request, response);
    this.collectionManager = checkNotNull(collectionManager, "collectionManager");
    this.moduleManager = checkNotNull(moduleManager, "moduleManager");
  }

  /**
   * Get on a CollectionId will redirect to Latest Version for that CollectionId.
   */
  @GET
  @Path(JerseyConstants.PATH_CONTENT_COLLECTION)
  public Response getCollectionContent(@Context UriInfo uriInfo) {
    String uri = uriInfo.getPath() + "/" + VERSION_LATEST_STR;
    return Response.seeOther(LightUtils.getURI(uri)).build();
  }
  
  /**
   * Get on a ModuleId will redirect to Latest Version for that CollectionId.
   */
  @GET
  @Path(JerseyConstants.PATH_CONTENT_MODULE)
  public Response getModuleContent(@Context UriInfo uriInfo) {
    String uri = uriInfo.getPath() + "/" + VERSION_LATEST_STR + "/";
    return Response.seeOther(LightUtils.getURI(uri)).build();
  }

  @GET
  @Path(JerseyConstants.PATH_CONTENT_COLLECTION_VERSION)
  @Produces(ContentTypeConstants.TEXT_HTML)
  public Response getCollectionVersionContent(
      @PathParam(JerseyConstants.PATH_PARAM_COLLECTION_ID) String collectionIdStr,
      @PathParam(JerseyConstants.PATH_PARAM_VERSION) String versionStr) {

    CollectionVersionEntity collectionVersionEntity = getCollectionVersionEntity(
        collectionIdStr, versionStr);

    CollectionTreeNodeDto tree = collectionVersionEntity.getCollectionTree();
    StringBuilder contentBuilder = new StringBuilder();

    contentBuilder.append("<ol>\n");
    appendCurrNode(tree, contentBuilder, 2, true, request.getRequestURI());
    contentBuilder.append("\n</ol>");

    request.setAttribute("collectionTitle", StringEscapeUtils.escapeHtml(tree.getTitle()));
    request.setAttribute("collectionContent", contentBuilder.toString());

    ServletUtils.forward(request, response, "/WEB-INF/pages/collection.jsp");

    return Response.ok().build();
  }

  @GET
  @Path(JerseyConstants.PATH_CONTENT_MODULE_IN_COLLECTION_VERSION_RESOURCE)
  public Response getModuleInCollectionVersionResource(
      @PathParam(JerseyConstants.PATH_PARAM_COLLECTION_ID) String collectionIdStr,
      @PathParam(JerseyConstants.PATH_PARAM_VERSION) String versionStr,
      @PathParam(JerseyConstants.PATH_PARAM_MODULE_ID) String moduleIdStr,
      @PathParam(JerseyConstants.PATH_PARAM_RESOURCE_TYPE) String resourceTypeStr,
      @PathParam(JerseyConstants.PATH_PARAM_RESOURCE) String resourceStr) {

    ModuleCollectionNodeTriple triple =
        new ModuleCollectionNodeTriple(this, collectionIdStr, versionStr, moduleIdStr);

    checkNotBlank(resourceTypeStr, "resourceType");
    checkNotBlank(resourceStr, "resource");
    ResourceTypes resourceType = ResourceTypes.valueOf(resourceTypeStr);

    String resourceId = resourceType.name() + "/" + resourceStr;

    Version moduleVersion = triple.moduleVersionEntity.getVersion();
    ModuleId moduleId = triple.moduleVersionEntity.getModuleId();
    return buildResponseForAndServeModuleResource(resourceId, moduleVersion, moduleId);
  }
  
  @GET
  @Path(JerseyConstants.PATH_CONTENT_MODULE_VERSION_RESOURCE)
  public Response getModuleVersionResource(
      @PathParam(JerseyConstants.PATH_PARAM_MODULE_ID) String moduleIdStr,
      @PathParam(JerseyConstants.PATH_PARAM_VERSION) String versionStr,
      @PathParam(JerseyConstants.PATH_PARAM_RESOURCE_TYPE) String resourceTypeStr,
      @PathParam(JerseyConstants.PATH_PARAM_RESOURCE) String resourceStr) {

    checkNotBlank(resourceTypeStr, "resourceType");
    checkNotBlank(resourceStr, "resource");
    ResourceTypes resourceType = ResourceTypes.valueOf(resourceTypeStr);

    ModuleId moduleId = new ModuleId(moduleIdStr);
    Version moduleVersion = new Version(versionStr);
    String resourceId = resourceType.name() + "/" + resourceStr;
    
    return buildResponseForAndServeModuleResource(resourceId, moduleVersion, moduleId);
  }

  @GET
  @Path(JerseyConstants.PATH_CONTENT_MODULE_IN_COLLECTION_VERSION_WITH_SLASH)
  @Produces(ContentTypeConstants.TEXT_HTML)
  public Response getModuleInCollectionVersionContent(
      @PathParam(JerseyConstants.PATH_PARAM_COLLECTION_ID) String collectionIdStr,
      @PathParam(JerseyConstants.PATH_PARAM_VERSION) String versionStr,
      @PathParam(JerseyConstants.PATH_PARAM_MODULE_ID) String moduleIdStr) {

    // TODO(waltercacau): Figure out how to deal with duplicate modules
    
    ModuleCollectionNodeTriple triple =
        new ModuleCollectionNodeTriple(this, collectionIdStr, versionStr, moduleIdStr);

    String htmlContent = triple.moduleVersionEntity.getContent();
    String bodyContent = extractBodyContent(htmlContent);
    if (bodyContent == null) {
      throw new InternalServerErrorException("Could not serve module " + moduleIdStr + " in collection "
          + collectionIdStr + ":" + versionStr);
    }

    // Here we respect the title the collection author gave to the module.
    request.setAttribute("moduleTitle",
        StringEscapeUtils.escapeHtml(triple.treeNode.getTitle()));

    request.setAttribute("moduleContent", bodyContent);

    CollectionTreeNodeDto tree = triple.collectionVersionEntity.getCollectionTree();
    StringBuilder contentBuilder = new StringBuilder();
    contentBuilder.append("<ol>\n");
    appendCurrNode(tree, contentBuilder, 2, true, "/collection/" + collectionIdStr + "/"
        + versionStr);
    contentBuilder.append("\n</ol>");

    request.setAttribute("collectionTitle", StringEscapeUtils.escapeHtml(tree.getTitle()));
    request.setAttribute("collectionContent", contentBuilder.toString());

    ServletUtils.forward(request, response, "/WEB-INF/pages/moduleInCollection.jsp");

    return Response.ok().build();
  }
  
  @GET
  @Path(JerseyConstants.PATH_CONTENT_MODULE_VERSION_WITH_SLASH)
  @Produces(ContentTypeConstants.TEXT_HTML)
  public Response getModuleVersionContent(
      @PathParam(JerseyConstants.PATH_PARAM_MODULE_ID) String moduleIdStr,
      @PathParam(JerseyConstants.PATH_PARAM_VERSION) String versionStr) {
    ModuleVersionEntity moduleEntity = getModuleVersionEntity(moduleIdStr, versionStr);
    
    String bodyContent = extractBodyContent(moduleEntity.getContent());
    if (bodyContent == null) {
      throw new InternalServerErrorException("Could not serve module " + moduleIdStr + ":" + versionStr);
    }

    request.setAttribute("moduleTitle",
        StringEscapeUtils.escapeHtml(moduleEntity.getTitle()));

    request.setAttribute("moduleContent", bodyContent);

    ServletUtils.forward(request, response, "/WEB-INF/pages/module.jsp");

    return Response.ok().build();
  }

  /**
   * Parses the given content and returns only the html body.
   * 
   * @param collectionIdStr
   * @param versionStr
   * @param moduleIdStr
   * @param htmlContent
   * @return
   */
  protected String extractBodyContent(String htmlContent) {
    // TODO(waltercacau): Fix this XGH implementation
    // TODO(waltercacau): Maybe move this method to a utility class.
    int bodyOpenStartPos = htmlContent.indexOf("<body");
    int bodyOpenEndPos = bodyOpenStartPos == -1 ? -1 : htmlContent.indexOf(">", bodyOpenStartPos);
    int bodyClosePos = bodyOpenEndPos == -1 ? -1 : htmlContent.indexOf("</body>", bodyOpenEndPos);

    if (bodyOpenStartPos == -1 || bodyOpenEndPos == -1 || bodyClosePos == -1) {
      return null;
    }
    String bodyContent = htmlContent.substring(bodyOpenEndPos + 1, bodyClosePos);
    return bodyContent;
  }

  /**
   * Finds the first occurrence of a module inside a collection tree.
   * 
   * @param moduleId The moduleId we are looking for.
   * @param node The root of the tree (or subtree) where we are looking for the module.
   * @return
   */
  private CollectionTreeNodeDto findModule(ModuleId moduleId, CollectionTreeNodeDto node) {
    // TODO(waltercacau): validate the assumption
    // "a module can only have one node in a collection tree"
    if (node.hasModuleId() && moduleId.equals(node.getModuleId())) {
      return node;
    }

    if (node.hasChildren()) {
      for (CollectionTreeNodeDto childNode : node.getChildren()) {
        CollectionTreeNodeDto foundNode = findModule(moduleId, childNode);
        if (foundNode != null) {
          return foundNode;
        }
      }
    }
    return null;
  }

  /**
   * Constructs a HTML representation of a tree using li, ol and a tags.
   */
  private void appendCurrNode(CollectionTreeNodeDto node, StringBuilder builder, int space,
      boolean isRoot, String collectionUri) {
    String sp = "";
    for (int i = 0; i < space; i++) {
      sp += " ";
    }
    if (node.isLeafNode()) {
      String uri = collectionUri + "/" + node.getModuleId().getValue() + "/";
      builder.append(sp).append("<li><a href=").append(StringEscapeUtils.escapeHtml(uri))
          .append(">")
          .append(StringEscapeUtils.escapeHtml(node.getTitle())).append("</a></li>\n");
    } else {
      if (!isRoot)
        builder.append(sp).append("<li>").append(StringEscapeUtils.escapeHtml(node.getTitle()))
            .append("<ol>\n");
      if (node.hasChildren()) {
        for (CollectionTreeNodeDto currChild : node.getChildren()) {
          appendCurrNode(currChild, builder, space + 4, false, collectionUri);
        }
      }
      if (!isRoot)
        builder.append(sp).append("</ol></li>\n");
    }
  }

  protected CollectionVersionEntity getCollectionVersionEntity(String collectionIdStr,
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

    return cvEntity;
  }

  protected ModuleVersionEntity getModuleVersionEntity(String moduleIdStr, String versionStr) {
    checkNotBlank(moduleIdStr, "Invalid ModuleId.");
    checkNotBlank(versionStr, "Invalid version.");
    ModuleId moduleId = new ModuleId(moduleIdStr);
    Version version = new Version(versionStr);

    ModuleVersionEntity moduleEntity = moduleManager.getModuleVersion(
        null /* ofy */, moduleId, version);

    if (moduleEntity == null) {
      throw new NotFoundException("Could not find " + moduleId + ":" + version);
    }

    return moduleEntity;
  }

  /**
   * Builds a response with the appropriate content type for a module content and makes the
   * blobService serve its content.
   * 
   * @param resourceId
   * @param moduleVersion
   * @param moduleId
   * @throws NotFoundException if the resource for the module cannot be found.
   * @return
   */
  protected Response buildResponseForAndServeModuleResource(String resourceId,
      Version moduleVersion,
      ModuleId moduleId) {
    ModuleVersionResourceEntity resourceEntity = moduleManager.getModuleResource(null /* ofy */,
        moduleId, moduleVersion, resourceId);

    if (resourceEntity == null) {
      throw new NotFoundException("Resource[" + resourceId + "] Not found for module "
          + moduleId + ":" + moduleVersion);
    }

    BlobstoreService blobService = checkNotNull(BlobstoreServiceFactory.getBlobstoreService());
    BlobKey blobKey = blobService.createGsBlobKey(resourceEntity.getGSBlobInfo().getGsKey());
    try {
      checkNotNull(response, "response");
      blobService.serve(blobKey, response);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    ResponseBuilder responseBuilder = Response.ok();
    responseBuilder.header(HttpHeaderEnum.CONTENT_TYPE.get(),
        resourceEntity.getGSBlobInfo().getContentType().get());
    return responseBuilder.build();
  }

}
