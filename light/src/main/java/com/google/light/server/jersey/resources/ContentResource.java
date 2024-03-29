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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.joda.time.Instant;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.light.server.constants.HttpHeaderEnum;
import com.google.light.server.constants.JerseyConstants;
import com.google.light.server.constants.ResourceTypes;
import com.google.light.server.constants.SupportedLanguagesEnum;
import com.google.light.server.constants.http.ContentTypeConstants;
import com.google.light.server.dto.JSVariablesPreloadDto;
import com.google.light.server.dto.module.ModuleState;
import com.google.light.server.dto.pojo.tree.collection.CollectionTreeNodeDto;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.CollectionId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.ModuleId;
import com.google.light.server.dto.pojo.typewrapper.longwrapper.Version;
import com.google.light.server.exception.unchecked.httpexception.InternalServerErrorException;
import com.google.light.server.exception.unchecked.httpexception.NotFoundException;
import com.google.light.server.manager.interfaces.CollectionManager;
import com.google.light.server.manager.interfaces.ModuleManager;
import com.google.light.server.persistence.entity.collection.CollectionVersionEntity;
import com.google.light.server.persistence.entity.module.ModuleEntity;
import com.google.light.server.persistence.entity.module.ModuleVersionEntity;
import com.google.light.server.persistence.entity.module.ModuleVersionResourceEntity;
import com.google.light.server.utils.GaeUtils;
import com.google.light.server.utils.LightUtils;
import com.google.light.server.utils.ServletUtils;

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
   * ModuleVersionEntity and the data related to them.
   * 
   * While being constructed it will do all necessary validation.
   * 
   * @author Walter Cacau
   */
  private static class ModuleCollectionNodeData {

    private ModuleVersionEntity moduleVersionEntity;
    private CollectionVersionEntity collectionVersionEntity;
    private CollectionTreeNodeDto treeNode;
    private CollectionTreeNodeDto treeNodePrevious;
    private CollectionTreeNodeDto treeNodeNext;
    private int navIndex;
    private int numberOfLeafs;

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
    private ModuleCollectionNodeData(ContentResource resource, String collectionIdStr,
        String collectionVersionStr, String moduleIdStr) {
      collectionVersionEntity = resource.getCollectionVersionEntity(
          collectionIdStr, collectionVersionStr);

      checkNotBlank(moduleIdStr, "Invalid ModuleId.");
      ModuleId moduleId = new ModuleId(moduleIdStr);

      CollectionTreeNodeDto treeRootNode = collectionVersionEntity.getCollectionTree();

      // We ignore the first
      List<CollectionTreeNodeDto> leafNodesList = getLeafNodeList(treeRootNode);
      numberOfLeafs = leafNodesList.size();
      for (navIndex = 0; navIndex < numberOfLeafs; navIndex++) {
        CollectionTreeNodeDto node = leafNodesList.get(navIndex);
        if (node.hasModuleId() && node.getModuleId().equals(moduleId)) {
          break;
        }
      }

      if (navIndex == numberOfLeafs) {
        throw new NotFoundException("Could not find " + moduleIdStr + " in collection "
            + collectionIdStr + ":" + collectionVersionStr);
      }

      if (navIndex > 0) {
        treeNodePrevious = leafNodesList.get(navIndex - 1);
      }
      if (navIndex + 1 < numberOfLeafs) {
        treeNodeNext = leafNodesList.get(navIndex + 1);
      }
      treeNode = leafNodesList.get(navIndex);

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
    CollectionId collectionId = new CollectionId(collectionIdStr);
    Version collectionVersion = new Version(versionStr);

    CollectionVersionEntity collectionVersionEntity = getCollectionVersionEntity(
        collectionIdStr, versionStr);

    CollectionTreeNodeDto tree = collectionVersionEntity.getCollectionTree();
    StringBuilder contentBuilder = new StringBuilder();

    contentBuilder.append("<ol>\n");
    appendCurrNode(tree, contentBuilder, 2, true, collectionId,
        collectionVersion);
    contentBuilder.append("\n</ol>");

    request.setAttribute("collectionTitle", StringEscapeUtils.escapeHtml(tree.getTitle()));
    List<CollectionTreeNodeDto> leafNodeList = getLeafNodeList(tree);
    StringBuilder startLinkBuilder = new StringBuilder();
    if (leafNodeList.size() > 0) {
      CollectionTreeNodeDto firstLeafNode = leafNodeList.get(0);
      startLinkBuilder
          .append("<a class=\"collectionStartLink\" href=\"")
          .append(
              StringEscapeUtils.escapeHtml(buildPathForModuleInCollection(
                  collectionId,
                  collectionVersion,
                  firstLeafNode.getModuleId())))
          .append("\">Start</a>");

    }
    request.setAttribute("collectionStartLink", startLinkBuilder.toString());
    request.setAttribute("collectionContent", contentBuilder.toString());
    setJSVariablesPreload(request, null);

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

    ModuleCollectionNodeData data =
        new ModuleCollectionNodeData(this, collectionIdStr, versionStr, moduleIdStr);

    checkNotBlank(resourceTypeStr, "resourceType");
    checkNotBlank(resourceStr, "resource");
    ResourceTypes resourceType = ResourceTypes.valueOf(resourceTypeStr);

    String resourceId = resourceType.name() + "/" + resourceStr;

    Version moduleVersion = data.moduleVersionEntity.getVersion();
    ModuleId moduleId = data.moduleVersionEntity.getModuleId();
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
    CollectionId collectionId = new CollectionId(collectionIdStr);
    Version collectionVersion = new Version(versionStr);

    // TODO(waltercacau): Figure out how to deal with duplicate modules

    ModuleCollectionNodeData data =
        new ModuleCollectionNodeData(this, collectionIdStr, versionStr, moduleIdStr);

    String htmlContent = data.moduleVersionEntity.getContent();
    String bodyContent = extractBodyContent(htmlContent);
    if (bodyContent == null) {
      throw new InternalServerErrorException("Could not serve module " + moduleIdStr
          + " in collection "
          + collectionIdStr + ":" + versionStr);
    }

    // Here we respect the title the collection author gave to the module.
    request.setAttribute("moduleTitle",
        StringEscapeUtils.escapeHtml(data.treeNode.getTitle()));

    request.setAttribute("moduleContent", bodyContent);

    CollectionTreeNodeDto tree = data.collectionVersionEntity.getCollectionTree();
    StringBuilder contentBuilder = new StringBuilder();
    contentBuilder.append("<ol>\n");
    String collectionUri = "/rest/content/general/collection/" + collectionIdStr + "/" + versionStr;
    appendCurrNode(tree, contentBuilder, 2, true,
        collectionId,
        collectionVersion);
    contentBuilder.append("\n</ol>");

    request.setAttribute("collectionTitle", StringEscapeUtils.escapeHtml(tree.getTitle()));
    request.setAttribute("collectionPath",
        StringEscapeUtils.escapeHtml(buildPathForCollection(collectionId, collectionVersion)));
    request.setAttribute("collectionContent", contentBuilder.toString());

    StringBuilder navigationBarBuilder = new StringBuilder();
    if (data.treeNodePrevious != null) {
      navigationBarBuilder.append("<a href=\"");
      navigationBarBuilder.append(StringEscapeUtils.escapeHtml(buildPathForModuleInCollection(
          collectionId,
          collectionVersion,
          data.treeNodePrevious.getModuleId())));
      navigationBarBuilder.append("\" class=\"previousLink\" >Previous</a> | ");
    }
    navigationBarBuilder.append(StringEscapeUtils.escapeHtml((data.navIndex + 1) + "/"
        + data.numberOfLeafs));
    if (data.treeNodeNext != null) {
      navigationBarBuilder.append(" | <a href=\"");
      navigationBarBuilder.append(StringEscapeUtils.escapeHtml(buildPathForModuleInCollection(
          collectionId,
          collectionVersion,
          data.treeNodeNext.getModuleId())));
      navigationBarBuilder.append("\" class=\"nextLink\" >Next</a>");
    }

    request.setAttribute("navigationBar", navigationBarBuilder.toString());
    request.setAttribute("usesIframe", bodyContent.indexOf("<iframe") != -1 ? "true" : "false");
    setJSVariablesPreload(request, new ModuleId(moduleIdStr));

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
      throw new InternalServerErrorException("Could not serve module " + moduleIdStr + ":"
          + versionStr);
    }

    request.setAttribute("moduleTitle",
        StringEscapeUtils.escapeHtml(moduleEntity.getTitle()));

    request.setAttribute("moduleContent", bodyContent);
    request.setAttribute("usesIframe", bodyContent.indexOf("<iframe") != -1 ? "true" : "false");
    setJSVariablesPreload(request, new ModuleId(moduleIdStr));

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
    String bodyProperties = htmlContent.substring(bodyOpenStartPos + 5, bodyOpenEndPos);

    // TODO(waltercacau): Do proper CSS parsing / move this to the google docs import flow maybe?
    // Here we are taking the google docs padding out
    Pattern p = Pattern.compile("padding\\s*:[^;\"']+([;\"'])");
    Matcher m = p.matcher(bodyProperties);
    StringBuffer bodyPropertiesBuilder = new StringBuffer();
    while (m.find()) {
      m.appendReplacement(bodyPropertiesBuilder, "margin: 0 auto;padding-top:1em" + m.group(1));
    }
    m.appendTail(bodyPropertiesBuilder);
    bodyProperties = bodyPropertiesBuilder.toString();

    String bodyContent = htmlContent.substring(bodyOpenEndPos + 1, bodyClosePos);
    return "<div" + bodyProperties + ">" + bodyContent + "</div>";
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
      boolean isRoot, CollectionId collectionId, Version collectionVersion) {
    String sp = "";
    for (int i = 0; i < space; i++) {
      sp += " ";
    }
    if (node.isLeafNode()) {
      String uri =
          buildPathForModuleInCollection(collectionId, collectionVersion, node.getModuleId());
      builder.append(sp).append("<li><a href=").append(StringEscapeUtils.escapeHtml(uri))
          .append(">")
          .append(StringEscapeUtils.escapeHtml(node.getTitle())).append("</a></li>\n");
    } else {
      if (!isRoot) {
        builder.append(sp).append("<li>").append(StringEscapeUtils.escapeHtml(node.getTitle()))
            .append("<ol>\n");
      }
      if (node.hasChildren()) {
        for (CollectionTreeNodeDto currChild : node.getChildren()) {
          appendCurrNode(currChild, builder, space + 4, false, collectionId, collectionVersion);
        }
      }
      if (!isRoot) {
        builder.append(sp).append("</ol></li>\n");
      }
    }
  }

  protected CollectionVersionEntity getCollectionVersionEntity(String collectionIdStr,
      String versionStr) {

    checkNotBlank(collectionIdStr, "Invalid CollectionId.");
    checkNotBlank(versionStr, "Invalid version.");
    CollectionId collectionId = new CollectionId(collectionIdStr);
    Version collectionVersion = new Version(versionStr);

    CollectionVersionEntity cvEntity = collectionManager.getCollectionVersion(
        null, collectionId, collectionVersion);

    if (cvEntity == null) {
      throw new NotFoundException("Could not find " + collectionId + ":" + collectionVersion);
    }

    return cvEntity;
  }

  protected ModuleVersionEntity getModuleVersionEntity(String moduleIdStr, String versionStr) {
    checkNotBlank(moduleIdStr, "Invalid ModuleId.");
    checkNotBlank(versionStr, "Invalid version.");
    ModuleId moduleId = new ModuleId(moduleIdStr);
    Version version = new Version(versionStr);

    ModuleEntity moduleEntity = moduleManager.get(null, moduleId);
    if(moduleEntity.getLatestPublishVersion().isNoVersion()) {
      // TODO(waltercacau): Remove this hack
      return new ModuleVersionEntity.Builder()
          .version(new Version(Version.DEFAULT_FIRST_VERSION))
          .content("<body><div class=\"importInProgressWarning\"><img src=\"/images/ajax-loader-white.gif\">Import in progress</body></div>")
          .title(moduleEntity.getTitle())
          .moduleKey(moduleEntity.getKey())
          .moduleState(ModuleState.IMPORTING)
          .contentLicenses(moduleEntity.getContentLicenses())
          .lastEditTime(new Instant(1))
          .build();
    }
    
    ModuleVersionEntity moduleVersionEntity = moduleManager.getModuleVersion(
        null /* ofy */, moduleId, version);

    if (moduleVersionEntity == null) {
      throw new NotFoundException("Could not find " + moduleId + ":" + version);
    }

    return moduleVersionEntity;
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

  private static List<CollectionTreeNodeDto> getLeafNodeList(CollectionTreeNodeDto root) {
    List<CollectionTreeNodeDto> list = new ArrayList<CollectionTreeNodeDto>();
    addLeafNodesToListInPreorder(list, root);
    return list;
  }

  private static void addLeafNodesToListInPreorder(List<CollectionTreeNodeDto> list,
      CollectionTreeNodeDto node) {
    if (node.isLeafNode()) {
      list.add(node);
    }
    if (node.hasChildren()) {
      for (CollectionTreeNodeDto child : node.getChildren()) {
        addLeafNodesToListInPreorder(list, child);
      }
    }
  }

  /**
   * Sets the JSVariablesPreload in the request with information
   * about the user/browser locale, but without any other user data.
   */
  private static void setJSVariablesPreload(HttpServletRequest request, ModuleId moduleId) {
    // TODO(waltercacau): Move this to a utility class.
    @SuppressWarnings("unchecked")
    JSVariablesPreloadDto.Builder dtoBuilder = new JSVariablesPreloadDto.Builder()
        .moduleId(moduleId)
        .locale(SupportedLanguagesEnum.getClosestPrefferedLanguage(
            Collections.list(request.getLocales())).getClientLanguageCode());

    request.setAttribute("preload", dtoBuilder.build().toHtml());
  }

  public static String buildAbsoluteLinkForModule(ModuleId moduleId) {
    return GaeUtils.getBaseUrlForDefaultVersion()
        + buildPathForModule(moduleId, new Version(VERSION_LATEST_STR));
  }

  private static String buildPathForModule(ModuleId moduleId, Version version) {
    return "/rest/content/general/module/" + moduleId.getValue() + "/"
        + version.toURLComponentString() + "/";
  }

  private static String buildPathForModuleInCollection(CollectionId collectionId,
      Version collectionVersion, ModuleId moduleId) {
    return "/rest/content/general/collection/" + collectionId.getValue() + "/"
        + collectionVersion.toURLComponentString() + "/" + moduleId.getValue();
  }

  private static String buildPathForCollection(CollectionId collectionId,
      Version collectionVersion) {
    return "/rest/content/general/collection/" + collectionId.getValue() + "/"
        + collectionVersion.toURLComponentString();
  }

}
